<template>
  <div class="chess-board">
    <div 
      v-for="(row, rowIndex) in 8" 
      :key="rowIndex" 
      class="chess-row"
      :class="{ 'reverse': rowIndex % 2 === 0 }"
    >
      <div 
        v-for="(col, colIndex) in 8" 
        :key="colIndex"
        class="chess-square"
        :class="{ 'light': (rowIndex + colIndex) % 2 === 0, 'dark': (rowIndex + colIndex) % 2 === 1 }"
        @click="handleSquareClick(rowIndex, colIndex)"
      >
        <div 
          v-if="board[rowIndex][colIndex]" 
          class="chess-piece"
          :class="{ 'selected': selectedPiece && selectedPiece.row === rowIndex && selectedPiece.col === colIndex }"
        >
          {{ getPieceSymbol(board[rowIndex][colIndex]) }}
        </div>
        <div 
          v-if="validMoves && validMoves.some(move => move.toRow === rowIndex && move.toCol === colIndex)"
          class="move-indicator"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useChessStore } from '../stores/chessStore';

const store = useChessStore();
const selectedPiece = ref(null);
const validMoves = ref([]);

const board = computed(() => store.board);
const currentPlayer = computed(() => store.currentPlayer);
const gameStatus = computed(() => store.gameStatus);

const getPieceSymbol = (piece) => {
  if (!piece) return '';
  const symbols = {
    'WHITE_PAWN': '♙',
    'WHITE_ROOK': '♖',
    'WHITE_KNIGHT': '♘',
    'WHITE_BISHOP': '♗',
    'WHITE_QUEEN': '♕',
    'WHITE_KING': '♔',
    'BLACK_PAWN': '♟',
    'BLACK_ROOK': '♜',
    'BLACK_KNIGHT': '♞',
    'BLACK_BISHOP': '♝',
    'BLACK_QUEEN': '♛',
    'BLACK_KING': '♚'
  };
  return symbols[piece.player + '_' + piece.type] || '';
};

const handleSquareClick = (row, col) => {
  const piece = board.value[row][col];
  
  // 如果点击的是当前玩家的棋子，显示其合法移动
  if (piece && piece.player === currentPlayer.value) {
    selectedPiece.value = { row, col };
    validMoves.value = store.getValidMovesForPiece(row, col);
  }
  // 如果点击的是合法移动的目标位置，执行移动
  else if (selectedPiece.value && validMoves.value.some(move => move.toRow === row && move.toCol === col)) {
    store.makeMove(selectedPiece.value.row, selectedPiece.value.col, row, col);
    selectedPiece.value = null;
    validMoves.value = [];
  }
  // 否则取消选择
  else {
    selectedPiece.value = null;
    validMoves.value = [];
  }
};
</script>

<style scoped>
.chess-board {
  display: flex;
  flex-direction: column;
  width: 400px;
  height: 400px;
  border: 2px solid #333;
}

.chess-row {
  display: flex;
  flex: 1;
}

.chess-row.reverse {
  flex-direction: row-reverse;
}

.chess-square {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}

.chess-square.light {
  background-color: #f0d9b5;
}

.chess-square.dark {
  background-color: #b58863;
}

.chess-piece {
  font-size: 36px;
  cursor: pointer;
  user-select: none;
}

.chess-piece.selected {
  background-color: rgba(255, 255, 0, 0.5);
  border-radius: 50%;
}

.move-indicator {
  width: 20px;
  height: 20px;
  background-color: rgba(0, 255, 0, 0.5);
  border-radius: 50%;
  position: absolute;
}
</style>