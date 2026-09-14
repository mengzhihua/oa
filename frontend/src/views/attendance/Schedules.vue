<template>
  <PageShell
    title="排班管理"
    code="ATT-SCHEDULE"
    ><el-card
      ><el-form
        inline
        :model="query"
        ><el-form-item label="员工 ID"><el-input v-model="query.employeeId" /></el-form-item
        ><el-form-item label="日期范围"
          ><el-date-picker
            v-model="range"
            type="daterange"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ><el-button
          v-if="canWrite('ADMIN', 'HR', 'MANAGER')"
          @click="batchVisible = true"
          >批量排班</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="employeeId"
          label="员工" /><el-table-column label="日期"
          ><template #default="{ row }">{{ fmtDate(row.workDate) }}</template></el-table-column
        ><el-table-column
          prop="shiftId"
          label="班次" /></el-table></el-card
    ><el-dialog
      v-model="batchVisible"
      title="批量排班"
      width="480px"
      ><el-form
        :model="batch"
        label-width="90px"
        ><el-form-item label="部门 ID"><el-input v-model="batch.deptId" /></el-form-item
        ><el-form-item label="日期范围"
          ><el-date-picker
            v-model="batchRange"
            type="daterange"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-form-item label="班次 ID"><el-input v-model="batch.shiftId" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="batchVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="saveBatch"
          >保存</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '../../api/attendance'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
import { fmtDate } from '../../utils/format'
const rows = ref([])
const range = ref([])
const batchRange = ref([])
const batchVisible = ref(false)
const query = reactive({ employeeId: undefined, from: '', to: '' })
const batch = reactive({ deptId: undefined, shiftId: undefined })
async function load() {
  query.from = range.value?.[0]
  query.to = range.value?.[1]
  rows.value = await attendanceApi.schedules(query)
}
async function saveBatch() {
  await attendanceApi.batchSchedule({ ...batch, from: batchRange.value[0], to: batchRange.value[1] })
  batchVisible.value = false
  ElMessage.success('排班成功')
  await load()
}
onMounted(load)
</script>
