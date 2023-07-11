<script setup>
import { ref } from 'vue'

const layout = ref('grid')
const categories = ref([
  {
    id: 1,
    name: 'Air & Fuel Delivery',
    stub: 'air-fuel-delivery'
  },
  {
    id: 2,
    name: 'Automatic Transmission',
    stub: 'automatic-transmission'
  },
  {
    id: 3,
    name: 'Belts & Cooling',
    stub: 'belts-cooling'
  },
  {
    id: 4,
    name: 'Body',
    stub: 'body'
  }
])
</script>

<template>
  <div class="surface-section text-center">
    <!-- TODO: Only display Welcome message for "root" category of asset -->
    <div class="mb-3 font-bold text-3xl">
      <span class="text-900">Welcome to Part Explorer!</span>
    </div>
    <div class="text-700 mb-6">Start your exploration by selecting a category</div>
    <!-- TODO: create prop for categories -->
    <DataView :value="categories" :layout="layout">
      <template #grid="slotProps">
        <div class="col-12 md:col-3 mb-4 px-5">
          <!-- TODO: Emit event to parent component of selection -->
          <Button class="p-3 shadow-2 mb-3 inline-block surface-card" style="width: unset; border-radius: 10px" text raised aria-label="Filter">
            <i class="pi pi-truck text-4xl text-blue-500"></i>
          </Button>
          <div class="text-900 text-xl mb-3 font-medium">{{ slotProps.data.name }}</div>
          <!--        <span class="text-700 line-height-3">Risus nec feugiat in fermentum posuere urna nec. Posuere sollicitudin aliquam ultrices sagittis.</span>-->
        </div>
      </template>
    </DataView>
  </div>
</template>

<style scoped>

</style>