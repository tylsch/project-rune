<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router'
const route = useRoute()

const items = ref([
  { label: 'Specs', icon: 'pi pi-fw pi-info-circle', to: `/convoy/${route.params.id}` },
  { label: 'Gallery', icon: 'pi pi-fw pi-images', to: `/convoy/${route.params.id}/gallery` },
  { label: 'Explorer', icon: 'pi pi-fw pi-wrench', to: `/convoy/${route.params.id}/explorer` },
  { label: 'Search', icon: 'pi pi-fw pi-search', to: `/convoy/${route.params.id}/search` }
]);

</script>

<template>
  <div class="flex flex-column md:flex-row w-full justify-content-between md:align-items-center">
    <div>
      <h2 class="mt-0 mb-2 text-900 font-medium text-2xl">{{ route.params.id }}</h2>
      <p class="mt-0 mb-0 text-500">
        Brief description of asset, maybe use chips for metadata
      </p>
    </div>
  </div>
  <Divider />
  <div class="p-fluid flex flex-column lg:flex-row pb-2">
    <DunanMenu :model="items" />
    <div class="p-5 surface-0 shadow-4 flex-auto xl:ml-5">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>

</style>