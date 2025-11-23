# 扫雷游戏
在当前项目添加一个扫雷，沿用当前项目UI风格，注意用户的交互游戏体验。
1. 项目概述
开发一个单机版扫雷游戏。玩家通过前端界面与游戏交互，后端负责维护核心游戏逻辑和状态。前端使用Vue 3构建响应式用户界面，后端使用Spring Boot提供RESTful API管理游戏逻辑。
2. 核心游戏规则与流程
游戏目标 在不触雷的情况下，揭开所有非地雷格子。
棋盘 默认采用9x9网格，内含10颗地雷（支持难度调整）。
格子状态 covered（未揭开）, revealed（已揭开）, flagged（插旗）, questioned（标记问号）。
交互操作 左键单击：揭开格子。右键单击：循环切换 covered -> flagged -> questioned -> covered 。
数字含义 已揭开的格子显示的数字代表其周围8个相邻格子中的地雷总数 。
空白扩散 如果揭开的格子周围地雷数为0（空白格），则自动递归揭开其所有相邻格子，直至遇到数字格 。
游戏开始 首次左键点击永远不会触雷（后端需在生成地雷时排除首次点击位置）。
游戏结束 失败：左键点击到地雷。胜利：所有非地雷格子均被正确揭开。

###  后端设计 (Spring Boot)
1. 核心数据模型
// 游戏状态类
public class GameState {
    private String gameId; // 游戏会话ID
    private int rows; // 棋盘行数
    private int cols; // 棋盘列数
    private int mineCount; // 地雷总数
    private Cell[][] board; // 棋盘数据（二维数组）
    private GameStatus status; // 游戏状态 (PLAYING, WON, LOST)
    private int flagsRemaining; // 剩余旗子数
}
// 格子状态类
public class Cell {
    private boolean isMine; // 是否是地雷
    private boolean isRevealed; // 是否被揭开
    private boolean isFlagged; // 是否被标记为旗子
    private int adjacentMines; // 周围地雷数 (0-8)
    // getters and setters
}

2. 核心API接口

方法 端点 描述 请求/响应示例

POST /api/game/new 创建新游戏，可接受难度参数。 {"difficulty": "BEGINNER"} -> {"gameId": "abc123", "board": [...], ...}

POST /api/game/{gameId}/reveal 揭开指定坐标的格子。处理空白格扩散逻辑。 {"x": 0, "y": 0} -> 返回更新后的完整 GameState

POST /api/game/{gameId}/flag 标记/取消标记指定坐标的格子。 {"x": 1, "y": 1, "flag": true}

GET /api/game/{gameId}/state 获取当前游戏状态。 返回 GameState

### 服务层核心逻辑

1.  游戏初始化 (GameService)：
    根据参数创建指定大小的棋盘（二维数组）。
    随机布雷：使用随机算法在棋盘上放置指定数量的地雷，需确保首次点击的位置绝不是地雷 。
    计算相邻地雷数：遍历每个非地雷格子，计算其周围8格的地雷数量并存储 。

2.  揭开格子逻辑：
    校验操作合法性（游戏是否结束、格子是否已揭开等）。
    如果揭开的是地雷，游戏状态设置为 LOST，并揭示所有地雷。
    如果揭开的是数字或空白格，更新格子状态。若为空白格（adjacentMines == 0），触发递归或迭代的扩散算法，自动揭开相连的空白区域和其边缘的数字 。
    每次操作后，检查是否满足胜利条件（所有非地雷格均被揭开）。

###  前端设计 (Vue 3 + Composition API)
1. 核心组件结构
    App.vue：根组件，管理游戏状态。
    GameBoard.vue：棋盘组件，使用CSS Grid或Flexbox渲染网格。
    GameCell.vue：单个格子组件，接收Cell数据作为prop，根据状态显示不同内容。
    GameInfo.vue：显示游戏信息，如剩余旗数、状态提示、重新开始按钮。
2. 状态管理 (推荐 Pinia)
// stores/game.js
import { defineStore } from 'pinia';

export const useGameStore = defineStore('game', {
  state: () => ({
    gameId: null,
    board: [],
    gameStatus: 'PLAYING', // PLAYING, WON, LOST
    flagsRemaining: 10
  }),
  actions: {
    async startNewGame(difficulty) { /* 调用后端 /api/game/new */ },
    async revealCell(x, y) { /* 调用后端 /api/game/reveal */ },
    async flagCell(x, y) { /* 调用后端 /api/game/flag */ }
  }
});
3. 用户交互与视图渲染

格子显示逻辑 ：
    isRevealed: true 且 isMine: true：显示地雷图标（游戏失败时）。
    isRevealed: true 且 isMine: false：显示 adjacentMines（为0则显示空白）。
    isRevealed: false 且 isFlagged: true：显示旗帜图标 🚩。
    isRevealed: false 且 isFlagged: false：显示为未揭开状态（如灰色方块）。
事件处理：
    在 GameCell 上监听 @click（左键）和 @contextmenu.prevent（右键，阻止默认菜单）。

    点击事件触发相应的 revealCell 或 flagCell Action，并更新Pinia状态。
### 关键实现细节与优化建议

1.  空白格扩散算法：使用广度优先搜索（BFS） 或深度优先搜索（DFS） 来实现。这是核心逻辑之一，确保性能 。
2.  响应式数据：Vue前端使用Pinia管理状态，利用Vue的响应式系统自动更新UI。
3.  用户体验：
    为格子点击和扩散效果添加简单的CSS过渡动画。
    提供即时反馈，例如点击地雷后高亮触雷的格子。

4.  难度扩展：支持初级（9x9, 10雷）、中级（16x16, 40雷）、高级（16x30, 99雷）等预设难度 。
5. 当前提供的代码只是逻辑示例，实际实现时需要考虑性能优化、错误处理、用户交互反馈等方面 。
