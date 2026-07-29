<script setup lang="ts">
import {
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from "vue";
import { useRouter } from "vue-router";

import type {
  AddPetRequest,
  ApiResponse,
  Pet,
  Photo,
  User,
} from "../types/api";

interface SelectedPhoto {
  id: string;
  file: File;
  previewUrl: string;
  description: string;
}

const router = useRouter();

const currentUser = ref<User | null>(null);
const selectedPhotos = ref<SelectedPhoto[]>([]);

const isSubmitting = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

const form = reactive<AddPetRequest>({
  name: "",
  species: "",
  breed: "",
  sex: "",
  dateOfBirth: "",
  color: "",
  microchipNumber: "",
  size: "",
  weight: null,
  description: "",
  friendlyLevel: null,
  isTrained: false,

  country: "",
  state: "",
  city: "",
  street: "",
  zipCode: "",

  vaccination: "",
  allergies: "",
  medications: "",
  surgeries: "",
  labResults: "",
  imagingResults: "",
  note: "",
  specialCare: "",
  spayedNeutered: false,
});

onMounted(() => {
  try {
    currentUser.value = getStoredUser();
  } catch {
    localStorage.removeItem("user");
    router.replace("/login");
  }
});

onBeforeUnmount(() => {
  revokeAllPreviewUrls();
});

function getStoredUser(): User {
  const storedUser = localStorage.getItem("user");

  if (!storedUser) {
    throw new Error("User is not logged in.");
  }

  const parsedUser = JSON.parse(storedUser) as User;

  if (
    !parsedUser.userId ||
    parsedUser.userId <= 0
  ) {
    throw new Error("User ID is missing.");
  }

  return parsedUser;
}

function handlePhotoSelection(
  event: Event,
): void {
  errorMessage.value = "";

  const input =
    event.target as HTMLInputElement;

  const files = Array.from(
    input.files ?? [],
  );

  if (files.length === 0) {
    return;
  }

  const availableSlots =
    10 - selectedPhotos.value.length;

  if (availableSlots <= 0) {
    errorMessage.value =
      "You can upload a maximum of 10 photos.";

    input.value = "";
    return;
  }

  const validContentTypes = [
    "image/jpeg",
    "image/png",
    "image/webp",
  ];

  const validFiles = files.filter((file) => {
    return validContentTypes.includes(file.type);
  });

  if (validFiles.length !== files.length) {
    errorMessage.value =
      "Only JPEG, PNG, and WebP images are allowed.";
  }

  const acceptedFiles =
    validFiles.slice(0, availableSlots);

  const newPhotos: SelectedPhoto[] =
    acceptedFiles.map((file) => ({
      id: crypto.randomUUID(),
      file,
      previewUrl:
        URL.createObjectURL(file),
      description: "",
    }));

  selectedPhotos.value.push(...newPhotos);

  if (validFiles.length > availableSlots) {
    errorMessage.value =
      "Only the first 10 photos were selected.";
  }

  input.value = "";
}

function removePhoto(
  photoId: string,
): void {
  const photo = selectedPhotos.value.find(
    (item) => item.id === photoId,
  );

  if (photo) {
    URL.revokeObjectURL(
      photo.previewUrl,
    );
  }

  selectedPhotos.value =
    selectedPhotos.value.filter(
      (item) => item.id !== photoId,
    );
}

function revokeAllPreviewUrls(): void {
  selectedPhotos.value.forEach((photo) => {
    URL.revokeObjectURL(
      photo.previewUrl,
    );
  });
}

function validateForm(): boolean {
  errorMessage.value = "";

  if (!currentUser.value?.userId) {
    errorMessage.value =
      "Current user is unavailable.";
    return false;
  }

  if (!form.name.trim()) {
    errorMessage.value =
      "Pet name is required.";
    return false;
  }

  if (!form.species.trim()) {
    errorMessage.value =
      "Species is required.";
    return false;
  }

  if (!form.breed.trim()) {
    errorMessage.value =
      "Breed is required.";
    return false;
  }

  if (!form.country.trim()) {
    errorMessage.value =
      "Country is required.";
    return false;
  }

  if (!form.state.trim()) {
    errorMessage.value =
      "State is required.";
    return false;
  }

  if (!form.city.trim()) {
    errorMessage.value =
      "City is required.";
    return false;
  }

  if (
    form.weight !== null &&
    form.weight < 0
  ) {
    errorMessage.value =
      "Weight cannot be negative.";
    return false;
  }

  if (
    form.friendlyLevel !== null &&
    (
      form.friendlyLevel < 1 ||
      form.friendlyLevel > 5
    )
  ) {
    errorMessage.value =
      "Friendly level must be between 1 and 5.";
    return false;
  }

  return true;
}

function createRequestBody(): AddPetRequest {
  return {
    name: form.name.trim(),
    species: form.species.trim(),
    breed: form.breed.trim(),
    sex: form.sex,
    dateOfBirth: form.dateOfBirth
      ? new Date(
          `${form.dateOfBirth}T00:00:00`,
        ).toISOString()
      : "",
    color: form.color.trim(),
    microchipNumber:
      form.microchipNumber.trim(),
    size: form.size,
    weight:
      form.weight === null
        ? 0
        : Number(form.weight),
    description:
      form.description.trim(),
    friendlyLevel:
      form.friendlyLevel === null
        ? 1
        : Number(form.friendlyLevel),
    isTrained: form.isTrained,

    country: form.country.trim(),
    state: form.state.trim(),
    city: form.city.trim(),
    street: form.street.trim(),
    zipCode: form.zipCode.trim(),

    vaccination:
      form.vaccination.trim(),
    allergies: form.allergies.trim(),
    medications:
      form.medications.trim(),
    surgeries: form.surgeries.trim(),
    labResults:
      form.labResults.trim(),
    imagingResults:
      form.imagingResults.trim(),
    note: form.note.trim(),
    specialCare:
      form.specialCare.trim(),
    spayedNeutered:
      form.spayedNeutered,
  };
}

async function readResponseBody(
  response: Response,
): Promise<unknown> {
  const contentType =
    response.headers.get(
      "content-type",
    );

  if (
    contentType?.includes(
      "application/json",
    )
  ) {
    return response.json();
  }

  const text = await response.text();

  if (!text) {
    return null;
  }

  return text;
}

function getErrorMessage(
  responseBody: unknown,
  fallback: string,
): string {
  if (
    typeof responseBody === "object" &&
    responseBody !== null &&
    "message" in responseBody &&
    typeof responseBody.message ===
      "string"
  ) {
    return responseBody.message;
  }

  if (
    typeof responseBody === "string" &&
    responseBody.trim()
  ) {
    return responseBody;
  }

  return fallback;
}

async function createPet(): Promise<Pet> {
  const user = getStoredUser();
  const userId = user.userId;

  const response = await fetch(
    `http://localhost:8080/pets/add_by_user/${userId}?userId=${userId}`,
    {
      method: "POST",
      headers: {
        "Content-Type":
          "application/json",
      },
      body: JSON.stringify(
        createRequestBody(),
      ),
    },
  );

  const responseBody =
    await readResponseBody(response);

  if (!response.ok) {
    throw new Error(
      getErrorMessage(
        responseBody,
        "Unable to create pet.",
      ),
    );
  }

  if (
    typeof responseBody !== "object" ||
    responseBody === null
  ) {
    throw new Error(
      "Pet was created, but the server returned an invalid response.",
    );
  }

  if ("data" in responseBody) {
    const apiResponse =
      responseBody as ApiResponse<Pet>;

    if (
      !apiResponse.state ||
      !apiResponse.data
    ) {
      throw new Error(
        apiResponse.message ||
          "Unable to create pet.",
      );
    }

    return apiResponse.data;
  }

  return responseBody as Pet;
}

async function uploadPhoto(
  petId: number,
  selectedPhoto: SelectedPhoto,
): Promise<Photo> {
  const user = getStoredUser();

  const formData = new FormData();

  formData.append(
    "file",
    selectedPhoto.file,
  );

  formData.append(
    "petId",
    String(petId),
  );

  formData.append(
    "uploaderId",
    String(user.userId),
  );

  formData.append(
    "description",
    selectedPhoto.description.trim(),
  );

  const response = await fetch(
    "http://localhost:8080/photos/upload/pet",
    {
      method: "POST",
      body: formData,
    },
  );

  const responseBody =
    await readResponseBody(response);

  if (!response.ok) {
    throw new Error(
      getErrorMessage(
        responseBody,
        `Unable to upload ${selectedPhoto.file.name}.`,
      ),
    );
  }

  if (
    typeof responseBody !== "object" ||
    responseBody === null
  ) {
    throw new Error(
      `The server returned an invalid response while uploading ${selectedPhoto.file.name}.`,
    );
  }

  if ("data" in responseBody) {
    const apiResponse =
      responseBody as ApiResponse<Photo>;

    if (
      !apiResponse.state ||
      !apiResponse.data
    ) {
      throw new Error(
        apiResponse.message ||
          `Unable to upload ${selectedPhoto.file.name}.`,
      );
    }

    return apiResponse.data;
  }

  return responseBody as Photo;
}

async function handleSubmit(): Promise<void> {
  successMessage.value = "";

  if (!validateForm()) {
    return;
  }

  try {
    isSubmitting.value = true;
    errorMessage.value = "";

    const createdPet =
      await createPet();

    if (
      !createdPet.petId ||
      createdPet.petId <= 0
    ) {
      throw new Error(
        "Pet was created, but no valid pet ID was returned.",
      );
    }

    const uploadedPhotos: Photo[] = [];

    for (
      const selectedPhoto
      of selectedPhotos.value
    ) {
      const uploadedPhoto =
        await uploadPhoto(
          createdPet.petId,
          selectedPhoto,
        );

      uploadedPhotos.push(
        uploadedPhoto,
      );
    }

    createdPet.photos =
      uploadedPhotos.length > 0
        ? uploadedPhotos
        : createdPet.photos ?? [];

    const storedUser = getStoredUser();

    if (!storedUser.ownedPets) {
      storedUser.ownedPets = [];
    }

    storedUser.ownedPets.push(
      createdPet,
    );

    localStorage.setItem(
      "user",
      JSON.stringify(storedUser),
    );

    currentUser.value = storedUser;

    successMessage.value =
      uploadedPhotos.length > 0
        ? "Pet and photos were added successfully."
        : "Pet was added successfully.";

    revokeAllPreviewUrls();
    selectedPhotos.value = [];

    window.setTimeout(() => {
      router.push("/dashboard");
    }, 800);
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to add pet.";
  } finally {
    isSubmitting.value = false;
  }
}

function cancel(): void {
  router.push("/dashboard");
}
</script>

<template>
  <main class="add-pet-page">
    <form
      class="add-pet-card"
      @submit.prevent="handleSubmit"
    >
      <header class="page-header">
        <div>
          <p class="section-label">
            Pet Management
          </p>

          <h1>Add a New Pet</h1>

          <p>
            Enter the pet profile, medical information,
            location, and photos.
          </p>
        </div>

        <button
          type="button"
          class="cancel-button"
          :disabled="isSubmitting"
          @click="cancel"
        >
          Cancel
        </button>
      </header>

      <section class="form-section">
        <div class="form-section-heading">
          <span>01</span>

          <div>
            <h2>Basic Information</h2>
            <p>
              General information about the pet.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="name">
              Name
            </label>

            <input
              id="name"
              v-model.trim="form.name"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="species">
              Species
            </label>

            <input
              id="species"
              v-model.trim="form.species"
              type="text"
              placeholder="Cat, Dog, Rabbit..."
              required
            />
          </div>

          <div class="form-group">
            <label for="breed">
              Breed
            </label>

            <input
              id="breed"
              v-model.trim="form.breed"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="sex">
              Sex
            </label>

            <select
              id="sex"
              v-model="form.sex"
            >
              <option value="">
                Select sex
              </option>

              <option value="Male">
                Male
              </option>

              <option value="Female">
                Female
              </option>

              <option value="Unknown">
                Unknown
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="dateOfBirth">
              Date of Birth
            </label>

            <input
              id="dateOfBirth"
              v-model="form.dateOfBirth"
              type="date"
            />
          </div>

          <div class="form-group">
            <label for="color">
              Color
            </label>

            <input
              id="color"
              v-model.trim="form.color"
              type="text"
            />
          </div>

          <div class="form-group">
            <label for="size">
              Size
            </label>

            <select
              id="size"
              v-model="form.size"
            >
              <option value="">
                Select size
              </option>

              <option value="Small">
                Small
              </option>

              <option value="Middle">
                Middle
              </option>

              <option value="Large">
                Large
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="weight">
              Weight
            </label>

            <input
              id="weight"
              v-model.number="form.weight"
              type="number"
              min="0"
              step="0.01"
            />
          </div>

          <div class="form-group">
            <label for="friendlyLevel">
              Friendly Level
            </label>

            <select
              id="friendlyLevel"
              v-model.number="
                form.friendlyLevel
              "
            >
              <option :value="null">
                Select level
              </option>

              <option :value="1">
                1
              </option>

              <option :value="2">
                2
              </option>

              <option :value="3">
                3
              </option>

              <option :value="4">
                4
              </option>

              <option :value="5">
                5
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="microchipNumber">
              Microchip Number
            </label>

            <input
              id="microchipNumber"
              v-model.trim="
                form.microchipNumber
              "
              type="text"
            />
          </div>

          <div class="checkbox-group">
            <input
              id="isTrained"
              v-model="form.isTrained"
              type="checkbox"
            />

            <label for="isTrained">
              This pet is trained
            </label>
          </div>

          <div class="form-group full-width">
            <label for="description">
              Description
            </label>

            <textarea
              id="description"
              v-model.trim="
                form.description
              "
              rows="5"
              placeholder="Describe the pet's personality and behavior."
            />
          </div>
        </div>
      </section>

      <section class="form-section">
        <div class="form-section-heading">
          <span>02</span>

          <div>
            <h2>Address</h2>

            <p>
              Current location of the pet.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="country">
              Country
            </label>

            <input
              id="country"
              v-model.trim="form.country"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="state">
              State
            </label>

            <input
              id="state"
              v-model.trim="form.state"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="city">
              City
            </label>

            <input
              id="city"
              v-model.trim="form.city"
              type="text"
              required
            />
          </div>

          <div class="form-group">
            <label for="zipCode">
              ZIP Code
            </label>

            <input
              id="zipCode"
              v-model.trim="form.zipCode"
              type="text"
            />
          </div>

          <div class="form-group full-width">
            <label for="street">
              Street
            </label>

            <input
              id="street"
              v-model.trim="form.street"
              type="text"
            />
          </div>
        </div>
      </section>

      <section class="form-section">
        <div class="form-section-heading">
          <span>03</span>

          <div>
            <h2>Medical Record</h2>

            <p>
              Health and care information.
            </p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="vaccination">
              Vaccination
            </label>

            <textarea
              id="vaccination"
              v-model.trim="
                form.vaccination
              "
              rows="3"
            />
          </div>

          <div class="form-group">
            <label for="allergies">
              Allergies
            </label>

            <textarea
              id="allergies"
              v-model.trim="
                form.allergies
              "
              rows="3"
            />
          </div>

          <div class="form-group">
            <label for="medications">
              Medications
            </label>

            <textarea
              id="medications"
              v-model.trim="
                form.medications
              "
              rows="3"
            />
          </div>

          <div class="form-group">
            <label for="surgeries">
              Surgeries
            </label>

            <textarea
              id="surgeries"
              v-model.trim="
                form.surgeries
              "
              rows="3"
            />
          </div>

          <div class="form-group">
            <label for="labResults">
              Lab Results
            </label>

            <textarea
              id="labResults"
              v-model.trim="
                form.labResults
              "
              rows="3"
            />
          </div>

          <div class="form-group">
            <label for="imagingResults">
              Imaging Results
            </label>

            <textarea
              id="imagingResults"
              v-model.trim="
                form.imagingResults
              "
              rows="3"
            />
          </div>

          <div class="form-group full-width">
            <label for="specialCare">
              Special Care
            </label>

            <textarea
              id="specialCare"
              v-model.trim="
                form.specialCare
              "
              rows="3"
            />
          </div>

          <div class="form-group full-width">
            <label for="note">
              Medical Note
            </label>

            <textarea
              id="note"
              v-model.trim="form.note"
              rows="5"
            />
          </div>

          <div class="checkbox-group">
            <input
              id="spayedNeutered"
              v-model="
                form.spayedNeutered
              "
              type="checkbox"
            />

            <label for="spayedNeutered">
              Spayed or neutered
            </label>
          </div>
        </div>
      </section>

      <section class="form-section">
        <div class="form-section-heading">
          <span>04</span>

          <div>
            <h2>Photos</h2>

            <p>
              Upload up to 10 JPEG, PNG, or WebP photos.
            </p>
          </div>

          <strong class="photo-limit">
            {{ selectedPhotos.length }} / 10
          </strong>
        </div>

        <label
          class="photo-upload-area"
          :class="{
            disabled:
              selectedPhotos.length >= 10,
          }"
        >
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            multiple
            :disabled="
              selectedPhotos.length >= 10
            "
            @change="
              handlePhotoSelection
            "
          />

          <span class="upload-symbol">
            +
          </span>

          <strong>
            Select pet photos
          </strong>

          <small>
            You may select multiple images.
          </small>
        </label>

        <div
          v-if="selectedPhotos.length"
          class="photo-preview-grid"
        >
          <article
            v-for="photo in selectedPhotos"
            :key="photo.id"
            class="photo-preview-card"
          >
            <div class="photo-preview-image">
              <img
                :src="photo.previewUrl"
                :alt="photo.file.name"
              />

              <button
                type="button"
                aria-label="Remove photo"
                @click="
                  removePhoto(photo.id)
                "
              >
                Remove
              </button>
            </div>

            <input
              v-model.trim="
                photo.description
              "
              type="text"
              placeholder="Photo description"
            />

            <small>
              {{ photo.file.name }}
            </small>
          </article>
        </div>
      </section>

      <p
        v-if="errorMessage"
        class="message error-message"
      >
        {{ errorMessage }}
      </p>

      <p
        v-if="successMessage"
        class="message success-message"
      >
        {{ successMessage }}
      </p>

      <footer class="form-actions">
        <button
          type="button"
          class="cancel-button"
          :disabled="isSubmitting"
          @click="cancel"
        >
          Cancel
        </button>

        <button
          type="submit"
          class="submit-button"
          :disabled="isSubmitting"
        >
          {{
            isSubmitting
              ? "Creating Pet..."
              : "Create Pet"
          }}
        </button>
      </footer>
    </form>
  </main>
</template>

<style scoped>
.add-pet-page {
  min-height: calc(100vh - 70px);
  padding: 32px 20px;
  background-color: #f4f7fb;
}

.add-pet-card {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.page-header,
.form-section-heading,
.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.page-header {
  padding-bottom: 28px;
  border-bottom: 1px solid #e2e8f0;
}

.section-label {
  margin: 0 0 7px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
}

.page-header p {
  margin: 9px 0 0;
  color: #64748b;
}

.form-section {
  margin-top: 28px;
  padding: 25px;
  background-color: #f8fafc;
  border: 1px solid #e5eaf0;
  border-radius: 14px;
}

.form-section-heading {
  justify-content: flex-start;
  margin-bottom: 22px;
}

.form-section-heading > span {
  display: flex;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #1d4ed8;
  font-weight: 800;
  background-color: #dbeafe;
  border-radius: 11px;
}

.form-section-heading h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.form-section-heading p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 13px;
}

.photo-limit {
  margin-left: auto;
  color: #2563eb;
}

.form-grid {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: 19px;
}

.form-group {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
}

.full-width {
  grid-column: 1 / -1;
}

label {
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

input,
select,
textarea {
  box-sizing: border-box;
  width: 100%;
  padding: 12px 13px;
  color: #0f172a;
  font: inherit;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  outline: none;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #2563eb;
  box-shadow:
    0 0 0 3px
    rgba(37, 99, 235, 0.11);
}

textarea {
  resize: vertical;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 9px;
}

.checkbox-group input {
  width: 17px;
  height: 17px;
}

.photo-upload-area {
  display: flex;
  min-height: 165px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 24px;
  color: #475569;
  text-align: center;
  background-color: #ffffff;
  border: 2px dashed #94a3b8;
  border-radius: 13px;
  cursor: pointer;
}

.photo-upload-area:hover {
  color: #1d4ed8;
  border-color: #2563eb;
  background-color: #eff6ff;
}

.photo-upload-area.disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.photo-upload-area input {
  display: none;
}

.upload-symbol {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 11px;
  color: #2563eb;
  font-size: 28px;
  background-color: #dbeafe;
  border-radius: 14px;
}

.photo-upload-area small {
  margin-top: 6px;
  color: #64748b;
}

.photo-preview-grid {
  display: grid;
  grid-template-columns:
    repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.photo-preview-card {
  min-width: 0;
  padding: 11px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.photo-preview-image {
  position: relative;
  overflow: hidden;
  border-radius: 9px;
}

.photo-preview-image img {
  display: block;
  width: 100%;
  height: 190px;
  object-fit: cover;
}

.photo-preview-image button {
  position: absolute;
  top: 9px;
  right: 9px;
  padding: 7px 10px;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  background-color:
    rgba(185, 28, 28, 0.9);
  border: 0;
  border-radius: 7px;
  cursor: pointer;
}

.photo-preview-card input {
  margin-top: 10px;
}

.photo-preview-card small {
  display: block;
  overflow: hidden;
  margin-top: 8px;
  color: #64748b;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message {
  margin: 24px 0 0;
  padding: 13px 15px;
  border-radius: 9px;
}

.error-message {
  color: #b91c1c;
  background-color: #fee2e2;
}

.success-message {
  color: #166534;
  background-color: #dcfce7;
}

.form-actions {
  justify-content: flex-end;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.cancel-button,
.submit-button {
  padding: 11px 19px;
  font-weight: 700;
  border-radius: 9px;
  cursor: pointer;
}

.cancel-button {
  color: #334155;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
}

.submit-button {
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
}

.submit-button:hover:not(:disabled) {
  background-color: #1d4ed8;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

@media (max-width: 800px) {
  .page-header,
  .form-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid,
  .photo-preview-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }

  .form-actions button {
    width: 100%;
  }
}

@media (max-width: 560px) {
  .add-pet-page {
    padding: 14px;
  }

  .add-pet-card {
    padding: 20px;
  }

  .form-section {
    padding: 18px;
  }
}
</style>