<script setup>
const props = defineProps({
  popup: {
    type: Boolean,
    default: false
  },
  model: {
    type: Array,
    default: null
  },
  appendTo: {
    type: String,
    default: 'body'
  },
  autoZIndex: {
    type: Boolean,
    default: true
  },
  baseZIndex: {
    type: Number,
    default: 0
  },
  exact: {
    type: Boolean,
    default: true
  },
  tabindex: {
    type: Number,
    default: 0
  },
  'aria-label': {
    type: String,
    default: null
  },
  'aria-labelledby': {
    type: String,
    default: null
  }
})
</script>

<template>
  <div>
    <Menu :popup="props.popup"
          :model="props.model"
          :append-to='props.appendTo'
          :auto-z-index='props.autoZIndex'
          :base-z-index='baseZIndex'
          :exact='props.exact'
          :tabindex='props.tabindex'
          :aria-label='props["aria-label"]'
          :aria-labelledby='props["aria-labelledby"]'
          data-test="menu"
    />
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
  @include styleclass('p-ripple flex align-items-center lg:p-3')
}
</style>