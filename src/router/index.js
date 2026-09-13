import Vue from 'vue';
import VueRouter from 'vue-router';
import HomeView from '../views/HomeView.vue';
import ChatView from '../views/ChatView.vue';
import StudioView from '../views/StudioView.vue';

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView,
    meta: { title: 'Astronaut Neon Bot Widget' }
  },
  {
    path: '/chat/:sessionId?',
    name: 'Chat',
    component: ChatView,
    meta: { title: 'Assistant Chat - AI Studio' }
  },
  {
    path: '/studio',
    name: 'Studio',
    component: StudioView,
    meta: { title: 'Studio Quản Trị Tri Thức' }
  },
  {
    path: '*',
    redirect: '/'
  }
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL || '/',
  routes
});

router.afterEach((to) => {
  if (to.meta && to.meta.title) {
    document.title = to.meta.title;
  }
});

export default router;
