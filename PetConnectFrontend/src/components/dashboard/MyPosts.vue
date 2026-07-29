<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";

import type { ApiResponse, Post, User } from "../../types/api";

type PostType = "rehoming" | "adoption" | "breeder";

type AddressMode = "user" | "custom";

interface CreatePostRequest {
  userId: number;
  petId: number;
  title: string;
  content: string;
  type: PostType;
  adoptionFee: number | null;
  country: string;
  state: string;
  city: string;
  street: string;
  zipcode: string;
  created_at: string;
}

const props = defineProps<{
  user: User | null;
}>();

const posts = ref<Post[]>([]);
const isLoading = ref(false);
const errorMessage = ref("");

const isCreateFormOpen = ref(false);
const isSubmitting = ref(false);
const createErrorMessage = ref("");
const createSuccessMessage = ref("");

const DEFAULT_PET_IMAGE = "https://placehold.co/320x220?text=No+Pet+Photo";

const storedUser = ref<User | null>(null);

const createForm = reactive({
  petId: 0,
  title: "",
  content: "",
  type: "rehoming" as PostType,
  adoptionFee: null as number | null,
  addressMode: "user" as AddressMode,
  country: "",
  state: "",
  city: "",
  street: "",
  zipcode: "",
});

const currentUser = computed(() => {
  return storedUser.value ?? props.user;
});

const ownedPets = computed(() => {
  return currentUser.value?.ownedPets ?? [];
});

const userAddress = computed(() => {
  return currentUser.value?.address ?? null;
});

const selectedPet = computed(() => {
  return ownedPets.value.find((pet) => pet.petId === createForm.petId) ?? null;
});
const emit = defineEmits<{
  "posts-count-updated": [count: number];
}>();

onMounted(() => {
  loadStoredUser();
  initializeCreateForm();
  fetchMyPosts();
});

function loadStoredUser(): void {
  const storedUserText = localStorage.getItem("user");

  if (!storedUserText) {
    storedUser.value = props.user;
    return;
  }

  try {
    storedUser.value = JSON.parse(storedUserText) as User;
  } catch {
    storedUser.value = props.user;
  }
}

function initializeCreateForm(): void {
  const firstPet = ownedPets.value[0];

  if (firstPet) {
    createForm.petId = firstPet.petId;
  }

  applyUserAddressToForm();
}

function getCurrentUserId(): number {
  const userId = currentUser.value?.userId;

  if (!userId) {
    throw new Error("Unable to identify the current user.");
  }

  return userId;
}

function applyUserAddressToForm(): void {
  const address = userAddress.value;

  createForm.country = address?.country ?? "";

  createForm.state = address?.state ?? "";

  createForm.city = address?.city ?? "";

  createForm.street = address?.street ?? "";

  createForm.zipcode = address?.zipCode ?? "";
}

function clearAddressForm(): void {
  createForm.country = "";
  createForm.state = "";
  createForm.city = "";
  createForm.street = "";
  createForm.zipcode = "";
}

function handleAddressModeChange(): void {
  if (createForm.addressMode === "user") {
    applyUserAddressToForm();
  } else {
    clearAddressForm();
  }
}

function toggleCreateForm(): void {
  isCreateFormOpen.value = !isCreateFormOpen.value;

  createErrorMessage.value = "";
  createSuccessMessage.value = "";

  if (isCreateFormOpen.value) {
    initializeCreateForm();
  }
}

function closeCreateForm(): void {
  isCreateFormOpen.value = false;
  createErrorMessage.value = "";
  createSuccessMessage.value = "";
}

function resetCreateForm(): void {
  createForm.petId = ownedPets.value[0]?.petId ?? 0;

  createForm.title = "";
  createForm.content = "";
  createForm.type = "rehoming";
  createForm.adoptionFee = null;
  createForm.addressMode = "user";

  applyUserAddressToForm();
}

function formatPostType(type: string): string {
  if (!type) {
    return "Unknown";
  }

  return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
}

function formatDate(dateValue: string | null | undefined): string {
  if (!dateValue) {
    return "Date unavailable";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "Date unavailable";
  }

  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

function formatAddress(post: Post): string {
  const address = post.pickupLocation;

  if (!address) {
    return "Location unavailable";
  }

  return [
    address.street,
    [address.city, address.state, address.zipCode].filter(Boolean).join(", "),
    address.country,
  ]
    .filter(Boolean)
    .join(" · ");
}

function handleImageError(event: Event): void {
  const image = event.target as HTMLImageElement;

  image.onerror = null;
  image.src = DEFAULT_PET_IMAGE;
}

async function readResponseBody(response: Response): Promise<unknown> {
  const contentType = response.headers.get("content-type");

  if (contentType?.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();

  return text || null;
}

function getResponseMessage(responseBody: unknown, fallback: string): string {
  if (
    typeof responseBody === "object" &&
    responseBody !== null &&
    "message" in responseBody &&
    typeof responseBody.message === "string"
  ) {
    return responseBody.message;
  }

  if (typeof responseBody === "string" && responseBody.trim()) {
    return responseBody;
  }

  return fallback;
}

async function fetchMyPosts(): Promise<void> {
  try {
    isLoading.value = true;
    errorMessage.value = "";

    const userId = getCurrentUserId();

    const response = await fetch(
      `http://localhost:8080/post/get_my_post/${userId}?userId=${userId}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
      },
    );

    const responseBody = (await readResponseBody(response)) as ApiResponse<
      Post[]
    > | null;

    if (!response.ok) {
      throw new Error(
        getResponseMessage(responseBody, "Unable to load your posts."),
      );
    }

    if (
      responseBody &&
      "state" in responseBody &&
      responseBody.state === false
    ) {
      throw new Error(responseBody.message || "Unable to load your posts.");
    }

    posts.value = responseBody?.data ?? [];

    emit("posts-count-updated", posts.value.length);
  } catch (error) {
    posts.value = [];

    errorMessage.value =
      error instanceof Error ? error.message : "Unable to load your posts.";
  } finally {
    isLoading.value = false;
  }
}

function validateCreateForm(): void {
  if (!createForm.petId) {
    throw new Error("Please select a pet.");
  }

  if (!createForm.title.trim()) {
    throw new Error("Title is required.");
  }

  if (!createForm.content.trim()) {
    throw new Error("Content is required.");
  }

  if (
    !createForm.country.trim() ||
    !createForm.state.trim() ||
    !createForm.city.trim() ||
    !createForm.street.trim() ||
    !createForm.zipcode.trim()
  ) {
    throw new Error("A complete pickup address is required.");
  }

  if (createForm.adoptionFee !== null && createForm.adoptionFee < 0) {
    throw new Error("Adoption fee cannot be negative.");
  }
}

async function createPost(): Promise<void> {
  createErrorMessage.value = "";
  createSuccessMessage.value = "";

  try {
    validateCreateForm();

    isSubmitting.value = true;

    const userId = getCurrentUserId();

    const requestBody: CreatePostRequest = {
      userId,
      petId: createForm.petId,
      title: createForm.title.trim(),
      content: createForm.content.trim(),
      type: createForm.type,
      adoptionFee: createForm.adoptionFee,
      country: createForm.country.trim(),
      state: createForm.state.trim(),
      city: createForm.city.trim(),
      street: createForm.street.trim(),
      zipcode: createForm.zipcode.trim(),
      created_at: new Date().toISOString(),
    };

    const response = await fetch("http://localhost:8080/post/create_post", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestBody),
    });

    const responseBody = await readResponseBody(response);

    if (!response.ok) {
      throw new Error(
        getResponseMessage(responseBody, "Unable to create the post."),
      );
    }

    if (
      typeof responseBody === "object" &&
      responseBody !== null &&
      "state" in responseBody &&
      responseBody.state === false
    ) {
      throw new Error(
        getResponseMessage(responseBody, "Unable to create the post."),
      );
    }

    createSuccessMessage.value = "Post created successfully.";

    resetCreateForm();

    await fetchMyPosts();

    window.setTimeout(() => {
      isCreateFormOpen.value = false;
      createSuccessMessage.value = "";
    }, 800);
  } catch (error) {
    createErrorMessage.value =
      error instanceof Error ? error.message : "Unable to create the post.";
  } finally {
    isSubmitting.value = false;
  }
}

function optOutOfPost(post: Post): void {
  /*
   * TODO:
   * Call the opt-out, close-post, archive-post,
   * or delete-post endpoint.
   *
   * Possible values:
   * - post.postId
   * - post.petId
   * - currentUser.value?.userId
   */

  console.log("Opt out clicked:", post);
}
</script>

<template>
  <section class="content-card">
    <div class="section-heading">
      <div>
        <p class="section-label">Content Management</p>

        <h2>My Posts</h2>

        <span class="section-description">
          Create and manage your published pet listings.
        </span>
      </div>

      <button
        type="button"
        class="create-post-button"
        @click="toggleCreateForm"
      >
        {{ isCreateFormOpen ? "Close Form" : "Create Post" }}
      </button>
    </div>

    <form
      v-if="isCreateFormOpen"
      class="create-post-form"
      @submit.prevent="createPost"
    >
      <div class="form-heading">
        <div>
          <p class="section-label">New Listing</p>

          <h3>Create New Post</h3>

          <p>Select one of your pets and provide the listing information.</p>
        </div>

        <span v-if="selectedPet" class="selected-pet-label">
          Selected:
          {{ selectedPet.name }}
        </span>
      </div>

      <div class="form-section">
        <h4>Post Information</h4>

        <div class="form-grid">
          <label class="form-field">
            <span>Pet</span>

            <select v-model.number="createForm.petId" required>
              <option :value="0" disabled>Select a pet</option>

              <option
                v-for="pet in ownedPets"
                :key="pet.petId"
                :value="pet.petId"
              >
                {{ pet.name }}
                ·
                {{ pet.species }}
                ·
                {{ pet.breed }}
              </option>
            </select>
          </label>

          <label class="form-field">
            <span>Post Type</span>

            <select v-model="createForm.type" required>
              <option value="rehoming">Rehoming</option>

              <option value="adoption">Adoption</option>

              <option value="breeder">Breeder</option>
            </select>
          </label>

          <label class="form-field form-field-wide">
            <span>Title</span>

            <input
              v-model="createForm.title"
              type="text"
              maxlength="500"
              placeholder="Enter a clear post title"
              required
            />
          </label>

          <label class="form-field form-field-wide">
            <span>Content</span>

            <textarea
              v-model="createForm.content"
              rows="5"
              maxlength="5000"
              placeholder="Describe the pet, circumstances, requirements, and next steps"
              required
            ></textarea>
          </label>

          <label class="form-field">
            <span> Adoption Fee </span>

            <input
              v-model.number="createForm.adoptionFee"
              type="number"
              min="0"
              step="0.01"
              placeholder="0.00"
            />
          </label>
        </div>
      </div>

      <div class="form-section">
        <div class="address-section-heading">
          <div>
            <h4>Pickup Address</h4>

            <p>
              Use your account address or provide a different pickup location.
            </p>
          </div>

          <div class="address-options">
            <label>
              <input
                v-model="createForm.addressMode"
                type="radio"
                value="user"
                @change="handleAddressModeChange"
              />

              Use My Address
            </label>

            <label>
              <input
                v-model="createForm.addressMode"
                type="radio"
                value="custom"
                @change="handleAddressModeChange"
              />

              New Address
            </label>
          </div>
        </div>

        <div
          v-if="createForm.addressMode === 'user'"
          class="saved-address-preview"
        >
          <strong> Account Address </strong>

          <p>
            {{ userAddress?.street || "Street unavailable" }},
            {{ userAddress?.city || "City unavailable" }},
            {{ userAddress?.state || "State unavailable" }}
            {{ userAddress?.zipCode || "" }},
            {{ userAddress?.country || "Country unavailable" }}
          </p>
        </div>

        <div class="form-grid address-grid">
          <label class="form-field">
            <span>Country</span>

            <input
              v-model="createForm.country"
              type="text"
              maxlength="30"
              required
            />
          </label>

          <label class="form-field">
            <span>State</span>

            <input
              v-model="createForm.state"
              type="text"
              maxlength="30"
              required
            />
          </label>

          <label class="form-field">
            <span>City</span>

            <input
              v-model="createForm.city"
              type="text"
              maxlength="100"
              required
            />
          </label>

          <label class="form-field">
            <span>ZIP Code</span>

            <input
              v-model="createForm.zipcode"
              type="text"
              maxlength="30"
              required
            />
          </label>

          <label class="form-field form-field-wide">
            <span>Street</span>

            <input
              v-model="createForm.street"
              type="text"
              maxlength="100"
              required
            />
          </label>
        </div>
      </div>

      <p v-if="createErrorMessage" class="form-message error-message">
        {{ createErrorMessage }}
      </p>

      <p v-if="createSuccessMessage" class="form-message success-message">
        {{ createSuccessMessage }}
      </p>

      <div class="form-actions">
        <button
          type="button"
          class="cancel-button"
          :disabled="isSubmitting"
          @click="closeCreateForm"
        >
          Cancel
        </button>

        <button
          type="submit"
          class="submit-button"
          :disabled="isSubmitting || !ownedPets.length"
        >
          {{ isSubmitting ? "Publishing..." : "Publish Post" }}
        </button>
      </div>

      <p v-if="!ownedPets.length" class="no-pets-message">
        You must add a pet before creating a post.
      </p>
    </form>

    <p v-if="errorMessage" class="error-message list-error">
      {{ errorMessage }}
    </p>

    <div v-if="isLoading" class="empty-state">
      <div class="loading-spinner"></div>

      <p>Loading your posts...</p>
    </div>

    <div v-else-if="posts.length" class="post-list">
      <article
        v-for="post in posts"
        :key="post.postId"
        class="post-row"
        :data-post-id="post.postId"
        :data-pet-id="post.petId"
      >
        <div class="post-image-wrapper">
          <img
            class="post-image"
            :src="post.firstPhotoUrl || DEFAULT_PET_IMAGE"
            :alt="post.title"
            @error="handleImageError"
          />

          <span class="post-type" :class="`post-type-${post.type}`">
            {{ formatPostType(post.type) }}
          </span>
        </div>

        <div class="post-information">
          <h3>
            {{ post.title }}
          </h3>

          <div class="content-block">
            <span>Content</span>

            <p>
              {{ post.content || "No content provided." }}
            </p>
          </div>

          <div class="post-meta">
            <div>
              <span>Location</span>

              <strong>
                {{ formatAddress(post) }}
              </strong>
            </div>

            <div>
              <span>Posted</span>

              <strong>
                {{ formatDate(post.createdAt) }}
              </strong>
            </div>

            <div>
              <span>Fee</span>

              <strong>
                {{
                  post.adoptionFee != null
                    ? `$${post.adoptionFee}`
                    : "Not specified"
                }}
              </strong>
            </div>
          </div>
        </div>

        <div class="post-actions">
          <button
            type="button"
            class="opt-out-button"
            @click="optOutOfPost(post)"
          >
            Opt Out
          </button>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <h3>No published posts yet</h3>

      <p>Create a post to publish one of your pets to the community.</p>
    </div>
  </section>
</template>

<style scoped>
.content-card {
  padding: 24px;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 14px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.section-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

.section-heading h2 {
  margin: 0;
  color: #0f172a;
}

.section-description {
  display: block;
  margin-top: 8px;
  color: #64748b;
}

.create-post-button {
  flex-shrink: 0;
  padding: 11px 17px;
  color: #ffffff;
  font: inherit;
  font-weight: 700;
  background-color: #2563eb;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
}

.create-post-button:hover {
  background-color: #1d4ed8;
}

.create-post-form {
  margin-top: 24px;
  padding: 24px;
  background-color: #f8fafc;
  border: 1px solid #dbe2ea;
  border-radius: 14px;
}

.form-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.form-heading h3 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.form-heading p:last-child {
  margin: 7px 0 0;
  color: #64748b;
}

.selected-pet-label {
  padding: 8px 12px;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  background-color: #dbeafe;
  border-radius: 999px;
}

.form-section {
  margin-top: 24px;
  padding: 20px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.form-section h4 {
  margin: 0 0 16px;
  color: #0f172a;
  font-size: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.form-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 7px;
}

.form-field-wide {
  grid-column: 1 / -1;
}

.form-field > span {
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

.form-field input,
.form-field select,
.form-field textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  color: #0f172a;
  font: inherit;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  outline: none;
}

.form-field input:focus,
.form-field select:focus,
.form-field textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-field textarea {
  resize: vertical;
}

.address-section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.address-section-heading h4 {
  margin-bottom: 5px;
}

.address-section-heading p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.address-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.address-options label {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 12px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  background-color: #f8fafc;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
  cursor: pointer;
}

.saved-address-preview {
  margin: 17px 0;
  padding: 14px;
  color: #334155;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 9px;
}

.saved-address-preview p {
  margin: 5px 0 0;
  color: #475569;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 11px;
  margin-top: 20px;
}

.cancel-button,
.submit-button {
  padding: 11px 18px;
  font: inherit;
  font-weight: 700;
  border-radius: 9px;
  cursor: pointer;
}

.cancel-button {
  color: #475569;
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
  opacity: 0.6;
}

.form-message,
.list-error {
  margin-top: 18px;
  padding: 12px 14px;
  border-radius: 9px;
}

.error-message {
  color: #b91c1c;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
}

.success-message {
  color: #166534;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.no-pets-message {
  margin: 14px 0 0;
  color: #b45309;
  text-align: right;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.post-row {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr) 120px;
  gap: 22px;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}
.post-row:hover {
  scale: 100.3%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.post-image-wrapper {
  position: relative;
  min-height: 190px;
  overflow: hidden;
  background-color: #e2e8f0;
}



.post-image {
  width: 100%;
  height: 100%;
  min-height: 190px;
  object-fit: cover;
}

.post-type {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 10px;
  color: #1e3a8a;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
  background-color: rgba(219, 234, 254, 0.94);
  border-radius: 999px;
}

.post-type-adoption {
  color: #166534;
  background-color: rgba(220, 252, 231, 0.94);
}

.post-type-rehoming {
  color: #92400e;
  background-color: rgba(254, 243, 199, 0.94);
}

.post-type-breeder {
  color: #6b21a8;
  background-color: rgba(243, 232, 255, 0.94);
}

.post-information {
  min-width: 0;
  padding: 22px 0;
}

.post-information h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.content-block {
  margin-top: 14px;
}


.content-block > span,
.post-meta span {
  display: block;
  margin-bottom: 5px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.content-block p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 28px;
  margin-top: 20px;
}

.post-meta strong {
  color: #334155;
  font-size: 13px;
}

.post-actions {
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.opt-out-button {
  width: 100%;
  padding: 11px 14px;
  color: #b91c1c;
  font: inherit;
  font-weight: 700;
  background-color: #ffffff;
  border: 1px solid #fca5a5;
  border-radius: 9px;
  cursor: pointer;
}

.opt-out-button:hover {
  background-color: #fef2f2;
}

.empty-state {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  margin-top: 24px;
  padding: 40px;
  color: #64748b;
  text-align: center;
  background-color: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

.empty-state h3 {
  margin: 0;
  color: #0f172a;
}

.empty-state p {
  margin-bottom: 0;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 4px solid #dbeafe;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .post-row {
    grid-template-columns: 170px minmax(0, 1fr);
  }

  .post-actions {
    grid-column: 1 / -1;
    padding: 0 18px 18px;
  }

  .opt-out-button {
    width: auto;
    min-width: 130px;
    margin-left: auto;
  }
}

@media (max-width: 700px) {
  .section-heading,
  .form-heading,
  .address-section-heading {
    flex-direction: column;
  }

  .create-post-button {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-field-wide {
    grid-column: auto;
  }

  .address-options {
    width: 100%;
  }

  .address-options label {
    flex: 1;
    justify-content: center;
  }

  .post-row {
    grid-template-columns: 1fr;
  }

  .post-image-wrapper,
  .post-image {
    min-height: 230px;
    max-height: 280px;
  }

  .post-information {
    padding: 4px 18px 0;
  }

  .post-actions {
    grid-column: auto;
  }

  .opt-out-button {
    width: 100%;
  }
}
</style>
