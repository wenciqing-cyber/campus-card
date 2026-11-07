# 并发验证：一部分验限流（代理模式），一部分验余额原子扣减（责任链 + SQL 条件更新）
# 脚本自己会把两张卡的余额补到目标值（只补不减），直接跑即可
import json
import sys
import threading
import time
import urllib.error
import urllib.request

BASE = "http://localhost:8080/api"
RATE_CARD = "622100000001"   # 限流用，余额充足
RACE_CARD = "622100000003"   # 余额竞争用，补到 100 元后再跑


def call(path, body):
    req = urllib.request.Request(
        BASE + path,
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return json.loads(e.read().decode("utf-8"))


def balance(card_no):
    with urllib.request.urlopen(BASE + "/accounts/" + card_no, timeout=10) as resp:
        return json.loads(resp.read().decode("utf-8"))["data"]["balance"]


def burst(card_no, count, amount, prefix):
    results = []
    lock = threading.Lock()
    barrier = threading.Barrier(count)

    def worker(i):
        barrier.wait()
        r = call("/pay/consume", {
            "cardNo": card_no,
            "terminalNo": "POS-001",
            "payload": {"amount": amount},
            "ruleCode": "NORMAL",
            "payPwd": "123456",
            "requestNo": "%s%d" % (prefix, i),
        })
        with lock:
            results.append(r)

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(count)]
    start = time.time()
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    return results, (time.time() - start) * 1000


def report(title, results, cost, before, after):
    ok = [r for r in results if r["code"] == 0]
    bad = [r for r in results if r["code"] != 0]
    print("\n== %s ==" % title)
    print("并发 %d 笔 · 耗时 %.0f ms · 成功 %d 笔 / 拒绝 %d 笔" % (len(results), cost, len(ok), len(bad)))
    reasons = {}
    for r in bad:
        reasons[r["message"]] = reasons.get(r["message"], 0) + 1
    for k, v in reasons.items():
        print("  拒绝原因: %s × %d" % (k, v))
    print("余额: %s -> %s" % (before, after))


def ensure_balance(card_no, target):
    """把余额补到目标值（只补不减）。充值本身也走一遍完整交易链路，顺带验证一次"""
    cur = float(balance(card_no))
    if cur >= target:
        return cur
    need = round(target - cur, 2)
    call("/pay/recharge", {
        "cardNo": card_no,
        "amount": need,
        "requestNo": "PREP" + str(int(time.time() * 1000)),
    })
    return float(balance(card_no))


if __name__ == "__main__":
    tag = str(int(time.time()))  # 请求号带时间戳，不然重复跑会被幂等拦住
    ensure_balance(RATE_CARD, 30)
    ensure_balance(RACE_CARD, 100)
    print("限流卡 %s 余额 %s" % (RATE_CARD, balance(RATE_CARD)))
    print("竞争卡 %s 余额 %s" % (RACE_CARD, balance(RACE_CARD)))

    before = balance(RATE_CARD)
    res, cost = burst(RATE_CARD, 25, 1, "RL" + tag + "-")
    time.sleep(1.2)
    report("限流验证（代理，单卡每秒最多 20 笔，发 25 笔）", res, cost, before, balance(RATE_CARD))

    time.sleep(1.2)
    before = balance(RACE_CARD)
    res, cost = burst(RACE_CARD, 5, 30, "RC" + tag + "-")
    after = balance(RACE_CARD)
    report("余额竞争验证（5 笔 30 元同时抢 100 元余额）", res, cost, before, after)

    print("\n结论: %s" % ("余额未透支，并发扣减正确" if float(after) >= 0 else "余额透支，存在并发漏洞"))
    sys.exit(0)
