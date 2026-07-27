<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";

import type {
  Pet,
  User,
} from "../../types/api";

defineProps<{
  user: User | null;
}>();

const selectedPet = ref<Pet | null>(null);

function openPet(pet: Pet): void {
  selectedPet.value = pet;
}

function closePet(): void {
  selectedPet.value = null;
}

const router = useRouter();

function goToAddPet(): void {
  router.push("/dashboard/pets/add");
}

function calculateAge(dateOfBirth?: string | null): string {
  if (!dateOfBirth) {
    return "Unknown age";
  }

  const birthDate = new Date(dateOfBirth);

  if (Number.isNaN(birthDate.getTime())) {
    return "Unknown age";
  }

  const today = new Date();

  let years =
    today.getFullYear() - birthDate.getFullYear();

  let months =
    today.getMonth() - birthDate.getMonth();

  if (
    months < 0 ||
    (
      months === 0 &&
      today.getDate() < birthDate.getDate()
    )
  ) {
    years--;
    months += 12;
  }

  if (today.getDate() < birthDate.getDate()) {
    months--;
  }

  if (months < 0) {
    months += 12;
  }

  if (years > 0) {
    return `${years} year${years === 1 ? "" : "s"} old`;
  }

  return `${months} month${months === 1 ? "" : "s"} old`;
}

function formatDate(date?: string | null): string {
  if (!date) {
    return "Not provided";
  }

  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return "Not provided";
  }

  return parsedDate.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function displayValue(
  value: string | number | null | undefined,
  fallback = "Not provided",
): string | number {
  if (
    value === null ||
    value === undefined ||
    value === ""
  ) {
    return fallback;
  }

  return value;
}

const selectedPetAge = computed(() => {
  return calculateAge(
    selectedPet.value?.petProfile?.dateOfBirth,
  );
});

const selectedPetMainPhoto = computed(() => {
  return selectedPet.value?.photos?.[0]?.url ?? null;
});
</script>

<template>
  <section class="content-card">
    <template v-if="!selectedPet">
      <div class="section-heading">
        <div>
          <p class="section-label">
            Pet Management
          </p>

          <h2>My Pets</h2>

          <span class="section-description">
            View and manage all pets connected to your account.
          </span>
        </div>

        <button
          type="button"
          class="primary-button"
          @click="goToAddPet"
        >
          Add Pet
        </button>
      </div>

      <div
        v-if="user?.ownedPets?.length"
        class="pet-list"
      >
        <article
          v-for="pet in user.ownedPets"
          :key="pet.petId"
          class="pet-list-item"
          tabindex="0"
          role="button"
          @click="openPet(pet)"
          @keydown.enter="openPet(pet)"
          @keydown.space.prevent="openPet(pet)"
        >
          <div class="pet-list-main">
            <div class="pet-avatar">
              <img
                v-if="pet.photos?.[0]?.url"
                :src="pet.photos[0].url"
                :alt="pet.name"
              />

              <span v-else>
                {{ pet.name.charAt(0).toUpperCase() }}
              </span>
            </div>

            <div class="pet-list-content">
              <h3>{{ pet.name }}</h3>

              <div class="pet-list-meta">
                <span>
                  {{ pet.species || "Unknown species" }}
                </span>

                <span class="meta-separator">
                  ·
                </span>

                <span>
                  {{ pet.breed || "Unknown breed" }}
                </span>

                <span class="meta-separator">
                  ·
                </span>

                <span>
                  {{
                    calculateAge(
                      pet.petProfile?.dateOfBirth,
                    )
                  }}
                </span>
              </div>
            </div>
          </div>

          <span class="view-detail">
            View details
          </span>
        </article>
      </div>

      <div
        v-else
        class="empty-state"
      >
        <div class="empty-icon">
          P
        </div>

        <h3>No pets yet</h3>

        <p>
          Pets connected to this account will appear here.
        </p>
      </div>
    </template>

    <template v-else>
      <div class="detail-toolbar">
        <button
          type="button"
          class="back-button"
          @click="closePet"
        >
          Back to My Pets
        </button>

        <div class="detail-actions">
          <button
            type="button"
            class="secondary-button"
          >
            Edit Pet
          </button>

          <button
            type="button"
            class="danger-button"
          >
            Delete Pet
          </button>
        </div>
      </div>

      <section class="pet-hero">
        <div class="pet-hero-image">
          <img
            v-if="selectedPetMainPhoto"
            :src="selectedPetMainPhoto"
            :alt="selectedPet.name"
          />

          <span v-else>
            {{ selectedPet.name.charAt(0).toUpperCase() }}
          </span>
        </div>

        <div class="pet-hero-content">
          <p class="section-label">
            Pet Profile
          </p>

          <h2>{{ selectedPet.name }}</h2>

          <p class="pet-subtitle">
            {{ selectedPet.species || "Unknown species" }}
            ·
            {{ selectedPet.breed || "Unknown breed" }}
          </p>

          <div class="pet-tags">
            <span>
              {{ selectedPetAge }}
            </span>

            <span>
              {{
                selectedPet.petProfile?.sex ||
                "Unknown sex"
              }}
            </span>

            <span>
              {{
                selectedPet.petProfile?.size ||
                "Unknown size"
              }}
            </span>

            <span>
              {{
                selectedPet.petProfile?.isTrained
                  ? "Trained"
                  : "Not trained"
              }}
            </span>
          </div>
        </div>
      </section>

      <section class="detail-section">
        <div class="detail-section-heading">
          <div>
            <p class="section-label">
              General
            </p>

            <h3>Basic Information</h3>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-field">
            <span>Pet ID</span>

            <strong>
              {{ selectedPet.petId }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Name</span>

            <strong>
              {{ selectedPet.name }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Species</span>

            <strong>
              {{
                displayValue(
                  selectedPet.species,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Breed</span>

            <strong>
              {{
                displayValue(
                  selectedPet.breed,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Sex</span>

            <strong>
              {{
                displayValue(
                  selectedPet.petProfile?.sex,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Date of Birth</span>

            <strong>
              {{
                formatDate(
                  selectedPet.petProfile?.dateOfBirth,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Age</span>

            <strong>
              {{ selectedPetAge }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Color</span>

            <strong>
              {{
                displayValue(
                  selectedPet.petProfile?.color,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Size</span>

            <strong>
              {{
                displayValue(
                  selectedPet.petProfile?.size,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Weight</span>

            <strong>
              {{
                selectedPet.petProfile?.weight !==
                null &&
                selectedPet.petProfile?.weight !==
                  undefined
                  ? `${selectedPet.petProfile.weight} lb`
                  : "Not provided"
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Friendly Level</span>

            <strong>
              {{
                selectedPet.petProfile?.friendlyLevel !==
                null &&
                selectedPet.petProfile?.friendlyLevel !==
                  undefined
                  ? `${selectedPet.petProfile.friendlyLevel} / 5`
                  : "Not provided"
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Trained</span>

            <strong>
              {{
                selectedPet.petProfile?.isTrained
                  ? "Yes"
                  : "No"
              }}
            </strong>
          </div>

          <div class="detail-field detail-field-wide">
            <span>Microchip Number</span>

            <strong>
              {{
                displayValue(
                  selectedPet.petProfile
                    ?.microchipNumber,
                )
              }}
            </strong>
          </div>
        </div>
      </section>

      <section class="detail-section">
        <div class="detail-section-heading">
          <div>
            <p class="section-label">
              About
            </p>

            <h3>Description</h3>
          </div>
        </div>

        <p class="description-text">
          {{
            selectedPet.petProfile?.description ||
            "No description has been provided."
          }}
        </p>
      </section>

      <section class="detail-section">
        <div class="detail-section-heading">
          <div>
            <p class="section-label">
              Location
            </p>

            <h3>Address</h3>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-field">
            <span>Address ID</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.addressId,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Country</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.country,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>State</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.state,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>City</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.city,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Street</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.street,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>ZIP Code</span>

            <strong>
              {{
                displayValue(
                  selectedPet.address?.zipCode,
                )
              }}
            </strong>
          </div>
        </div>
      </section>

      <section class="detail-section">
        <div class="detail-section-heading">
          <div>
            <p class="section-label">
              Health
            </p>

            <h3>Medical Record</h3>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-field">
            <span>Vaccination</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.vaccination,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Allergies</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.allergies,
                  "None",
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Medications</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.medications,
                  "None",
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Surgeries</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.surgeries,
                  "None",
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Lab Results</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.labResults,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Imaging Results</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.imagingResults,
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Special Care</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord
                    ?.specialCare,
                  "None",
                )
              }}
            </strong>
          </div>

          <div class="detail-field">
            <span>Spayed or Neutered</span>

            <strong>
              {{
                selectedPet.medicalRecord
                  ?.spayedNeutered
                  ? "Yes"
                  : "No"
              }}
            </strong>
          </div>

          <div class="detail-field detail-field-wide">
            <span>Medical Note</span>

            <strong>
              {{
                displayValue(
                  selectedPet.medicalRecord?.note,
                  "No medical notes",
                )
              }}
            </strong>
          </div>
        </div>
      </section>

      <section class="detail-section">
        <div class="detail-section-heading">
          <div>
            <p class="section-label">
              Media
            </p>

            <h3>Photos</h3>
          </div>

          <span class="photo-count">
            {{ selectedPet.photos?.length ?? 0 }} photos
          </span>
        </div>

        <div
          v-if="selectedPet.photos?.length"
          class="photo-gallery"
        >
          <figure
            v-for="photo in selectedPet.photos"
            :key="photo.photoId"
            class="photo-card"
          >
            <img
              :src="photo.url"
              :alt="
                photo.description ||
                selectedPet.name
              "
            />

            <figcaption>
              {{
                photo.description ||
                "Pet photo"
              }}
            </figcaption>
          </figure>
        </div>

        <div
          v-else
          class="photo-empty-state"
        >
          No photos have been added for this pet.
        </div>
      </section>
    </template>
  </section>
</template>

<style scoped>
.content-card {
  padding: 26px;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.section-heading,
.detail-section-heading,
.detail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.section-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.section-heading h2,
.detail-section-heading h3 {
  margin: 0;
  color: #0f172a;
}

.section-description {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 14px;
}

.primary-button,
.back-button,
.secondary-button,
.danger-button {
  padding: 10px 16px;
  font-weight: 600;
  border-radius: 9px;
  cursor: pointer;
}

.primary-button,
.back-button {
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
}

.primary-button:hover,
.back-button:hover {
  background-color: #1d4ed8;
}

.secondary-button {
  color: #334155;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
}

.secondary-button:hover {
  background-color: #f8fafc;
}

.danger-button {
  color: #b91c1c;
  background-color: #ffffff;
  border: 1px solid #fecaca;
}

.danger-button:hover {
  background-color: #fef2f2;
}

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 26px;
}

.pet-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 18px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.pet-list-item:hover,
.pet-list-item:focus-visible {
  border-color: #60a5fa;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.08);
  outline: none;
  transform: translateY(-2px);
}

.pet-list-main {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.pet-avatar {
  display: flex;
  flex: 0 0 58px;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  overflow: hidden;
  color: #1d4ed8;
  font-size: 20px;
  font-weight: 700;
  background-color: #dbeafe;
  border-radius: 13px;
}

.pet-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-list-content {
  min-width: 0;
}

.pet-list-content h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.pet-list-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 7px;
  color: #64748b;
  font-size: 14px;
}

.meta-separator {
  color: #cbd5e1;
}

.view-detail {
  flex: 0 0 auto;
  padding: 7px 11px;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  background-color: #eff6ff;
  border-radius: 999px;
}

.empty-state {
  display: flex;
  min-height: 280px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  margin-top: 26px;
  padding: 40px;
  color: #64748b;
  text-align: center;
  background-color: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
}

.empty-state h3 {
  margin: 14px 0 6px;
  color: #0f172a;
}

.empty-state p {
  margin: 0;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  color: #1d4ed8;
  font-size: 22px;
  font-weight: 800;
  background-color: #dbeafe;
  border-radius: 16px;
}

.detail-toolbar {
  margin-bottom: 24px;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

.pet-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 26px;
  color: #ffffff;
  background:
    linear-gradient(
      120deg,
      #1d4ed8,
      #2563eb 55%,
      #0ea5e9
    );
  border-radius: 16px;
}

.pet-hero-image {
  display: flex;
  flex: 0 0 130px;
  align-items: center;
  justify-content: center;
  width: 130px;
  height: 130px;
  overflow: hidden;
  color: #1d4ed8;
  font-size: 44px;
  font-weight: 800;
  background-color: #ffffff;
  border: 4px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
}

.pet-hero-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-hero .section-label {
  color: #bfdbfe;
}

.pet-hero h2 {
  margin: 0;
  font-size: 32px;
}

.pet-subtitle {
  margin: 8px 0 0;
  color: #dbeafe;
  font-size: 17px;
}

.pet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin-top: 18px;
}

.pet-tags span {
  padding: 7px 11px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
}

.detail-section {
  margin-top: 22px;
  padding: 22px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.detail-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: 16px;
  background-color: #f8fafc;
  border: 1px solid #edf2f7;
  border-radius: 10px;
}

.detail-field-wide {
  grid-column: 1 / -1;
}

.detail-field span {
  margin-bottom: 7px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.detail-field strong {
  overflow-wrap: anywhere;
  color: #1e293b;
  line-height: 1.5;
}

.description-text {
  margin: 18px 0 0;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.photo-count {
  padding: 7px 11px;
  color: #64748b;
  font-size: 12px;
  background-color: #f1f5f9;
  border-radius: 999px;
}

.photo-gallery {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.photo-card {
  margin: 0;
  overflow: hidden;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.photo-card img {
  display: block;
  width: 100%;
  height: 190px;
  object-fit: cover;
}

.photo-card figcaption {
  padding: 12px;
  color: #64748b;
  font-size: 13px;
}

.photo-empty-state {
  margin-top: 20px;
  padding: 36px;
  color: #64748b;
  text-align: center;
  background-color: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

@media (max-width: 800px) {
  .section-heading,
  .detail-section-heading,
  .detail-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-actions {
    width: 100%;
  }

  .detail-actions button {
    flex: 1;
  }

  .pet-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-grid,
  .photo-gallery {
    grid-template-columns: 1fr;
  }

  .detail-field-wide {
    grid-column: auto;
  }
}

@media (max-width: 560px) {
  .content-card {
    padding: 18px;
  }

  .pet-list-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .view-detail {
    align-self: flex-end;
  }

  .pet-hero-image {
    width: 110px;
    height: 110px;
    flex-basis: 110px;
  }

  .detail-actions {
    flex-direction: column;
  }
}
</style>