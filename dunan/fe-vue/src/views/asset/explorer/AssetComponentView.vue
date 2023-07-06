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
  <div class="card flex">
    <Breadcrumb :home="home" :model="items" />
  </div>
  <Category />
</template>

<style lang='scss' scoped>
@import 'primeflex/primeflex.scss';
:deep(.p-button) {
  width: unset;
}
:deep(.p-breadcrumb) {
  border: unset;
  border-radius: unset;
}
</style>