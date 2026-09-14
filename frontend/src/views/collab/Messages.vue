<template>
  <PageShell
    title="消息中心"
    code="CO-MESSAGE"
    ><el-card
      ><el-tabs v-model="tab"
        ><el-tab-pane
          label="未读"
          name="unread"
          ><MessageList
            :rows="unreadRows"
            @read="read" /></el-tab-pane
        ><el-tab-pane
          label="全部"
          name="all"
          ><MessageList
            :rows="rows"
            @read="read" /></el-tab-pane></el-tabs></el-card
  ></PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { collabApi } from '../../api/collab'
import PageShell from '../../components/PageShell.vue'
import MessageList from '../../components/MessageList.vue'
const router = useRouter()
const rows = ref([])
const tab = ref('unread')
const unreadRows = computed(() => rows.value.filter((row) => !row.readAt))
async function load() {
  rows.value = await collabApi.messages()
}
async function read(row) {
  await collabApi.readMessage(row.id)
  if (row.link) router.push(row.link)
  await load()
}
onMounted(load)
</script>
