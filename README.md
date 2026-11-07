# 校园一卡通消费与结算系统

软件设计模式课程期末考查项目 · 题目一

## 一、技术栈

| 层次 | 选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.7、MyBatis 3.5.16（注解式 SQL，手动装配 SqlSessionFactory） |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3（浏览器 ESM 运行时，无需构建工具）+ 原生 CSS，单页应用 |
| 构建 | Maven（本地离线仓库） |

前端刻意不引入 Vite/webpack：`vendor/vue.global.js` 直接由浏览器加载，双击即可运行，答辩现场不依赖网络。

## 二、目录结构

```
campus-card/
├── backend/
│   ├── src/main/java/com/lwb/campuscard/
│   │   ├── common/          统一返回体、业务异常、错误码
│   │   ├── config/          MyBatis 与跨域配置
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
│   ├── lib/                 运行期依赖 jar（离线可直接启动）
│   └── pom.xml
├── sql/campus_card.sql      建库建表 + 初始数据
├── test/                    接口与并发测试脚本、测试记录
├── docs/                    考查报告（docx / pdf）、插图与生成脚本
├── build.sh                 离线编译脚本
├── prepare_lib.sh           从本地 m2 收集运行期 jar
├── run.bat                  Windows 一键启动
└── README.md
```

## 三、运行步骤

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/campus_card.sql
```

脚本会创建 `campus_card` 库、8 张表，并写入 4 个终端、4 条优惠规则、4 个学生账户。

### 2. 设置数据库密码

配置里不写明文密码，启动时从环境变量读取：

```yaml
spring:
  datasource:
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:change-me}
```

Windows 下：

```bat
set DB_PASSWORD=你的MySQL密码
```

Linux / macOS 下：

```bash
export DB_PASSWORD=你的MySQL密码
```

不改环境变量也可以，直接把 `application.yml` 里的 `${DB_PASSWORD:change-me}` 换成自己的密码，只是别把改后的文件提交到仓库。

### 3. 编译

```bash
bash build.sh -q compile
```

脚本会把 `JAVA_HOME` 临时指向 IDEA 自带的 JDK 25，并用 `-o` 走本地离线仓库。

### 4. 启动

先设好 `DB_PASSWORD`，再双击 `run.bat`，或：

```bash
cd backend && java -cp "target/classes;lib/*" com.lwb.campuscard.CampusCardApplication
```

启动后浏览器打开 <http://localhost:8080>。

`lib/` 里已经放好运行期 jar，不需要 Maven 打包成 fat jar。若换机器，先执行一次 `bash prepare_lib.sh` 从本地仓库重新收集。

## 四、初始数据

| 卡号 | 姓名 | 余额 | 积分 | 支付密码 |
| --- | --- | --- | --- | --- |
| 622100000001 | LWB | 200.00 | 500 | 123456 |
| 622100000002 | KR | 88.00 | 120 | 123456 |
| 622100000003 | 张三 | 15.00 | 0 | 123456 |
| 622100000004 | 李四 | 0.00（已挂失） | 30 | 123456 |

终端：`POS-001` 第一食堂 3 号窗口、`POS-002` 校园超市、`WATER-01` 梅园宿舍水控（0.50 元/升）、`GATE-01` 图书馆门禁（按次计费）。

优惠规则：`NORMAL` 原价、`DISC-85` 八五折、`SUB-2` 立减 2 元、`PTS-100` 100 积分抵 1 元。多条规则用逗号拼接可叠加，例如 `DISC-85,SUB-2`。

## 五、设计模式落点

| 模式 | 解决的变化点 | 关键类 |
| --- | --- | --- |
| 策略 | 计价规则随营销活动变化 | `PricingStrategy` 及四个实现 |
| 装饰器 | 多种优惠需要任意叠加、封顶 | `SubsidyDecorator`、`PointsDecorator`、`CapDecorator` |
| 责任链 | 支付前校验项可能增减、顺序可变 | `PayHandler` 六个校验节点 |
| 工厂 | 充值/消费/退款流程差异 | `TransactionFactory` |
| 建造者 | 交易单字段多、组装步骤固定 | `TransactionTicketBuilder`、`TransactionDirector` |
| 观察者 | 交易成功后要通知对账、统计、消息、积分 | `TradeEventPublisher` 与四个观察者 |
| 适配器 | 三类终端报文格式不同 | `PosTerminalAdapter`、`WaterTerminalAdapter`、`GateTerminalAdapter` |
| 代理 | 支付入口需要限流与操作留痕 | `CardServiceProxy`（`@Primary`） |
| 单例 | 全局配置与流水号生成器 | `SystemConfig`、`TradeNoGenerator` |

一笔消费的完整链路：

```
PayController → CardServiceProxy(限流/日志)
  → CardServiceImpl
      → TerminalAdapterRegistry.adapt     把终端报文转成统一 PayRequest
      → PricingService.calculate          策略 + 装饰器算实付金额
      → PayChainFactory.execute           责任链逐项校验并原子扣款
      → TransactionDirector.buildConsume  建造者组装交易单
      → TradeMapper.insert                落库
      → TradeEventPublisher.publish       通知对账/统计/消息/积分
```

## 六、接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/accounts` | 账户列表 |
| POST | `/api/accounts` | 开户 |
| GET | `/api/accounts/{cardNo}` | 账户详情 |
| POST | `/api/accounts/{cardNo}/status?status=` | 挂失/解挂/销户 |
| POST | `/api/accounts/{cardNo}/password` | 修改支付密码 |
| POST | `/api/pay/consume` | 消费扣款 |
| POST | `/api/pay/recharge` | 充值 |
| POST | `/api/pay/refund` | 退费冲正 |
| GET | `/api/trades` | 流水分页查询 |
| GET | `/api/stats/overview` | 总览指标 |
| GET | `/api/stats/trend?days=7` | 消费趋势 |
| GET | `/api/stats/type` | 交易类型分布 |
| GET | `/api/stats/terminal` | 终端排行 |
| GET | `/api/notifications` | 消息通知 |
| GET | `/api/reconciliation` | 实时对账快照 |
| GET | `/api/terminals` | 终端列表 |
| GET | `/api/rules` | 优惠规则 |
| POST | `/api/settlement/run?date=` | 执行日终结账 |
| GET | `/api/settlement?date=` | 查询结账单 |

统一返回体：`{ "code": 0, "message": "ok", "data": ... }`，业务异常时 `code` 为错误码、`message` 为提示语。

## 七、测试

`test/` 下是测试脚本，覆盖功能、边界、异常与并发四类场景。脚本只依赖 Python 标准库，服务启动后直接运行：

```bash
python test/api_test.py          # 48 个接口用例，逐条断言错误码
python test/concurrency_check.py # 并发限流与余额竞争
```

`test/测试记录.md` 是实际的执行结果表格，可直接对照报告第五章。

## 八、文档

`docs/期末考查报告.docx` 与同名的 pdf 是本次提交的报告，共 35 页，包含用例图、流程图、E-R 图、核心类图、时序图五张插图与 16 张表格。插图由 `docs/make_figures.py` 用 Pillow 绘制，报告由 `docs/make_report.py` 读取学院模板后生成，两个脚本都可重复执行。

## 九、常见问题

**端口被占用**

`application.yml` 里默认 8080，改 `server.port` 即可；或者先 `netstat -ano | findstr :8080` 找到占用进程。

**启动报 `Access denied for user 'root'`**

没有设置 `DB_PASSWORD` 环境变量，或者密码不对。Windows 下 `set DB_PASSWORD=你的密码` 后再启动，Linux/macOS 用 `export DB_PASSWORD=...`。

**启动报 `Unknown database 'campus_card'`**

还没有导入建库脚本，先执行第三节第 1 步。

**`mvn package` 失败**

本机离线仓库里缺 surefire 与 jar 插件的依赖，`package` 打不出 fat jar。这个项目不需要打包，`bash build.sh -q compile` 编译出 `target/classes`，再用 `run.bat`（或 `java -cp "target/classes;lib/*"`）启动即可。`lib/` 由 `prepare_lib.sh` 从本地 Maven 仓库复制。

**页面打开是空白**

确认服务已启动，并且 `backend/target/classes/static/index.html` 存在（编译时会从 `src/main/resources/static` 复制过去）。

## 十、说明

开发过程中使用 AI 辅助编码与文档整理，具体说明见报告第 7 章。
