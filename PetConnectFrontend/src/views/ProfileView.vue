<script setup lang="ts">
import { reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { changePassword } from "../api/auth";

const auth = useAuthStore();

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
});

const message = ref("");
const error = ref("");
const loading = ref(false);

async function handleChangePassword() {
  if (!auth.user) return;
  message.value = "";
  error.value = "";
  loading.value = true;
  try {
    const res = await changePassword(auth.user.username, passwordForm.oldPassword, passwordForm.newPassword);
    if (res.state) {
      message.value = "Password updated.";
      passwordForm.oldPassword = "";
      passwordForm.newPassword = "";
    } else {
      error.value = res.message || "Couldn't update password.";
    }
  } catch {
    error.value = "Couldn't reach the server. Is the backend running?";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="pc-container profile">
    <h1>Your profile</h1>

    <div class="profile__grid">
      <section class="profile__card pc-card">
        <h2>Account</h2>
        <dl>
          <dt>Username</dt>
          <dd>{{ auth.user?.username }}</dd>
          <dt>Full name</dt>
          <dd>{{ auth.user?.fullname }}</dd>
          <dt>Email</dt>
          <dd>{{ auth.user?.email }}</dd>
        </dl>
      </section>

      <section class="profile__card pc-card">
        <h2>Change password</h2>
        <form @submit.prevent="handleChangePassword">
          <div class="pc-field">
            <label for="old-password">Current password</label>
            <input id="old-password" v-model="passwordForm.oldPassword" type="password" required />
          </div>
          <div class="pc-field">
            <label for="new-password">New password</label>
            <input id="new-password" v-model="passwordForm.newPassword" type="password" required />
          </div>
          <p v-if="error" class="pc-error">{{ error }}</p>
          <p v-if="message" class="profile__success">{{ message }}</p>
          <button class="pc-btn pc-btn--primary" type="submit" :disabled="loading">
            {{ loading ? "Updating…" : "Update password" }}
          </button>
        </form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.profile {
  padding: 40px 24px 60px;
}

.profile__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 24px;
}

@media (max-width: 720px) {
  .profile__grid {
    grid-template-columns: 1fr;
  }
}

.profile__card {
  padding: 24px;
}

dl {
  margin: 0;
}

dt {
  font-size: 0.8rem;
  color: var(--pc-muted);
  margin-top: 12px;
}

dt:first-child {
  margin-top: 0;
}

dd {
  margin: 2px 0 0;
  font-weight: 600;
}

.profile__success {
  color: var(--pc-primary-light);
  font-weight: 600;
  font-size: 0.9rem;
}
</style>
