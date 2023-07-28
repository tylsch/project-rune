<script setup lang="ts">
import Divider from 'primevue/divider'
</script>

<template>
    <div class="flex flex-col md:flex-row bg-white">
        <div>
            <h2 class="mt-0 mb-2 text-900 font-medium text-2xl">Resource Name: TBD</h2>
            <p class="mt-0 mb-0 text-500">
                Brief description of asset, maybe use chips for metadata
            </p>
        </div>
        <div>
            <Divider />
        </div>
        <div class="p-fluid flex flex-column lg:flex-row pb-2">
            Hello
            <!-- <DunanMenu :model="items" /> -->
            <div class="p-5 surface-0 shadow-4 flex-auto xl:ml-5">
                <!-- <RouterView /> -->
            </div>
        </div>
    </div>
</template>
