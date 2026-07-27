<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";

import type {
  ApiResponse,
  LoginRequest,
  User,
} from "../types/api";

const router = useRouter();

const loginForm = reactive<LoginRequest>({
  username: "",
  password: "",
});

const errorMessage = ref("");
const isLoading = ref(false);

async function handleLogin(): Promise<void> {
  errorMessage.value = "";

  if (!loginForm.username.trim()) {
    errorMessage.value = "Username is required.";
    return;
  }

  if (!loginForm.password) {
    errorMessage.value = "Password is required.";
    return;
  }

  try {
    isLoading.value = true;

    const response = await fetch(
      "http://localhost:8080/users/login",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(loginForm),
      },
    );

    const responseData =
      (await response.json()) as ApiResponse<User>;

    if (!response.ok || !responseData.state) {
      throw new Error(
        responseData.message || "Login failed.",
      );
    }

    if (!responseData.data) {
      throw new Error(
        "Login succeeded, but no user data was returned.",
      );
    }

    localStorage.setItem(
      "user",
      JSON.stringify(responseData.data),
    );

    await router.push("/dashboard");
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unexpected login error.";
  } finally {
    isLoading.value = false;
  }
}
</script>

<template>
  <section class="login-page">
    <form
      class="login-card"
      @submit.prevent="handleLogin"
    >
      <h1>Login</h1>

      <p class="subtitle">
        Sign in to your Pet Connect account.
      </p>

      <div class="form-group">
        <label for="username">
          Username
        </label>

        <input
          id="username"
          v-model.trim="loginForm.username"
          type="text"
          placeholder="Enter your username"
          autocomplete="username"
        />
      </div>

      <div class="form-group">
        <label for="password">
          Password
        </label>

        <input
          id="password"
          v-model="loginForm.password"
          type="password"
          placeholder="Enter your password"
          autocomplete="current-password"
        />
      </div>

      <p
        v-if="errorMessage"
        class="error-message"
      >
        {{ errorMessage }}
      </p>

      <button
        type="submit"
        :disabled="isLoading"
      >
        {{
          isLoading
            ? "Logging in..."
            : "Login"
        }}
      </button>

      <RouterLink
        class="signup-link"
        to="/signup"
      >
        Create an account
      </RouterLink>

      <RouterLink
        class="back-link"
        to="/"
      >
        Back to home
      </RouterLink>
    </form>
  </section>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  min-height: calc(100vh - 80px);
  padding: 80px 20px 40px;
  background-color: #f4f6f8;
}

.login-card {
  width: 100%;
  max-width: 420px;
  height: fit-content;
  padding: 32px;
  background-color: white;
  border: 1px solid #dddddd;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

h1 {
  margin: 0 0 8px;
  font-size: 30px;
  text-align: center;
}

.subtitle {
  margin: 0 0 28px;
  color: #666666;
  text-align: center;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

label {
  font-weight: 600;
}

input {
  box-sizing: border-box;
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 1px solid #cccccc;
  border-radius: 6px;
  outline: none;
}

input:focus {
  border-color: #2563eb;
}

button {
  margin: 20px;
  width: 100%;
  padding: 12px;
  color: white;
  font-size: 16px;
  background-color: #517ddd;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button:hover:not(:disabled) {
  background-color: #1d4ed8;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.error-message {
  margin-bottom: 16px;
  color: #dc2626;
}

.signup-link,
.back-link {
  display: block;
  margin-top: 18px;
  color: #2563eb;
  text-align: center;
  text-decoration: none;
}

.signup-link:hover,
.back-link:hover {
  text-decoration: underline;
}
</style>