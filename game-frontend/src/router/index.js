import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/config', name: 'Config', component: () => import('../views/Config.vue') },
  { path: '/tic-tac-toe', name: 'TicTacToe', component: () => import('../views/TicTacToe.vue') },
  { path: '/gobang', name: 'Gobang', component: () => import('../views/Gobang.vue') },
  { path: '/chess', name: 'Chess', component: () => import('../views/Chess.vue') },
  // 其他游戏路由可以在这里添加
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router