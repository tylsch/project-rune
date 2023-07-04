<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router'
const route = useRoute()
const assetTag = ref()

watch(
  () => route.params.id,
  newId => {
    assetTag.value = newId
  }
)
</script>

<template>
  <!-- TODO: Breadcrumb component will live here to track the progression of the exploration -->
  <!-- TODO: Replace CategoryView/AssemblyView with generic ContainerView that will dynamically render a category view or assembly view -->
  <RouterView />
</template>

<style scoped>

</style>