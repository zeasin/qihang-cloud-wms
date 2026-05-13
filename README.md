# 启航跨境云仓WMS仓配系统

> **专为跨境云仓服务商打造** > **智能仓配，高效管理，一键无忧。**

**启航跨境云仓WMS** 是一款专为跨境云仓服务商量身定制的智能仓储管理系统。我们深知跨境云仓的核心痛点：多商户管理混乱、库存不准、计费复杂、物流对接麻烦。本系统旨在解决这些问题，通过标准化的仓储作业流程、灵活的多租户架构、强大的计费引擎，帮助云仓服务商提升效率、降低成本、服务更多商户。

---

## 💎 核心价值

* **多租户架构**：支持多货主数据隔离，一套系统服务多个客户
* **智能仓配**：波次拣货、推荐库位、PDA作业，提升仓储效率30%+
* **灵活计费**：支持按件、按重量、按体积、按天等多种计费规则
* **跨境赋能**：对接主流跨境物流渠道，支持报关资料生成
* **全链路打通**：从收货→仓储→打包→发货→报关，全流程数字化

---

## 🛠 主体功能矩阵

### 🏭 仓储作业管理 (WMS)
* **入库管理**：收货、质检、上架（推荐库位）、入库确认
* **在库管理**：库存查询、库存盘点、库存调拨、库存调整、库存预警
* **出库管理**：订单接收、波次管理、拣货（PDA拣货）、复核打包、出库发货
* **库位管理**：仓库、库区、库位三级管理，可视化库存布局

### 👥 货主管理 (多租户)
* **货主档案**：货主注册、资质审核、权限管理、数据隔离
* **货主商品**：商品资料同步、条码管理、批次/效期管理
* **货主账户**：账户余额、账单管理、费用对账

### 💰 计费系统 (云仓盈利核心)
* **计费规则**：入库费、仓储费、出库费、打包费、物流费、增值服务费
* **账单管理**：日账单、月账单、账单导出
* **费用明细**：费用记录、费用调整、费用对账

### 🌐 跨境特色功能
* **多仓管理**：国内仓、海外仓、仓间调拨
* **跨境物流**：物流渠道管理、物流单生成、物流轨迹、物流对账
* **报关管理**：报关资料生成、报关单、报关状态

### 📊 数据报表
* **库存报表**：库存周转率、呆滞料分析、库存预警报表
* **作业报表**：入库/出库/盘点作业报表、人员绩效报表
* **货主报表**：货主业务量报表、货主费用报表
* **财务报表**：收入报表、成本报表、利润报表

---

## 🚀 为什么选择启航？

1. **懂跨境电商**：承袭启航电商ERP 10年行业经验，真正懂跨境电商
2. **灵活计费**：支持各种复杂的计费规则，满足不同云仓的需求
3. **PDA作业**：支持PDA移动作业，提升拣货效率，减少错发漏发
4. **全链路打通**：从收货到发货到报关，全流程数字化管理
5. **开放API**：提供丰富的API接口，方便与货主的ERP系统对接

---

**[启航电商ERP：电商AI基座]** **[启航跨境云仓：您的智能仓配管家]**

---

### 🚀 需要更强大的功能？

当您的云仓规模扩大，需要更多高级功能（如自动化设备对接、智能算法优化、定制化开发）时，欢迎咨询：

👉 **启航跨境云仓商业版**

* **自动化对接**：支持AGV、自动化分拣线、智能货架等设备对接
* **智能优化**：智能波次、智能拣货路径、智能推荐库位
* **定制化开发**：支持个性化需求定制开发
* **专属服务**：专属技术顾问、7x24小时技术支持

---

## 部署说明

#### 0 版本说明
+ Java：17
+ Nodejs：v16
+ MySQL:8
+ Redis:7

#### 1 配置MySQL

+ 创建数据库`qihang-cross-border-wms`
+ 导入数据库结构：sql脚本`docs\qihang-cross-border-wms.sql`

#### 2 启动Redis
项目开发采用Redis7

#### 3 修改项目配置

+ 修改`api`项目中的配置文件`application.yml`配置`Mysql`相关配置。

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
| **跨境云仓WMS ⬅** | **跨境云仓智能仓配系统** | [Gitee](https://gitee.com/qiliping/qihang-stock-books) | [GitHub](https://github.com/zeasin/qihang-stock-books) |
| [跨境ERP](https://gitee.com/qiliping/qihang-cb-erp) | 跨境电商 | [Gitee](https://gitee.com/qiliping/qihang-cb-erp) | [GitHub](https://github.com/zeasin/qihang-cb-erp) |
| [SCM 供应链](https://gitee.com/qiliping/qihangerp-scm) | 多商户多供应商系统（已合并到商业版） | [Gitee](https://gitee.com/qiliping/qihangerp-scm) | [GitHub](https://github.com/zeasin/qihangerp-scm) |

## 💼 商业版

👉 **[启航跨境云仓商业版功能预览](https://gitee.com/qiliping/qihangerp-docs)**

👉 **了解更多？→** 电话/微信：15818590119

---

## 📱 关注我们

|                   公众号：启航电商ERP                   |                   个人号：码农老齐                   |
|:-----------------------------------------------:|:--------------------------------------------:|
|                 产品动态·行业方案·客户案例                  |                技术实战·开源故事·创业心得                |
| <img src="docs/wxmp_qherp.jpg" width="200px" /> | <img src="docs/wxmp_qi.jpg" width="200px" /> |

**感谢关注！我希望将从事电商10余年的行业经验沉淀在代码中，帮助大家真正提升经营效率。**

💖 如果项目对您有帮助，请点个 **Star ⭐** 给予鼓励！

---

## ☕ 支持作者

如果这个项目对您有用，欢迎请作者吃个盒饭，您的支持是项目持续更新的动力！

|                      微信支付                       |                     支付宝                      |
|:-----------------------------------------------:|:--------------------------------------------:|
|                   捐赠随意，捐赠进交流群                   |                捐赠随意，捐赠进交流群                |
| <img src="docs/微信收款码.jpg" width="200px" /> | <img src="docs/支付宝收款码.jpg" width="200px" /> |
