<template>
  <PageShell
    title="薪资项目"
    code="PAY-ITEM"
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="code"
          label="编码" /><el-table-column
          prop="name"
          label="名称" /><el-table-column
          prop="type"
          label="类型"
          ><template #default="{ row }"><StatusTag :value="row.type" /></template></el-table-column
        ><el-table-column
          prop="calcType"
          label="计算方式" /><el-table-column
          prop="formula"
          label="公式" /><el-table-column
          prop="taxable"
          label="计税" /><el-table-column
          prop="isSystem"
          label="系统项" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const rows = ref([])
onMounted(async () => {
  rows.value = await payrollApi.items()
})
</script>
