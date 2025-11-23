import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/config', name: 'Config', component: () => import('../views/Config.vue') },
  { path: '/tic-tac-toe', name: 'TicTacToe', component: () => import('../views/TicTacToe.vue') },
  { path: '/gobang', name: 'Gobang', component: () => import('../views/Gobang.vue') },
  { path: '/chess', name: 'Chess', component: () => import('../views/Chess.vue') },
  { path: '/checkers', name: 'Checkers', component: () => import('../views/Checkers.vue') },
  { path: '/international-chess', name: 'InternationalChess', component: () => import('../views/InternationalChess.vue') },
  { path: '/card-game', name: 'CardGame', component: () => import('../views/CardGame.vue') },
  { path: '/minesweeper', name: 'Minesweeper', component: () => import('../views/Minesweeper.vue') },
  { path: '/snake', name: 'Snake', component: () => import('../views/Snake.vue') },
  { path: '/tetris', name: 'Tetris', component: () => import('../views/Tetris.vue') },
  { path: '/mahjong', name: 'Mahjong', component: () => import('../views/Mahjong.vue') },
  { path: '/puzzle', name: 'Puzzle', component: () => import('../views/Puzzle.vue') },
  { path: '/poker', name: 'Poker', component: () => import('../views/Poker.vue') },
  { path: '/blackjack', name: 'Blackjack', component: () => import('../views/Blackjack.vue') },
  { path: '/backgammon', name: 'Backgammon', component: () => import('../views/Backgammon.vue') },
  { path: '/connect-four', name: 'ConnectFour', component: () => import('../views/ConnectFour.vue') }
  // 其他游戏路由可以在这里添加
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router