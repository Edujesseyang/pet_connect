import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import LoginView from "../views/LoginView.vue";
import SignupView from "@/views/SignupView.vue";
import DashboardView from "../views/DashboardView.vue";
import AddPetView from "../views/AddPetView.vue";
import PetDetailView from "../views/PetDetailView.vue";

const router = createRouter({
  history: createWebHistory(),

  routes: [
    {
      path: "/",
      name: "home",
      component: HomeView,
    },
    {
      path: "/login",
      name: "login",
      component: LoginView,
    },
    {
      path: "/signup",
      name: "signup",
      component: SignupView,
    },
    {
      path: "/dashboard",
      name: "dashboard",
      component: DashboardView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: "/:pathMatch(.*)*",
      redirect: "/",
    },
    {
      path: "/dashboard/pets/add",
      name: "add-pet",
      component: AddPetView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: "/pets/:petId",
      name: "pet-detail",
      component: PetDetailView,
      props: true,
    },
  ],
});

export default router;
