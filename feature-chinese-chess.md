# 中国象棋
在当前游戏中，添加一个中国象棋的游戏，界面和UI沿用当前游戏的样式
## 核心功能
开发一个单机版中国象棋游戏。玩家（红方）在浏览器前端与电脑AI（黑方）进行对战。后端负责维护游戏核心状态与AI逻辑，前端负责渲染界面和处理玩家交互。

⚙️ 后端 (Spring Boot) 设计要点

1. 核心数据模型参考
// 棋子模型
public class ChessPiece {
    private String type; // 如"rook"（车）、"horse"（马）
    private String color; // "red" 或 "black"
    private int x, y;     // 棋盘坐标 (0-8, 0-9)
}

// 游戏状态模型
public class GameState {
    private ChessPiece[][] board = new ChessPiece[9][10]; // 9列10行棋盘
    private String currentPlayer; // 当前行棋方："red" | "black"
    private String gameStatus;     // 状态："PLAYING"、"RED_WIN"、"BLACK_WIN"
    private List<Move> moveHistory; // 走子记录
}
2. 关键API接口
POST /api/game/start：开始新游戏，初始化棋盘，红方先行。
POST /api/game/move：处理玩家走子。接收参数：fromX, fromY, toX, toY。后端需校验走法合法性，更新棋盘，随后调用AI逻辑。
GET /api/game/state：获取当前完整的游戏状态，用于前端同步。
3. 核心业务逻辑
走法规则引擎：为每种棋子（车、马、炮、兵、帅/将、士、相/象）实现其特定的移动与吃子规则。需特别注意马蹩腿、炮隔山打牛、将帅不能照面等中国象棋特殊规则。
胜负判定：实时检查是否出现“将死”情况。当一方“将”或“帅”被吃掉时，游戏结束。
4. 电脑AI实现（关键）

可采用Minimax算法为基础，并应用 Alpha-Beta剪枝进行优化，以减少不必要的计算，提升AI响应速度。
java
下载
复制
运行
// AI逻辑核心伪代码
public Move calculateAIMove(GameState state) {
    // 使用Minimax算法与Alpha-Beta剪枝进行决策
    return minimax(state, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false).getBestMove();
}
可通过调整搜索深度（maxDepth）来设置不同的AI难度级别。

🎨 前端 (Vue 3) 设计要点

1. 核心组件结构
ChessBoard.vue：棋盘组件。使用CSS Grid或Flexbox绘制10行9列的棋盘，并渲染棋子。
ChessPiece.vue：棋子组件。根据棋子类型和颜色（红/黑）显示不同的文字或图标。
GameStatus.vue：显示当前状态，如“红方行棋”、“黑方（电脑）思考中”、“游戏结束”。
GameControls.vue：提供“新游戏”、“悔棋”等操作按钮。
2. 状态管理 (推荐使用 Pinia)
// stores/game.js
export const useGameStore = defineStore('game', {
  state: () => ({
    board: [],
    currentPlayer: 'red',
    gameStatus: 'PLAYING'
  }),
  actions: {
    async makeMove(fromPos, toPos) { /* 调用走子API */ },
    async startNewGame() { /* 调用开始游戏API */ }
  }
});
3. 用户交互与动画
走子流程：玩家点击己方棋子（高亮显示）-> 系统计算并高亮其所有合法落点 -> 玩家点击目标位置 -> 前端发送走子请求。
动画效果：为棋子的移动和吃子添加平滑的CSS过渡动画，提升体验。
响应式布局：确保棋盘在不同尺寸的屏幕上都能正确显示。
🔗 前后端数据流示例
前端：加载页面，调用 /api/game/start初始化游戏。
玩家：点击棋子（如红方“车”），前端高亮其合法走法。
玩家：点击目标位置，前端调用 POST /api/game/move。
后端：校验走法 -> 若合法，更新棋盘 -> 自动触发AI计算并走子 -> 返回最新的游戏状态。
前端：根据响应更新界面，展示玩家和AI的两步走子结果。