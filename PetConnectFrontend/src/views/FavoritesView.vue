<script setup lang="ts">
import { ref, onMounted } from "vue";
import PetCard from "../components/PetCard.vue";
import { getFavorites } from "../api/pets";
import { mockPets } from "../api/mockData";
import type { Pet } from "../types";

const pets = ref<Pet[]>([]);
const loading = ref(true);
const usingMockData = ref(false);

onMounted(async () => {
  try {
    const res = await getFavorites();
    pets.value = res.data ?? [];
  } catch {
    // GET /favorites doesn't exist yet — show a couple of sample favorites
    pets.value = mockPets.slice(0, 2);
    usingMockData.value = true;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="pc-container favorites">
    <h1>Your favorites</h1>

    <div v-if="usingMockData" class="favorites__notice">
      Showing sample data — connect to <code>GET /favorites</code> to see saved pets.
    </div>

    <p v-if="loading" class="pc-empty">Loading…</p>
    <p v-else-if="pets.length === 0" class="pc-empty">
      You haven't saved any pets yet. Browse listings and tap ♡ to save one.
    </p>
    <div v-else class="favorites__grid">
      <PetCard v-for="pet in pets" :key="pet.petId" :pet="pet" />
    </div>
  </div>
</template>

<style scoped>
.favorites {
  padding: 40px 24px 60px;
}

.favorites__notice {
  background: #FBF1DC;
  border: 1px solid #EFD9A0;
  color: #6B5417;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.favorites__notice code {
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 6px;
  border-radius: 4px;
}

.favorites__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
</style>
