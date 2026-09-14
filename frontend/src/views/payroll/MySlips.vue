<template>
  <PageShell
    title="我的工资条"
    code="PAY-MINE"
    ><el-card
      v-for="slip in rows"
      :key="slip.id"
      class="slip-card"
      ><div class="slip-header">
        <strong>{{ slip.periodId }} 工资条</strong
        ><span
          >实发：<b>¥ {{ fmtMoney(slip.net) }}</b></span
        >
      </div>
      <el-row
        ><el-col :span="12"
          ><h4>收入项</h4>
          <div
            v-for="item in entries(slip, true)"
            :key="item[0]"
            class="slip-line"
          >
            <span>{{ item[0] }}</span
            ><span>¥ {{ fmtMoney(item[1]) }}</span>
          </div></el-col
        ><el-col :span="12"
          ><h4>扣款项</h4>
          <div
            v-for="item in entries(slip, false)"
            :key="item[0]"
            class="slip-line"
          >
            <span>{{ item[0] }}</span
            ><span>¥ {{ fmtMoney(item[1]) }}</span>
          </div></el-col
        ></el-row
      ></el-card
    ><el-empty
      v-if="!rows.length"
      description="暂无已发放工资条"
  /></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { payrollApi } from '../../api/payroll'
import PageShell from '../../components/PageShell.vue'
import { fmtMoney } from '../../utils/format'
const rows = ref([])
function entries(slip, income) {
  let data = {}
  try {
    data = JSON.parse(slip.itemsJson || '{}')
  } catch {
    data = {}
  }
  return Object.entries(data).filter((item) => (income ? Number(item[1]) >= 0 : Number(item[1]) < 0))
}
onMounted(async () => {
  rows.value = await payrollApi.mine()
})
</script>
