<template>
  <section class="page-shell">
    <div class="page-heading">
      <div>
        <div class="eyebrow">集团 OA / {{ code }}</div>
        <h1>{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </div>
      <div class="heading-actions"><slot name="actions" /></div>
    </div>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: String,
  code: { type: String, default: 'OA' },
  description: String,
})
</script>
