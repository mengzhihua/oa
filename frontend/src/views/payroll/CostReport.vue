<template>
  <PageShell
    title="人力成本报表"
    code="PAY-COST"
    ><el-card
      ><el-form inline
        ><el-form-item label="年份"
          ><el-date-picker
            v-model="year"
            type="year"
            value-format="YYYY" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table
        :data="rows"
        show-summary
        ><el-table-column
          prop="deptId"
          label="部门"
        /><el-table-column
          prop="yearMonth"
          label="月份"
        /><el-table-column label="应发"
          ><template #default="{ row }">{{ fmtMoney(row.gross) }}</template></el-table-column
        ><el-table-column label="公司社保公积金"
          ><template #default="{ row }">{{ fmtMoney(row.companyInsurance) }}</template></el-table-column
        ><el-table-column label="人力成本"
          ><template #default="{ row }">{{ fmtMoney(row.totalCost) }}</template></el-table-column
        ></el-table
      ></el-card
    ></PageShell
  >
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
import { fmtMoney } from '../../utils/format'
const year = ref(String(new Date().getFullYear()))
const rows = ref([])
async function load() {
  rows.value = await payrollApi.cost(year.value)
}
onMounted(load)
</script>
