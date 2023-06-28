<script setup>
import { ref } from 'vue';
// TODO: Add route navigation and active selection when user navigates to page from URL
const items = ref([
  { label: 'Specs', icon: 'pi pi-fw pi-info-circle' },
  { label: 'Gallery', icon: 'pi pi-fw pi-images' },
  { label: 'Part Explorer', icon: 'pi pi-fw pi-wrench' },
  { label: 'Part Search', icon: 'pi pi-fw pi-search' }
]);

</script>

<template>
  <div class="flex flex-column md:flex-row w-full justify-content-between md:align-items-center">
    <div>
      <h2 class="mt-0 mb-2 text-900 font-medium text-2xl">Asset Name Goes Here</h2>
      <p class="mt-0 mb-0 text-500">
        Brief description of asset, maybe use chips for metadata
      </p>
    </div>
  </div>
  <Divider />
  <div class="p-fluid flex flex-column lg:flex-row pb-2">
    <Menu :model="items" />
    <div class="p-5 surface-0 shadow-4 flex-auto xl:ml-5">
      <RouterView />
    </div>
  </div>
</template>

<style lang='scss' scoped>
@import 'primeflex/primeflex.scss';
:deep(.p-menu) {
  width: unset;
  border: unset;
  border-radius: unset;
}
:deep(.p-menu-list) {
  @include styleclass('list-none m-0 p-0 flex flex-row lg:flex-column justify-content-between lg:justify-content-start mb-5 lg:mb-0')
}
:deep(.p-menuitem-content) {
  @include styleclass('border-round-lg')
}
:deep(.p-menuitem-link) {
  @include styleclass('p-ripple lg:w-15rem flex align-items-center p-3')
}
</style>