<template>
  <PageShell
    title="部门考勤日报"
    code="ATT-DEPT-DAILY"
    ><el-card
      ><el-form inline
        ><el-form-item label="部门 ID"><el-input v-model="query.deptId" /></el-form-item
        ><el-form-item label="日期"
          ><el-date-picker
            v-model="query.date"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-checkbox v-model="abnormalOnly">只看异常</el-checkbox
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table
        :data="rows"
        :row-class-name="rowClass"
        ><el-table-column
          prop="employeeId"
          label="员工" /><el-table-column label="日期"
          ><template #default="{ row }">{{ fmtDate(row.workDate) }}</template></el-table-column
        ><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column
          prop="firstIn"
          label="上班" /><el-table-column
          prop="lastOut"
          label="下班" /><el-table-column
          prop="lateMinutes"
          label="迟到分钟" /><el-table-column
          prop="earlyMinutes"
          label="早退分钟" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { attendanceApi } from '../../api/attendance'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtDate, fmtDateTime } from '../../utils/format'
const query = reactive({ deptId: undefined, date: new Date().toISOString().slice(0, 10) })
const rows = ref([])
const abnormalOnly = ref(false)
async function load() {
  rows.value = abnormalOnly.value
    ? await attendanceApi.deptDaily({ ...query, abnormal: true })
    : await attendanceApi.deptDaily(query)
}
function rowClass({ row }) {
  return ['LATE', 'EARLY', 'ABSENT'].includes(row.status) ? 'warning-row' : ''
}
onMounted(load)
</script>
