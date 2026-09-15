<template>
  <PageShell
    :title="`${employee.name || '员工'} · 档案详情`"
    code="HR-DETAIL"
  >
    <el-card
      ><el-descriptions
        :column="3"
        border
        ><el-descriptions-item label="工号">{{ employee.employeeNo }}</el-descriptions-item
        ><el-descriptions-item label="姓名">{{ employee.name }}</el-descriptions-item
        ><el-descriptions-item label="状态"
          ><StatusTag :value="employee.employmentStatus" /></el-descriptions-item
        ><el-descriptions-item label="手机号">{{ employee.mobile }}</el-descriptions-item
        ><el-descriptions-item label="邮箱">{{ employee.email }}</el-descriptions-item
        ><el-descriptions-item label="入职日期">{{ fmtDate(employee.hireDate) }}</el-descriptions-item
        ><el-descriptions-item label="身份证号">{{ employee.idCard }}</el-descriptions-item
        ><el-descriptions-item label="学历">{{ employee.education }}</el-descriptions-item
        ><el-descriptions-item label="地址">{{ employee.address }}</el-descriptions-item></el-descriptions
      ></el-card
    >
    <el-card class="detail-tabs"
      ><el-tabs
        ><el-tab-pane label="合同"
          ><el-table :data="contracts"
            ><el-table-column
              prop="contractNo"
              label="合同编号" /><el-table-column
              prop="type"
              label="类型" /><el-table-column label="开始日期"
              ><template #default="{ row }">{{ fmtDate(row.startDate) }}</template></el-table-column
            ><el-table-column label="结束日期"
              ><template #default="{ row }">{{ fmtDate(row.endDate) }}</template></el-table-column
            ><el-table-column label="状态"
              ><template #default="{ row }"
                ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
        ><el-tab-pane label="异动记录"
          ><el-table :data="changes"
            ><el-table-column
              prop="changeType"
              label="异动类型" /><el-table-column label="生效日期"
              ><template #default="{ row }">{{ fmtDate(row.effectiveDate) }}</template></el-table-column
            ><el-table-column
              prop="reason"
              label="原因" /><el-table-column label="详情"
              ><template #default="{ row }"
                ><JsonDrawer
                  title="异动详情"
                  :value="row" /></template></el-table-column></el-table></el-tab-pane
        ><el-tab-pane label="薪资方案"
          ><el-empty description="薪资方案请在工资管理中查看" /></el-tab-pane></el-tabs
    ></el-card>
  </PageShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { hrApi } from '../../api/hr'
import JsonDrawer from '../../components/JsonDrawer.vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtDate } from '../../utils/format'
const route = useRoute()
const employee = ref({})
const contracts = ref([])
const changes = ref([])
onMounted(async () => {
  employee.value = await hrApi.employee(route.params.id)
  contracts.value = await hrApi.employeeContracts(route.params.id)
  changes.value = (await hrApi.changes({ employeeId: route.params.id })).records || []
})
</script>
