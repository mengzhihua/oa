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
      v-if="selectedPeriod"
      class="period-flow"
      ><div class="flow-header">
        <div>
          <b>{{ selectedPeriod.yearMonth }}</b
          ><StatusTag :value="selectedPeriod.status" />
        </div>
        <div class="flow-summary">
          人数 {{ selectedPeriod.headcount || 0 }} · 总应发 ¥{{ fmtMoney(selectedPeriod.totalGross) }} ·
          总实发 ¥{{ fmtMoney(selectedPeriod.totalNet) }}
        </div>
      </div>
      <el-steps
        :active="step(selectedPeriod.status)"
        simple
        finish-status="success"
        process-status="process"
        ><el-step title="开放" /><el-step title="已计算" /><el-step title="已审核" /><el-step
          title="已发放" /><el-step title="已关闭"
      /></el-steps>
      <div class="flow-actions">
        <el-tooltip
          v-if="selectedPeriod.status === 'OPEN'"
          content="请先锁定该月考勤"
          placement="top"
          ><span
            ><el-button
              type="primary"
              :disabled="selectedPeriod.attLocked !== 1"
              @click="action('calculate', selectedPeriod)"
              >计算</el-button
            ></span
          ></el-tooltip
        ><el-button
          v-if="selectedPeriod.status === 'CALCULATED' && canWrite('ADMIN', 'FINANCE')"
          type="primary"
          @click="action('approve', selectedPeriod)"
          >审核</el-button
        ><el-button
          v-if="selectedPeriod.status === 'CALCULATED' && canWrite('ADMIN', 'FINANCE')"
          @click="action('calculate', selectedPeriod)"
          >重算</el-button
        ><el-button
          v-if="selectedPeriod.status === 'APPROVED' && canWrite('ADMIN', 'FINANCE')"
          type="primary"
          @click="action('pay', selectedPeriod)"
          >发放</el-button
        ><el-button
          v-if="selectedPeriod.status === 'PAID' && canWrite('ADMIN', 'FINANCE')"
          @click="action('close', selectedPeriod)"
          >关闭</el-button
        ><el-button @click="exportCsv(selectedPeriod)">导出工资表</el-button>
      </div></el-card
    ><el-card
      ><el-table
        :data="rows"
        row-key="id"
        highlight-current-row
        @row-click="selectPeriod"
        ><el-table-column type="expand"
          ><template #default="{ row }"
            ><div class="period-expand">
              <StatusTag :value="row.status" />
              <span>考勤{{ row.attLocked === 1 ? '已锁定' : '未锁定' }}</span>
              <span>人数 {{ row.headcount || 0 }}</span>
              <span>应发 ¥{{ fmtMoney(row.totalGross) }}</span>
              <span>实发 ¥{{ fmtMoney(row.totalNet) }}</span>
            </div></template
          ></el-table-column
        ><el-table-column
          prop="yearMonth"
          label="期间"
          width="120"
        /><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column
          prop="headcount"
          label="人数"
        /><el-table-column label="总应发"
          ><template #default="{ row }">{{ fmtMoney(row.totalGross) }}</template></el-table-column
        ><el-table-column label="总实发"
          ><template #default="{ row }">{{ fmtMoney(row.totalNet) }}</template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click.stop="selectPeriod(row)"
              >查看流程</el-button
            ><el-button
              link
              @click.stop="exportCsv(row)"
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
import StatusTag from '../../components/StatusTag.vue'
import { fmtMoney } from '../../utils/format'

const rows = ref([])
const selectedPeriod = ref(null)
const openVisible = ref(false)
const yearMonth = ref(new Date().toISOString().slice(0, 7))

function step(status) {
  return { OPEN: 0, CALCULATED: 1, APPROVED: 2, PAID: 3, CLOSED: 4 }[status] ?? 0
}

function selectPeriod(row) {
  selectedPeriod.value = row
}

async function load() {
  const data = await payrollApi.periods({ page: 1, size: 100 })
  rows.value = data.records || []
  if (selectedPeriod.value) {
    selectedPeriod.value = rows.value.find((row) => row.id === selectedPeriod.value.id) || rows.value[0]
  } else {
    selectedPeriod.value = rows.value[0] || null
  }
}

async function openPeriod() {
  await payrollApi.open(yearMonth.value)
  openVisible.value = false
  ElMessage.success('期间已开启')
  await load()
}

async function action(type, row) {
  try {
    await payrollApi[type](row.id)
    ElMessage.success('操作成功')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '操作失败')
  }
}

async function exportCsv(row) {
  const blob = await payrollApi.exportPeriod(row.id)
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `payroll-${row.yearMonth}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

onMounted(load)
</script>
