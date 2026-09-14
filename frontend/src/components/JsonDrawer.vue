<template>
  <el-drawer
    v-model="open"
    :title="title"
    size="55%"
  >
    <pre class="json-view">{{ formatted }}</pre>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ modelValue: Boolean, title: String, value: [Object, String] })
const emit = defineEmits(['update:modelValue'])
const open = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})
const formatted = computed(() => {
  if (typeof props.value !== 'string') return JSON.stringify(props.value || {}, null, 2)
  try {
    return JSON.stringify(JSON.parse(props.value), null, 2)
  } catch {
    return props.value || ''
  }
})
</script>
