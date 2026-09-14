<template>
  <PageShell
    title="角色管理"
    code="SYS-ROLE"
    ><template #actions
      ><el-button
        type="primary"
        @click="open()"
        >新增角色</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="code"
          label="编码"
        /><el-table-column
          prop="name"
          label="名称"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="open(row)"
              >编辑</el-button
            ><el-button
              link
              @click="assign(row)"
              >分配菜单</el-button
            ><el-button
              link
              type="danger"
              @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="visible"
      :title="form.id ? '编辑角色' : '新增角色'"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="编码"><el-input v-model="form.code" /></el-form-item
        ><el-form-item label="名称"><el-input v-model="form.name" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    ><el-dialog
      v-model="menuVisible"
      title="分配菜单"
      ><el-tree
        ref="menuTree"
        :data="menuTreeData"
        node-key="id"
        show-checkbox
        :props="{ label: 'name' }"
      /><template #footer
        ><el-button
          type="primary"
          @click="saveMenus"
          >保存</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '../../api/system'
import PageShell from '../../components/PageShell.vue'
const rows = ref([])
const menuTreeData = ref([])
const visible = ref(false)
const menuVisible = ref(false)
const form = reactive({})
const selected = ref({})
const menuTree = ref()
async function load() {
  rows.value = await systemApi.roles()
  menuTreeData.value = await systemApi.menus()
}
function open(row = {}) {
  Object.assign(form, row)
  visible.value = true
}
async function save() {
  await systemApi.saveRole(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除角色？', '删除确认')
  await systemApi.deleteRole(row.id)
  await load()
}
function assign(row) {
  selected.value = row
  menuVisible.value = true
}
async function saveMenus() {
  await systemApi.assignMenus(selected.value.id, { menuIds: menuTree.value.getCheckedKeys() })
  menuVisible.value = false
  ElMessage.success('菜单权限已保存')
}
onMounted(load)
</script>
