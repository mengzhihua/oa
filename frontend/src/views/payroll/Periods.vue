<template>
  <PageShell
    title="工资期间"
    code="PAY-PERIOD"
    description="按考勤锁定、计算、审核、发放和关闭工资"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'FINANCE')"
        type="primary"
        @click="openVisible = true"
        >开启新期间</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="yearMonth"
          label="期间"
          width="120"
        /><el-table-column label="流程"
          ><template #default="{ row }"
            ><el-steps
              :active="step(row.status)"
              simple
              ><el-step title="开放" /><el-step title="已计算" /><el-step title="已审核" /><el-step
                title="已发放" /><el-step title="已关闭" /></el-steps></template></el-table-column
        ><el-table-column
          prop="headcount"
          label="人数"
        /><el-table-column
          prop="totalGross"
          label="总应发"
        /><el-table-column
          prop="totalNet"
          label="总实发"
        /><el-table-column
          label="操作"
          width="300"
          ><template #default="{ row }"
            ><el-button
              v-if="row.status === 'OPEN'"
              type="primary"
              link
              @click="action('calculate', row)"
              >计算</el-button
            ><el-button
              v-if="row.status === 'CALCULATED' && canWrite('ADMIN', 'FINANCE')"
              link
              @click="action('approve', row)"
              >审核</el-button
            ><el-button
              v-if="row.status === 'APPROVED' && canWrite('ADMIN', 'FINANCE')"
              link
              @click="action('pay', row)"
              >发放</el-button
            ><el-button
              v-if="row.status === 'PAID'"
              link
              @click="action('close', row)"
              >关闭</el-button
            ><el-button
              link
              @click="exportCsv(row)"
              >导出</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="openVisible"
      title="开启工资期间"
      ><el-date-picker
        v-model="yearMonth"
        type="month"
        value-format="YYYY-MM"
      /><template #footer
        ><el-button @click="openVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="openPeriod"
          >开启</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payrollApi } from '../../api/payroll'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const openVisible = ref(false)
const yearMonth = ref(new Date().toISOString().slice(0, 7))
function step(status) {
  return { OPEN: 0, CALCULATED: 1, APPROVED: 2, PAID: 3, CLOSED: 4 }[status] ?? 0
}
async function load() {
  const data = await payrollApi.periods({ page: 1, size: 100 })
  rows.value = data.records || []
}
async function openPeriod() {
  await payrollApi.open(yearMonth.value)
  openVisible.value = false
  ElMessage.success('期间已开启')
  await load()
}
async function action(type, row) {
  await payrollApi[type](row.id)
  ElMessage.success('操作成功')
  await load()
}
async function exportCsv(row) {
  const blob = await payrollApi.exportPeriod(row.id)
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `payroll-${row.id}.csv`
  link.click()
}
onMounted(load)
</script>
