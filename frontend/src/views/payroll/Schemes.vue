<template>
  <PageShell
    title="薪资方案"
    code="PAY-SCHEME"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR', 'FINANCE')"
        type="primary"
        @click="open"
        >调薪</el-button
      ></template
    ><el-card
      ><el-form inline
        ><el-form-item label="员工 ID"><el-input v-model="employeeId" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="employeeId"
          label="员工" /><el-table-column label="生效日期"
          ><template #default="{ row }">{{ fmtDate(row.effectiveDate) }}</template></el-table-column
        ><el-table-column
          prop="baseSalary"
          label="基本工资" /><el-table-column
          prop="postSalary"
          label="岗位工资" /><el-table-column
          prop="perfSalary"
          label="绩效工资" /><el-table-column
          prop="status"
          label="状态"
          ><template #default="{ row }"
            ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-card
    ><el-dialog
      v-model="visible"
      title="调薪"
      width="460px"
      ><el-form
        :model="form"
        label-width="100px"
        ><el-form-item label="员工 ID"><el-input v-model="form.employeeId" /></el-form-item
        ><el-form-item label="新基本工资"
          ><el-input-number
            v-model="form.baseSalary"
            :min="0" /></el-form-item
        ><el-form-item label="新岗位工资"
          ><el-input-number
            v-model="form.postSalary"
            :min="0" /></el-form-item
        ><el-form-item label="新绩效工资"
          ><el-input-number
            v-model="form.perfSalary"
            :min="0" /></el-form-item
        ><el-form-item label="生效日期"
          ><el-date-picker
            v-model="form.effectiveDate"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-form-item label="原因"
          ><el-input
            v-model="form.reason"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
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
import { fmtDate } from '../../utils/format'
const rows = ref([])
const employeeId = ref()
const visible = ref(false)
const form = reactive({
  employeeId: undefined,
  baseSalary: 0,
  postSalary: 0,
  perfSalary: 0,
  effectiveDate: '',
  reason: '',
  allowancesJson: '{}',
  siBase: 0,
  hfBase: 0,
})
async function load() {
  const data = await payrollApi.schemes({ page: 1, size: 100, employeeId: employeeId.value })
  rows.value = data.records || []
}
function open() {
  visible.value = true
}
async function save() {
  await payrollApi.adjustScheme(form.employeeId, form)
  visible.value = false
  ElMessage.success('调薪方案已保存')
  await load()
}
onMounted(load)
</script>
