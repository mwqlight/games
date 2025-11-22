<template>
  <div class="config-container">
    <el-card class="config-card" shadow="hover">
      <template #header>
        <span class="card-title">游戏配置</span>
      </template>
      
      <el-form label-width="120px" :model="gameConfig" :rules="rules" ref="configForm">
        <!-- 游戏难度设置 -->
        <el-form-item label="游戏难度" prop="difficulty">
          <el-select v-model="gameConfig.difficulty" placeholder="请选择游戏难度">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>

        <!-- AI对战设置 -->
        <el-form-item label="AI对战" prop="aiEnabled">
          <el-switch v-model="gameConfig.aiEnabled" />
        </el-form-item>

        <!-- 游戏音效设置 -->
        <el-form-item label="游戏音效" prop="soundEnabled">
          <el-switch v-model="gameConfig.soundEnabled" />
        </el-form-item>

        <!-- 背景音乐设置 -->
        <el-form-item label="背景音乐" prop="musicEnabled">
          <el-switch v-model="gameConfig.musicEnabled" />
        </el-form-item>

        <!-- 游戏速度设置 -->
        <el-form-item label="游戏速度" prop="gameSpeed">
          <el-slider v-model="gameConfig.gameSpeed" :min="1" :max="10" :step="1" show-input />
        </el-form-item>

        <!-- 保存按钮 -->
        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="saving">
            <el-icon v-if="saving"><Loading /></el-icon>
            保存配置
          </el-button>
          <el-button @click="resetConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const saving = ref(false)
const configForm = ref(null)

// 游戏配置数据
const gameConfig = reactive({
  difficulty: 'medium',
  aiEnabled: true,
  soundEnabled: true,
  musicEnabled: true,
  gameSpeed: 5
})

// 表单验证规则
const rules = {
  difficulty: [
    { required: true, message: '请选择游戏难度', trigger: 'change' }
  ]
}

// 从本地存储加载配置
const loadConfig = () => {
  try {
    const savedConfig = localStorage.getItem('gameConfig')
    if (savedConfig) {
      Object.assign(gameConfig, JSON.parse(savedConfig))
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

// 保存配置到本地存储
const saveConfig = async () => {
  try {
    await configForm.value.validate()
    saving.value = true
    
    // 模拟API请求
    setTimeout(() => {
      localStorage.setItem('gameConfig', JSON.stringify(gameConfig))
      ElMessage.success('配置保存成功')
      saving.value = false
      router.push('/')
    }, 1000)
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败，请检查表单')
  }
}

// 重置配置
const resetConfig = () => {
  Object.assign(gameConfig, {
    difficulty: 'medium',
    aiEnabled: true,
    soundEnabled: true,
    musicEnabled: true,
    gameSpeed: 5
  })
  configForm.value.clearValidate()
}

// 页面挂载时加载配置
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.config-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 0 20px;
}

.config-card {
  border-radius: 12px;
}

.card-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-button {
  margin-right: 12px;
}
</style>