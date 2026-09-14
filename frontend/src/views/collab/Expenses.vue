<template>
  <PageShell
    title="报销管理"
    code="CO-EXPENSE"
    ><template #actions
      ><el-button
        type="primary"
        @click="visible = true"
        >新建报销</el-button
      ></template
    ><el-tabs v-model="tab"
      ><el-tab-pane
        label="我的报销"
        name="mine"
        ><ExpenseTable :rows="rows" /></el-tab-pane
      ><el-tab-pane
        v-if="canWrite('ADMIN', 'FINANCE')"
        label="财务待付款"
        name="finance"
        ><ExpenseTable
          :rows="rows.filter((row) => row.status === 'APPROVED')"
          @pay="pay" /></el-tab-pane></el-tabs
    ><el-dialog
      v-model="visible"
      title="新建报销"
      width="680px"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item
        ><el-form-item label="明细"
          ><div
            v-for="(item, index) in items"
            :key="index"
            class="expense-line"
          >
            <el-input
              v-model="item.name"
              placeholder="费用名称"
            /><el-input-number
              v-model="item.amount"
              :min="0"
              @change="recalculate"
            /><el-button
              link
              type="danger"
              @click="items.splice(index, 1)"
              >删除</el-button
            >
          </div>
          <el-button @click="items.push({ name: '', amount: 0 })">新增明细</el-button></el-form-item
        ><el-form-item label="合计"
          ><strong>¥ {{ total.toFixed(2) }}</strong></el-form-item
        ></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >提交</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { collabApi } from '../../api/collab'
import { canWrite } from '../../auth'
import ExpenseTable from '../../components/ExpenseTable.vue'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const visible = ref(false)
const tab = ref('mine')
const form = reactive({ title: '' })
const items = ref([{ name: '', amount: 0 }])
const total = computed(() => items.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
async function load() {
  rows.value = await collabApi.expenses({})
}
async function save() {
  await collabApi.createExpense({ ...form, itemsJson: JSON.stringify(items.value), total: total.value })
  visible.value = false
  ElMessage.success('报销已提交')
  await load()
}
async function pay(row) {
  await collabApi.payExpense(row.id)
  ElMessage.success('已标记付款')
  await load()
}
function recalculate() {}
onMounted(load)
</script>
