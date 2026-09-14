<template>
  <PageShell
    title="社保公积金规则"
    code="PAY-INSURANCE"
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="city"
          label="城市" /><el-table-column
          prop="pensionP"
          label="养老个人" /><el-table-column
          prop="pensionC"
          label="养老公司" /><el-table-column
          prop="medicalP"
          label="医疗个人" /><el-table-column
          prop="medicalC"
          label="医疗公司" /><el-table-column
          prop="hfP"
          label="公积金个人" /><el-table-column
          prop="hfC"
          label="公积金公司" /><el-table-column
          prop="siBaseMin"
          label="社保下限" /><el-table-column
          prop="siBaseMax"
          label="社保上限" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
onMounted(async () => {
  rows.value = await payrollApi.insuranceRules()
})
</script>
