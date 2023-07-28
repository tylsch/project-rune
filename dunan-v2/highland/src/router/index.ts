import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import GarageView from '@/views/garage/GarageView.vue';
import GarageItemsView from '@/views/garage/GarageItemsView.vue';
import ResourceView from '@/views/resource/ResourceView.vue'; 

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/garage',
      name: 'garage',
      component: GarageView,
      children: [
        {
          path: '',
          name: 'garage-items-view',
          component: GarageItemsView
        },
        {
          path: ':id',
          name: 'resource-view',
          component: ResourceView
        }
      ]
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    }
  ]
})

export default router
