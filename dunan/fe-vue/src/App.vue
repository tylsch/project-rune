<script setup>
import { RouterLink, RouterView } from 'vue-router'

import { ref } from "vue";
const items = ref([
  {
    label: 'Home',
    icon: 'pi pi-fw pi-home',
    to: '/'
  },
  {
    label: 'Convoy',
    icon: 'pi pi-fw pi-car',
    to: '/convoy'
  },
  {
    label: 'Find Vehicle',
    icon: 'pi pi-fw pi-search',
    to: '/about'
  },
  {
    label: 'Ask Gladstone',
    icon: 'pi pi-fw pi-question',
    to: '/about'
  },
  {
    label: 'About',
    icon: 'pi pi-fw pi-file',
    to: '/about'
  }
]);
</script>

<template>
  <div>
    <Menubar :model="items">
      <template #start>
        <Image class="mr-2" src="/src/assets/logo.svg" alt="Image" height="40" />
      </template>
      <template #end>
        <!-- TODO: Replace with something else       -->
        <InputText placeholder="Search" type="text" />
      </template>
    </Menubar>
    <div class="flex flex-column flex-auto pt-1">
      <div class="surface-overlay px-1 py-1 lg:py-2 lg:px-3 h-full">
        <RouterView />
      </div>
    </div>
    <!-- TODO: Refactor to a Footer Component -->
    <div class="flex flex-column flex-auto pt-1">
      <div class="surface-overlay pt-1 px-1 py-1 lg:py-2 lg:px-3 h-full">
        <div class="flex flex-column sm:flex-row sm:align-items-center justify-content-between">
          <div>
            <Image class="mr-2" src="/src/assets/logo.svg" alt="Image" height="40" />
            <p class="mt-2 mb-0 line-height-3 font-medium text-base text-700">
              Footer Information
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>
