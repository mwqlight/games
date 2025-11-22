<template>
  <div class="home-container">
    <header class="header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="title">小游戏集合</h1>
          <p class="subtitle">欢迎来到小游戏世界，选择你喜欢的游戏开始吧！</p>
        </div>
        <el-button 
          type="primary" 
          icon="Setting"
          @click="goToConfig"
          class="config-button"
        >
          游戏配置
        </el-button>
      </div>
    </header>
    <main class="main-content">
      <div class="game-grid">
        <el-card class="game-card" v-for="game in games" :key="game.id" @click="goToGame(game)">
          <el-icon size="48" :color="game.iconColor"><component :is="game.icon" /></el-icon>
          <h3>{{ game.name }}</h3>
          <p>{{ game.description }}</p>
        </el-card>
      </div>
    </main>
    <footer class="footer">
      <p>&copy; 2025 小游戏集合</p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Trophy, Medal, Star } from '@element-plus/icons-vue'

const router = useRouter()

const games = ref([
  { 
    id: 1, 
    name: '井字棋', 
    description: '经典的井字棋游戏，支持双人对战和AI对战', 
    icon: Grid, 
    iconColor: '#E6A23C', 
    path: '/tic-tac-toe' 
  },
  { 
    id: 2, 
    name: '五子棋', 
    description: '规则极简，策略性强，易学难精', 
    icon: Star, 
    iconColor: '#409EFF', 
    path: '/gobang' 
  },
  { 
    id: 3, 
    name: '象棋', 
    description: '博大精深，锻炼思维，是经典的策略博弈', 
    icon: Trophy, 
    iconColor: '#67C23A', 
    path: '/chess' 
  },
  { 
    id: 4, 
    name: '跳棋', 
    description: '节奏明快，适合2-6人同乐', 
    icon: Medal, 
    iconColor: '#F56C6C', 
    path: '/checkers' 
  },
  { 
    id: 5, 
    name: '国际象棋', 
    description: '全球流行，战术层次丰富', 
    icon: Star, 
    iconColor: '#909399', 
    path: '/international-chess' 
  },
  { 
    id: 6, 
    name: '扑克', 
    description: '经典的扑克牌游戏，支持多种玩法', 
    icon: Star, 
    iconColor: '#D6A2E8', 
    path: '/poker' 
  },
  { 
    id: 7, 
    name: '二十一点', 
    description: '经典的赌场游戏，目标是接近21点', 
    icon: Trophy, 
    iconColor: '#FFD700', 
    path: '/blackjack' 
  },
  { 
    id: 8, 
    name: '双陆棋', 
    description: '古老的策略游戏，需要技巧和运气', 
    icon: Star, 
    iconColor: '#9370DB', 
    path: '/backgammon' 
  },
  { 
    id: 9, 
    name: '十点半', 
    description: '经典的扑克牌游戏，目标是接近10.30点', 
    icon: Trophy, 
    iconColor: '#FFA500', 
    path: '/ten-point-half' 
  }
])

const goToGame = (game) => {
  router.push(game.path)
}

const goToConfig = () => {
  router.push('/config')
}
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  text-align: center;
  padding: 2rem 1rem;
  color: white;
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.header p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.main-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.game-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.game-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.game-card h3 {
  margin: 1rem 0;
  font-size: 1.5rem;
  color: #303133;
}

.game-card p {
  text-align: center;
  color: #606266;
  margin-bottom: 1rem;
  flex: 1;
}

.footer {
  text-align: center;
  padding: 1rem;
  color: white;
  opacity: 0.8;
}
</style>