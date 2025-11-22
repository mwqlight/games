# 小游戏集合

一个基于Vue 3和Spring Boot的小游戏集合项目，包含多种经典游戏。

## 功能特性

### 已实现的游戏

1. **井字棋游戏**
   - 支持双人对战和AI对战
   - 简单的AI策略
   - 游戏状态显示
   - 重新开始功能

2. **五子棋游戏**
   - 支持双人对战和AI对战
   - 简单的AI策略
   - 游戏状态显示
   - 重新开始和悔棋功能

3. **象棋游戏**
   - 支持双人对战和AI对战
   - 完整的象棋规则
   - 游戏状态显示
   - 重新开始、悔棋和翻转棋盘功能

### 游戏配置

- 游戏难度设置
- AI对战开关
- 游戏音效开关
- 背景音乐开关
- 游戏速度设置

## 技术栈

### 前端

- **Vue 3**: 渐进式JavaScript框架
- **Element Plus**: 基于Vue 3的组件库
- **Vue Router**: Vue.js的官方路由管理器
- **Vite**: 下一代前端构建工具

### 后端

- **Spring Boot**: 基于Spring的Java应用框架
- **Spring Data JPA**: Java持久化API
- **H2 Database**: 嵌入式数据库
- **Spring Security**: 安全框架

## 项目结构

```
games/
├── game-frontend/          # 前端项目
│   ├── src/
│   │   ├── components/     # Vue组件
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── utils/          # 工具函数
│   │   ├── App.vue         # 根组件
│   │   └── main.js         # 入口文件
│   ├── public/             # 静态资源
│   ├── package.json        # 前端依赖
│   └── vite.config.js      # Vite配置
├── game-backend/           # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java源代码
│   │   │   └── resources/  # 配置文件
│   │   └── test/           # 测试代码
│   ├── pom.xml             # Maven依赖
│   └── application.properties # 应用配置
└── README.md               # 项目说明
```

## 快速开始

### 环境要求

- Node.js 16+ 
- Java 17+ 
- Maven 3.8+ 

### 启动前端服务

```bash
cd game-frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 启动后端服务

```bash
cd game-backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080/api 启动

## 游戏说明

### 井字棋游戏

1. 点击棋盘上的空格放置棋子
2. 先连成三子者获胜
3. 可以选择双人对战或AI对战

### 五子棋游戏

1. 点击棋盘上的空格放置棋子
2. 先连成五子者获胜
3. 可以选择双人对战或AI对战
4. 支持悔棋功能

### 象棋游戏

1. 点击棋子选择，再次点击合法位置移动
2. 遵循中国象棋规则
3. 可以选择双人对战或AI对战
4. 支持悔棋和翻转棋盘功能

## 配置说明

在游戏配置页面可以设置：

- **游戏难度**: 简单、中等、困难
- **AI对战**: 开启/关闭AI
- **游戏音效**: 开启/关闭音效
- **背景音乐**: 开启/关闭背景音乐
- **游戏速度**: 1-10级调节

## 开发计划

- [ ] 添加更多游戏（贪吃蛇、扫雷、2048等）
- [ ] 实现游戏排行榜
- [ ] 添加用户系统
- [ ] 实现游戏存档功能
- [ ] 优化AI算法
- [ ] 添加游戏音效和背景音乐

## 贡献

欢迎提交Issue和Pull Request！

## 许可证

MIT License

