import './assets/main.css'
// TODO: Replace with Tailwind Theme once available
import 'primevue/resources/themes/lara-light-blue/theme.css'
import 'primevue/resources/primevue.css'
import 'primeicons/primeicons.css'


// import BadgeDirective from "primevue/badgedirective";
// // import ConfirmationService from 'primevue/confirmationservice';
// // import DialogService from 'primevue/dialogservice'
// import FocusTrap from 'primevue/focustrap';
// import Ripple from 'primevue/ripple';
// // import StyleClass from 'primevue/styleclass';
// // import ToastService from 'primevue/toastservice';
// import Tooltip from 'primevue/tooltip';


import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config'
// import Tailwind from 'primevue/tailwind'

const app = createApp(App)

// app.use(PrimeVue, { unstyled: true, pt: Tailwind });
app.use(PrimeVue, { ripple: true });
// app.use(ConfirmationService);
// app.use(ToastService);
// app.use(DialogService);
app.use(createPinia())
app.use(router)

// app.directive('tooltip', Tooltip);
// app.directive('badge', BadgeDirective);
// app.directive('ripple', Ripple);
// //app.directive('styleclass', StyleClass);
// app.directive('focustrap', FocusTrap);


app.mount('#app')
