<template>
  <PageShell
    title="OAuth 客户端"
    code="SYS-OAUTH"
    ><template #actions
      ><el-button
        type="primary"
        @click="open()"
        >新增客户端</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="clientId"
          label="客户端 ID"
        /><el-table-column
          prop="clientName"
          label="名称"
        /><el-table-column
          prop="redirectUris"
          label="回调地址"
        /><el-table-column
          prop="grantTypes"
          label="授权类型"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="open(row)"
              >编辑/重置 Secret</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="visible"
      title="OAuth 客户端"
      ><el-form
        :model="form"
        label-width="100px"
        ><el-form-item label="客户端 ID"><el-input v-model="form.clientId" /></el-form-item
        ><el-form-item label="名称"><el-input v-model="form.clientName" /></el-form-item
        ><el-form-item label="回调地址"><el-input v-model="form.redirectUris" /></el-form-item
        ><el-form-item label="授权类型"><el-input v-model="form.grantTypes" /></el-form-item
        ><el-form-item label="Secret"
          ><el-input
            v-model="form.clientSecret"
            placeholder="留空则自动生成" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    ><el-dialog
      v-model="secretVisible"
      title="客户端 Secret（仅显示一次）"
      ><el-input
        v-model="secret"
        readonly
        ><template #append><el-button @click="copy">复制</el-button></template></el-input
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { oauthApi } from '../../api/oauth'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const visible = ref(false)
const secretVisible = ref(false)
const secret = ref('')
const form = reactive({})
async function load() {
  rows.value = await oauthApi.clients()
}
function open(row = {}) {
  Object.assign(form, row, { clientSecret: '' })
  visible.value = true
}
async function save() {
  const data = await oauthApi.saveClient(form)
  visible.value = false
  if (data.clientSecret) {
    secret.value = data.clientSecret
    secretVisible.value = true
  }
  ElMessage.success('客户端已保存')
  await load()
}
async function copy() {
  await navigator.clipboard.writeText(secret.value)
  ElMessage.success('已复制')
}
onMounted(load)
</script>
