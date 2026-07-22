import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { User } from "../types";
import * as authApi from "../api/auth";

export const useAuthStore = defineStore("auth", () => {
  const user = ref<User | null>(
    JSON.parse(localStorage.getItem("pc_user") || "null")
  );
  const isLoggedIn = computed(() => !!user.value);

  async function login(username: string, password: string) {
    const res = await authApi.login(username, password);
    if (res.state && res.data) {
      user.value = res.data;
      localStorage.setItem("pc_user", JSON.stringify(res.data));
      // Once the backend issues a JWT this is where we'd store it:
      // localStorage.setItem("pc_token", res.token)
    }
    return res;
  }

  async function signup(payload: authApi.SignupPayload) {
    const res = await authApi.signup(payload);
    return res;
  }

  function logout() {
    user.value = null;
    localStorage.removeItem("pc_user");
    localStorage.removeItem("pc_token");
  }

  return { user, isLoggedIn, login, signup, logout };
});
