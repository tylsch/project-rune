<script setup>
import { computed, ref } from 'vue'
import { useRoute, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router';
import Category from '@/components/assets/Category.vue'
import { useFetch } from '@vueuse/core'

const route = useRoute()
const componentRouteParams = ref(route.params.components)

const isRootComponent = computed(() => {
  if (typeof componentRouteParams.value !== 'undefined' ) {
    return componentRouteParams.value.length < 0
  }
  else {
    return true
  }
})

const getComponentsUrl = computed(() => {
  if (typeof componentRouteParams.value !== 'undefined' ) {
    return '/src/data/asset-data.json'
  }
  else {
    return '/src/data/asset-data.json'
  }
})

// TODO: Implement loading component during isFetching
// TODO: Implement error component for error
const { isFetching, error, data } = useFetch(getComponentsUrl, { refetch: true }).get().json()
onBeforeRouteLeave((to, from) => {
  //TBD
})

onBeforeRouteUpdate((to, from) => {
  if (to.params.components !== from.params.components) {
    componentRouteParams.value = to.params.components
  }
})

// TODO: Work on breadcrumb navigation
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
  <div v-if="!isRootComponent" class="card flex">
    <Breadcrumb
      :home="home"
      :model="items"
      :pt="{
        root: { style: 'border: unset; border-radius: unset;' }
      }"
    />
  </div>
  <!-- TODO: Selection from Category, Assembly, or Product component will drive what next component is displayed -->
  <Category :is-root-category="isRootComponent" :categories="data" />
</template>

<style scoped>

</style>