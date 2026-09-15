import Vue from 'vue';
import App from './App.vue';
import router from './router';

import '@fontsource/noto-sans/400.css';
import '@fontsource/noto-sans/500.css';
import '@fontsource/noto-sans/600.css';
import '@fontsource/noto-sans/700.css';

// Giả lập file CSS Reset toàn cục từ dự án cha của người dùng
import './style-override.css';

Vue.config.productionTip = false;

new Vue({
  router,
  render: (h) => h(App)
}).$mount('#app');

