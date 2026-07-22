<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import PetCard from "../components/PetCard.vue";
import { searchPets } from "../api/pets";
import { mockPets } from "../api/mockData";
import type { Pet } from "../types";

const pets = ref<Pet[]>([]);
const loading = ref(true);
const usingMockData = ref(false);
const speciesFilter = ref("all");
const query = ref("");

async function load() {
  loading.value = true;
  try {
    const res = await searchPets({});
    pets.value = res.data ?? [];
    usingMockData.value = false;
  } catch {
    // /pets/search doesn't exist on the backend yet — fall back to sample
    // data so the page is still browsable and reviewable.
    pets.value = mockPets;
    usingMockData.value = true;
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const speciesOptions = computed(() => {
  const set = new Set(pets.value.map((p) => p.species));
  return ["all", ...Array.from(set)];
});

const filteredPets = computed(() => {
  return pets.value.filter((p) => {
    const matchesSpecies = speciesFilter.value === "all" || p.species === speciesFilter.value;
    const matchesQuery =
      !query.value ||
      p.name.toLowerCase().includes(query.value.toLowerCase()) ||
      p.breed.toLowerCase().includes(query.value.toLowerCase());
    return matchesSpecies && matchesQuery;
  });
});
</script>

<template>
  <div class="pc-container home">
    <section class="home__hero">
      <h1>Find your next best friend</h1>
      <p>Browse pets from owners and shelters near you.</p>
    </section>

    <div v-if="usingMockData" class="home__notice">
      Showing sample pets — connect to <code>GET /pets/search</code> to see real listings.
    </div>

    <div class="home__filters">
      <input
        v-model="query"
        type="search"
        placeholder="Search by name or breed…"
        class="home__search"
      />
      <select v-model="speciesFilter" class="home__select">
        <option v-for="opt in speciesOptions" :key="opt" :value="opt">
          {{ opt === "all" ? "All species" : opt }}
        </option>
      </select>
    </div>

    <p v-if="loading" class="pc-empty">Loading pets…</p>
    <p v-else-if="filteredPets.length === 0" class="pc-empty">
      No pets match your search yet. Try a different filter.
    </p>
    <div v-else class="home__grid">
      <PetCard v-for="pet in filteredPets" :key="pet.petId" :pet="pet" />
    </div>
  </div>
</template>

<style scoped>
.home {
  padding: 40px 24px 60px;
}

.home__hero {
  margin-bottom: 28px;
}

.home__hero h1 {
  font-size: 2rem;
}

.home__hero p {
  color: var(--pc-muted);
  margin: 0;
}

.home__notice {
  background: #FBF1DC;
  border: 1px solid #EFD9A0;
  color: #6B5417;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.home__notice code {
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 6px;
  border-radius: 4px;
}

.home__filters {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
}

.home__search {
  flex: 1;
  border: 1px solid var(--pc-border);
  border-radius: 10px;
  padding: 10px 14px;
  background: var(--pc-surface);
}

.home__select {
  border: 1px solid var(--pc-border);
  border-radius: 10px;
  padding: 10px 14px;
  background: var(--pc-surface);
}

.home__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
</style>
