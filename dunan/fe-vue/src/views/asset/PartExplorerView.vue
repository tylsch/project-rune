<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router'
const route = useRoute()
const assetTag = ref()

watch(
  () => route.params.id,
  newId => {
    assetTag.value = newId
  }
)
</script>

<template>

  <div class="surface-section text-center">
    <div class="mb-3 font-bold text-3xl">
      <span class="text-900">Welcome to Part Explorer!</span>
    </div>
    <div class="text-700 mb-6">Start your exploration by selecting a category</div>
    <div class="grid">
      <div class="col-12 md:col-4">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-desktop text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">Built for Developers</div>

      </div>
      <div class="col-12 md:col-4 mb-4 px-5">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-lock text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">End-to-End Encryption</div>
        <span class="text-700 line-height-3">Risus nec feugiat in fermentum posuere urna nec. Posuere sollicitudin aliquam ultrices sagittis.</span>
      </div>
      <div class="col-12 md:col-4 mb-4 px-5">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-check-circle text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">Easy to Use</div>
        <span class="text-700 line-height-3">Ornare suspendisse sed nisi lacus sed viverra tellus. Neque volutpat ac tincidunt vitae semper.</span>
      </div>
      <div class="col-12 md:col-4 mb-4 px-5">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-globe text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">Fast & Global Support</div>
        <span class="text-700 line-height-3">Fermentum et sollicitudin ac orci phasellus egestas tellus rutrum tellus.</span>
      </div>
      <div class="col-12 md:col-4 mb-4 px-5">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-github text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">Open Source</div>
        <span class="text-700 line-height-3">Nec tincidunt praesent semper feugiat. Sed adipiscing diam donec adipiscing tristique risus nec feugiat. </span>
      </div>
      <div class="col-12 md:col-4 md:mb-4 mb-0 px-3">
            <span class="p-3 shadow-2 mb-3 inline-block surface-card" style="border-radius: 10px">
                <i class="pi pi-shield text-4xl text-blue-500"></i>
            </span>
        <div class="text-900 text-xl mb-3 font-medium">Trusted Security</div>
        <span class="text-700 line-height-3">Mattis rhoncus urna neque viverra justo nec ultrices. Id cursus metus aliquam eleifend.</span>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>