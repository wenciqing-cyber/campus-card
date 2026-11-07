# 一卡通系统接口测试：功能 + 边界 + 异常
# 用法：先启动服务，再 python test/api_test.py [--md 输出路径]
import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime

BASE = "http://localhost:8080/api"
ROWS = []
_seq = 0


def http(method, path, body=None):
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return json.loads(e.read().decode("utf-8"))
    except Exception as e:  # 服务没起或者端口不通
        return {"code": -1, "message": str(e), "data": None}


def check(name, expected, resp, ok, actual=None):
    global _seq
    _seq += 1
    got = actual if actual is not None else "code=%s %s" % (resp.get("code"), resp.get("message") or "")
    ROWS.append((_seq, name, expected, got, "通过" if ok else "失败"))


def expect_ok(name, resp, note=""):
    ok = resp.get("code") == 0
    check(name, "code=0" + (" " + note if note else ""), resp, ok)


def expect_fail(name, code, resp, hint=""):
    ok = resp.get("code") == code
    check(name, "code=%d%s" % (code, (" " + hint) if hint else ""), resp, ok)


def balance(card_no):
    r = http("GET", "/accounts/" + card_no)
    return float(r["data"]["balance"]) if r.get("code") == 0 else -1


def find_card_no(student_no):
    """开户接口只回账户，卡号得从列表里捞"""
    r = http("GET", "/accounts")
    if r.get("code") != 0:
        return ""
    for a in r["data"]:
        if a.get("studentNo") == student_no:
            return a.get("cardNo")
    return ""


def run():
    stamp = datetime.now().strftime("%H%M%S")
    student_no = "TEST" + stamp
    print("测试账户学号:", student_no)

    # ---------- A 开户与账户 ----------
    r = http("POST", "/accounts", {"studentNo": student_no, "studentName": "测试同学",
                                   "dept": "软件工程学院", "payPwd": "123456"})
    expect_ok("A1 正常开户", r)
    card_no = find_card_no(student_no) if r.get("code") == 0 else ""
    print("测试卡号:", card_no)

    r = http("POST", "/accounts", {"studentNo": student_no, "studentName": "重复",
                                   "dept": "软件工程学院", "payPwd": "123456"})
    expect_fail("A2 学号重复开户被拒", 1000, r, "学号已存在")

    r = http("POST", "/accounts", {"studentNo": " ", "studentName": "空学号",
                                   "dept": "软件工程学院", "payPwd": "123456"})
    expect_fail("A3 空学号被拒", 1000, r)
    r = http("GET", "/accounts/" + card_no)
    ok = r.get("code") == 0 and r["data"]["studentName"] == "测试同学"
    check("A4 查询账户详情", "返回姓名与余额", r, ok,
          "余额=%s 姓名=%s" % (r["data"]["balance"], r["data"]["studentName"]) if r.get("code") == 0 else r.get("message"))

    r = http("GET", "/accounts/999999999999")
    ok = r.get("code") != 0
    check("A5 查询不存在的卡号", "业务异常", r, ok, "code=%s %s" % (r.get("code"), r.get("message")))

    # ---------- B 密码 ----------
    r = http("POST", "/accounts/%s/password?oldPwd=123456&newPwd=654321" % card_no)
    expect_ok("B1 修改交易密码", r)
    r = http("POST", "/accounts/%s/password?oldPwd=123456&newPwd=111111" % card_no)
    expect_fail("B2 原密码错误被拒", 1005, r)
    r = http("POST", "/accounts/%s/password?oldPwd=654321&newPwd=123" % card_no)
    expect_fail("B3 新密码长度非法被拒", 1005, r)
    r = http("POST", "/accounts/%s/password?oldPwd=654321&newPwd=123456" % card_no)
    expect_ok("B4 密码改回", r)

    # ---------- C 充值 ----------
    r = http("POST", "/pay/recharge", {"cardNo": card_no, "amount": 500, "requestNo": "T" + stamp + "R1"})
    expect_ok("C1 充值 500 元", r)
    ok = abs(balance(card_no) - 500.0) < 0.001
    check("C2 充值后余额正确", "500.00", r, ok, "余额=%.2f" % balance(card_no))

    r = http("POST", "/pay/recharge", {"cardNo": card_no, "amount": -1, "requestNo": "T" + stamp + "R2"})
    expect_fail("C3 充值负数被拒", 1009, r)
    r = http("POST", "/pay/recharge", {"cardNo": card_no, "amount": 0, "requestNo": "T" + stamp + "R3"})
    expect_fail("C4 充值 0 被拒", 1009, r)
    r = http("POST", "/pay/recharge", {"cardNo": card_no, "amount": 100, "requestNo": "T" + stamp + "R1"})
    expect_fail("C5 重复请求号幂等拦截", 1008, r)

    # ---------- D 挂失 / 解挂 ----------
    r = http("POST", "/accounts/%s/status?status=2" % card_no)
    expect_ok("D1 挂失成功", r)
    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"amount": 10}, "requestNo": "T" + stamp + "C1"})
    expect_fail("D2 挂失卡消费被拒", 1002, r)
    r = http("POST", "/accounts/%s/status?status=1" % card_no)
    expect_ok("D3 解挂成功", r)

    # ---------- E 消费与优惠 ----------
    before = balance(card_no)
    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "ruleCode": "NORMAL", "payload": {"amount": 12.5},
                                      "requestNo": "T" + stamp + "C2"})
    expect_ok("E1 普通消费 12.5 元", r)
    first_trade = r["data"]["tradeNo"] if r.get("code") == 0 else ""
    ok = abs(float(r["data"]["payAmount"]) - 12.5) < 0.001 and abs(before - balance(card_no) - 12.5) < 0.001
    check("E2 普通消费无优惠且余额扣减正确", "实付 12.50", r, ok, "实付=%s 余额=%.2f" % (r["data"]["payAmount"], balance(card_no)))

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "ruleCode": "DISC-85", "payload": {"amount": 100},
                                      "requestNo": "T" + stamp + "C3"})
    ok = r.get("code") == 0 and abs(float(r["data"]["payAmount"]) - 85) < 0.001
    check("E3 折扣规则 8.5 折（100→85）", "实付 85.00", r, ok,
          "实付=%s 优惠=%s" % (r["data"]["payAmount"], r["data"]["discountAmount"]) if r.get("code") == 0 else r.get("message"))

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "ruleCode": "SUB-2", "payload": {"amount": 20},
                                      "requestNo": "T" + stamp + "C4"})
    ok = r.get("code") == 0 and abs(float(r["data"]["payAmount"]) - 18) < 0.001
    check("E4 补贴规则减 2 元（20→18）", "实付 18.00", r, ok,
          "实付=%s" % r["data"]["payAmount"] if r.get("code") == 0 else r.get("message"))

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "ruleCode": "DISC-85,SUB-2", "payload": {"amount": 100},
                                      "requestNo": "T" + stamp + "C5"})
    ok = r.get("code") == 0 and abs(float(r["data"]["payAmount"]) - 83) < 0.001
    check("E5 装饰器叠加折扣+补贴（100→83）", "实付 83.00", r, ok,
          "实付=%s 优惠=%s" % (r["data"]["payAmount"], r["data"]["discountAmount"]) if r.get("code") == 0 else r.get("message"))

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "WATER-01", "payPwd": "123456",
                                      "payload": {"volume": 20}, "requestNo": "T" + stamp + "C6"})
    ok = r.get("code") == 0 and abs(float(r["data"]["payAmount"]) - 10) < 0.001
    check("E6 水控适配器 20 升 × 0.5 元", "实付 10.00", r, ok,
          "实付=%s" % r["data"]["payAmount"] if r.get("code") == 0 else r.get("message"))

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "GATE-01", "payPwd": "123456",
                                      "payload": {}, "requestNo": "T" + stamp + "C7"})
    ok = r.get("code") == 0 and abs(float(r["data"]["payAmount"]) - 1) < 0.001
    check("E7 门禁适配器按次计费", "实付 1.00", r, ok,
          "实付=%s" % r["data"]["payAmount"] if r.get("code") == 0 else r.get("message"))

    time.sleep(1.1)  # 限流是每秒 20 笔，歇一秒让窗口重开

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "000000",
                                      "payload": {"amount": 10}, "requestNo": "T" + stamp + "C8"})
    expect_fail("E8 交易密码错误被拒", 1005, r)

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"amount": 201}, "requestNo": "T" + stamp + "C9"})
    expect_fail("E9 超单笔限额 200 元被拒", 1006, r)

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"amount": 0}, "requestNo": "T" + stamp + "C10"})
    expect_fail("E10 消费金额为 0 被拒", 1000, r)

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-999", "payPwd": "123456",
                                      "payload": {"amount": 10}, "requestNo": "T" + stamp + "C11"})
    expect_fail("E11 终端不存在被拒", 1010, r)

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"x": 1}, "requestNo": "T" + stamp + "C12"})
    expect_fail("E12 POS 报文缺 amount 字段被拒", 1000, r)

    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"amount": 10}, "requestNo": "T" + stamp + "C2"})
    expect_fail("E13 重复请求号幂等拦截", 1008, r)

    # 积分抵扣放在后面，前面消费攒的积分够用了
    acc = http("GET", "/accounts/" + card_no)
    points_before = acc["data"]["points"] if acc.get("code") == 0 else 0
    r = http("POST", "/pay/consume", {"cardNo": card_no, "terminalNo": "POS-001", "payPwd": "123456",
                                      "ruleCode": "PTS-100", "payload": {"amount": 50},
                                      "requestNo": "T" + stamp + "C13"})
    ok = r.get("code") == 0 and int(r["data"]["pointsDelta"]) < 0
    check("E14 积分抵扣（消费攒的分抵扣现金）", "积分减少且实付 < 50",
          r, ok,
          "积分=%d→%d 实付=%s" % (points_before, points_before + int(r["data"]["pointsDelta"]),
                                r["data"]["payAmount"]) if r.get("code") == 0 else r.get("message"))

    # ---------- F 退款冲正 ----------
    if first_trade:
        bal = balance(card_no)
        r = http("POST", "/pay/refund", {"originTradeNo": first_trade, "requestNo": "T" + stamp + "F1"})
        ok = r.get("code") == 0 and str(r["data"]["tradeType"]) == "REFUND"
        check("F1 退款冲正生成反向单", "新单类型 REFUND，余额 +12.50", r, ok,
              "类型=%s 余额 %.2f→%.2f" % (r["data"]["tradeType"], bal, balance(card_no)) if r.get("code") == 0 else r.get("message"))

        r = http("POST", "/pay/refund", {"originTradeNo": first_trade, "requestNo": "T" + stamp + "F2"})
        expect_fail("F2 同一笔重复退款被拒", 1012, r)

    r = http("POST", "/pay/refund", {"originTradeNo": "NOT_EXIST_001", "requestNo": "T" + stamp + "F3"})
    expect_fail("F3 退款原单不存在被拒", 1011, r)

    r = http("GET", "/trades?cardNo=%s&tradeType=REFUND" % card_no)
    ok = r.get("code") == 0 and r["data"]["total"] >= 1
    check("F4 按卡号+类型筛选退款流水", "至少 1 条 REFUND", r, ok, "总数=%s" % r["data"]["total"])

    # ---------- G 销户校验 ----------
    r = http("POST", "/accounts/%s/status?status=3" % card_no)
    expect_fail("G1 有余额不允许销户", 1000, r, "余额不为 0")

    # ---------- H 查询与统计 ----------
    r = http("GET", "/trades?page=1&size=5")
    ok = r.get("code") == 0 and len(r["data"]["rows"]) == 5 and r["data"]["total"] > 0
    check("H1 流水分页查询", "返回 5 条且 total>0", r, ok,
          "本页=%d 总=%s" % (len(r["data"]["rows"]), r["data"]["total"]))

    r = http("GET", "/trades?tradeType=CONSUME&page=1&size=100")
    ok = r.get("code") == 0 and all(x["tradeType"] == "CONSUME" for x in r["data"]["rows"])
    check("H2 按交易类型筛选", "结果全为 CONSUME", r, ok, "条数=%d" % len(r["data"]["rows"]))

    r = http("GET", "/stats/overview")
    ok = r.get("code") == 0 and r["data"]["accountCount"] > 0
    check("H3 概览统计", "返回账户数/交易数/终端汇总", r, ok,
          "账户=%s 成功交易=%s 消费总额=%s" % (r["data"]["accountCount"], r["data"]["tradeCount"], r["data"]["consumeTotal"]))

    r = http("GET", "/stats/trend?days=7")
    ok = r.get("code") == 0 and len(r["data"]) == 7
    check("H4 近 7 天趋势补零", "7 个数据点", r, ok, "点数=%d" % len(r["data"]))

    r = http("GET", "/stats/type")
    check("H5 交易类型分布", "返回各类型笔数与金额", r, r.get("code") == 0, "类型数=%d" % len(r.get("data") or []))

    r = http("GET", "/stats/terminal")
    check("H6 各终端今日汇总", "返回终端笔数金额", r, r.get("code") == 0, "终端数=%d" % len(r.get("data") or []))

    r = http("GET", "/notifications")
    ok = r.get("code") == 0 and len(r["data"]) > 0
    check("H7 观察者推送通知", "有消息队列内容", r, ok, "条数=%d" % len(r.get("data") or []))

    r = http("GET", "/reconciliation")
    ok = r.get("code") == 0 and r["data"]["count"] >= 0
    check("H8 实时对账快照", "返回按终端金额与笔数", r, ok, "笔数=%s" % r["data"]["count"])

    r = http("GET", "/logs?limit=5")
    ok = r.get("code") == 0 and len(r["data"]) > 0
    check("H9 代理层操作日志", "有耗时记录", r, ok, "条数=%d" % len(r.get("data") or []))

    r = http("POST", "/settlement/run")
    ok = r.get("code") == 0
    check("H10 日终结账批处理", "按终端生成结算记录", r, ok,
          "生成 %d 条" % len(r.get("data") or []) if ok else r.get("message"))

    # ---------- I 副账户：清零后销户 ----------
    r = http("POST", "/accounts", {"studentNo": student_no + "B", "studentName": "清零同学",
                                   "dept": "网络工程学院", "payPwd": "123456"})
    card2 = find_card_no(student_no + "B") if r.get("code") == 0 else ""
    if card2:
        http("POST", "/pay/recharge", {"cardNo": card2, "amount": 50, "requestNo": "T" + stamp + "B1"})
        http("POST", "/pay/consume", {"cardNo": card2, "terminalNo": "POS-001", "payPwd": "123456",
                                      "payload": {"amount": 50}, "requestNo": "T" + stamp + "B2"})
        r = http("POST", "/accounts/%s/status?status=3" % card2)
        ok = r.get("code") == 0 and abs(balance(card2)) < 0.001
        check("I1 余额为 0 时销户成功", "code=0 且卡状态变为已销户", r, ok,
              "余额=%.2f" % balance(card2))

        r = http("POST", "/accounts/%s/status?status=1" % card2)
        check("I2 已销户不能恢复", "业务异常", r, r.get("code") != 0, "code=%s %s" % (r.get("code"), r.get("message")))


def report(path):
    passed = sum(1 for x in ROWS if x[4] == "通过")
    total = len(ROWS)
    lines = []
    lines.append("# 接口测试记录\n")
    lines.append("执行时间：%s\n" % datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    lines.append("被测服务：http://localhost:8080\n")
    lines.append("用例总数：%d，通过 %d，失败 %d，通过率 %.1f%%\n" % (total, passed, total - passed, passed * 100.0 / total))
    lines.append("| 编号 | 用例 | 预期 | 实际 | 结果 |")
    lines.append("| --- | --- | --- | --- | --- |")
    for no, name, exp, act, res in ROWS:
        lines.append("| %d | %s | %s | %s | %s |" % (
            no, name, exp, str(act).replace("|", "\\|")[:90], res))
    text = "\n".join(lines) + "\n"
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)
    print(text[:1200])
    print("... 共 %d 条，通过 %d 条，失败 %d 条" % (total, passed, total - passed))
    print("记录已写入", path)


if __name__ == "__main__":
    run()
    out = sys.argv[2] if len(sys.argv) > 2 else "test/测试记录.md"
    report(out)
