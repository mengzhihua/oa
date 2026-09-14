<template>
  <el-tree-select
    v-model="value"
    :data="tree"
    check-strictly
    clearable
    filterable
    node-key="id"
    :props="{ label: 'name', children: 'children' }"
    placeholder="选择部门"
    @change="$emit('update:modelValue', $event)"
  />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { orgApi } from '../api/org'

const props = defineProps({ modelValue: [Number, String], data: Array })
const emit = defineEmits(['update:modelValue'])
const rows = ref([])
const value = computed({
  get: () => props.modelValue,
  set: (next) => emit('update:modelValue', next),
})
const tree = computed(() => {
  const source = props.data || rows.value
  const map = Object.fromEntries(source.map((item) => [item.id, { ...item, children: [] }]))
  const roots = []
  source.forEach((item) => {
    if (item.parentId && map[item.parentId]) map[item.parentId].children.push(map[item.id])
    else roots.push(map[item.id])
  })
  return roots
})
onMounted(async () => {
  if (!props.data) rows.value = await orgApi.depts()
})
</script>
