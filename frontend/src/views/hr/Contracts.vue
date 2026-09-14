<template>
  <PageShell
    title="合同管理"
    code="HR-CONTRACT"
    description="管理劳动合同并关注近期到期记录"
  >
    <template #actions
      ><el-checkbox v-model="expiringOnly">即将到期</el-checkbox
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增合同</el-button
      ></template
    >
    <el-card
      ><el-table
        :data="filtered"
        stripe
        ><el-table-column
          prop="contractNo"
          label="合同编号"
        /><el-table-column
          prop="employeeId"
          label="员工 ID"
          width="100"
        /><el-table-column
          prop="type"
          label="类型"
        /><el-table-column label="开始日期"
          ><template #default="{ row }">{{ fmtDate(row.startDate) }}</template></el-table-column
        ><el-table-column label="结束日期"
          ><template #default="{ row }"
            ><el-tag :type="isExpiring(row) ? 'danger' : 'info'">{{ fmtDate(row.endDate) }}</el-tag></template
          ></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              @click="open(row)"
              >续签/编辑</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    >
    <el-dialog
      v-model="visible"
      :title="form.id ? '续签合同' : '新增合同'"
      width="460px"
      ><el-form
        :model="form"
        label-width="90px"
        ><el-form-item label="员工 ID"><el-input v-model="form.employeeId" /></el-form-item
        ><el-form-item label="合同编号"><el-input v-model="form.contractNo" /></el-form-item
        ><el-form-item label="类型"><el-input v-model="form.type" /></el-form-item
        ><el-form-item label="开始日期"
          ><el-date-picker
            v-model="form.startDate"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-form-item label="结束日期"
          ><el-date-picker
            v-model="form.endDate"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-form-item label="签订日期"
          ><el-date-picker
            v-model="form.signDate"
            value-format="YYYY-MM-DD" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    >
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { hrApi } from '../../api/hr'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
import { fmtDate } from '../../utils/format'
const rows = ref([])
const visible = ref(false)
const expiringOnly = ref(false)
const form = reactive({})
const filtered = computed(() => (expiringOnly.value ? rows.value.filter(isExpiring) : rows.value))
function isExpiring(row) {
  return row.endDate && (new Date(row.endDate) - new Date()) / 86400000 <= 30
}
async function load() {
  rows.value = await hrApi.contracts()
}
function open(row = {}) {
  Object.assign(form, {
    id: row.id,
    employeeId: row.employeeId,
    contractNo: row.contractNo,
    type: row.type || '劳动合同',
    startDate: row.startDate,
    endDate: row.endDate,
    signDate: row.signDate,
  })
  visible.value = true
}
async function save() {
  form.id ? await hrApi.updateContract(form.id, form) : await hrApi.createContract(form)
  visible.value = false
  ElMessage.success('合同保存成功')
  await load()
}
onMounted(load)
</script>
