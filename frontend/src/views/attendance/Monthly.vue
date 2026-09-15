<template>
  <PageShell
    title="月度考勤汇总"
    code="ATT-MONTHLY"
    description="生成、确认并锁定工资核算所需考勤数据"
    ><template #actions
      ><el-button
        type="primary"
        @click="generate"
        >生成</el-button
      ><el-button
        :disabled="!canConfirm"
        @click="confirm"
        >确认</el-button
      ><el-button
        type="warning"
        :disabled="!canLock"
        @click="lock"
        >锁定</el-button
      ><el-button @click="exportCsv">导出 CSV</el-button></template
    ><el-card
      ><el-form inline
        ><el-form-item label="月份"
          ><el-date-picker
            v-model="yearMonth"
            type="month"
            value-format="YYYY-MM" /></el-form-item
        ><el-form-item label="部门 ID"><el-input v-model="deptId" /></el-form-item
        ><el-button @click="load">查询</el-button></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="employeeId"
          label="员工" /><el-table-column
          prop="yearMonth"
          label="月份" /><el-table-column
          prop="shouldDays"
          label="应出勤" /><el-table-column
          prop="actualDays"
          label="实出勤" /><el-table-column
          prop="lateCount"
          label="迟到次数" /><el-table-column
          prop="absentDays"
          label="缺勤天数" /><el-table-column
          prop="status"
          label="状态"
          ><template #default="{ row }"
            ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '../../api/attendance'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const yearMonth = ref(new Date().toISOString().slice(0, 7))
const deptId = ref()
const rows = ref([])
const canConfirm = computed(() => rows.value.length > 0 && rows.value.every((row) => row.status === 'DRAFT'))
const canLock = computed(
  () =>
    rows.value.length > 0 &&
    rows.value.some((row) => row.status !== 'LOCKED') &&
    rows.value.every((row) => row.status === 'DRAFT' || row.status === 'CONFIRMED'),
)
async function load() {
  const data = await attendanceApi.monthly({
    page: 1,
    size: 100,
    yearMonth: yearMonth.value,
    deptId: deptId.value,
  })
  rows.value = data.records || []
}
async function generate() {
  await attendanceApi.generateMonthly({ yearMonth: yearMonth.value, deptId: deptId.value })
  ElMessage.success('已生成')
  await load()
}
async function confirm() {
  await attendanceApi.confirmMonthly(yearMonth.value)
  ElMessage.success('已确认')
  await load()
}
async function lock() {
  await attendanceApi.lockMonthly(yearMonth.value)
  ElMessage.success('已锁定')
  await load()
}
async function exportCsv() {
  const blob = await attendanceApi.exportMonthly(yearMonth.value)
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `attendance-${yearMonth.value}.csv`
  link.click()
}
onMounted(load)
</script>
