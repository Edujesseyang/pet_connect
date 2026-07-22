<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();

const form = reactive({
  username: "",
  fullname: "",
  email: "",
  passwordHash: "",
  city: "",
  state: "",
});

const error = ref("");
const loading = ref(false);

async function handleSubmit() {
  error.value = "";
  loading.value = true;
  try {
    const res = await auth.signup({ ...form });
    if (res.state) {
      router.push({ name: "login" });
    } else {
      error.value = res.message || "Signup failed. Try a different username or email.";
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
      <h1>Create your account</h1>
      <p class="auth__subtitle">Join PetConnect to list pets or start adopting.</p>

      <div class="pc-field">
        <label for="fullname">Full name</label>
        <input id="fullname" v-model="form.fullname" type="text" required />
      </div>

      <div class="pc-field">
        <label for="username">Username</label>
        <input id="username" v-model="form.username" type="text" required autocomplete="username" />
      </div>

      <div class="pc-field">
        <label for="email">Email</label>
        <input id="email" v-model="form.email" type="email" required autocomplete="email" />
      </div>

      <div class="pc-field">
        <label for="password">Password</label>
        <input
          id="password"
          v-model="form.passwordHash"
          type="password"
          required
          autocomplete="new-password"
        />
      </div>

      <div class="auth__row">
        <div class="pc-field">
          <label for="city">City</label>
          <input id="city" v-model="form.city" type="text" />
        </div>
        <div class="pc-field">
          <label for="state">State</label>
          <input id="state" v-model="form.state" type="text" />
        </div>
      </div>

      <p v-if="error" class="pc-error">{{ error }}</p>

      <button class="pc-btn pc-btn--primary auth__submit" type="submit" :disabled="loading">
        {{ loading ? "Creating account…" : "Sign up" }}
      </button>

      <p class="auth__footer">
        Already have an account? <RouterLink to="/login">Log in</RouterLink>
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
  max-width: 440px;
  padding: 32px;
}

.auth__subtitle {
  color: var(--pc-muted);
  margin: 0 0 24px;
}

.auth__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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
