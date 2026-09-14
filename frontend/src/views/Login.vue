<template>
  <div class="login-page">
    <el-card
      class="login-card"
      shadow="never"
    >
      <div class="login-title">集团 OA</div>
      <div class="login-subtitle">协同办公平台 · 统一身份认证</div>
      <el-form
        :model="form"
        @submit.prevent="submit"
      >
        <el-form-item>
          <el-input
            v-model="form.username"
            size="large"
            placeholder="账号"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            placeholder="密码"
            :prefix-icon="Lock"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="full-button"
          @click="submit"
        >
          登录
        </el-button>
      </el-form>
      <div class="demo-hint">演示账号：admin / admin123 · zhangsan / emp123</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { setAuth, setMe } from '../auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const result = await authApi.login(form)
    setAuth(result.token, result.user)
    setMe(await authApi.me())
    const redirect = route.query.redirect || '/dashboard'
    router.replace(String(redirect))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.full-button {
  width: 100%;
}
.demo-hint {
  color: #99a4b5;
  font-size: 12px;
  margin-top: 20px;
  text-align: center;
}
</style>
