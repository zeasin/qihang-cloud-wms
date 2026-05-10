# 启航电商库存账簿（开源版）


> **启航电商 ERP 官方配套系统** > **数据不出门，利润更有数。**

**启航电商库存账簿（开源版）** 是一款回归商业本质的经营管理工具。我们深知，对于广大线下门店和中小电商卖家而言，申请不到平台 AppKey（API 权限）是常态。因此，我们摒弃了复杂的自动化抓取，转而深耕“账务逻辑”与“实操效率”。本系统旨在解决中小卖家最头疼的三大问题：库存对不上、成本算不清、利润不透明。通过简单的“订单导入”与“进销存管理”，帮您把生意做成一本清清楚楚的明细账。

---

## 💎 核心价值

* **数据主权，绝对私有**：采用独立部署架构，所有商品、订单、供应商及利润数据均存储在用户自有服务器或本地环境，彻底杜绝数据泄露风险。
* **业财一体，精准核算**：打破“管货的不懂钱，算账的不懂货”的僵局，实现进销存变动与财务损益的深度勾稽。
* **ERP 基因，轻量体验**：承袭启航电商 ERP 全业务链的专业架构，却拥有账本工具级的上手难度，是中小卖家从 Excel 记账进化到系统化管理的最佳选择。

---

## 🛠 主体功能矩阵

### 📦 深度进销存管理 (Stock)
* **商品资料管理**：支持多规格 SKU 管理，建立标准化的商品档案与条码体系。
* **入/出库管理**：标准化的采购入库、销售出库流程，实时更新库存水位。
* **仓储精细化**：涵盖**仓库盘点、库内移库**等核心场景，确保账实相符。

### 💰 数字化财务核算 (Books)
* **店铺订单导入**：兼容主流电商平台导出报表，一键识别销售流水，替代繁琐的人工录入。
* **销售成本管理**：支持移动加权平均法等多种成本核算方式，精准捕捉每一单的真实货值成本。
* **店铺费用管理**：支持记录平台扣点、推广支出、仓储物流费及房租人工等杂费，全面覆盖经营成本。

### 📊 经营决策中心 (Analysis)
* **销售利润统计**：实时生成多维度利润报表（按店铺、按商品、按时间），净利润一目了然。
* **进销存数据分析**：动态监控库存周转率与呆滞料，优化资金占用，提升经营效率。

---

## 🚀 为什么选择启航？

1.  **安全屏障**：专为对经营数据敏感的卖家设计，是 2026 年数据安全时代下的必备“私有保险箱”。
2.  **万能适配**：即使没有技术接口，通过强大的报表导入解析引擎，依然能实现高效的数据同步。
3.  **极简运维**：基于现代技术栈（如 Node.js/Docker），支持快速一键部署，将技术门槛降至最低。

---

**[启航电商 ERP：电商 AI 基座]** **[启航店铺账本：您的私家进销存财务管家]**


---

### 🚀 业务规模扩大，需要全自动同步？

当您的店铺流水达到一定规模、申请到了官方 API (AppKey) 权限，或者需要处理千万级订单的并发自动化抓取时，欢迎升级至：

👉 **[启航电商ERP商业版](https://qihangerp.cn)**

* **全自动集成**：通过 API 实现全渠道订单、商品、库存的实时自动同步，无需人工导入。
* **多平台对接**：已对接淘宝、京东、拼多多、抖音、快手、小红书等主流平台。
* **AI-Native**：深度集成的 AI 接口协议，支持构建企业级 AI 电商助手。
* **省心省力**：无需自建技术团队，即买即用，持续更新。

---

## 部署说明

#### 0 版本说明
+ Java：17
+ Nodejs：v16
+ MySQL:8
+ Redis:7

#### 1 配置MySQL

+ 创建数据库`qihang-stock-books`
+ 导入数据库结构：sql脚本`docs\qihang-stock-books.sql`



#### 2 启动Redis
项目开发采用Redis7

#### 3 修改项目配置

+ 修改`app`项目中的配置文件`application.yml`配置`Mysql`相关配置。


#### 4 mvn打包部署
+ Java版本：`Java 17`
+ Maven版本：`3.8`
  `mvn clean package`


#### 5 前端 `vue`打包
+ nodejs版本要求：`v16.x`
+ 安装依赖：`npm install --registry=https://registry.npmmirror.com`
+ 打包`npm run build:prod`

#### 6 修改Nginx配置

```
# 前端web配置
location / {
        #root   /opt/qihangerp/nginx/dist;
        root /usr/share/nginx/html;
        index  index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
# 增加后台api转发
##### 修改Nginx配置（增加vue404、增加后台api转发）

location /prod-api/ {
    proxy_set_header Host $http_host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header REMOTE-HOST $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_pass http://localhost:8088/;
}
```
#### 7 访问web
+ 访问地址：`http://localhost`
+ 登录名：`admin`
+ 登录密码：`admin123`



---

## 📦 启航电商开源生态

启航电商旗下开源项目矩阵，所有项目共同指向统一商业版：

| 项目 | 定位 | Gitee | GitHub |
|:----|:----|:-----|:-------|
| [启航电商ERP](https://gitee.com/qiliping/qihang-ecom-erp-open) | 电商业务中台底座（微服务） | [Gitee](https://gitee.com/qiliping/qihang-ecom-erp-open) | [GitHub](https://github.com/zeasin/qihang-ecom-erp-open) |
| [OMS 订单中台](https://gitee.com/qiliping/qihang-oms) | 轻量级订单管理 | [Gitee](https://gitee.com/qiliping/qihang-oms) | [GitHub](https://github.com/zeasin/qihang-ecom-oms) |
| **库存账簿 ⬅** | **极简进销存** | [Gitee](https://gitee.com/qiliping/qihang-stock-books) | [GitHub](https://github.com/zeasin/qihang-stock-books) |
| [跨境ERP](https://gitee.com/qiliping/qihang-cb-erp) | 跨境电商 | [Gitee](https://gitee.com/qiliping/qihang-cb-erp) | [GitHub](https://github.com/zeasin/qihang-cb-erp) |
| [SCM 供应链](https://gitee.com/qiliping/qihangerp-scm) | 多商户多供应商系统（已合并到商业版） | [Gitee](https://gitee.com/qiliping/qihangerp-scm) | [GitHub](https://github.com/zeasin/qihangerp-scm) |

## 💼 商业版

👉 **[启航电商ERP商业版功能预览](https://gitee.com/qiliping/qihangerp-docs)**

**了解更多？→**

电话/微信：15818590119



## 📱 关注我们

|                   公众号：启航电商ERP                   |                   个人号：码农老齐                   |
|:-----------------------------------------------:|:--------------------------------------------:|
|                 产品动态·行业方案·客户案例                  |                技术实战·开源故事·创业心得                |
| <img src="docs/wxmp_qherp.jpg" width="200px" /> | <img src="docs/wxmp_qi.jpg" width="200px" /> |


**感谢关注！我希望将从事电商 10 余年的行业经验沉淀在代码中，帮助大家真正提升经营效率。**

💖 如果项目对您有帮助，请点个 **Star ⭐** 给予鼓励！


---

## ☕ 捐助作者

如果这个项目对您有用，欢迎请作者吃个盒饭，您的支持是项目持续更新的动力！

|                      微信支付                       |                     支付宝                      |
|:-----------------------------------------------:|:--------------------------------------------:|
|                   捐赠随意，捐赠进交流群                   |                捐赠随意，捐赠进交流群                |
| <img src="docs/微信收款码.jpg" width="200px" /> | <img src="docs/支付宝收款码.jpg" width="200px" /> |
