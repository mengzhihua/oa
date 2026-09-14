<template>
  <PageShell
    title="年假余额"
    code="ATT-BALANCE"
    description="查看当前年度各类假期余额"
    ><el-card
      ><el-descriptions
        :column="3"
        border
        ><el-descriptions-item label="年度">{{ balance.year }}</el-descriptions-item
        ><el-descriptions-item label="假期类型"><StatusTag :value="balance.leaveType" /></el-descriptions-item
        ><el-descriptions-item label="总天数">{{ balance.totalDays || 0 }} 天</el-descriptions-item
        ><el-descriptions-item label="已用天数">{{ balance.usedDays || 0 }} 天</el-descriptions-item
        ><el-descriptions-item label="剩余天数"
          ><strong
            >{{ ((balance.totalDays || 0) - (balance.usedDays || 0)).toFixed(2) }} 天</strong
          ></el-descriptions-item
        ></el-descriptions
      ></el-card
    ></PageShell
  >
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { attendanceApi } from '../../api/attendance'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const balance = ref({})
onMounted(async () => {
  balance.value = await attendanceApi.balance({ type: 'ANNUAL' })
})
</script>
