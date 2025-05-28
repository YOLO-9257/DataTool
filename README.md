# DataTool

[![License](https://img.shields.io/github/license/yourusername/DataTool)](https://github.com/yourusername/DataTool/blob/main/LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/yourusername/DataTool/releases)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.java.com/)

灵活的数据导入工具，支持多种数据库和Excel/CSV格式

## 项目介绍

DataTool是一个灵活的数据导入工具，主要特性：

- 支持多种数据库：Oracle （目前支持这个后续可以不断添加）
- 支持多种文件格式：Excel（目前支持这个后续可以不断添加）
- 可扩展的数据处理管道
- 自定义数据处理器
- 数据转换和映射
- 值生成器（ID、时间戳、UUID等）

## 开发环境

- JDK 17
- Spring Boot 2.7.10
- Maven 3.8+

## 构建项目

```bash
mvn clean package
```

## 运行项目

```bash
java -jar target/datatool-1.0.0.jar
```

## ✨ 核心特性

- **多格式支持** - 完美兼容XLS、XLSX等多种Excel格式文件
- **灵活映射规则** - 允许用户自定义Excel列与数据库字段的映射关系
- **智能类型转换** - 自动处理数据类型转换，无需手动调整格式
- **批量导入能力** - 支持海量数据一次性导入，显著提升工作效率
- **数据校验功能** - 内置数据校验机制，确保导入数据的准确性
- **错误处理机制** - 友好的错误提示与日志记录，方便定位问题
- **可视化操作界面** - 简洁直观的用户界面，无需编程经验
- **自定义配置保存** - 可保存常用配置方案，一次配置多次使用

## 🚀 快速开始

### 环境要求

- Java 8+
- 支持的数据库：Oracle（目前支持这个后续可以不断添加）

### 安装方法

#### 方法一：直接下载

```bash
# 下载最新版本的JAR包
wget https://github.com/yourusername/DataTool/releases/download/v1.0.0/datatool-1.0.0.jar

# 启动应用
java -jar datatool-1.0.0.jar
```

#### 方法二：从源码构建

```bash
# 克隆代码仓库
git clone https://github.com/yourusername/DataTool.git

# 进入项目目录
cd DataTool

# 使用Maven构建项目
mvn clean package

# 启动应用
java -jar target/datatool-1.0.0.jar
```

## 📊 使用示例

### 示例1：Excel数据导入MySQL数据库

1. 启动DataTool应用
2. 选择Excel文件源
3. 配置数据库连接参数
4. 定义字段映射规则
5. 点击"开始导入"按钮
6. 查看导入结果报告

![Excel导入MySQL示例](https://example.com/screenshots/excel-to-mysql.png)

### 示例2：CSV批量更新数据库记录

1. 准备包含ID字段的CSV文件
2. 在DataTool中选择"更新模式"
3. 配置主键映射关系
4. 选择需要更新的字段
5. 执行更新操作

## 💡 应用场景

- **数据迁移** - 系统升级或更换时的历史数据迁移
- **批量数据处理** - 大量数据的快速导入与更新
- **报表数据整合** - 将多个Excel报表数据整合到数据库
- **数据仓库建设** - ETL过程中的数据抽取与加载
- **临时数据分析** - 将Excel分析结果快速导入数据库进行进一步处理

## 🔧 技术架构

- **前端**：JavaFX / Swing 用户界面
- **核心引擎**：Apache POI (Excel处理)、JDBC (数据库连接)
- **数据处理**：自研高性能数据处理框架
- **配置管理**：XML/JSON配置文件存储

## 🤝 参与贡献

我们非常欢迎您的贡献！无论是功能建议、代码贡献还是文档改进，您都可以：

1. Fork本项目
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个Pull Request

## 📜 版本历史

- **v1.0.0** - 2025-5-26
  - 首次公开发布
  - 支持基本的Excel到数据库导入功能
  - 实现自定义映射规则

## 📄 开源许可

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
