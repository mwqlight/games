# 项目概述

开发一款支持在线对战的跳棋游戏。后端使用SpringBoot提供API和WebSocket支持，前端使用Vue3构建响应式用户界面。游戏核心是遵循经典跳棋规则，实现双人对战。
## 核心功能需求
游戏房间管理：玩家可创建或加入房间，等待对手准备后开始游戏。
棋盘与棋子渲染：前端使用Canvas或SVG清晰绘制六星形棋盘（10x10格）及不同阵营的棋子。
游戏规则逻辑：
移动规则：实现棋子的“走子”（向相邻空位移动一步）和“跳子”（跳过相邻一枚棋子落到对称空位，并可连续跳跃）。
胜负判定：一方将所有棋子率先移动到对角阵营即为胜利。
实时对战：利用WebSocket实现双人实时同步，落子状态实时推送至对方。
游戏状态控制：包括行棋方提示、胜负判定提示、悔棋（可选）、重新开始等功能。
技术规格与实现要点
后端 (SpringBoot)
核心类设计：
GameRoom: 管理房间状态、玩家信息、当前棋局。
Board& Piece: 数据模型，维护棋盘状态和棋子位置。
MoveValidator: 核心服务，校验移动（走子/跳子）是否符合规则。
GameController: 处理HTTP请求（如创建房间）。
WebSocketConfig& WebSocketHandler: 配置和处理WebSocket连接，广播游戏状态。
API接口​ (示例):
POST /api/room/create: 创建房间。
POST /api/game/move: 处理移动请求（也可通过WebSocket消息处理）。
WebSocket消息​ (示例):
客户端发送：{type: "MAKE_MOVE", fromPos: [x1, y1], toPos: [x2, y2], roomId: "123"}
服务端广播：{type: "GAME_STATE_UPDATED", boardState: {...}, currentPlayer: "RED", gameStatus: "PLAYING"}
前端 (Vue 3 + Composition API)
核心组件：
GameBoard.vue: 渲染棋盘和棋子，处理用户点击事件。
GameRoom.vue: 管理游戏房间状态，处理WebSocket通信。
PlayerInfo.vue: 显示玩家信息和当前行棋方。
状态管理 (可使用 Pinia)：管理游戏状态（棋盘数据、当前玩家、房间信息等）。
用户交互：
点击棋子高亮，并标记出所有合法移动位置（绿色高亮）。
点击合法目标位置完成移动。