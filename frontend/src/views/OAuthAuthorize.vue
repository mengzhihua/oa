<template>
  <div class="login-page">
    <el-card
      class="oauth-card"
      shadow="never"
    >
      <div class="eyebrow">集团统一身份认证</div>
      <h1>授权访问</h1>
      <p>
        <strong>{{ query.client_id || '第三方系统' }}</strong>
        请求访问您的 OA 账号信息
      </p>
      <div class="oauth-scope">
        <el-tag
          v-for="scope in scopes"
          :key="scope"
          >{{ scopeLabel(scope) }}</el-tag
        >
      </div>
      <el-alert
        title="授权后将返回原系统，您可以随时在系统管理中撤销授权。"
        type="info"
        :closable="false"
      />
      <div
        class="heading-actions"
        style="margin-top: 24px"
      >
        <el-button @click="reject">拒绝</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="agree"
          >同意并继续</el-button
        >
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { auth } from '../auth'
import { authApi } from '../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const query = reactive({ ...route.query })
const scopes = computed(() =>
  String(query.scope || 'openid profile')
    .split(' ')
    .filter(Boolean),
)

function scopeLabel(scope) {
  return (
    { openid: '身份标识', profile: '基本资料', email: '邮箱', phone: '手机号', roles: '角色' }[scope] || scope
  )
}

async function agree() {
  loading.value = true
  try {
    const response = await axios.get('/api/oauth/authorize', {
      params: { ...query, oa_token: auth.token },
      validateStatus: () => true,
    })
    if (response.status === 401) {
      router.replace({ path: '/login', query: { redirect: route.fullPath } })
      return
    }
    if (response.data.code !== 0) {
      ElMessage.error(response.data.msg || '授权失败')
      return
    }
    window.location.href = response.data.data.redirectUrl
  } finally {
    loading.value = false
  }
}

function reject() {
  const redirect = query.redirect_uri
  if (redirect) {
    const url = new URL(redirect)
    url.searchParams.set('error', 'access_denied')
    if (query.state) url.searchParams.set('state', query.state)
    window.location.href = url.toString()
  } else {
    router.push('/dashboard')
  }
}

onMounted(async () => {
  if (!auth.token) return
  await authApi.me().catch(() => {})
})
</script>
