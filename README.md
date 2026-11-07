# 校园一卡通消费与结算系统

一套可直接运行的校园一卡通后端 + 单页前端，覆盖开户、充值、多终端消费、优惠计价、退费冲正、日终结账、实时对账与统计查询。后端用九种设计模式组织业务，前端零构建、开箱即用。

- **多终端统一接入**：食堂 POS、超市收银、宿舍水控、图书馆门禁四类报文，适配器统一成同一笔支付请求
- **优惠可叠加**：折扣、立减、积分抵扣任意组合，装饰器逐层包装，最后统一封顶
- **并发安全扣款**：条件更新 `UPDATE ... WHERE balance >= ?` 保证余额不透支，附带并发测试脚本
- **流水只追加**：退款不修改原单，靠新增反向单 + 原单标记已冲正
- **零构建前端**：Vue 3 浏览器运行时直接加载，不需要 Node.js

## 功能

| 模块 | 内容 |
| --- | --- |
| 账户与卡片 | 开户、挂失、解挂、销户、修改支付密码、账户详情 |
| 消费扣款 | POS 收银、水控按量计费、门禁按次计费；普通价、8.5 折、立减、积分抵扣及叠加 |
| 交易与结算 | 充值、消费、退费冲正；请求号幂等、并发扣款、日终按终端结账 |
| 查询与统计 | 流水分页筛选、总览指标、七日趋势、类型分布、终端排行、实时对账、操作日志 |
| 消息通知 | 交易完成后推送对账、统计、消息与积分四类观察者 |

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.7、MyBatis 3.5.16（注解式 SQL，手动装配 `SqlSessionFactory`） |
| 数据库 | MySQL 8.0（InnoDB，8 张表） |
| 前端 | Vue 3（浏览器 ESM 运行时，无需构建工具）+ 原生 CSS |
| 构建 | Maven |

## 快速开始

### 环境要求

- JDK 17 或更高
- MySQL 8.0
- Maven 3.6+（只有用 `build.sh` 的离线模式才需要额外的本地仓库，见下文）

### 1. 建库

```bash
mysql -uroot -p < sql/campus_card.sql
```

脚本会创建 `campus_card` 库、8 张表，写入 4 台终端、4 条优惠规则、4 个学生账户。

### 2. 配置数据库密码

`backend/src/main/resources/application.yml` 的密码从环境变量读取：

```yaml
spring:
  datasource:
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:change-me}
```

```bash
# Linux / macOS
export DB_PASSWORD=你的MySQL密码

# Windows
set DB_PASSWORD=你的MySQL密码
```

也可以直接改 `application.yml`，但改完别提交。

### 3. 编译

```bash
cd backend
mvn clean package
```

### 4. 启动

```bash
java -jar backend/target/campus-card-1.0.0.jar
```

浏览器打开 <http://localhost:8080>。

> **没有网络的环境**：本项目作者在完全离线的机器上开发，额外提供了 `build.sh`（自动找 JDK 与 Maven，参数原样透传给 mvn）和 `prepare_lib.sh`（把运行期 jar 收拢到 `backend/lib`）。离线时用 `bash build.sh -o -q compile` 编译，再执行 `run.bat`（Windows）或 `bash run.sh`（Linux / macOS）启动，不必打成 fat jar。

## 初始数据

| 卡号 | 姓名 | 余额 | 积分 | 支付密码 |
| --- | --- | --- | --- | --- |
| 622100000001 | LWB | 200.00 | 500 | 123456 |
| 622100000002 | KR | 88.00 | 120 | 123456 |
| 622100000003 | 张三 | 15.00 | 0 | 123456 |
| 622100000004 | 李四 | 0.00（已挂失） | 30 | 123456 |

终端：`POS-001` 第一食堂 3 号窗口、`POS-002` 校园超市、`WATER-01` 梅园宿舍水控（0.50 元/升）、`GATE-01` 图书馆门禁（按次计费）。

优惠规则：`NORMAL` 原价、`DISC-85` 八五折、`SUB-2` 立减 2 元、`PTS-100` 100 积分抵 1 元。多条规则用逗号拼接即可叠加，例如 `DISC-85,SUB-2`。

## 项目结构

```
campus-card/
├── backend/
│   ├── src/main/java/com/lwb/campuscard/
│   │   ├── common/          统一返回体、业务异常、错误码、全局异常处理
│   │   ├── config/          MyBatis、跨域、时间序列化、慢请求统计
│   │   ├── controller/      REST 接口层
│   │   ├── dao/             MyBatis Mapper（注解式）
│   │   ├── model/           实体、枚举、DTO
│   │   ├── pattern/         九种设计模式实现
│   │   │   ├── adapter/     终端报文适配器
│   │   │   ├── builder/     交易单据建造者
│   │   │   ├── chain/       支付校验责任链
│   │   │   ├── decorator/   优惠叠加装饰器
│   │   │   ├── factory/     交易类型工厂
│   │   │   ├── observer/    交易事件观察者
│   │   │   ├── proxy/       支付服务静态代理
│   │   │   ├── singleton/   配置与流水号单例
│   │   │   └── strategy/    计价策略
│   │   └── service/         账户、支付、查询、结算服务
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── static/          前端页面（index.html / css / js / vendor）
│   └── pom.xml
├── sql/campus_card.sql      建库建表 + 初始数据
├── test/                    接口与并发测试脚本
├── build.sh                 编译脚本（自动找 JDK 与 Maven）
├── run.sh / run.bat         启动脚本
├── prepare_lib.sh           离线环境收集运行期 jar
└── README.md
```

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/accounts` | 账户列表 |
| POST | `/api/accounts` | 开户 |
| GET | `/api/accounts/{cardNo}` | 账户详情 |
| POST | `/api/accounts/{cardNo}/status?status=` | 挂失 / 解挂 / 销户（1 正常 2 挂失 3 销户） |
| POST | `/api/accounts/{cardNo}/password?oldPwd=&newPwd=` | 修改支付密码 |
| POST | `/api/pay/consume` | 消费扣款 |
| POST | `/api/pay/recharge` | 充值 |
| POST | `/api/pay/refund` | 退费冲正 |
| GET | `/api/trades` | 流水分页查询（`cardNo`、`tradeType`、`terminalNo`、`from`、`to`、`page`、`size`） |
| GET | `/api/stats/overview` | 总览指标 |
| GET | `/api/stats/trend?days=7` | 消费趋势 |
| GET | `/api/stats/type` | 交易类型分布 |
| GET | `/api/stats/terminal` | 终端排行 |
| GET | `/api/notifications` | 消息通知 |
| GET | `/api/reconciliation` | 实时对账快照 |
| GET | `/api/logs?limit=20` | 操作日志 |
| GET | `/api/terminals` | 终端列表 |
| GET | `/api/rules` | 优惠规则 |
| POST | `/api/settlement/run?date=` | 执行日终结账 |
| GET | `/api/settlement?date=` | 查询结账单 |

统一返回体 `{ "code": 0, "message": "ok", "data": ... }`，业务异常时 `code` 非 0。

<details>
<summary>错误码</summary>

| 码 | 含义 |
| --- | --- |
| 1000 | 通用业务错误 |
| 1001 | 账户不存在 |
| 1002 | 卡已挂失 |
| 1003 | 卡已注销 |
| 1004 | 余额不足 |
| 1005 | 交易密码错误 |
| 1006 | 超单笔限额 |
| 1007 | 超日限额 |
| 1008 | 重复请求 |
| 1009 | 金额非法 |
| 1010 | 终端不可用 |
| 1011 | 原交易不存在 |
| 1012 | 已冲正过 |

</details>

消费请求示例：

```bash
curl -X POST http://localhost:8080/api/pay/consume \
  -H "Content-Type: application/json" \
  -d '{"terminalNo":"POS-001","cardNo":"622100000001","payPwd":"123456",
       "requestNo":"demo-001","ruleCode":"DISC-85","payload":{"amount":100}}'
```

`payload` 是终端原始字段：POS 传 `amount`，水控传 `volume`（升），门禁可留空按次计费。

## 设计说明

### 九种模式的落点

| 模式 | 解决的变化点 | 关键类 |
| --- | --- | --- |
| 策略 | 计价规则随活动变化 | `PricingStrategy` 及四个实现 |
| 装饰器 | 多种优惠任意叠加、封顶 | `SubsidyDecorator`、`PointsDecorator`、`CapDecorator` |
| 责任链 | 支付前校验项增减、顺序可变 | `PayHandler` 六个校验节点 |
| 工厂 | 充值 / 消费 / 退款流程差异 | `TransactionFactory` |
| 建造者 | 交易单字段多、组装步骤固定 | `TransactionTicketBuilder`、`TransactionDirector` |
| 观察者 | 交易成功后通知对账、统计、消息、积分 | `TradeEventPublisher` 与四个观察者 |
| 适配器 | 三类终端报文格式不同 | `PosTerminalAdapter`、`WaterTerminalAdapter`、`GateTerminalAdapter` |
| 代理 | 支付入口需要限流与操作留痕 | `CardServiceProxy`（`@Primary`） |
| 单例 | 全局配置与流水号生成器 | `SystemConfig`、`TradeNoGenerator` |

### 一笔消费的调用链路

```
PayController
  → CardServiceProxy            限流（每卡每秒 20 笔）+ 耗时统计 + 操作日志
  → CardServiceImpl             事务边界，幂等校验
      → TerminalAdapterRegistry.adapt      终端报文 → 统一 PayRequest
      → PricingService.calculate           策略 + 装饰器算实付，统一封顶 20 元
      → PayChainFactory.execute            挂失 → 密码 → 单笔限额 → 日限额 → 余额 → 原子扣款
      → TransactionDirector.buildConsume   建造者组装交易单
      → TradeMapper.insert                 落库
      → TradeEventPublisher.publish        通知对账 / 统计 / 消息 / 积分
```

## 测试

`test/` 下的脚本只依赖 Python 标准库，服务启动后直接运行：

```bash
python test/api_test.py          # 48 个接口用例，逐条断言错误码
python test/concurrency_check.py # 并发限流与余额竞争
```

`test/api_test.py` 覆盖开户、密码、充值、挂失、消费、优惠叠加、退款、销户、查询与结算共 12 组场景，包含金额为负、余额不足、重复请求、终端不存在等边界与异常。

`test/concurrency_check.py` 分两段：25 笔并发打同一张卡，验证代理限流生效；5 笔 30 元同时抢 100 元余额，验证条件更新不会把余额扣成负数。

## 常见问题

**端口被占用** —— 改 `application.yml` 里的 `server.port`，或 `netstat -ano | findstr :8080` 找到占用进程。

**启动报 `Access denied for user 'root'`** —— 没有设置 `DB_PASSWORD` 环境变量，或者密码不对。

**启动报 `Unknown database 'campus_card'`** —— 还没导入建库脚本，回到「快速开始」第 1 步。

**页面打开是空白** —— 确认服务已启动、控制台无异常，并且 `backend/target/classes/static/index.html` 存在（编译时从 `src/main/resources/static` 复制过去）。

**`mvn package` 卡在下载依赖** —— 检查网络或配置国内镜像。完全离线时改用 `bash build.sh -o -q compile` + `run.bat` / `run.sh` 的方式启动。

## 许可证

[MIT](LICENSE)
