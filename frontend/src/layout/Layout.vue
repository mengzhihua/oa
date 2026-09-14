<template>
  <el-container class="app-shell">
    <el-aside
      width="238px"
      class="sidebar"
    >
      <div class="brand">
        <div class="brand-mark">OA</div>
        <div><strong>集团 OA</strong><small>协同办公平台</small></div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="transparent"
        text-color="#b7c2d9"
        active-text-color="#ffffff"
      >
        <el-sub-menu
          v-for="group in visibleGroups"
          :key="group.name"
          :index="group.name"
        >
          <template #title
            ><el-icon><component :is="group.icon" /></el-icon>{{ group.name }}</template
          >
          <el-menu-item
            v-for="item in group.items"
            :key="item.path"
            :index="item.path"
            >{{ item.title }}</el-menu-item
          >
        </el-sub-menu>
      </el-menu>
      <div class="sidebar-foot">行业标准 OA · 后端服务 8086</div>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="crumb">
          <span>集团总部</span><b>/</b><strong>{{ route.meta.title || '工作台' }}</strong>
        </div>
        <div class="top-actions">
          <el-autocomplete
            v-model="search"
            :fetch-suggestions="searchContacts"
            placeholder="搜索通讯录"
            class="search-box"
            @select="selectContact"
          />
          <el-badge
            :value="unread"
            :hidden="!unread"
            class="notice-bell"
          >
            <el-button
              text
              circle
              @click="router.push('/collab/messages')"
              ><el-icon><Bell /></el-icon
            ></el-button>
          </el-badge>
          <el-dropdown @command="userCommand">
            <span class="user-entry">
              <el-avatar :size="32">{{ (auth.user?.realName || '员').slice(0, 1) }}</el-avatar>
              <span>{{ auth.user?.realName || auth.user?.username }}</span
              ><el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown
              ><el-dropdown-menu
                ><el-dropdown-item command="password">修改密码</el-dropdown-item
                ><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu
              ></template
            >
          </el-dropdown>
        </div>
      </el-header>
      <main class="main-content"><router-view /></main>
    </el-container>
  </el-container>
  <el-dialog
    v-model="passwordVisible"
    title="修改密码"
    width="420px"
  >
    <el-form
      :model="passwordForm"
      label-width="80px"
    >
      <el-form-item label="原密码"
        ><el-input
          v-model="passwordForm.oldPassword"
          type="password"
          show-password
      /></el-form-item>
      <el-form-item label="新密码"
        ><el-input
          v-model="passwordForm.newPassword"
          type="password"
          show-password
      /></el-form-item>
    </el-form>
    <template #footer
      ><el-button @click="passwordVisible = false">取消</el-button
      ><el-button
        type="primary"
        @click="changePassword"
        >保存</el-button
      ></template
    >
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import { auth, canWrite, clearAuth, setMe } from '../auth'
import { authApi } from '../api/auth'
import { collabApi } from '../api/collab'
import { groups } from '../router/groups'

const route = useRoute()
const router = useRouter()
const search = ref('')
const unread = ref(0)
const passwordVisible = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const visibleGroups = computed(() =>
  groups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => !item.roles.length || item.roles.some((role) => canWrite(role))),
    }))
    .filter((group) => group.items.length),
)

async function searchContacts(query, callback) {
  if (!query) return callback([])
  try {
    const rows = await collabApi.contacts({ keyword: query })
    callback(rows.map((row) => ({ value: `${row.name} · ${row.deptName || ''}`, row })))
  } catch {
    callback([])
  }
}

function selectContact(item) {
  router.push({ path: '/collab/contacts', query: { keyword: item.row.name } })
}

async function userCommand(command) {
  if (command === 'logout') {
    await authApi.logout().catch(() => {})
    clearAuth()
    router.push('/login')
  } else {
    passwordVisible.value = true
  }
}

async function refreshUnread() {
  unread.value = await collabApi.unread().catch(() => 0)
}

async function changePassword() {
  await authApi.password(passwordForm)
  passwordVisible.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  ElMessage.success('密码修改成功')
}

let timer
onMounted(async () => {
  if (!auth.me) {
    try {
      setMe(await authApi.me())
    } catch {
      return
    }
  }
  await refreshUnread()
  timer = window.setInterval(refreshUnread, 60000)
})
onUnmounted(() => window.clearInterval(timer))
</script>
