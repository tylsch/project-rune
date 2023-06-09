import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SearchView from '@/views/convoy/SearchView.vue'
import AssetView from '@/views/asset/AssetView.vue'
import SpecificationView from '@/views/asset/SpecificationView.vue'
import GalleryView from '@/views/asset/GalleryView.vue'
import PartExplorerView from '@/views/asset/PartExplorerView.vue'
import PartSearchView from '@/views/asset/PartSearchView.vue'
import AssetComponentView from '@/views/asset/explorer/AssetComponentView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/convoy',
      name: 'convoy',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/ConvoyView.vue'),
      children: [
        {
          path: '',
          name: 'convoy-search',
          component: SearchView
        },
        {
          path: ':id',
          name: 'asset',
          component: AssetView,
          children: [
            {
              path: '',
              name: 'asset-spec',
              component: SpecificationView
            },
            {
              path: 'gallery',
              name: 'asset-gallery',
              component: GalleryView
            },
            {
              path: 'explorer',
              name: 'asset-part-explorer',
              component: PartExplorerView,
              children: [
                {
                  path: '',
                  name: 'asset-part-explorer-root-component',
                  component: AssetComponentView
                },
                {
                  path: ':components+',
                  name: 'asset-part-explorer-component-tree',
                  component: AssetComponentView
                }
              ]
            },
            {
              path: 'search',
              name: 'asset-part-search',
              component: PartSearchView
            }
          ]
        }
      ]
    }
  ]
})

export default router
