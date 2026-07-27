<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-container">
    <header class="navbar">
      <RouterLink class="logo" to="/">
        Pet Connect
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/">
          Home
        </RouterLink>

        <RouterLink to="/login">
          Login
        </RouterLink>
      </nav>
    </header>

    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: #f4f6f8;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  background-color: white;
  border-bottom: 1px solid #dddddd;
}

.logo {
  color: #222222;
  font-size: 22px;
  font-weight: 700;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-links a {
  color: #444444;
  text-decoration: none;
}

.nav-links a:hover {
  color: #000000;
}

.nav-links a.router-link-active {
  color: #2563eb;
  font-weight: 600;
}

.main-content {
  padding: 32px;
}
</style>