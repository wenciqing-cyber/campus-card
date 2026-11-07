-- ============================================================
-- 校园一卡通消费与结算系统  数据库初始化脚本
-- MySQL 8.0
-- ============================================================

DROP DATABASE IF EXISTS campus_card;
CREATE DATABASE campus_card DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_card;

-- 账户表：一卡一账户
CREATE TABLE account (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no    VARCHAR(20)  NOT NULL UNIQUE COMMENT '学号',
    student_name  VARCHAR(30)  NOT NULL COMMENT '姓名',
    dept          VARCHAR(50)  COMMENT '院系',
    balance       DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    points        INT          NOT NULL DEFAULT 0 COMMENT '积分余额',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 2挂失 3销户',
    version       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '账户';

-- 卡片表：记录卡片信息与交易密码
CREATE TABLE card (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_no     VARCHAR(20) NOT NULL UNIQUE COMMENT '卡号',
    account_id  BIGINT      NOT NULL,
    card_status TINYINT     NOT NULL DEFAULT 1 COMMENT '1正常 2挂失 3注销',
    pay_pwd     CHAR(6)     NOT NULL DEFAULT '123456' COMMENT '交易密码',
    issued_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_card_account (account_id)
) COMMENT '卡片';

-- 消费终端表：POS / 门禁 / 水控
CREATE TABLE terminal (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_no   VARCHAR(24) NOT NULL UNIQUE COMMENT '终端编号',
    terminal_name VARCHAR(50) COMMENT '终端名称',
    terminal_type VARCHAR(16) NOT NULL COMMENT 'POS/GATE/WATER',
    merchant      VARCHAR(50) COMMENT '所属商户',
    rate          DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '水控倍率等',
    status        TINYINT     NOT NULL DEFAULT 1 COMMENT '1在线 0停用'
) COMMENT '消费终端';

-- 交易流水表：只追加，不修改金额
CREATE TABLE trade (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    trade_no       VARCHAR(32)   NOT NULL UNIQUE COMMENT '交易单号',
    request_no     VARCHAR(40)   NOT NULL COMMENT '幂等键',
    account_id     BIGINT        NOT NULL,
    card_no        VARCHAR(20)   NOT NULL,
    trade_type     VARCHAR(16)   NOT NULL COMMENT 'RECHARGE/CONSUME/REFUND',
    terminal_no    VARCHAR(24),
    origin_amount  DECIMAL(12,2) COMMENT '原价',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount     DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    points_delta   INT           NOT NULL DEFAULT 0 COMMENT '积分变动',
    strategy_code  VARCHAR(32)   COMMENT '本次采用的优惠策略',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '1成功 2失败 3已冲正',
    origin_trade_no VARCHAR(32)  COMMENT '冲正时指向原交易单号',
    remark         VARCHAR(200),
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_request_no (request_no),
    KEY idx_trade_account (account_id, created_at),
    KEY idx_trade_terminal (terminal_no, created_at),
    KEY idx_trade_time (created_at)
) COMMENT '交易流水';

-- 优惠规则表：策略模式的配置来源
CREATE TABLE discount_rule (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    code       VARCHAR(32) NOT NULL UNIQUE COMMENT '策略编码',
    rule_name  VARCHAR(50) NOT NULL,
    rule_type  VARCHAR(16) NOT NULL COMMENT 'NORMAL/DISCOUNT/SUBSIDY/POINTS',
    params     VARCHAR(200) COMMENT '参数，如 0.85 或 2.00',
    priority   INT NOT NULL DEFAULT 0,
    enabled    TINYINT NOT NULL DEFAULT 1
) COMMENT '优惠规则';

-- 操作日志表：代理模式统一落库
CREATE TABLE operation_log (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    op_type    VARCHAR(32) NOT NULL COMMENT '操作类型',
    target     VARCHAR(64) COMMENT '操作对象',
    detail     VARCHAR(300),
    cost_ms    INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_op_time (created_at)
) COMMENT '操作日志';

-- 日终结算表：对账结果
CREATE TABLE settlement (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    settle_date  DATE NOT NULL,
    terminal_no  VARCHAR(24) NOT NULL,
    trade_count  INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    refund_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    status       TINYINT NOT NULL DEFAULT 1 COMMENT '1已平账 2有差异',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settle (settle_date, terminal_no)
) COMMENT '日终结算';

-- ------------------------------------------------------------
-- 初始化数据
-- ------------------------------------------------------------

INSERT INTO terminal (terminal_no, terminal_name, terminal_type, merchant, rate) VALUES
('POS-001', '第一食堂 3 号窗口', 'POS',  '第一食堂', 1.00),
('POS-002', '校园超市收银台',   'POS',  '教育超市', 1.00),
('WATER-01', '梅园宿舍水控',    'WATER', '后勤水电', 0.50),
('GATE-01', '图书馆门禁',       'GATE', '图书馆', 1.00);

INSERT INTO discount_rule (code, rule_name, rule_type, params, priority) VALUES
('NORMAL',   '普通扣款',   'NORMAL',   '',      0),
('DISC-85',  '食堂 85 折', 'DISCOUNT', '0.85',  1),
('SUB-2',    '早餐补贴 2 元','SUBSIDY',  '2.00',  2),
('PTS-100',  '100 积分抵 1 元','POINTS','100',   3);

-- 演示账户：余额 200 元，初始积分 500
INSERT INTO account (student_no, student_name, dept, balance, points, status) VALUES
('2024081120', 'LWB', '软件工程', 200.00, 500, 1),
('2024081121', 'KR',  '软件工程', 88.00,  120, 1),
('2024081122', '张三', '计算机科学', 15.00, 0,  1),
('2024081123', '李四', '网络工程',  0.00,  30, 2);

INSERT INTO card (card_no, account_id, card_status, pay_pwd)
SELECT CONCAT('6221', LPAD(id, 8, '0')), id, 1, '123456' FROM account;

-- 李四这张卡是挂失状态，卡面状态跟着账户一起置成 2
UPDATE card SET card_status = 2 WHERE account_id = (SELECT id FROM account WHERE student_no = '2024081123');
