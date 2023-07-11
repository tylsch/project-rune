<script setup>
import { ref } from 'vue';
import { useRoute, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router';
import Category from '@/components/assets/Category.vue'

const route = useRoute()
const componentData = ref(route.params.components)

onBeforeRouteLeave((to, from) => {
  //TBD
})

onBeforeRouteUpdate((to, from) => {
  if (to.params.components !== from.params.components) {
    // Function will be responsible for updating breadcrumb ref on route changes
  }
})

const home = ref({
  icon: 'pi pi-home',
  to: '/',
});
const items = ref([
  {label: 'Computer'},
  {label: 'Notebook'},
  {label: 'Accessories'},
  {label: 'Backpacks'},
  {label: 'Item'}
]);

</script>

<template>
  <!-- TODO: Breadcrumb will only be displayed when on the root-level of the asset tree -->
  <div class="card flex">
    <Breadcrumb
      :home="home"
      :model="items"
      :pt="{
        root: { style: 'border: unset; border-radius: unset;' }
      }"
    />
  </div>
  <!-- TODO: Selection from Category, Assembly, or Product component will drive what next component is displayed -->
  <Category />
</template>

<style scoped>

</style>