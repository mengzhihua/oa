<template>
  <PageShell
    title="工资单"
    code="PAY-SLIP"
    ><el-card
      ><el-form inline
        ><el-form-item label="期间 ID"><el-input v-model="query.periodId" /></el-form-item
        ><el-form-item label="部门 ID"><el-input v-model="query.deptId" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="yearMonth"
          label="期间"
        /><el-table-column
          prop="employeeId"
          label="员工"
        /><el-table-column label="应发"
          ><template #default="{ row }">{{ fmtMoney(row.gross) }}</template></el-table-column
        ><el-table-column label="社保"
          ><template #default="{ row }">{{ fmtMoney(row.siPersonal) }}</template></el-table-column
        ><el-table-column label="公积金"
          ><template #default="{ row }">{{ fmtMoney(row.hfPersonal) }}</template></el-table-column
        ><el-table-column label="个税"
          ><template #default="{ row }">{{ fmtMoney(row.tax) }}</template></el-table-column
        ><el-table-column label="实发"
          ><template #default="{ row }">{{ fmtMoney(row.net) }}</template></el-table-column
        ><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="detail(row)"
              >明细</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'FINANCE')"
              link
              @click="adjust(row)"
              >手工调整</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-drawer
      v-model="detailVisible"
      title="工资明细"
      ><el-descriptions
        :column="1"
        border
        ><el-descriptions-item label="应发">{{ fmtMoney(current.gross) }}</el-descriptions-item
        ><el-descriptions-item label="社保">{{ fmtMoney(current.siPersonal) }}</el-descriptions-item
        ><el-descriptions-item label="公积金">{{ fmtMoney(current.hfPersonal) }}</el-descriptions-item
        ><el-descriptions-item label="个税">{{ fmtMoney(current.tax) }}</el-descriptions-item
        ><el-descriptions-item label="实发">{{
          fmtMoney(current.net)
        }}</el-descriptions-item></el-descriptions
      >
      <pre>{{ format(current.itemsJson) }}</pre></el-drawer
    ><el-dialog
      v-model="adjustVisible"
      title="手工调整"
      ><el-form
        :model="adjustForm"
        label-width="80px"
        ><el-form-item label="项目编码"><el-input v-model="adjustForm.itemCode" /></el-form-item
        ><el-form-item label="金额"><el-input-number v-model="adjustForm.amount" /></el-form-item
        ><el-form-item label="原因"
          ><el-input
            v-model="adjustForm.reason"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="adjustVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="saveAdjust"
          >保存</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payrollApi } from '../../api/payroll'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtMoney } from '../../utils/format'
const rows = ref([])
const query = reactive({ page: 1, size: 20, periodId: undefined, deptId: undefined })
const current = ref({})
const adjustForm = reactive({})
const detailVisible = ref(false)
const adjustVisible = ref(false)
async function load() {
  const data = await payrollApi.slips(query)
  rows.value = data.records || []
}
function format(value) {
  try {
    return JSON.stringify(JSON.parse(value || '{}'), null, 2)
  } catch {
    return value || '{}'
  }
}
function detail(row) {
  current.value = row
  detailVisible.value = true
}
function adjust(row) {
  current.value = row
  Object.assign(adjustForm, { itemCode: '', amount: 0, reason: '' })
  adjustVisible.value = true
}
async function saveAdjust() {
  await payrollApi.adjustSlip(current.value.id, adjustForm)
  adjustVisible.value = false
  ElMessage.success('调整成功')
  await load()
}
onMounted(load)
</script>
