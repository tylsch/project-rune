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
    label: 'My Vehicle(s)',
    icon: 'pi pi-fw pi-car',
    items: [
      {
        label: '2021 Chevy Silverado 1500 LT Trail Boss',
        to: '/about'
      },
      {
        separator: true
      },
      {
        label: '2021 F-150 King Ranch',
        to: '/about'
      }
    ]
  },
  {
    label: 'Find My Vehicle',
    icon: 'pi pi-fw pi-search',
    to: '/about'
  },
  {
    label: 'Ask a Technician',
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
  <div class="card">
    <!-- TODO: Add item template to customize menu items    -->
    <Menubar :model="items">
      <template #start>
        <Image class="mr-2" src="src/assets/logo.svg" alt="Image" height="40" />
      </template>
      <template #end>
        <!-- TODO: Replace with something else       -->
        <InputText placeholder="Search" type="text" />
      </template>
    </Menubar>
    <div class="flex flex-column flex-auto pt-1">
      <div class="surface-section px-1 py-1 lg:py-2 lg:px-3 h-full border-3 border-round border-yellow-200">
        <RouterView />
      </div>
    </div>
  </div>
</template>
