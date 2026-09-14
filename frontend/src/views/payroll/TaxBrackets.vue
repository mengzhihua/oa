<template>
  <PageShell
    title="个税税率表"
    code="PAY-TAX"
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="level"
          label="级数" /><el-table-column
          prop="minAmount"
          label="起始区间" /><el-table-column
          prop="maxAmount"
          label="结束区间" /><el-table-column
          prop="rate"
          label="税率" /><el-table-column
          prop="quickDeduction"
          label="速算扣除数" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
onMounted(async () => {
  rows.value = await payrollApi.taxBrackets()
})
</script>
