<script setup lang="ts">
import { RouterLink, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();

function handleLogout() {
  auth.logout();
  router.push({ name: "home" });
}
</script>

<template>
  <header class="navbar">
    <div class="pc-container navbar__inner">
      <RouterLink to="/" class="navbar__brand">
        <span class="navbar__mark">🐾</span>
        <span>PetConnect</span>
      </RouterLink>

      <nav class="navbar__links">
        <RouterLink to="/" class="navbar__link">Browse</RouterLink>
        <RouterLink v-if="auth.isLoggedIn" to="/favorites" class="navbar__link">Favorites</RouterLink>
        <RouterLink v-if="auth.isLoggedIn" to="/pets/new" class="navbar__link">List a pet</RouterLink>
      </nav>

      <div class="navbar__actions">
        <template v-if="auth.isLoggedIn">
          <RouterLink to="/profile" class="navbar__user">{{ auth.user?.username }}</RouterLink>
          <button class="pc-btn pc-btn--ghost" @click="handleLogout">Log out</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="pc-btn pc-btn--ghost">Log in</RouterLink>
          <RouterLink to="/signup" class="pc-btn pc-btn--primary">Sign up</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  background: var(--pc-surface);
  border-bottom: 1px solid var(--pc-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.navbar__inner {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 68px;
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--pc-font-display);
  font-weight: 700;
  font-size: 1.2rem;
  text-decoration: none;
  color: var(--pc-primary);
  margin-right: auto;
}

.navbar__mark {
  font-size: 1.3rem;
}

.navbar__links {
  display: flex;
  gap: 20px;
}

.navbar__link {
  text-decoration: none;
  color: var(--pc-muted);
  font-weight: 500;
  font-size: 0.95rem;
}

.navbar__link.router-link-exact-active {
  color: var(--pc-primary);
}

.navbar__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.navbar__user {
  text-decoration: none;
  font-weight: 600;
  color: var(--pc-text);
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--pc-bg);
}

@media (max-width: 640px) {
  .navbar__links {
    display: none;
  }
}
</style>
