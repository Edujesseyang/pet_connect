<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

import type { ApiResponse, Pet } from "../types/api";

const route = useRoute();
const router = useRouter();

const pet = ref<Pet | null>(null);
const isLoading = ref(false);
const errorMessage = ref("");

const petId = computed(() => {
  return Number(route.params.petId);
});

const mainPhotoUrl = computed(() => {
  return (
    pet.value?.photos?.[0]?.url ||
    "https://placehold.co/900x600?text=No+Pet+Photo"
  );
});

const formattedDateOfBirth = computed(() => {
  const value = pet.value?.petProfile?.dateOfBirth;

  if (!value) {
    return "Not provided";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Not provided";
  }

  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
});

onMounted(() => {
  fetchPet();
});

async function fetchPet(): Promise<void> {
  const petId = Number(route.params.petId);

  if (!Number.isInteger(petId) || petId <= 0) {
    errorMessage.value = "Invalid pet ID.";
    return;
  }

  const requestUrl = `http://localhost:8080/pets/get_by_id/${petId}?petId=${petId}`;

  console.log("Fetching pet from:", requestUrl);

  try {
    isLoading.value = true;
    errorMessage.value = "";

    const response = await fetch(requestUrl, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    });

    const responseText = await response.text();

    console.log("Pet response status:", response.status);
    console.log("Pet response body:", responseText);

    if (!response.ok) {
      throw new Error(
        responseText || `Request failed with status ${response.status}`,
      );
    }

    const responseBody = JSON.parse(responseText) as Pet | ApiResponse<Pet>;

    if (
      typeof responseBody === "object" &&
      responseBody !== null &&
      "data" in responseBody
    ) {
      pet.value = responseBody.data ?? null;
    } else {
      pet.value = responseBody as Pet;
    }

    if (!pet.value) {
      throw new Error("The server returned no pet data.");
    }
  } catch (error) {
    console.error("Failed to fetch pet:", error);

    errorMessage.value =
      error instanceof Error ? error.message : "Unable to load pet details.";
  } finally {
    isLoading.value = false;
  }
}

function goBack(): void {
  router.back();
}
</script>

<template>
  <main class="pet-detail-page">
    <button type="button" class="back-button" @click="goBack">
      ← Back to Posts
    </button>

    <div v-if="isLoading" class="status-card">Loading pet information...</div>

    <div v-else-if="errorMessage" class="status-card error-card">
      {{ errorMessage }}
    </div>

    <template v-else-if="pet">
      <section class="pet-hero">
        <img :src="mainPhotoUrl" :alt="pet.name" />

        <div class="pet-hero-content">
          <p class="eyebrow">
            {{ pet.species }}
          </p>

          <h1>{{ pet.name }}</h1>

          <p class="breed">
            {{ pet.breed }}
          </p>

          <p class="description">
            {{ pet.petProfile?.description || "No description provided." }}
          </p>
        </div>
      </section>

      <section class="detail-card">
        <h2>Pet Information</h2>

        <div class="detail-grid">
          <div>
            <span>Pet ID</span>
            <strong>{{ pet.petId }}</strong>
          </div>

          <div>
            <span>Species</span>
            <strong>{{ pet.species }}</strong>
          </div>

          <div>
            <span>Breed</span>
            <strong>{{ pet.breed }}</strong>
          </div>

          <div>
            <span>Sex</span>
            <strong>
              {{ pet.petProfile?.sex || "Not provided" }}
            </strong>
          </div>

          <div>
            <span>Date of Birth</span>
            <strong>
              {{ formattedDateOfBirth }}
            </strong>
          </div>

          <div>
            <span>Color</span>
            <strong>
              {{ pet.petProfile?.color || "Not provided" }}
            </strong>
          </div>

          <div>
            <span>Size</span>
            <strong>
              {{ pet.petProfile?.size || "Not provided" }}
            </strong>
          </div>

          <div>
            <span>Weight</span>
            <strong>
              {{
                pet.petProfile?.weight != null
                  ? `${pet.petProfile.weight} lb`
                  : "Not provided"
              }}
            </strong>
          </div>

          <div>
            <span>Friendly Level</span>
            <strong>
              {{ pet.petProfile?.friendlyLevel ?? "Not provided" }}
            </strong>
          </div>

          <div>
            <span>Trained</span>
            <strong>
              {{ pet.petProfile?.isTrained ? "Yes" : "No" }}
            </strong>
          </div>
        </div>
      </section>

      <section class="detail-card">
        <h2>Medical Record</h2>

        <div class="detail-grid">
          <div>
            <span>Vaccination</span>
            <strong>
              {{ pet.medicalRecord?.vaccination || "Not provided" }}
            </strong>
          </div>

          <div>
            <span>Spayed / Neutered</span>
            <strong>
              {{ pet.medicalRecord?.spayedNeutered ? "Yes" : "No" }}
            </strong>
          </div>

          <div>
            <span>Allergies</span>
            <strong>
              {{ pet.medicalRecord?.allergies || "None" }}
            </strong>
          </div>

          <div>
            <span>Medications</span>
            <strong>
              {{ pet.medicalRecord?.medications || "None" }}
            </strong>
          </div>

          <div>
            <span>Special Care</span>
            <strong>
              {{ pet.medicalRecord?.specialCare || "None" }}
            </strong>
          </div>

          <div>
            <span>Surgeries</span>
            <strong>
              {{ pet.medicalRecord?.surgeries || "None" }}
            </strong>
          </div>
        </div>
      </section>

      <section class="detail-card">
        <h2>Location</h2>

        <p>
          {{ pet.address?.street }}, {{ pet.address?.city }},
          {{ pet.address?.state }}
          {{ pet.address?.zipCode }},
          {{ pet.address?.country }}
        </p>
      </section>

      <section v-if="pet.photos?.length" class="detail-card">
        <h2>Photos</h2>

        <div class="photo-grid">
          <img
            v-for="photo in pet.photos"
            :key="photo.photoId ?? photo.url"
            :src="photo.url"
            :alt="photo.description || pet.name"
          />
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.pet-detail-page {
  width: min(1100px, calc(100% - 32px));
  margin: 0 auto;
  padding: 32px 0 60px;
}

.back-button {
  margin-bottom: 20px;
  padding: 10px 15px;
  color: #1d4ed8;
  font-weight: 700;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  cursor: pointer;
}

.pet-hero {
  display: grid;
  grid-template-columns: minmax(300px, 44%) minmax(0, 1fr);
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
}

.pet-hero > img {
  width: 100%;
  height: 100%;
  min-height: 390px;
  object-fit: cover;
}

.pet-hero-content {
  display: flex;
  justify-content: center;
  flex-direction: column;
  padding: 38px;
}

.eyebrow {
  margin: 0;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.pet-hero-content h1 {
  margin: 8px 0;
  color: #0f172a;
  font-size: 42px;
}

.breed {
  margin: 0;
  color: #475569;
  font-size: 19px;
  font-weight: 700;
}

.description {
  margin: 22px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.detail-card {
  margin-top: 22px;
  padding: 26px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.detail-card h2 {
  margin: 0 0 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.detail-grid > div {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 15px;
  background-color: #f8fafc;
  border-radius: 9px;
}

.detail-grid span {
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.photo-grid img {
  width: 100%;
  height: 230px;
  object-fit: cover;
  border-radius: 10px;
}

.status-card {
  padding: 40px;
  text-align: center;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.error-card {
  color: #b91c1c;
  background-color: #fef2f2;
}

@media (max-width: 800px) {
  .pet-hero {
    grid-template-columns: 1fr;
  }

  .detail-grid,
  .photo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .detail-grid,
  .photo-grid {
    grid-template-columns: 1fr;
  }

  .pet-hero-content {
    padding: 24px;
  }
}
</style>
