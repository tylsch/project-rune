<script setup lang="ts">
import { RouterView } from 'vue-router'
import Menubar from 'primevue/menubar';
import Button from 'primevue/button';
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
    <Menubar :model="items">
      <template #start>
        <img class="h-8 w-8 mx-2" src="https://tailwindui.com/img/logos/mark.svg?color=indigo&shade=500" alt="Your Company">
      </template>
      <template #end>
        <Button icon="pi pi-bell" text rounded aria-label="Notification" />
      </template>
    </Menubar>
    <main class="bg-white shadow rounded-md mt-1.5">
      <div class="mx-auto px-4 py-6 sm:px-6 lg:px-8">
        <RouterView />
      </div>
    </main>
    <footer class="bg-white shadow rounded-md mt-1.5">
      <div class="mx-auto px-4 py-6 sm:px-6 lg:px-8">
        <h1 class="text-3xl font-bold tracking-tight">Footer</h1>
      </div>
    </footer>
  </div>
</template>

<style scoped></style>
