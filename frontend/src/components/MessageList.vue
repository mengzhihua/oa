<template>
  <el-empty
    v-if="!rows.length"
    description="暂无消息"
  />
  <div
    v-for="row in rows"
    :key="row.id"
    class="message-row"
    @click="$emit('read', row)"
  >
    <div>
      <el-tag size="small">{{ row.type }}</el-tag
      ><strong>{{ row.title }}</strong>
    </div>
    <p>{{ row.content }}</p>
    <small>{{ row.createdAt || row.sentAt }}</small>
  </div>
</template>
<script setup>
defineProps({ rows: Array })
defineEmits(['read'])
</script>
