<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();

function goToLogin(): void {
  router.push("/login");
}

function goToSignup(): void {
  router.push("/signup");
}
</script>

<template>
  <section class="home-page">
    <h1>Pet Connect</h1>

    <p>Connect pets, owners, shelters, and adopters.</p>

    <button type="button" @click="goToLogin">Login</button>
    <button type="button" @click="goToSignup">Signup</button>
  </section>
</template>

<style scoped>
.home-page {
  max-width: 800px;
  margin: 80px auto;
  text-align: center;
}

h1 {
  margin-top: 100px;
  margin-bottom: 16px;
  font-size: 48px;
}

p {
  margin-bottom: 24px;
  color: #666666;
  font-size: 18px;
}

button {
  margin: 30px;
  padding: 12px 24px;
  color: white;
  background-color: #2563eb;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button:hover {
  background-color: #1d4ed8;
}
</style>
