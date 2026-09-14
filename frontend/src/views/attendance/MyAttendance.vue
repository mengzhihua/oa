<template>
  <PageShell
    title="我的考勤"
    code="ATT-MINE"
    description="查看月度打卡结果和今日打卡状态"
  >
    <template #actions
      ><el-button
        type="primary"
        :loading="clocking"
        @click="clock"
        >立即打卡</el-button
      ></template
    >
    <el-card class="today-card"
      ><div><span>今日状态</span><StatusTag :value="today.daily?.status" /></div>
      <div>上班：{{ today.firstIn || today.daily?.firstIn || '—' }}</div>
      <div>下班：{{ today.lastOut || today.daily?.lastOut || '—' }}</div></el-card
    >
    <el-card
      ><el-calendar v-model="calendarDate"
        ><template #date-cell="{ data }"
          ><div class="calendar-cell">
            <span>{{ data.day.split('-').pop() }}</span
            ><StatusTag
              v-if="byDate[data.day]"
              :value="byDate[data.day].status"
            /></div></template></el-calendar
    ></el-card>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '../../api/attendance'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const calendarDate = ref(new Date())
const today = ref({})
const rows = ref([])
const clocking = ref(false)
const yearMonth = computed(() => {
  const date = calendarDate.value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
})
const byDate = computed(() => Object.fromEntries(rows.value.map((row) => [row.workDate, row])))
async function load() {
  today.value = await attendanceApi.today()
  rows.value = await attendanceApi.dailyMine({ yearMonth: yearMonth.value })
}
async function clock() {
  clocking.value = true
  try {
    await attendanceApi.clock({ source: 'WEB' })
    ElMessage.success('打卡成功')
    await load()
  } finally {
    clocking.value = false
  }
}
watch(yearMonth, load)
onMounted(load)
</script>
