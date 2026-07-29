<script setup lang="ts">
import { ref } from "vue";

import type { Photo } from "../../types/api";

const props = defineProps<{
  petId: number;
  uploaderId: number;
}>();

const emit = defineEmits<{
  uploaded: [Photo];
}>();

const file = ref<File | null>(null);
const previewUrl = ref("");
const description = ref("");

const isUploading = ref(false);
const errorMessage = ref("");

function selectFile(event: Event): void {
  const input = event.target as HTMLInputElement;

  const selected = input.files?.[0];

  if (!selected) {
    return;
  }

  file.value = selected;

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }

  previewUrl.value = URL.createObjectURL(selected);
}

async function uploadPhoto(): Promise<void> {
  errorMessage.value = "";

  if (!file.value) {
    errorMessage.value = "Please select an image.";
    return;
  }

  const formData = new FormData();

  formData.append("file", file.value);
  formData.append("petId", String(props.petId));
  formData.append("uploaderId", String(props.uploaderId));
  formData.append("description", description.value);

  try {
    isUploading.value = true;

    const response = await fetch(
      "http://localhost:8080/photos/upload/pet",
      {
        method: "POST",
        body: formData,
      },
    );

    if (!response.ok) {
      throw new Error("Upload failed.");
    }

    const uploadedPhoto =
      (await response.json()) as Photo;

    emit("uploaded", uploadedPhoto);

    file.value = null;
    description.value = "";

    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value);
      previewUrl.value = "";
    }
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Upload failed.";
  } finally {
    isUploading.value = false;
  }
}
</script>

<template>
  <div class="upload-card">
    <h3>Upload Pet Photo</h3>

    <input
      type="file"
      accept="image/png,image/jpeg,image/webp"
      @change="selectFile"
    />

    <img
      v-if="previewUrl"
      :src="previewUrl"
      class="preview-image"
    />

    <textarea
      v-model="description"
      placeholder="Photo description..."
      rows="3"
    />

    <p
      v-if="errorMessage"
      class="error-message"
    >
      {{ errorMessage }}
    </p>

    <button
      :disabled="isUploading"
      @click="uploadPhoto"
    >
      {{
        isUploading
          ? "Uploading..."
          : "Upload"
      }}
    </button>
  </div>
</template>

<style scoped>
.upload-card {
  display: flex;
  flex-direction: column;
  gap: 14px;

  margin-top: 24px;
  padding: 20px;

  background: white;

  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.upload-card h3 {
  margin: 0;
}

.preview-image {
  width: 240px;
  height: 240px;

  object-fit: cover;

  border-radius: 10px;
  border: 1px solid #d1d5db;
}

textarea {
  padding: 12px;

  font: inherit;

  border: 1px solid #cbd5e1;
  border-radius: 8px;

  resize: vertical;
}

button {
  align-self: flex-start;

  padding: 10px 18px;

  color: white;

  background: #2563eb;

  border: none;
  border-radius: 8px;

  cursor: pointer;
}

button:hover:not(:disabled) {
  background: #1d4ed8;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #dc2626;
}
</style>