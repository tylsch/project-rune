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
  <div class="min-h-full p-2">
    <Menubar :model="items"></Menubar>
    <main class="bg-cyan-400 shadow rounded-md mt-1.5">
      <div class="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8 bg-red-500 rounded-md">
        <h1 class="text-3xl font-bold tracking-tight text-white">Content</h1>
      </div>
    </main>
    <footer class="bg-gray-900 shadow rounded-md mt-1.5">
      <div class="max-w-7xl px-4 py-6 sm:px-6 lg:px-8 bg-red-500 rounded-md">
        <h1 class="text-3xl font-bold tracking-tight text-white">Footer</h1>
      </div>
    </footer>
  </div>
</template>

<style scoped></style>
