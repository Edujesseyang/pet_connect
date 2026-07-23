<script setup lang="ts">
import { ref } from "vue";
import { useRouter, useRoute, RouterLink } from "vue-router";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const username = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

async function handleSubmit() {
  error.value = "";
  loading.value = true;
  try {
    const res = await auth.login(username.value, password.value);
    if (res.state) {
      const redirect = (route.query.redirect as string) || "/";
      router.push(redirect);
    } else {
      error.value = res.message || "Login failed. Check your username and password.";
    }
  } catch {
    error.value = "Couldn't reach the server. Is the backend running?";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth pc-container">
    <form class="auth__card pc-card" @submit.prevent="handleSubmit">
      <h1>Welcome back</h1>
      <p class="auth__subtitle">Log in to save pets and message owners.</p>

      <div class="pc-field">
        <label for="username">Username</label>
        <input id="username" v-model="username" type="text" required autocomplete="username" />
      </div>

      <div class="pc-field">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" required autocomplete="current-password" />
      </div>

      <p v-if="error" class="pc-error">{{ error }}</p>

      <button class="pc-btn pc-btn--primary auth__submit" type="submit" :disabled="loading">
        {{ loading ? "Logging in…" : "Log in" }}
      </button>

      <p class="auth__footer">
        Don't have an account? <RouterLink to="/signup">Sign up</RouterLink>
      </p>
    </form>
  </div>
</template>

<style scoped>
.auth {
  display: flex;
  justify-content: center;
  padding: 60px 24px;
}

.auth__card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
}

.auth__subtitle {
  color: var(--pc-muted);
  margin: 0 0 24px;
}

.auth__submit {
  width: 100%;
  margin-top: 8px;
}

.auth__footer {
  text-align: center;
  margin: 20px 0 0;
  font-size: 0.9rem;
  color: var(--pc-muted);
}
</style>
