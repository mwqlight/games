# 国际象棋
在当前项目添加一个国际象棋的功能，UI风格和当前项目的一致。
## 游戏功能
构建一个单机版国际象棋游戏。玩家（白方）在Vue3前端界面与电脑AI（黑方）对弈。SpringBoot后端提供RESTful API并封装核心游戏逻辑与AI决策。

⚙️ 后端 (Spring Boot) 设计要点

1. 核心数据模型
// 棋盘位置
public class Position {
    private int x; // 0-7 (a-h)
    private int y; // 0-7 (1-8)
}

// 棋子
public class Piece {
    private PieceType type; // KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
    private PieceColor color; // WHITE, BLACK
    private Position position;
}

// 棋步
public class Move {
    private Position from;
    private Position to;
    private PieceType promotion; // 兵的升变
}
2. 关键API接口
POST /api/game/start：开始新游戏，初始化棋盘。
POST /api/game/move：处理玩家走子。接收Move对象，校验合法性后更新棋盘，并触发AI回应。
GET /api/game/board：获取当前棋盘状态。
3. 服务层核心逻辑
规则引擎：为每种棋子实现特定移动规则（如王的王车易位、兵的首次两格、吃过路兵等）。
胜负判定：实时检查将军、将死、和棋（僵局、三次重复等）状态。
AI算法：采用Minimax算法与Alpha-Beta剪枝，并设计评估函数（基于子力价值、棋盘控制、位置优劣）来计算最佳着法。
🎨 前端 (Vue 3) 设计要点

1. 核心组件结构
ChessBoard.vue：棋盘组件，使用SVG或CSS Grid渲染8x8棋盘，并监听点击事件。
ChessPiece.vue：棋子组件，根据棋子类型和颜色显示对应图标。
GameStatus.vue：显示当前状态（如“白方行棋”、“将军”、“黑方思考中”）。
2. 状态管理 (Pinia)
// stores/game.js
export const useGameStore = defineStore('game', {
  state: () => ({
    board: [],
    currentPlayer: 'white',
    gameStatus: 'playing', // 'check', 'checkmate', 'stalemate'
    selectedPiece: null,
    validMoves: []
  }),
  actions: {
    async makeMove(from, to) { /* 调用API */ },
    async fetchGameState() { /* 同步状态 */ }
  }
});
3. 用户交互
走子流程：点击棋子高亮显示，并计算显示所有合法落点（validMoves）。点击目标位置后发送移动请求。
动画效果：为棋子移动添加平滑的CSS过渡动画。