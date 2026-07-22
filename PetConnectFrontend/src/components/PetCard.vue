<script setup lang="ts">
import { RouterLink } from "vue-router";
import type { Pet } from "../types";

defineProps<{ pet: Pet }>();
</script>

<template>
  <RouterLink :to="{ name: 'pet-detail', params: { id: pet.petId } }" class="pet-card pc-card">
    <div class="pet-card__photo">
      <img
        v-if="pet.photos?.[0]?.photoUrl"
        :src="pet.photos[0].photoUrl"
        :alt="pet.name"
      />
      <div v-else class="pet-card__photo--placeholder">🐾</div>
      <span
        v-if="pet.listingStatus && pet.listingStatus !== 'AVAILABLE'"
        class="pet-card__badge"
      >
        {{ pet.listingStatus === "ADOPTED" ? "Adopted" : "Pending" }}
      </span>
    </div>
    <div class="pet-card__body">
      <div class="pet-card__title-row">
        <h3>{{ pet.name }}</h3>
        <span v-if="pet.age !== undefined" class="pet-card__age">{{ pet.age }}y</span>
      </div>
      <p class="pet-card__meta">{{ pet.breed }} · {{ pet.species }}</p>
      <p v-if="pet.address?.city" class="pet-card__location">
        📍 {{ pet.address.city }}{{ pet.address.state ? `, ${pet.address.state}` : "" }}
      </p>
    </div>
  </RouterLink>
</template>

<style scoped>
.pet-card {
  display: block;
  text-decoration: none;
  color: var(--pc-text);
  overflow: hidden;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.pet-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(30, 35, 31, 0.1);
}

.pet-card__photo {
  position: relative;
  aspect-ratio: 4 / 3;
  background: var(--pc-bg);
}

.pet-card__photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-card__photo--placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  color: var(--pc-border);
}

.pet-card__badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--pc-primary);
  color: white;
  font-size: 0.72rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
}

.pet-card__body {
  padding: 14px 16px 16px;
}

.pet-card__title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.pet-card__title-row h3 {
  font-size: 1.05rem;
  margin: 0;
}

.pet-card__age {
  font-size: 0.85rem;
  color: var(--pc-muted);
}

.pet-card__meta {
  margin: 4px 0 0;
  color: var(--pc-muted);
  font-size: 0.9rem;
}

.pet-card__location {
  margin: 6px 0 0;
  font-size: 0.85rem;
  color: var(--pc-muted);
}
</style>
