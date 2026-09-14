<template>
  <PageShell
    title="岗位管理"
    code="ORG-POSITION"
    description="维护岗位编码、名称、职级和归属部门"
  >
    <template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增岗位</el-button
      ></template
    >
    <el-card
      ><el-table
        :data="rows"
        stripe
        ><el-table-column
          prop="code"
          label="编码"
          width="150"
        /><el-table-column
          prop="name"
          label="岗位名称"
        /><el-table-column
          prop="level"
          label="职级"
          width="100"
        /><el-table-column
          prop="deptId"
          label="部门"
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
      :title="form.id ? '编辑岗位' : '新增岗位'"
      width="450px"
    >
      <el-form
        :model="form"
        label-width="90px"
        ><el-form-item
          label="岗位编码"
          required
          ><el-input v-model="form.code" /></el-form-item
        ><el-form-item
          label="岗位名称"
          required
          ><el-input v-model="form.name" /></el-form-item
        ><el-form-item label="职级"
          ><el-input-number
            v-model="form.level"
            :min="1" /></el-form-item
        ><el-form-item label="所属部门"><DeptTreeSelect v-model="form.deptId" /></el-form-item
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
import DeptTreeSelect from '../../components/DeptTreeSelect.vue'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const visible = ref(false)
const form = reactive({})
async function load() {
  rows.value = await orgApi.positions()
}
function open(row = {}) {
  Object.assign(form, {
    id: row.id,
    code: row.code,
    name: row.name,
    level: row.level || 1,
    deptId: row.deptId,
  })
  visible.value = true
}
async function save() {
  if (!form.code || !form.name) return ElMessage.warning('请填写岗位编码和名称')
  form.id ? await orgApi.updatePosition(form.id, form) : await orgApi.createPosition(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除岗位“${row.name}”？`, '删除确认', { type: 'warning' })
  await orgApi.deletePosition(row.id)
  ElMessage.success('删除成功')
  await load()
}
onMounted(load)
</script>
