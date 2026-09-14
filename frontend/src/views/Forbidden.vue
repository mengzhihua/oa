<template>
  <el-result
    icon="warning"
    title="无权访问"
    sub-title="您没有访问此页面的权限"
  >
    <template #extra
      ><el-button
        type="primary"
        @click="$router.push('/dashboard')"
        >返回工作台</el-button
      ></template
    >
  </el-result>
</template>
