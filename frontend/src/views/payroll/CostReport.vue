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
          label="部门" /><el-table-column
          prop="yearMonth"
          label="月份" /><el-table-column
          prop="gross"
          label="应发" /><el-table-column
          prop="companyInsurance"
          label="公司社保公积金" /><el-table-column
          prop="totalCost"
          label="人力成本" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
const year = ref(String(new Date().getFullYear()))
const rows = ref([])
async function load() {
  rows.value = await payrollApi.cost(year.value)
}
onMounted(load)
</script>
