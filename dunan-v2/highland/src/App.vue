<script setup lang="ts">
import { RouterView } from 'vue-router'
import Menubar from 'primevue/menubar';
import InputText from 'primevue/inputtext';

import { ref } from "vue";
const items = ref([
  {
    label: 'Home',
    icon: 'pi pi-fw pi-home',
    to: '/'
  },
  {
    label: 'My Garage',
    icon: 'pi pi-fw pi-car',
    to: '/garage'
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
  <div class="flex flex-col gap-2 p-2">
    <div class="basis-1/3 rounded-md shadow-lg">
      <!-- TODO: Style using Passthrough  -->
      <Menubar :model="items">
        <template #end>
          <!-- TODO: Replace with something else       -->
          <span class="p-input-icon-left">
            <i class="pi pi-search" />
            <InputText placeholder="Search" />
          </span>
        </template>
      </Menubar>
    </div>

    <div class="basis-1/3 rounded-md shadow-lg border-solid border-2 border-red-600">
      <!-- <h1>Router View Placeholder</h1> -->
      <RouterView />
    </div>
    <!-- TODO: Refactor to a Footer Component -->
    <div class="basis-1/3 rounded-md shadow-lg border-solid border-2 border-red-600">
      <h1>Footer Placeholder</h1>
    </div>
  </div>
</template>

<style scoped></style>
