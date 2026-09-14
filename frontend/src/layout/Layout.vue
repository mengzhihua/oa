<template>
  <el-container class="app-shell">
    <el-aside
      width="238px"
      class="sidebar"
    >
      <div class="brand">
        <div class="brand-mark">OA</div>
        <div>
          <strong>集团 OA</strong>
          <small>协同办公平台</small>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="transparent"
        text-color="#b7c2d9"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard"
          ><el-icon><Odometer /></el-icon>工作台</el-menu-item
        >
        <el-menu-item index="/org"
          ><el-icon><OfficeBuilding /></el-icon>组织架构</el-menu-item
        >
        <el-sub-menu index="hr">
          <template #title
            ><el-icon><User /></el-icon>组织人事</template
          >
          <el-menu-item index="/hr/employees">员工档案</el-menu-item>
          <el-menu-item index="/hr/contracts">合同管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="attendance">
          <template #title
            ><el-icon><Calendar /></el-icon>考勤管理</template
          >
          <el-menu-item index="/attendance">我的考勤</el-menu-item>
          <el-menu-item index="/attendance/manage">考勤管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/workflow"
          ><el-icon><Finished /></el-icon>审批中心</el-menu-item
        >
        <el-menu-item index="/payroll"
          ><el-icon><Wallet /></el-icon>工资管理</el-menu-item
        >
        <el-sub-menu index="collab">
          <template #title
            ><el-icon><Connection /></el-icon>协同办公</template
          >
          <el-menu-item index="/collab">公告与日程</el-menu-item>
          <el-menu-item index="/contacts">通讯录</el-menu-item>
        </el-sub-menu>
        <el-menu-item
          index="/system"
          v-if="isAdmin"
          ><el-icon><Setting /></el-icon>系统管理</el-menu-item
        >
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
              @click="router.push('/collab')"
              ><el-icon><Bell /></el-icon
            ></el-button>
          </el-badge>
          <el-dropdown @command="userCommand">
            <span class="user-entry">
              <el-avatar :size="32">{{ (auth.user?.realName || '员').slice(0, 1) }}</el-avatar>
              <span>{{ auth.user?.realName || auth.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <main class="main-content"><router-view /></main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Bell,
  Calendar,
  Connection,
  Finished,
  Odometer,
  OfficeBuilding,
  Setting,
  User,
  Wallet,
} from '@element-plus/icons-vue'
import { auth, clearAuth, setMe } from '../auth'
import { authApi, collabApi } from '../api'

const route = useRoute()
const router = useRouter()
const search = ref('')
const unread = ref(0)
const isAdmin = computed(() => (auth.me?.roles || []).some((role) => ['ADMIN', 'HR'].includes(role.code)))

async function searchContacts(query, callback) {
  if (!query) {
    callback([])
    return
  }
  try {
    const rows = await collabApi.contacts({ keyword: query })
    callback(rows.map((row) => ({ value: `${row.name} · ${row.deptName || ''}`, row })))
  } catch {
    callback([])
  }
}

function selectContact(item) {
  ElMessage.info(`${item.row.name} · ${item.row.mobile || item.row.email || '暂无联系方式'}`)
}

async function userCommand(command) {
  if (command === 'logout') {
    await authApi.logout().catch(() => {})
    clearAuth()
    router.push('/login')
  } else {
    ElMessageBox.prompt('请输入新密码', '修改密码', { inputType: 'password' })
      .then(async ({ value }) => {
        await authApi.password({ oldPassword: '', newPassword: value })
        ElMessage.success('密码修改成功')
      })
      .catch(() => {})
  }
}

onMounted(async () => {
  if (!auth.me) {
    try {
      setMe(await authApi.me())
    } catch {
      return
    }
  }
  unread.value = await collabApi.unread().catch(() => 0)
})
</script>
