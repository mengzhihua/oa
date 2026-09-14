<template>
  <PageShell
    title="职级管理"
    code="ORG-GRADE"
    description="维护职级编码、名称和排序等级"
  >
    <template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增职级</el-button
      ></template
    >
    <el-card
      ><el-table
        :data="rows"
        stripe
        ><el-table-column
          prop="code"
          label="编码"
          width="160"
        /><el-table-column
          prop="name"
          label="职级名称"
        /><el-table-column
          prop="level"
          label="等级"
          width="100"
        /><el-table-column
          label="操作"
          width="180"
          ><template #default="{ row }"
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              type="primary"
              @click="open(row)"
              >编辑</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              type="danger"
              @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    >
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑职级' : '新增职级'"
      width="420px"
    >
      <el-form
        :model="form"
        label-width="90px"
        ><el-form-item
          label="职级编码"
          required
          ><el-input v-model="form.code" /></el-form-item
        ><el-form-item
          label="职级名称"
          required
          ><el-input v-model="form.name" /></el-form-item
        ><el-form-item label="等级"
          ><el-input-number
            v-model="form.level"
            :min="1" /></el-form-item
      ></el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      >
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orgApi } from '../../api/org'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const visible = ref(false)
const form = reactive({})
async function load() {
  rows.value = await orgApi.grades()
}
function open(row = {}) {
  Object.assign(form, { id: row.id, code: row.code, name: row.name, level: row.level || 1 })
  visible.value = true
}
async function save() {
  if (!form.code || !form.name) return ElMessage.warning('请填写职级编码和名称')
  form.id ? await orgApi.updateGrade(form.id, form) : await orgApi.createGrade(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除职级“${row.name}”？`, '删除确认', { type: 'warning' })
  await orgApi.deleteGrade(row.id)
  ElMessage.success('删除成功')
  await load()
}
onMounted(load)
</script>
