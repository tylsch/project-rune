<script setup>

</script>

<template>
  <!-- TODO: Refactor to an Convoy Asset Header Component -->
  <div class="flex flex-column md:flex-row w-full justify-content-between md:align-items-center">
    <div>
      <h2 class="mt-0 mb-2 text-900 font-medium text-2xl">Asset Name Goes Here</h2>
      <p class="mt-0 mb-0 text-500">
        Brief description of asset, maybe use chips for metadata
      </p>
    </div>
  </div>
  <Divider />
  <!-- TODO: Refactor to an Convoy Asset Menu Component -->
  <div class="p-fluid flex flex-column lg:flex-row pb-2">
    <ul class="list-none m-0 p-0 flex flex-row lg:flex-column justify-content-between lg:justify-content-start mb-5 lg:mb-0">
      <li>
        <a class="p-ripple lg:w-15rem flex align-items-center cursor-pointer p-3 border-round hover:surface-200 transition-duration-150 transition-colors">
          <i class="pi pi-info-circle md:mr-2 text-600"></i>
          <span class="font-medium hidden md:block text-700">Specs</span>
          <span class="p-ink" role="presentation" aria-hidden="true" style="height: 240px; width: 240px; top: -74px; left: -47px;"></span>
        </a>
      </li>
      <li>
        <a class="p-ripple lg:w-15rem flex align-items-center cursor-pointer p-3 border-round hover:surface-200 transition-duration-150 transition-colors">
          <i class="pi pi-wrench md:mr-2 text-600"></i>
          <span class="font-medium hidden md:block text-700">Parts</span>
          <span class="p-ink" role="presentation" aria-hidden="true" style="height: 240px; width: 240px; top: -74px; left: -47px;"></span>
        </a>
      </li>
      <li>
        <a class="p-ripple lg:w-15rem flex align-items-center cursor-pointer p-3 border-round hover:surface-200 transition-duration-150 transition-colors">
          <i class="pi pi-file md:mr-2 text-600"></i>
          <span class="font-medium hidden md:block text-700">Documents</span>
          <span class="p-ink" role="presentation" aria-hidden="true" style="height: 240px; width: 240px; top: -74px; left: -47px;"></span>
        </a>
      </li>
    </ul>
    <!-- TODO: Create router view to navigate menu items & new components -->
    <div class="p-5 surface-0 shadow-4 flex-auto xl:ml-5">
      <div class="text-900 font-semibold text-lg mt-3">Specs</div>
      <Divider />
      <div class="flex gap-5 flex-column-reverse md:flex-row">
        Page content
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>