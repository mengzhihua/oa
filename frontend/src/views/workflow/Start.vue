<template>
  <PageShell
    title="发起申请"
    code="WF-START"
    description="发起通用申请并提交审批"
  >
    <el-card class="form-card"
      ><el-form
        :model="form"
        label-width="90px"
        ><el-form-item label="流程"
          ><el-select v-model="form.definitionCode"
            ><el-option
              v-for="item in definitions"
              :key="item.code"
              :label="item.name || item.code"
              :value="item.code" /></el-select></el-form-item
        ><el-form-item
          label="标题"
          required
          ><el-input v-model="form.title" /></el-form-item
        ><el-form-item
          label="事由"
          required
          ><el-input
            v-model="form.form.reason"
            type="textarea" /></el-form-item
        ><el-form-item label="附加字段"
          ><el-input
            v-model="extra"
            type="textarea"
            placeholder='JSON，例如 {"days": 1}' /></el-form-item
        ><el-button
          type="primary"
          @click="submit"
          >提交申请</el-button
        ></el-form
      ></el-card
    >
  </PageShell>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { workflowApi } from '../../api/workflow'
import PageShell from '../../components/PageShell.vue'
const definitions = ref([])
const extra = ref('{}')
const form = reactive({ definitionCode: 'GENERAL', businessType: 'GENERAL', title: '', form: { reason: '' } })
onMounted(async () => {
  definitions.value = await workflowApi.definitions()
  if (definitions.value[0]) form.definitionCode = definitions.value[0].code
})
async function submit() {
  Object.assign(form.form, JSON.parse(extra.value || '{}'))
  await workflowApi.start(form)
  ElMessage.success('申请已提交')
  form.title = ''
  form.form.reason = ''
}
</script>
