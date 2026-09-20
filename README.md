# 基于 SpringBoot 的在线文本情感分析系统（纯 Java 版）

前后端分离项目的**后端部分**。情感分析引擎采用**情感词典法**（纯 Java 实现，中文分词使用 jieba 的 Java 移植版 `jieba-analysis`），
不依赖 Python、不依赖外部 API，开箱即用，适合作为毕业设计主体代码。

## 技术栈

- Spring Boot 2.7.18（JDK 8+）
- MyBatis-Plus 3.5.5 + MySQL 8
- JJWT 0.11.5（登录鉴权）
- jieba-analysis 1.0.2（中文分词）
- Spring Security Crypto（仅用 BCrypt 做密码加密）

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户模块 | 注册 / 登录（JWT）、用户信息、角色（USER / ADMIN） |
| 文本分析 | 单条文本情感分析、批量文本分析（返回每条结果） |
| 情感引擎 | 词典法：分词 → 去停用词 → 情感词加权（含否定词反转、程度副词加权）→ 输出极性/得分/置信度/关键词 |
| 历史记录 | 分页查询本人分析记录、删除记录 |
| 统计分析 | 情感分布（饼图数据）、近 7 日趋势（折线图数据）、总体概览 |
| 词典管理（ADMIN） | 自定义情感词典增删改查，实时生效 |

## 快速启动

1. 建库建表：执行 `sql/init.sql`（MySQL 8，默认库名 `sentiment_db`）
2. 修改 `src/main/resources/application.yml` 里的数据库用户名密码
3. 启动 `com.example.sentiment.SentimentApplication`
4. 首次启动会自动创建管理员账号：`admin / admin123`（控制台会打印）

## 主要 API（前缀 /api，除登录注册外需请求头 `Authorization: Bearer <token>`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册，body: {username, password, nickname} |
| POST | /api/auth/login | 登录，body: {username, password}，返回 token |
| POST | /api/analysis/analyze | 情感分析，body: {content} |
| POST | /api/analysis/batch | 批量分析，body: {contents: []} |
| GET | /api/record/list?page=1&size=10 | 分页查询本人记录 |
| DELETE | /api/record/delete/{id} | 删除记录 |
| GET | /api/stats/overview | 统计概览：总量/正向率/分布/近7日趋势 |
| GET | /api/dict/list | 词典列表（ADMIN） |
| POST | /api/dict/add | 新增情感词（ADMIN） |
| PUT | /api/dict/update | 修改情感词（ADMIN） |
| DELETE | /api/dict/delete/{id} | 删除情感词（ADMIN） |

## 词典文件说明

- `src/main/resources/data/sentiment_dict.csv`：情感词，格式 `词,极性(1/-1),权重(可省,默认1.0)`
- `src/main/resources/data/negation.txt`：否定词（每行一个）
- `src/main/resources/data/degree.txt`：程度副词，格式 `词,倍率`
- `src/main/resources/data/stopwords.txt`：停用词（每行一个）

> 注意：`negation.txt` 与 `degree.txt` 中的词**不要**再放进 `stopwords.txt`，否则会被过滤导致无法生效。
> 内置词典是精简起步集，论文实验时可替换为公开中文情感词典（如大连理工情感本体库）并扩充。

## 常见问题（排障）

### Q1：编译报 `ExceptionInInitializerError` / `com.sun.tools.javac.code.TypeTag :: UNKNOWN`
这是 **Lombok 与 JDK 版本不兼容**。原因：Spring Boot 2.7.18 默认托管的 Lombok 版本较老，
不支持 JDK 21+（JDK 23/24/25 上尤其明显）。
**本工程已在 `pom.xml` 中把 Lombok 固定为 `1.18.46`（支持到 JDK 25）**，拉取最新代码后：

1. IDEA 中 `File → Project Structure → Project → SDK` 建议选 **JDK 17 或 21**；
2. 执行 `mvn clean compile`（或 IDEA 右侧 Maven 面板点击刷新后重新 Build）；
3. 若仍报错：`File → Invalidate Caches / Restart`，再重新导入。

> 说明：`JDK 8` 用户可正常使用；新版 JDK 编译时提示 “要隐藏有关已过时选项的警告，
> 请使用 -Xlint:-options” 只是警告，本项目已通过 compilerArgs 静默处理，不影响运行。

### Q2：启动时报 `--add-opens` / 反射访问警告
JDK 21+ 运行 Spring Boot 2.7 时可能提示模块访问警告，可加 JVM 参数：
`--add-opens java.base/java.lang=ALL-UNNAMED`（仅影响日志，不影响功能）。

### Q3：控制台打印大量 SQL 日志
`application.yml` 中 `mybatis-plus.configuration.log-impl` 配的是 `StdOutImpl`，
论文/演示时可改为 `org.apache.ibatis.logging.nologging.NoLoggingImpl` 关闭。

## 评分算法简述（论文理论部分可直接引用）

1. 对文本分词、去停用词，得到词序列。
2. 遍历每个情感词 w，向前回看最多 3 个词：
   - 出现否定词 → 极性反转（乘 -1）；
   - 出现程度副词 → 强度乘以对应倍率。
3. 累加所有情感词贡献得到总分 score。
4. `score > 0.5` 判正向，`score < -0.5` 判负向，否则中性。
5. 置信度取 `min(1, |score|)` 作为启发式归一。
