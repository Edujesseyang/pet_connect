<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from "vue";

import type { User } from "../../types/api";

interface ProfilePhotoUploadData {
  profilePhotoUrl?: string;
  url?: string;
}

interface ProfilePhotoUploadResponse {
  state?: boolean;
  message?: string;
  data?: ProfilePhotoUploadData | string;
  profilePhotoUrl?: string;
  url?: string;
}

const props = defineProps<{
  user: User | null;
}>();

const emit = defineEmits<{
  "user-updated": [user: User];
}>();

const selectedPhoto = ref<File | null>(null);
const previewUrl = ref("");
const isUploading = ref(false);
const uploadError = ref("");
const uploadSuccess = ref("");

const displayName = computed(() => {
  return props.user?.fullname || props.user?.username || "User";
});

const userInitial = computed(() => {
  return displayName.value.charAt(0).toUpperCase();
});

const currentProfilePhotoUrl = computed(() => {
  return props.user?.userProfile?.profilePhotoUrl || "";
});

const displayedPhotoUrl = computed(() => {
  return previewUrl.value || currentProfilePhotoUrl.value;
});

const formattedDateOfBirth = computed(() => {
  const dateValue = props.user?.userProfile?.dateOfBirth;

  if (!dateValue) {
    return "Not provided";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "Not provided";
  }

  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
});

const formattedAddress = computed(() => {
  const address = props.user?.address;

  if (!address) {
    return "Not provided";
  }

  const cityStateZip = [address.city, address.state, address.zipCode]
    .filter(Boolean)
    .join(", ");

  return [address.street, cityStateZip, address.country]
    .filter(Boolean)
    .join(", ");
});

watch(
  () => props.user?.userProfile?.profilePhotoUrl,
  () => {
    if (!selectedPhoto.value) {
      clearPreview();
    }
  },
);

onBeforeUnmount(() => {
  clearPreview();
});

function getProfilePhotoUploadUrl(userId: number): string {
  return `http://localhost:8080/photos/upload/users/${userId}?userId=${userId}`;
}

function handlePhotoSelection(event: Event): void {
  uploadError.value = "";
  uploadSuccess.value = "";

  const input = event.target as HTMLInputElement;

  const file = input.files?.[0];

  if (!file) {
    return;
  }

  const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

  if (!allowedTypes.includes(file.type)) {
    uploadError.value = "Only JPEG, PNG, and WebP images are allowed.";

    input.value = "";
    return;
  }

  const maximumSize = 10 * 1024 * 1024;

  if (file.size > maximumSize) {
    uploadError.value = "The profile photo cannot exceed 10 MB.";

    input.value = "";
    return;
  }

  clearPreview();

  selectedPhoto.value = file;
  previewUrl.value = URL.createObjectURL(file);

  input.value = "";
}

function clearPreview(): void {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);

    previewUrl.value = "";
  }
}

function cancelSelectedPhoto(): void {
  selectedPhoto.value = null;
  uploadError.value = "";
  uploadSuccess.value = "";

  clearPreview();
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

function extractPhotoUrl(responseBody: unknown): string | null {
  if (typeof responseBody === "string" && responseBody.trim()) {
    return responseBody;
  }

  if (typeof responseBody !== "object" || responseBody === null) {
    return null;
  }

  const response = responseBody as ProfilePhotoUploadResponse;

  if (typeof response.profilePhotoUrl === "string") {
    return response.profilePhotoUrl;
  }

  if (typeof response.url === "string") {
    return response.url;
  }

  if (typeof response.data === "string") {
    return response.data;
  }

  if (typeof response.data === "object" && response.data !== null) {
    if (typeof response.data.profilePhotoUrl === "string") {
      return response.data.profilePhotoUrl;
    }

    if (typeof response.data.url === "string") {
      return response.data.url;
    }
  }

  return null;
}

function getStoredUser(): User {
  const storedUser = localStorage.getItem("user");

  if (!storedUser) {
    throw new Error("User is not logged in.");
  }

  const user = JSON.parse(storedUser) as User;

  if (!user.userId) {
    throw new Error("User ID is missing.");
  }

  return user;
}

function updateStoredProfilePhoto(profilePhotoUrl: string): User {
  const storedUser = getStoredUser();

  if (!storedUser.userProfile) {
    throw new Error("User profile is unavailable.");
  }

  storedUser.userProfile.profilePhotoUrl = profilePhotoUrl;

  localStorage.setItem("user", JSON.stringify(storedUser));

  return storedUser;
}

async function uploadProfilePhoto(): Promise<void> {
  uploadError.value = "";
  uploadSuccess.value = "";

  if (!selectedPhoto.value) {
    uploadError.value = "Please select a profile photo.";
    return;
  }

  let storedUser: User;

  try {
    storedUser = getStoredUser();
  } catch (error) {
    uploadError.value =
      error instanceof Error
        ? error.message
        : "Unable to read the current user.";

    return;
  }

  const formData = new FormData();
  formData.append("file", selectedPhoto.value, selectedPhoto.value.name);
  try {
    isUploading.value = true;

    const response = await fetch(
      `http://localhost:8080/photos/upload/users?userId=${storedUser.userId}`,
      {
        method: "POST",
        body: formData,
      },
    );

    const responseBody = await readResponseBody(response);

    if (!response.ok) {
      throw new Error(
        getResponseMessage(responseBody, "Unable to upload profile photo."),
      );
    }

    const profilePhotoUrl = extractPhotoUrl(responseBody);

    if (!profilePhotoUrl) {
      throw new Error(
        "The photo was uploaded, but the server did not return a photo URL.",
      );
    }

    const updatedUser = updateStoredProfilePhoto(profilePhotoUrl);

    emit("user-updated", updatedUser);

    uploadSuccess.value = "Profile photo updated successfully.";

    selectedPhoto.value = null;
    clearPreview();
  } catch (error) {
    uploadError.value =
      error instanceof Error
        ? error.message
        : "Unable to upload profile photo.";
  } finally {
    isUploading.value = false;
  }
}

function editProfile(): void {
  /*
   * TODO:
   * Open the edit profile component (编辑资料组件)
   * or navigate to the edit profile route (编辑资料路由).
   */
}

function changePassword(): void {
  /*
   * TODO:
   * Open the change password component (修改密码组件)
   * or navigate to the change password route (修改密码路由).
   */
}
</script>

<template>
  <section class="profile-page">
    <header class="profile-header">
      <div>
        <p class="section-label">Account Management</p>

        <h2>My Profile</h2>

        <p class="section-description">
          Review your account, personal profile, contact information, and
          address.
        </p>
      </div>

      <div class="header-actions">
        <button type="button" class="danger-button" @click="changePassword">
          Change Password
        </button>

        <button type="button" class="primary-button" @click="editProfile">
          Edit Profile
        </button>
      </div>
    </header>

    <section class="profile-summary-card">
      <div class="profile-avatar">
        <img
          v-if="displayedPhotoUrl"
          :src="displayedPhotoUrl"
          :alt="displayName"
        />

        <span v-else>
          {{ userInitial }}
        </span>
      </div>

      <div class="profile-summary-content">
        <p class="section-label">Account Owner</p>

        <h3>{{ displayName }}</h3>

        <p class="username">@{{ user?.username || "user" }}</p>

        <div class="summary-tags">
          <span>
            {{ user?.role || "No role" }}
          </span>

          <span>
            {{ user?.userProfile?.householdType || "Household not provided" }}
          </span>

          <span>
            {{ user?.userProfile?.adoptionExp || "Experience not provided" }}
          </span>
        </div>
      </div>
    </section>

    <section class="photo-upload-card">
      <div class="upload-heading">
        <div>
          <p class="section-label">Profile Photo</p>

          <h3>Update Avatar</h3>

          <p>
            Select a JPEG, PNG, or WebP image. The maximum file size is 10 MB.
          </p>
        </div>
      </div>

      <div class="upload-content">
        <label class="file-selector">
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            :disabled="isUploading"
            @change="handlePhotoSelection"
          />

          <span>Select Photo</span>
        </label>

        <div v-if="selectedPhoto" class="selected-file">
          <div>
            <strong>
              {{ selectedPhoto.name }}
            </strong>

            <span>
              {{ (selectedPhoto.size / 1024 / 1024).toFixed(2) }}
              MB
            </span>
          </div>

          <button
            type="button"
            class="text-button"
            :disabled="isUploading"
            @click="cancelSelectedPhoto"
          >
            Remove
          </button>
        </div>

        <button
          type="button"
          class="upload-button"
          :disabled="!selectedPhoto || isUploading"
          @click="uploadProfilePhoto"
        >
          {{ isUploading ? "Uploading..." : "Upload Profile Photo" }}
        </button>
      </div>

      <p v-if="uploadError" class="message error-message">
        {{ uploadError }}
      </p>

      <p v-if="uploadSuccess" class="message success-message">
        {{ uploadSuccess }}
      </p>
    </section>

    <section class="information-card">
      <div class="card-heading">
        <div>
          <p class="section-label">Account</p>

          <h3>Account Information</h3>
        </div>
      </div>

      <div class="profile-grid">
        <div class="profile-field">
          <span>User ID</span>

          <strong>
            {{ user?.userId || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Username</span>

          <strong>
            {{ user?.username || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Full Name</span>

          <strong>
            {{ user?.fullname || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Email</span>

          <strong>
            {{ user?.email || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Role</span>

          <strong>
            {{ user?.role || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Published Posts</span>

          <strong>
            {{ user?.posts?.length ?? 0 }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Owned Pets</span>

          <strong>
            {{ user?.ownedPets?.length ?? 0 }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Saved Posts</span>

          <strong>
            {{ user?.savedPosts?.length ?? 0 }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Applied Posts</span>

          <strong>
            {{ user?.appliedPosts?.length ?? 0 }}
          </strong>
        </div>
      </div>
    </section>

    <section class="information-card">
      <div class="card-heading">
        <div>
          <p class="section-label">Personal</p>

          <h3>Personal Information</h3>
        </div>
      </div>

      <div class="profile-grid">
        <div class="profile-field">
          <span>Gender</span>

          <strong>
            {{ user?.userProfile?.gender || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Date of Birth</span>

          <strong>
            {{ formattedDateOfBirth }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Phone Number</span>

          <strong>
            {{ user?.userProfile?.phoneNumber || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Household Type</span>

          <strong>
            {{ user?.userProfile?.householdType || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Adoption Experience</span>

          <strong>
            {{ user?.userProfile?.adoptionExp || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Social Media</span>

          <a
            v-if="user?.userProfile?.socialMediaLinks"
            :href="user.userProfile.socialMediaLinks"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ user.userProfile.socialMediaLinks }}
          </a>

          <strong v-else> Not provided </strong>
        </div>

        <div class="profile-field profile-field-wide">
          <span>Bio</span>

          <strong class="long-text">
            {{ user?.userProfile?.bio || "No biography has been provided." }}
          </strong>
        </div>
      </div>
    </section>

    <section class="information-card">
      <div class="card-heading">
        <div>
          <p class="section-label">Location</p>

          <h3>Address Information</h3>
        </div>
      </div>

      <div class="profile-grid">
        <div class="profile-field">
          <span>Address ID</span>

          <strong>
            {{ user?.address?.addressId || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Country</span>

          <strong>
            {{ user?.address?.country || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>State</span>

          <strong>
            {{ user?.address?.state || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>City</span>

          <strong>
            {{ user?.address?.city || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>Street</span>

          <strong>
            {{ user?.address?.street || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field">
          <span>ZIP Code</span>

          <strong>
            {{ user?.address?.zipCode || "Not provided" }}
          </strong>
        </div>

        <div class="profile-field profile-field-wide">
          <span>Full Address</span>

          <strong>
            {{ formattedAddress }}
          </strong>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.profile-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 26px;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.section-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.profile-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
}

.section-description {
  margin: 9px 0 0;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.primary-button,
.danger-button,
.upload-button,
.text-button {
  font: inherit;
  font-weight: 700;
  border-radius: 9px;
  cursor: pointer;
}

.primary-button {
  padding: 11px 17px;
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
}

.primary-button:hover {
  background-color: #1d4ed8;
}

.danger-button {
  padding: 11px 17px;
  color: #b91c1c;
  background-color: #ffffff;
  border: 1px solid #fca5a5;
}

.danger-button:hover {
  background-color: #fef2f2;
}

.profile-summary-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px;
  color: #ffffff;
  background: linear-gradient(120deg, #1d4ed8, #2563eb 55%, #0ea5e9);
  border-radius: 16px;
  box-shadow: 0 14px 34px rgba(37, 99, 235, 0.2);
}

.profile-avatar {
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
  border: 4px solid rgba(255, 255, 255, 0.42);
  border-radius: 50%;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-summary-card .section-label {
  color: #bfdbfe;
}

.profile-summary-content h3 {
  margin: 0;
  font-size: 30px;
}

.username {
  margin: 7px 0 0;
  color: #dbeafe;
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin-top: 18px;
}

.summary-tags span {
  padding: 7px 11px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
}

.photo-upload-card,
.information-card {
  padding: 24px;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.04);
}

.upload-heading h3,
.card-heading h3 {
  margin: 0;
  color: #0f172a;
}

.upload-heading p:last-child {
  margin: 7px 0 0;
  color: #64748b;
}

.upload-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 20px;
}

.file-selector {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 11px 17px;
  color: #1d4ed8;
  font-weight: 700;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 9px;
  cursor: pointer;
}

.file-selector:hover {
  background-color: #dbeafe;
}

.file-selector input {
  display: none;
}

.selected-file {
  display: flex;
  min-width: 260px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 11px 14px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
}

.selected-file div {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.selected-file strong {
  overflow: hidden;
  color: #1e293b;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-file span {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.text-button {
  padding: 0;
  color: #b91c1c;
  background: transparent;
  border: 0;
}

.upload-button {
  padding: 11px 17px;
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
}

.upload-button:hover:not(:disabled) {
  background-color: #1d4ed8;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.message {
  margin: 18px 0 0;
  padding: 12px 14px;
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

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
}

.profile-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: 17px;
  background-color: #f8fafc;
  border: 1px solid #e8edf3;
  border-radius: 10px;
}

.profile-field-wide {
  grid-column: 1 / -1;
}

.profile-field span {
  margin-bottom: 7px;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.profile-field strong,
.profile-field a {
  overflow-wrap: anywhere;
  color: #1e293b;
  line-height: 1.5;
}

.profile-field a {
  color: #2563eb;
  text-decoration: none;
}

.profile-field a:hover {
  text-decoration: underline;
}

.long-text {
  white-space: pre-wrap;
}

@media (max-width: 800px) {
  .profile-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions button {
    flex: 1;
  }

  .profile-summary-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-field-wide {
    grid-column: auto;
  }
}

@media (max-width: 560px) {
  .profile-header,
  .photo-upload-card,
  .information-card {
    padding: 18px;
  }

  .header-actions {
    flex-direction: column;
  }

  .header-actions button {
    width: 100%;
  }

  .profile-avatar {
    width: 110px;
    height: 110px;
    flex-basis: 110px;
  }

  .upload-content {
    align-items: stretch;
    flex-direction: column;
  }

  .file-selector,
  .upload-button {
    width: 100%;
    box-sizing: border-box;
  }

  .selected-file {
    min-width: 0;
  }
}
</style>
