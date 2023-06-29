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
  <div class="text-900 font-semibold text-lg mt-3">Specs</div>
  <Divider />
  <!-- TODO: Specification will be a DataTable with columns: Category, Name, Content -->
  <div class="flex gap-5 flex-column-reverse md:flex-row">
    Page content: {{ route.params.id }}
  </div>
</template>

<style scoped>

</style>