<template>
  <PageShell
    title="人事异动"
    code="HR-CHANGE"
    description="查询员工转正、调岗、调薪和离职记录"
  >
    <el-card
      ><el-form inline
        ><el-form-item label="员工 ID"
          ><el-input
            v-model="employeeId"
            clearable /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table
        :data="rows"
        stripe
        ><el-table-column
          prop="employeeId"
          label="员工 ID"
          width="100" /><el-table-column
          prop="changeType"
          label="异动类型" /><el-table-column
          prop="effectiveDate"
          label="生效日期" /><el-table-column
          prop="reason"
          label="原因" /><el-table-column label="前后信息"
          ><template #default="{ row }"
            ><JsonDrawer
              title="异动详情"
              :value="row" /></template></el-table-column></el-table
    ></el-card>
  </PageShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { hrApi } from '../../api/hr'
import JsonDrawer from '../../components/JsonDrawer.vue'
import PageShell from '../../components/PageShell.vue'
const employeeId = ref('')
const rows = ref([])
async function load() {
  const data = await hrApi.changes({ employeeId: employeeId.value || undefined })
  rows.value = data.records || data || []
}
onMounted(load)
</script>
