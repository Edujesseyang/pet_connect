<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getPetById, addFavorite, removeFavorite } from "../api/pets";
import { mockPets } from "../api/mockData";
import { useAuthStore } from "../stores/auth";
import type { Pet } from "../types";

const route = useRoute();
const auth = useAuthStore();
const pet = ref<Pet | null>(null);
const loading = ref(true);
const favoriteBusy = ref(false);

async function load() {
  loading.value = true;
  const id = Number(route.params.id);
  try {
    const res = await getPetById(id);
    pet.value = res.data;
  } catch {
    // GET /pets/{id} doesn't exist yet — fall back to sample data
    pet.value = mockPets.find((p) => p.petId === id) ?? mockPets[0];
  } finally {
    loading.value = false;
  }
}

async function toggleFavorite() {
  if (!pet.value) return;
  favoriteBusy.value = true;
  try {
    if (pet.value.isFavorited) {
      await removeFavorite(pet.value.petId);
      pet.value.isFavorited = false;
    } else {
      await addFavorite(pet.value.petId);
      pet.value.isFavorited = true;
    }
  } catch {
    // Endpoint not built yet — flip the UI state locally so this is reviewable.
    pet.value.isFavorited = !pet.value.isFavorited;
  } finally {
    favoriteBusy.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="pc-container detail">
    <p v-if="loading" class="pc-empty">Loading…</p>
    <template v-else-if="pet">
      <div class="detail__grid">
        <div class="detail__photo pc-card">
          <img
            v-if="pet.photos?.[0]?.photoUrl"
            :src="pet.photos[0].photoUrl"
            :alt="pet.name"
          />
          <div v-else class="detail__photo--placeholder">🐾</div>
        </div>

        <div class="detail__info">
          <div class="detail__header">
            <h1>{{ pet.name }}</h1>
            <button
              v-if="auth.isLoggedIn"
              class="pc-btn"
              :class="pet.isFavorited ? 'pc-btn--accent' : 'pc-btn--ghost'"
              :disabled="favoriteBusy"
              @click="toggleFavorite"
            >
              {{ pet.isFavorited ? "♥ Favorited" : "♡ Save" }}
            </button>
          </div>

          <p class="detail__meta">
            {{ pet.breed }} · {{ pet.species }} · {{ pet.sex }}
            <span v-if="pet.age !== undefined"> · {{ pet.age }} years old</span>
          </p>
          <p v-if="pet.address?.city" class="detail__location">
            📍 {{ pet.address.city }}{{ pet.address.state ? `, ${pet.address.state}` : "" }}
          </p>

          <p class="detail__description">{{ pet.description }}</p>

          <div class="detail__actions">
            <RouterLink
              v-if="auth.isLoggedIn"
              :to="{ name: 'home' }"
              class="pc-btn pc-btn--primary"
            >
              Message owner
            </RouterLink>
            <RouterLink v-else to="/login" class="pc-btn pc-btn--primary">
              Log in to message the owner
            </RouterLink>
          </div>

          <div v-if="pet.medicalRecord" class="detail__medical pc-card">
            <h3>Health details</h3>
            <ul>
              <li v-if="pet.medicalRecord.vaccination">Vaccination: {{ pet.medicalRecord.vaccination }}</li>
              <li v-if="pet.medicalRecord.spayedNeutered !== undefined">
                Spayed/Neutered: {{ pet.medicalRecord.spayedNeutered ? "Yes" : "No" }}
              </li>
              <li v-if="pet.medicalRecord.specialCare">Special care: {{ pet.medicalRecord.specialCare }}</li>
            </ul>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail {
  padding: 40px 24px 60px;
}

.detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

@media (max-width: 800px) {
  .detail__grid {
    grid-template-columns: 1fr;
  }
}

.detail__photo {
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--pc-bg);
}

.detail__photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail__photo--placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem;
  color: var(--pc-border);
}

.detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.detail__header h1 {
  font-size: 2.2rem;
  margin: 0;
}

.detail__meta {
  color: var(--pc-muted);
  font-size: 1rem;
  margin: 8px 0 0;
}

.detail__location {
  color: var(--pc-muted);
  margin: 4px 0 0;
}

.detail__description {
  margin: 20px 0;
  line-height: 1.6;
}

.detail__actions {
  margin-bottom: 24px;
}

.detail__medical {
  padding: 16px 20px;
}

.detail__medical h3 {
  margin-bottom: 8px;
}

.detail__medical ul {
  margin: 0;
  padding-left: 18px;
  color: var(--pc-muted);
}
</style>
