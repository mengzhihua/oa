<template>
  <PageShell
    title="工作台"
    code="DASHBOARD"
    description="今天也高效完成每一项工作"
  >
    <template #actions>
      <el-button
        type="primary"
        :loading="clocking"
        @click="clock"
      >
        <el-icon><Timer /></el-icon>一键打卡
      </el-button>
    </template>
    <div class="metric-grid">
      <div
        v-for="metric in metrics"
        :key="metric.label"
        class="metric"
      >
        <div class="metric-label">{{ metric.label }}</div>
        <div class="metric-value">{{ metric.value }}</div>
        <div class="metric-hint">{{ metric.hint }}</div>
      </div>
    </div>
    <div class="dashboard-grid">
      <div class="panel">
        <div class="panel-title">
          最新公告
          <el-button
            link
            @click="router.push('/collab')"
            >查看全部</el-button
          >
        </div>
        <el-table
          :data="view.latestNotices || []"
          :show-header="false"
        >
          <el-table-column prop="title" />
          <el-table-column width="180">
            <template #default="{ row }">{{ fmtDateTime(row.publishedAt) }}</template>
          </el-table-column>
          <el-table-column width="90"
            ><template #default="{ row }"><StatusTag :value="row.status" /></template
          ></el-table-column>
        </el-table>
        <div
          v-if="!view.latestNotices?.length"
          class="empty"
        >
          暂无最新公告
        </div>
      </div>
      <div class="panel">
        <div class="panel-title">人事与财务概览</div>
        <div
          v-if="isHr"
          class="bar-list"
        >
          <div
            v-for="(count, name) in view.departmentCounts || {}"
            :key="name"
            class="bar-row"
          >
            <span>{{ name }}</span
            ><el-progress
              :percentage="Math.min(100, (count / Math.max(view.activeEmployees || 1, 1)) * 100)"
              :format="() => String(count)"
            /><b>{{ count }}</b>
          </div>
          <div class="bar-row">
            <span>在职人数</span
            ><el-progress
              :percentage="100"
              :format="() => String(view.activeEmployees || 0)"
            /><b>{{ view.activeEmployees || 0 }}</b>
          </div>
          <div class="bar-row">
            <span>本月入职</span
            ><el-progress
              :percentage="Math.min(100, (view.monthHires || 0) * 10)"
              :format="() => String(view.monthHires || 0)"
            /><b>{{ view.monthHires || 0 }}</b>
          </div>
          <div class="bar-row">
            <span>异常打卡</span
            ><el-progress
              status="warning"
              :percentage="Math.min(100, (view.abnormalClocks || 0) * 10)"
              :format="() => String(view.abnormalClocks || 0)"
            /><b>{{ view.abnormalClocks || 0 }}</b>
          </div>
        </div>
        <div
          v-else-if="isFinance"
          class="finance-card"
        >
          <span>当前工资期间</span><strong>{{ view.payrollStatus || '暂无' }}</strong>
          <span>本月人力成本</span><strong>¥ {{ fmtMoney(view.payrollCost) }}</strong>
        </div>
        <div
          v-else
          class="personal-card"
        >
          <span>今日打卡</span><strong>{{ view.todayClock?.daily?.status || '未打卡' }}</strong>
          <span>本月请假</span><strong>{{ view.monthLeaveDays || 0 }} 天</strong>
        </div>
      </div>
      <div class="panel">
        <div class="panel-title">今日日程</div>
        <div
          v-if="view.todaySchedules?.length"
          class="schedule-list"
        >
          <div
            v-for="item in view.todaySchedules"
            :key="`${item.title}-${item.start_time || item.startTime}`"
          >
            <strong>{{ item.title }}</strong>
            <span
              >{{ fmtDateTime(item.start_time || item.startTime) }} ·
              {{ item.location || '未设置地点' }}</span
            >
          </div>
        </div>
        <div
          v-else
          class="empty"
        >
          今日暂无日程
        </div>
      </div>
    </div>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import { attendanceApi, dashboardApi } from '../api'
import { hasRole } from '../auth'
import PageShell from '../components/PageShell.vue'
import StatusTag from '../components/StatusTag.vue'
import { fmtDateTime, fmtMoney } from '../utils/format'

const router = useRouter()
const view = ref({})
const clocking = ref(false)
const isHr = computed(() => hasRole('ADMIN', 'HR'))
const isFinance = computed(() => hasRole('FINANCE'))
const metrics = computed(() => [
  { label: '我的待办', value: view.value.pendingTasks || 0, hint: '需要及时处理的审批' },
  { label: '申请中', value: view.value.runningApplications || 0, hint: '正在流转的申请' },
  { label: '未读消息', value: view.value.unreadMessages || 0, hint: '来自系统与协同' },
  {
    label: '本月迟到',
    value: view.value.monthLateCount || 0,
    hint: `${view.value.monthLeaveDays || 0} 天请假`,
  },
])

async function load() {
  view.value = await dashboardApi.summary()
}

async function clock() {
  clocking.value = true
  try {
    await attendanceApi.clock({ source: 'WEB', remark: '工作台打卡' })
    ElMessage.success('打卡成功')
    await load()
  } finally {
    clocking.value = false
  }
}

onMounted(load)
</script>
