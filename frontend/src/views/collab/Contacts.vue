<template>
  <PageShell
    title="通讯录"
    code="CO-CONTACT"
    ><div class="contacts-layout">
      <el-card class="dept-filter"
        ><el-tree
          :data="tree"
          node-key="id"
          :props="{ label: 'name' }"
          @node-click="selectDept" /></el-card
      ><el-card
        ><el-form inline
          ><el-input
            v-model="keyword"
            placeholder="姓名/工号"
            clearable
            @keyup.enter="load"
          /><el-button
            type="primary"
            @click="load"
            >搜索</el-button
          ></el-form
        ><el-row :gutter="16"
          ><el-col
            v-for="row in rows"
            :key="row.employeeId"
            :span="8"
            ><el-card class="contact-card"
              ><el-avatar>{{ row.name?.slice(0, 1) }}</el-avatar>
              <h3>{{ row.name }}</h3>
              <p>{{ row.position }} · {{ row.deptName }}</p>
              <p>{{ row.mobile || '暂无手机' }}</p>
              <p>{{ row.email || '暂无邮箱' }}</p></el-card
            ></el-col
          ></el-row
        ></el-card
      >
    </div></PageShell
  >
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { collabApi } from '../../api/collab'
import { orgApi } from '../../api/org'
import PageShell from '../../components/PageShell.vue'
const route = useRoute()
const keyword = ref(route.query.keyword || '')
const deptId = ref()
const rows = ref([])
const tree = ref([])
async function load() {
  rows.value = await collabApi.contacts({ keyword: keyword.value || undefined, deptId: deptId.value })
}
function selectDept(node) {
  deptId.value = node.id
  load()
}
onMounted(async () => {
  tree.value = await orgApi.depts()
  await load()
})
</script>
