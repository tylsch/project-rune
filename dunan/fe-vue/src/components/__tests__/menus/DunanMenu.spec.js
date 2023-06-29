import { describe, it, beforeEach, expect } from 'vitest'
import { mount } from '@vue/test-utils';
import PrimeVue from 'primevue/config';
import DunanMenu from '@/components/menus/DunanMenu.vue'
import Menu from 'primevue/menu'

describe('DunanMenu.vue', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(DunanMenu, {
      global: {
        components: {
          Menu
        },
        plugins: [PrimeVue],
        stubs: {
          'router-link': true,
          teleport: true,
        }
      },
      props: {
        model: [
          {
            label: 'Options',
            items: [
              {
                label: 'Update',
                icon: 'pi pi-refresh',
                command: () => {
                  this.$toast.add({ severity: 'success', summary: 'Updated', detail: 'Data Updated', life: 3000 });
                }
              },
              {
                label: 'Delete',
                icon: 'pi pi-times',
                command: () => {
                  this.$toast.add({ severity: 'warn', summary: 'Delete', detail: 'Data Deleted', life: 3000 });
                }
              }
            ]
          },
          {
            label: 'Navigate',
            items: [
              {
                label: 'Vue Website',
                icon: 'pi pi-external-link',
                url: 'https://vuejs.org/'
              },
              {
                label: 'Router',
                icon: 'pi pi-upload',
                to: '/fileupload'
              }
            ]
          }
        ]
      }
    });
  });

  it('should exist', () => {
    expect(wrapper.find('.p-menu.p-component').exists()).toBe(true);
    expect(wrapper.findAll('.p-submenu-header').length).toBe(2);
    expect(wrapper.findAll('.p-submenu-header')[0].text()).toBe('Options');
    expect(wrapper.findAll('.p-menuitem').length).toBe(4);
    expect(wrapper.findAll('.p-menuitem')[0].find('span.p-menuitem-text').text()).toBe('Update');
    expect(wrapper.findAll('.p-menuitem')[2].find('a').attributes().href).toBe('https://vuejs.org/');
  });
});