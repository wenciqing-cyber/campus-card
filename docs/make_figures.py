# -*- coding: utf-8 -*-
"""生成报告插图：UML 用例图、架构图、类图、E-R 图、时序图"""
import math
import os

from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "images")
FONT = "C:/Windows/Fonts/msyh.ttc"
FONT_B = "C:/Windows/Fonts/msyhbd.ttc"

INK = "#1d1d1f"
GRAY = "#6e6e73"
LINE = "#86868b"
BLUE = "#0071e3"
PALE = "#f5f5f7"
BORDER = "#d2d2d7"


def _font(size, bold=False):
    return ImageFont.truetype(FONT_B if bold else FONT, size)


class Canvas:
    def __init__(self, w, h, bg="#ffffff"):
        self.w, self.h = w, h
        self.img = Image.new("RGB", (w, h), bg)
        self.d = ImageDraw.Draw(self.img)

    def text(self, xy, s, size=20, bold=False, fill=INK, anchor="mm"):
        self.d.text(xy, s, font=_font(size, bold), fill=fill, anchor=anchor)

    def tw(self, s, size=20, bold=False):
        return self.d.textlength(s, font=_font(size, bold))

    def box(self, x, y, w, h, label="", fill="#ffffff", outline=BORDER,
            radius=12, size=20, bold=False, fill_text=INK, width=2):
        self.d.rounded_rectangle([x, y, x + w, y + h], radius=radius,
                                 fill=fill, outline=outline, width=width)
        if label:
            self.text((x + w / 2, y + h / 2), label, size, bold, fill_text)

    def rect(self, x, y, w, h, outline=LINE, width=2):
        self.d.rectangle([x, y, x + w, y + h], outline=outline, width=width)

    def ellipse(self, x, y, w, h, label="", fill="#ffffff", outline=BORDER, size=18, bold=False):
        self.d.ellipse([x, y, x + w, y + h], fill=fill, outline=outline, width=2)
        if label:
            self.text((x + w / 2, y + h / 2), label, size, bold)

    def line(self, p1, p2, color=LINE, width=2, dashed=False):
        if not dashed:
            self.d.line([p1, p2], fill=color, width=width)
            return
        x1, y1 = p1
        x2, y2 = p2
        dist = math.hypot(x2 - x1, y2 - y1)
        if dist == 0:
            return
        dash, gap = 10, 7
        n = int(dist // (dash + gap)) + 1
        ux, uy = (x2 - x1) / dist, (y2 - y1) / dist
        for i in range(n):
            s = i * (dash + gap)
            e = min(s + dash, dist)
            self.d.line([(x1 + ux * s, y1 + uy * s), (x1 + ux * e, y1 + uy * e)], fill=color, width=width)

    def arrow(self, p1, p2, color=LINE, width=2, dashed=False, head=True, size=11):
        self.line(p1, p2, color, width, dashed)
        if not head:
            return
        x1, y1 = p1
        x2, y2 = p2
        ang = math.atan2(y2 - y1, x2 - x1)
        for sign in (1, -1):
            a = ang + sign * math.radians(26)
            self.d.line([(x2, y2), (x2 - size * math.cos(a), y2 - size * math.sin(a))], fill=color, width=width)

    def open_arrow(self, p1, p2, color=LINE, width=2, dashed=False):
        """UML 依赖/关联的空心箭头"""
        self.arrow(p1, p2, color, width, dashed)

    def actor(self, x, y, name, size=20):
        """火柴人，x/y 是头顶中心"""
        self.d.ellipse([x - 16, y, x + 16, y + 32], outline=INK, width=2)
        self.d.line([(x, y + 32), (x, y + 74)], fill=INK, width=2)
        self.d.line([(x - 26, y + 48), (x + 26, y + 48)], fill=INK, width=2)
        self.d.line([(x, y + 74), (x - 24, y + 112)], fill=INK, width=2)
        self.d.line([(x, y + 74), (x + 24, y + 112)], fill=INK, width=2)
        self.text((x, y + 136), name, size, True, INK)

    def save(self, name):
        path = os.path.join(OUT, name)
        self.img.save(path, dpi=(200, 200))
        print("saved", path, self.img.size)
        return path


# ---------------- 图 1 用例图 ----------------
def fig_usecase():
    c = Canvas(1720, 1080)
    c.box(280, 100, 1170, 860, "", "#fbfbfd", "#c7c7cc", 18)
    c.text((865, 140), "校园一卡通消费与结算系统", 26, True, INK)

    c.actor(140, 250, "学生（持卡人）")
    c.actor(140, 730, "终端设备")
    c.actor(1580, 500, "系统管理员")

    EW, EH = 250, 74
    pos = {}

    def uc(x, y, name):
        c.ellipse(x, y, EW, EH, name, "#ffffff", BLUE, 19)
        pos[name] = (x, y)

    c.text((475, 172), "账户与卡片", 21, True, BLUE)
    uc(350, 200, "查询账户")
    uc(350, 310, "充值")
    uc(350, 420, "查询流水")

    c.text((475, 612), "消费扣款", 21, True, BLUE)
    uc(350, 640, "刷卡消费")
    uc(350, 750, "水控计费")
    uc(350, 860, "门禁通行")

    c.text((740, 612), "事务与通知", 21, True, BLUE)
    uc(740, 640, "优惠计算")
    uc(740, 800, "交易通知推送")

    c.text((1245, 172), "结算与统计", 21, True, BLUE)
    uc(1110, 200, "开户 · 挂失")
    uc(1110, 310, "销户")
    uc(1110, 420, "日终结账")
    uc(1110, 530, "统计报表")
    uc(1110, 640, "实时对账")

    def left(name):
        x, y = pos[name]
        return (x, y + EH / 2)

    def right(name):
        x, y = pos[name]
        return (x + EW, y + EH / 2)

    for name in ["查询账户", "充值", "查询流水"]:
        c.line((178, 290), left(name), LINE, 2)
    for name in ["刷卡消费", "水控计费", "门禁通行"]:
        c.line((178, 770), left(name), LINE, 2)
    for name in ["开户 · 挂失", "销户", "日终结账", "统计报表", "实时对账"]:
        c.line((1548, 540), right(name), LINE, 2)

    # include：消费必须先算优惠，成交后统一推送通知
    c.arrow((600, 672), (740, 676), GRAY, 2, True)
    c.text((665, 655), "«include»", 15, False, GRAY)
    c.arrow((590, 700), (770, 800), GRAY, 2, True)
    c.text((705, 735), "«include»", 15, False, GRAY)

    c.save("fig1_usecase.png")


# ---------------- 图 2 分层架构 ----------------
def fig_arch():
    c = Canvas(1600, 1060)
    c.text((800, 48), "系统分层架构", 28, True, INK)

    layers = [
        ("表示层 Presentation", "Vue 3 单页应用（原生 ES Module，无构建）· 概览 / 消费 / 账户 / 流水 / 统计", "#eef5ff", BLUE),
        ("接口层 Controller", "AccountController · PayController · QueryController · MetaController", "#ffffff", BORDER),
        ("业务层 Service", "CardServiceImpl（交易编排）· AccountService · QueryService · SettlementService", "#ffffff", BORDER),
        ("设计模式层 pattern", "策略 · 装饰器 · 责任链 · 工厂 · 建造者 · 观察者 · 适配器 · 代理 · 单例", "#fff7e8", "#ff9f0a"),
        ("数据访问层 DAO", "AccountMapper · CardMapper · TradeMapper · TerminalMapper · DiscountRuleMapper", "#ffffff", BORDER),
        ("数据层 Data", "MySQL 8.0 · 8 张表 · 原子扣减 UPDATE ... WHERE balance >= ?", "#eef5ff", BLUE),
    ]
    y = 110
    for i, (title, desc, fill, color) in enumerate(layers):
        c.box(120, y, 1360, 118, "", fill, color, 14, width=2)
        c.text((150, y + 38), title, 24, True, INK, "lm")
        c.text((150, y + 80), desc, 19, False, GRAY, "lm")
        if i < len(layers) - 1:
            c.arrow((800, y + 118), (800, y + 148), LINE, 3)
        y += 150

    # 右侧标注模式所在层
    c.box(1210, 606, 260, 44, "九种模式集中在这一层", "#fff7e8", "#ff9f0a", 10, 17)
    c.save("fig2_arch.png")


# ---------------- 图 5 E-R 图 ----------------
def entity(c, x, y, w, title, fields):
    row_h, head_h = 31, 48
    h = head_h + len(fields) * row_h + 10
    c.d.rounded_rectangle([x, y, x + w, y + h], radius=10, fill="#ffffff", outline=BLUE, width=2)
    c.text((x + w / 2, y + head_h / 2), title, 21, True, INK)
    c.line((x + 2, y + head_h), (x + w - 2, y + head_h), BORDER, 2)
    for i, (name, tag) in enumerate(fields):
        yy = y + head_h + 8 + i * row_h + row_h / 2
        c.text((x + 16, yy), name, 17, False, INK, "lm")
        if tag:
            c.text((x + w - 16, yy), tag, 15, False, BLUE, "rm")
    return {"x": x, "y": y, "w": w, "h": h, "cx": x + w / 2, "cy": y + h / 2}


def fig_er():
    c = Canvas(1700, 1220)
    c.text((850, 44), "数据库 E-R 图", 28, True, INK)

    account = entity(c, 90, 130, 320, "account 账户", [
        ("id", "PK"), ("student_no", "UK"), ("student_name", ""), ("dept", ""),
        ("balance", ""), ("points", ""), ("status", ""), ("version", ""),
    ])
    card = entity(c, 1270, 130, 320, "card 卡", [
        ("id", "PK"), ("card_no", "UK"), ("account_id", "FK"),
        ("card_status", ""), ("pay_pwd", ""), ("issued_at", ""),
    ])
    terminal = entity(c, 90, 800, 320, "terminal 终端", [
        ("id", "PK"), ("terminal_no", "UK"), ("terminal_name", ""),
        ("terminal_type", ""), ("merchant", ""), ("rate", ""), ("status", ""),
    ])
    rule = entity(c, 1270, 780, 320, "discount_rule 规则", [
        ("id", "PK"), ("code", "UK"), ("rule_name", ""),
        ("rule_type", ""), ("params", ""), ("priority", ""), ("enabled", ""),
    ])
    trade = entity(c, 610, 300, 420, "trade 交易流水", [
        ("id", "PK"), ("trade_no", "UK"), ("request_no", "UK"),
        ("account_id", "FK"), ("card_no", "FK"), ("terminal_no", "FK"),
        ("trade_type", ""), ("origin_amount", ""), ("pay_amount", ""),
        ("strategy_code", "FK"), ("status", ""), ("origin_trade_no", ""),
    ])
    settle = entity(c, 620, 880, 400, "settlement 日终结算", [
        ("id", "PK"), ("settle_date", "UK"), ("terminal_no", "UK/FK"),
        ("trade_count", ""), ("total_amount", ""), ("refund_amount", ""),
    ])
    log = entity(c, 1270, 460, 320, "operation_log 操作日志", [
        ("id", "PK"), ("op_type", ""), ("target", ""), ("detail", ""), ("cost_ms", ""),
    ])

    def rel(p1, p2, l1, l2):
        c.line(p1, p2, LINE, 2)
        dx, dy = p2[0] - p1[0], p2[1] - p1[1]
        d = math.hypot(dx, dy) or 1
        nx, ny = -dy / d, dx / d

        def mark(t, s):
            x = p1[0] + dx * t + nx * 18
            y = p1[1] + dy * t + ny * 18
            c.text((x, y), s, 19, True, BLUE)

        mark(0.16, l1)
        mark(0.84, l2)

    rel((410, 277), (610, 400), "1", "N")       # account - trade
    rel((1270, 320), (1030, 360), "1", "N")     # card - trade
    rel((410, 880), (610, 700), "1", "N")       # terminal - trade
    rel((1270, 830), (1030, 690), "1", "N")     # discount_rule - trade
    rel((410, 960), (620, 960), "1", "N")       # terminal - settlement

    c.text((850, 1160), "关系说明：一个账户可持有一张卡、产生多笔交易；一台终端产生多笔交易与多份日终结算。",
           18, False, GRAY)
    c.text((110, 1160), "PK 主键   UK 唯一键   FK 外键", 18, False, GRAY, "lm")
    c.save("fig5_er.png")


# ---------------- 图 6 消费时序图 ----------------
def fig_sequence():
    c = Canvas(1800, 1320)
    c.text((900, 40), "一笔刷卡消费的调用时序", 28, True, INK)

    names = [
        ("终端设备", "Terminal"), ("PayController", "接口层"),
        ("CardServiceProxy", "代理"), ("CardServiceImpl", "业务编排"),
        ("PricingService", "策略 + 装饰器"), ("PayChainFactory", "责任链"),
        ("Mapper 层", "数据访问"), ("TradeEventPublisher", "观察者"),
    ]
    N = len(names)
    xs = [150 + i * 215 for i in range(N)]
    top, bot = 90, 1250
    for (cn, en), x in zip(names, xs):
        c.box(x - 90, top, 180, 76, "", "#eef5ff" if en != "观察者" else "#fff7e8", BLUE, 10)
        c.text((x, top + 27), cn, 16, True, INK)
        c.text((x, top + 56), en, 13, False, GRAY)
        c.line((x, top + 76), (x, bot), "#b9b9be", 2, dashed=True)

    def msg(a, b, y, label, ret=False):
        c.arrow((xs[a], y), (xs[b], y), INK if not ret else GRAY, 2, dashed=ret)
        c.text(((xs[a] + xs[b]) / 2, y - 15), label, 16, False, GRAY if ret else INK)

    def self_msg(i, y, label, side=1):
        x = xs[i]
        c.line((x, y), (x + 46 * side, y), LINE, 2)
        c.line((x + 46 * side, y), (x + 46 * side, y + 30), LINE, 2)
        c.arrow((x + 46 * side, y + 30), (x + 2 * side, y + 30), LINE, 2)
        if side > 0:
            c.text((x + 54, y + 15), label, 15, False, GRAY, "lm")
        else:
            c.text((x - 54, y + 15), label, 15, False, GRAY, "rm")

    y = 230
    steps = [
        ("msg", 0, 1, "POST /api/pay/consume"),
        ("msg", 1, 2, "consume(指令)　requestNo 为空则补 UUID"),
        ("self", 2, 0, "限流校验"),
        ("msg", 2, 3, "target.consume(req)"),
        ("msg", 3, 6, "幂等校验 + 取卡 / 账户 / 终端"),
        ("self", 3, 0, "适配器统一报文"),
        ("msg", 3, 4, "calculate(ruleCodes, req)"),
        ("self", 4, 0, "策略 + 装饰器叠加"),
        ("msg", 4, 3, "PricingResult", True),
        ("msg", 3, 5, "payChain.execute(ctx)"),
        ("msg", 5, 6, "UPDATE account SET balance=balance-?\nWHERE balance >= ?"),
        ("msg", 5, 3, "六道校验通过", True),
        ("msg", 3, 6, "tradeMapper.insert(trade)"),
        ("msg", 3, 7, "publish(TradeEvent)"),
        ("self", 7, 0, "四个观察者回调", -1),
        ("msg", 3, 2, "trade", True),
        ("msg", 2, 1, "Result.ok(trade)", True),
        ("msg", 1, 0, "扣款成功，返回实付金额", True),
    ]
    for st in steps:
        if st[0] == "msg":
            msg(st[1], st[2], y, st[3], len(st) > 4 and st[4])
        else:
            self_msg(st[1], y - 12, st[3], st[4] if len(st) > 4 else 1)
        y += 56

    c.text((900, 1295), "实线箭头为调用，虚线箭头为返回；自消息为组件内部处理。", 17, False, GRAY)
    c.save("fig6_sequence.png")


# ---------------- 图 3 交易流水线上的设计模式 ----------------
def pill(c, x, y, label, fill="#fff7e8", border="#ff9f0a", color="#b25e00", size=16):
    w = c.tw(label, size) + 26
    c.d.rounded_rectangle([x, y, x + w, y + 34], radius=17, fill=fill, outline=border, width=2)
    c.text((x + w / 2, y + 17), label, size, True, color)
    return w


def fig_pipeline():
    c = Canvas(1800, 660)
    c.text((900, 46), "一笔交易经过的设计模式", 28, True, INK)

    stages = [
        ("① 请求入口", "代理 Proxy",
         ["CardServiceProxy", "RateLimiter", "OperationLogMapper"], "限流、计时、写操作日志，业务本身不感知"),
        ("② 报文适配", "适配器 Adapter",
         ["TerminalAdapter «interface»", "PosTerminalAdapter", "WaterTerminalAdapter", "GateTerminalAdapter",
          "TerminalAdapterRegistry"], "收银报文、用水量、按次放行，统一成应付金额"),
        ("③ 优惠计价", "策略 Strategy + 装饰器",
         ["PricingContext", "Normal / Discount", "Subsidy / Points", "SubsidyDecorator", "PointsDecorator",
          "CapDecorator"], "一种基准规则，多层优惠叠加，最后封顶"),
        ("④ 校验扣款", "责任链 Chain",
         ["CardStatusHandler", "PasswordHandler", "SingleLimitHandler", "DailyLimitHandler",
          "BalanceHandler", "DeductHandler"], "六道校验串成一条链，最后一环做原子扣减"),
        ("⑤ 落单通知", "工厂 + 建造者 + 观察者",
         ["TransactionFactory", "TransactionDirector", "TransactionTicketBuilder", "TradeEventPublisher",
          "四个 TradeObserver"], "交易对象由工厂产出，单据由建造者组装，事件广播给观察者"),
    ]

    x = 25
    for i, (title, pat, items, desc) in enumerate(stages):
        c.box(x, 110, 330, 450, "", "#fbfbfd", BORDER, 16)
        c.text((x + 24, 152), title, 22, True, INK, "lm")
        py = 186
        # 模式名 pill 太长时换两行
        words = pat.split(" + ")
        line = ""
        for wd in words:
            trial = (line + " + " + wd) if line else wd
            if c.tw(trial, 16) > 275:
                pill(c, x + 24, py, line)
                py += 42
                line = wd
            else:
                line = trial
        if line:
            pill(c, x + 24, py, line)
            py += 56

        for it in items:
            c.text((x + 26, py), "· " + it, 17, False, INK, "lm")
            py += 34

        # 说明放框底
        words2 = desc
        yy = 500
        c.text((x + 24, yy), words2[:16], 16, False, GRAY, "lm")
        if len(words2) > 16:
            c.text((x + 24, yy + 26), words2[16:], 16, False, GRAY, "lm")

        if i < len(stages) - 1:
            c.arrow((x + 330, 335), (x + 355, 335), LINE, 3)
        x += 355

    c.text((25, 615), "同一笔刷卡消费依次经过上述五个环节；环节之间通过接口耦合，新增一种优惠或一台终端都不需要改动交易主流程。",
           18, False, GRAY, "lm")
    c.save("fig3_pipeline.png")


# ---------------- 图 4 核心类图 ----------------
def uml_class(c, x, y, w, name, stereo="", members=(), h=None, accent=BLUE, fill="#ffffff"):
    head = 46
    mh = 26
    if h is None:
        h = head + len(members) * mh + 14
    c.d.rounded_rectangle([x, y, x + w, y + h], radius=8, fill=fill, outline=accent, width=2)
    if stereo:
        c.text((x + w / 2, y + 17), stereo, 14, False, BLUE)
        c.text((x + w / 2, y + 37), name, 18, True, INK)
        c.line((x + 2, y + 50), (x + w - 2, y + 50), BORDER, 2)
        top = y + 58
    else:
        c.text((x + w / 2, y + 23), name, 18, True, INK)
        c.line((x + 2, y + head - 4), (x + w - 2, y + head - 4), BORDER, 2)
        top = y + head
    for i, m in enumerate(members):
        c.text((x + 12, top + i * mh + 13), m, 15, False, GRAY, "lm")
    return {"x": x, "y": y, "w": w, "h": h, "cx": x + w / 2, "cy": y + h / 2,
            "top": (x + w / 2, y), "bottom": (x + w / 2, y + h),
            "left": (x, y + h / 2), "right": (x + w, y + h / 2)}


def fig_class():
    c = Canvas(1600, 940)
    c.text((800, 42), "核心类图（上方为设计模式角色）", 28, True, INK)

    strat = uml_class(c, 80, 70, 380, "PricingStrategy", "«Strategy 策略»",
                      ["+ calculate(req) : PricingResult"])
    deco = uml_class(c, 80, 230, 380, "PricingDecorator", "«Decorator 装饰器»",
                     ["- inner : PricingStrategy", "+ calculate(req) : PricingResult"], accent="#ff9f0a")
    handler = uml_class(c, 80, 420, 380, "PayHandler", "«Chain 责任链»",
                        ["- next : PayHandler", "+ doHandle(ctx)", "# check(ctx)"])
    adapter = uml_class(c, 80, 620, 380, "TerminalAdapter", "«Adapter 适配器»",
                        ["+ support() : TerminalType", "+ adapt(raw, terminal) : PayRequest"])

    ctrl = uml_class(c, 640, 70, 400, "PayController", "@RestController",
                     ["+ consume(cmd) : Result", "+ recharge(cmd) : Result", "+ refund(cmd) : Result"])
    impl = uml_class(c, 640, 330, 400, "CardServiceImpl", "«Service 业务编排»",
                     ["- pricing : PricingService", "- chain : PayChainFactory",
                      "- adapters : TerminalAdapterRegistry", "- publisher : TradeEventPublisher"],
                     accent="#1d1d1f")
    mapper = uml_class(c, 640, 680, 400, "AccountMapper / TradeMapper", "«DAO 数据访问»",
                       ["+ deduct(id, amount) : int", "+ findByRequestNo(no)",
                        "+ insert(trade)", "+ settleByTerminal(date)"])

    facade = uml_class(c, 1180, 70, 340, "PayFacade", "«interface»",
                       ["+ consume(cmd)", "+ recharge(cmd)", "+ refund(cmd)"])
    proxy = uml_class(c, 1180, 260, 340, "CardServiceProxy", "«Proxy 代理»",
                      ["- target : CardServiceImpl", "- limiter : RateLimiter",
                       "- logMapper : OperationLogMapper"])
    pub = uml_class(c, 1180, 470, 340, "TradeEventPublisher", "«Subject 主题»",
                    ["- observers : List", "+ publish(event)"])
    obs = uml_class(c, 1180, 660, 340, "TradeObserver", "«Observer 观察者»",
                    ["+ onTrade(event)", "+ name() : String"])

    def link(p1, p2, label="", dashed=False, color=GRAY, dx=0, dy=-16):
        c.arrow(p1, p2, color, 2, dashed)
        if label:
            c.text(((p1[0] + p2[0]) / 2 + dx, (p1[1] + p2[1]) / 2 + dy), label, 14, False, GRAY)

    link(ctrl["right"], facade["left"], "依赖", False, LINE, 0, -14)
    link(facade["bottom"], proxy["top"], "«implements»", True, GRAY, 96, 0)
    link(proxy["left"], (impl["x"] + impl["w"], impl["y"] + 70), "", False, LINE)
    link((impl["x"] + impl["w"], impl["y"] + 110), pub["left"], "", False, LINE)
    link(pub["bottom"], obs["top"], "订阅", True, GRAY, 0, 4)
    link(ctrl["bottom"], impl["top"], "", False, LINE)

    link((impl["x"], impl["y"] + 50), deco["right"], "", False, LINE)
    link((impl["x"], impl["y"] + 100), handler["right"], "", False, LINE)
    link((impl["x"], impl["y"] + 150), adapter["right"], "", False, LINE)
    link(impl["bottom"], mapper["top"], "", False, LINE)
    link(deco["top"], strat["bottom"], "«implements»", True, GRAY, 84, 0)

    c.text((800, 900), "实线为关联 / 依赖，虚线箭头为接口实现；成员只保留与模式相关的部分，省略 getter / setter。",
           17, False, GRAY)
    c.save("fig4_class.png")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    fig_usecase()
    fig_arch()
    fig_er()
    fig_sequence()
    fig_pipeline()
    fig_class()
