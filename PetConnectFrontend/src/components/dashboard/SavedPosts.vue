<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

import type {
  ApiResponse,
  Post,
  User,
} from "../../types/api";

defineProps<{
  user: User | null;
}>();

const router = useRouter();

const savedPosts = ref<Post[]>([]);
const isLoading = ref(false);

const errorMessage = ref("");
const successMessage = ref("");

const unsavingPostId = ref<number | null>(null);
const applyingPostId = ref<number | null>(null);

const DEFAULT_PET_IMAGE =
  "https://placehold.co/320x220?text=No+Pet+Photo";

onMounted(() => {
  void fetchSavedPosts();
});

function getStoredUser(): User {
  const storedUserText = localStorage.getItem("user");

  if (!storedUserText) {
    throw new Error("User is not logged in.");
  }

  let storedUser: User;

  try {
    storedUser = JSON.parse(storedUserText) as User;
  } catch {
    throw new Error("Unable to read the stored user.");
  }

  if (!storedUser.userId) {
    throw new Error("User ID is missing.");
  }

  return storedUser;
}

async function readResponseBody(
  response: Response,
): Promise<unknown> {
  const contentType =
    response.headers.get("content-type");

  if (
    contentType?.includes("application/json")
  ) {
    return response.json();
  }

  const text = await response.text();

  return text || null;
}

function getResponseMessage(
  responseBody: unknown,
  fallback: string,
): string {
  if (
    typeof responseBody === "object" &&
    responseBody !== null &&
    "message" in responseBody &&
    typeof responseBody.message === "string"
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

function responseStateIsFalse(
  responseBody: unknown,
): boolean {
  return (
    typeof responseBody === "object" &&
    responseBody !== null &&
    "state" in responseBody &&
    responseBody.state === false
  );
}

async function fetchSavedPosts(): Promise<void> {
  try {
    isLoading.value = true;
    errorMessage.value = "";
    successMessage.value = "";

    const storedUser = getStoredUser();
    const userId = storedUser.userId;

    const response = await fetch(
      `http://localhost:8080/post/get_saved_post/${userId}?userId=${userId}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
      },
    );

    const responseBody =
      (await readResponseBody(
        response,
      )) as ApiResponse<Post[]> | null;

    if (!response.ok) {
      throw new Error(
        getResponseMessage(
          responseBody,
          "Unable to load saved posts.",
        ),
      );
    }

    if (responseBody?.state === false) {
      throw new Error(
        responseBody.message ||
          "Unable to load saved posts.",
      );
    }

    savedPosts.value = responseBody?.data ?? [];
  } catch (error) {
    savedPosts.value = [];

    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to load saved posts.";
  } finally {
    isLoading.value = false;
  }
}

async function viewPet(
  petId: number,
): Promise<void> {
  await router.push({
    name: "pet-detail",
    params: {
      petId: String(petId),
    },
  });
}

async function applyPost(
  post: Post,
): Promise<void> {
  if (
    applyingPostId.value !== null ||
    unsavingPostId.value !== null
  ) {
    return;
  }

  try {
    errorMessage.value = "";
    successMessage.value = "";
    applyingPostId.value = post.postId;

    const storedUser = getStoredUser();

    const currentUserId = storedUser.userId;
    const postOwnerId = post.userId;
    const postId = post.postId;
    const petId = post.petId;

    if (!currentUserId) {
      throw new Error(
        "Current user ID is missing.",
      );
    }

    if (!postOwnerId) {
      throw new Error(
        "Post owner ID is missing.",
      );
    }

    if (!postId) {
      throw new Error(
        "Post ID is missing.",
      );
    }

    if (!petId) {
      throw new Error(
        "Pet ID is missing.",
      );
    }

    if (currentUserId === postOwnerId) {
      throw new Error(
        "You cannot apply to your own post.",
      );
    }

    const applyEndpoint =
      `http://localhost:8080/post/apply_post/` +
      `${postId}/${currentUserId}` +
      `?post_id=${postId}` +
      `&user_id=${currentUserId}`;

    const applyResponse = await fetch(
      applyEndpoint,
      {
        method: "POST",
        headers: {
          Accept: "*/*",
        },
      },
    );

    const applyResponseBody =
      await readResponseBody(applyResponse);

    if (!applyResponse.ok) {
      throw new Error(
        getResponseMessage(
          applyResponseBody,
          "Unable to apply to this post.",
        ),
      );
    }

    if (
      responseStateIsFalse(
        applyResponseBody,
      )
    ) {
      throw new Error(
        getResponseMessage(
          applyResponseBody,
          "Unable to apply to this post.",
        ),
      );
    }

    const conversationEndpoint =
      `http://localhost:8080/message/begin_conversation/` +
      `${currentUserId}/${postOwnerId}/${petId}` +
      `?from_uid=${currentUserId}` +
      `&to_uid=${postOwnerId}` +
      `&pet_id=${petId}`;

    const conversationResponse = await fetch(
      conversationEndpoint,
      {
        method: "POST",
        headers: {
          Accept: "*/*",
        },
      },
    );

    const conversationResponseBody =
      await readResponseBody(
        conversationResponse,
      );

    if (!conversationResponse.ok) {
      throw new Error(
        getResponseMessage(
          conversationResponseBody,
          "The application was created, but the conversation could not be created.",
        ),
      );
    }

    if (
      responseStateIsFalse(
        conversationResponseBody,
      )
    ) {
      throw new Error(
        getResponseMessage(
          conversationResponseBody,
          "The application was created, but the conversation could not be created.",
        ),
      );
    }

    const unsaveEndpoint =
      `http://localhost:8080/post/unsave_post/` +
      `${currentUserId}/${postId}` +
      `?user_id=${currentUserId}` +
      `&post_id=${postId}`;

    const unsaveResponse = await fetch(
      unsaveEndpoint,
      {
        method: "POST",
        headers: {
          Accept: "*/*",
        },
      },
    );

    const unsaveResponseBody =
      await readResponseBody(
        unsaveResponse,
      );

    if (!unsaveResponse.ok) {
      throw new Error(
        getResponseMessage(
          unsaveResponseBody,
          "The conversation was created, but the post could not be removed from saved posts.",
        ),
      );
    }

    if (
      responseStateIsFalse(
        unsaveResponseBody,
      )
    ) {
      throw new Error(
        getResponseMessage(
          unsaveResponseBody,
          "The conversation was created, but the post could not be removed from saved posts.",
        ),
      );
    }

    updateStoredSavedPosts(postId);

    savedPosts.value =
      savedPosts.value.filter(
        (savedPost) =>
          savedPost.postId !== postId,
      );

    successMessage.value =
      "Conversation created successfully. You can now go to Conversations to contact the post author.";
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to apply to this post.";
  } finally {
    applyingPostId.value = null;
  }
}

async function unsavePost(
  post: Post,
): Promise<void> {
  if (
    unsavingPostId.value !== null ||
    applyingPostId.value !== null
  ) {
    return;
  }

  try {
    errorMessage.value = "";
    successMessage.value = "";
    unsavingPostId.value = post.postId;

    const storedUser = getStoredUser();

    const userId = storedUser.userId;
    const postId = post.postId;

    const response = await fetch(
      `http://localhost:8080/post/unsave_post/${userId}/${postId}?user_id=${userId}&post_id=${postId}`,
      {
        method: "POST",
        headers: {
          Accept: "application/json",
        },
      },
    );

    const responseBody =
      await readResponseBody(response);

    if (!response.ok) {
      throw new Error(
        getResponseMessage(
          responseBody,
          "Unable to unsave this post.",
        ),
      );
    }

    if (
      responseStateIsFalse(
        responseBody,
      )
    ) {
      throw new Error(
        getResponseMessage(
          responseBody,
          "Unable to unsave this post.",
        ),
      );
    }

    updateStoredSavedPosts(postId);

    savedPosts.value =
      savedPosts.value.filter(
        (savedPost) =>
          savedPost.postId !== postId,
      );

    successMessage.value =
      "Post removed from saved posts.";
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to unsave this post.";
  } finally {
    unsavingPostId.value = null;
  }
}

function updateStoredSavedPosts(
  postId: number,
): void {
  try {
    const storedUser = getStoredUser();

    storedUser.savedPosts =
      storedUser.savedPosts?.filter(
        (post) => post.postId !== postId,
      ) ?? [];

    localStorage.setItem(
      "user",
      JSON.stringify(storedUser),
    );
  } catch {
    /*
     * The backend operation already succeeded.
     * A localStorage update failure should not
     * prevent the page from updating.
     */
  }
}

function formatPostType(
  type: string,
): string {
  if (!type) {
    return "Unknown";
  }

  return (
    type.charAt(0).toUpperCase() +
    type.slice(1).toLowerCase()
  );
}

function formatDate(
  dateValue:
    | string
    | null
    | undefined,
): string {
  if (!dateValue) {
    return "Date unavailable";
  }

  const date = new Date(dateValue);

  if (
    Number.isNaN(
      date.getTime(),
    )
  ) {
    return "Date unavailable";
  }

  return date.toLocaleString(
    "en-US",
    {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
    },
  );
}

function formatAddress(
  post: Post,
): string {
  const address = post.pickupLocation;

  if (!address) {
    return "Location unavailable";
  }

  const cityStateZip = [
    address.city,
    address.state,
    address.zipCode,
  ]
    .filter(Boolean)
    .join(", ");

  return [
    address.street,
    cityStateZip,
    address.country,
  ]
    .filter(Boolean)
    .join(" · ");
}

function handleImageError(
  event: Event,
): void {
  const image =
    event.target as HTMLImageElement;

  image.onerror = null;
  image.src = DEFAULT_PET_IMAGE;
}
</script>

<template>
  <section class="content-card">
    <div class="section-heading">
      <div>
        <p class="section-label">
          Saved Listings
        </p>

        <h2>Saved Posts</h2>

        <span class="section-description">
          Review the pet listings you saved for
          later.
        </span>
      </div>

      <button
        type="button"
        class="refresh-button"
        :disabled="
          isLoading ||
          applyingPostId !== null ||
          unsavingPostId !== null
        "
        @click="fetchSavedPosts"
      >
        {{
          isLoading
            ? "Loading..."
            : "Refresh"
        }}
      </button>
    </div>

    <div
      v-if="errorMessage"
      class="message error-message"
      role="alert"
    >
      <strong>Operation failed</strong>

      <span>{{ errorMessage }}</span>
    </div>

    <div
      v-if="successMessage"
      class="message success-message"
      role="status"
    >
      <div class="success-icon">
        ✓
      </div>

      <div class="success-content">
        <strong>Conversation created</strong>

        <span>{{ successMessage }}</span>
      </div>
    </div>

    <div
      v-if="isLoading"
      class="empty-state"
    >
      <div class="loading-spinner"></div>

      <p>Loading saved posts...</p>
    </div>

    <div
      v-else-if="savedPosts.length"
      class="post-list"
    >
      <article
        v-for="post in savedPosts"
        :key="post.postId"
        class="post-row"
        :data-post-id="post.postId"
        :data-pet-id="post.petId"
        :data-owner-id="post.userId"
      >
        <span class="visually-hidden">
          Post ID:
          {{ post.postId }}
        </span>

        <span class="visually-hidden">
          Pet ID:
          {{ post.petId }}
        </span>

        <span class="visually-hidden">
          Owner ID:
          {{ post.userId }}
        </span>

        <div class="post-image-wrapper">
          <img
            class="post-image"
            :src="
              post.firstPhotoUrl ||
              DEFAULT_PET_IMAGE
            "
            :alt="post.title"
            @error="handleImageError"
          />

          <span
            class="post-type"
            :class="`post-type-${post.type}`"
          >
            {{
              formatPostType(
                post.type,
              )
            }}
          </span>
        </div>

        <div class="post-information">
          <h3>
            {{ post.title }}
          </h3>

          <div class="content-block">
            <span>Content</span>

            <p>
              {{
                post.content ||
                "No content provided."
              }}
            </p>
          </div>

          <div class="post-meta">
            <div class="meta-item">
              <span>Location</span>

              <strong>
                {{
                  formatAddress(
                    post,
                  )
                }}
              </strong>
            </div>

            <div class="meta-item">
              <span>Posted</span>

              <strong>
                {{
                  formatDate(
                    post.createdAt,
                  )
                }}
              </strong>
            </div>

            <div class="meta-item">
              <span>Adoption Fee</span>

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
            class="view-pet-button"
            :disabled="
              applyingPostId !== null ||
              unsavingPostId !== null
            "
            @click="viewPet(post.petId)"
          >
            View Pet
          </button>

          <button
            type="button"
            class="apply-button"
            :disabled="
              applyingPostId !== null ||
              unsavingPostId !== null
            "
            @click="applyPost(post)"
          >
            {{
              applyingPostId === post.postId
                ? "Applying..."
                : "Apply"
            }}
          </button>

          <button
            type="button"
            class="unsave-button"
            :disabled="
              applyingPostId !== null ||
              unsavingPostId !== null
            "
            @click="unsavePost(post)"
          >
            {{
              unsavingPostId === post.postId
                ? "Removing..."
                : "Unsave Post"
            }}
          </button>
        </div>
      </article>
    </div>

    <div
      v-else
      class="empty-state"
    >
      <h3>No saved posts yet</h3>

      <p>
        Posts you save from the community page
        will appear here.
      </p>
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

.refresh-button {
  flex-shrink: 0;
  padding: 10px 16px;
  color: #1d4ed8;
  font: inherit;
  font-weight: 700;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  cursor: pointer;
}

.refresh-button:hover:not(:disabled) {
  background-color: #dbeafe;
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin: 20px 0 0;
  padding: 14px 16px;
  border-radius: 10px;
}

.message strong,
.message span {
  display: block;
}

.message strong {
  margin-bottom: 3px;
}

.error-message {
  flex-direction: column;
  color: #b91c1c;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
}

.success-message {
  color: #166534;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.success-icon {
  display: flex;
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 900;
  background-color: #16a34a;
  border-radius: 50%;
}

.success-content {
  min-width: 0;
}

.success-content span {
  color: #15803d;
  line-height: 1.5;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.post-row {
  position: relative;
  display: grid;
  grid-template-columns:
    190px minmax(0, 1fr) 145px;
  gap: 22px;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.post-row:hover {
  border-color: #bfdbfe;
  box-shadow:
    0 10px 28px
    rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.post-image-wrapper {
  position: relative;
  min-height: 220px;
  overflow: hidden;
  background-color: #e2e8f0;
}

.post-image {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 220px;
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
  letter-spacing: 0.04em;
  text-transform: uppercase;
  background-color:
    rgba(219, 234, 254, 0.94);
  border-radius: 999px;
}

.post-type-adoption {
  color: #166534;
  background-color:
    rgba(220, 252, 231, 0.94);
}

.post-type-rehoming {
  color: #92400e;
  background-color:
    rgba(254, 243, 199, 0.94);
}

.post-type-breeder {
  color: #6b21a8;
  background-color:
    rgba(243, 232, 255, 0.94);
}

.post-information {
  min-width: 0;
  padding: 22px 0;
}

.post-information h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.35;
}

.content-block {
  margin-top: 14px;
}

.content-block > span,
.meta-item > span {
  display: block;
  margin-bottom: 5px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.content-block p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #64748b;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 28px;
  margin-top: 20px;
}

.meta-item {
  min-width: 120px;
}

.meta-item strong {
  color: #334155;
  font-size: 13px;
  line-height: 1.45;
}

.post-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 22px 22px 22px 0;
}

.view-pet-button,
.apply-button,
.unsave-button {
  width: 100%;
  padding: 11px 14px;
  font: inherit;
  font-weight: 800;
  border-radius: 9px;
  cursor: pointer;
}

.view-pet-button {
  color: #1d4ed8;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
}

.view-pet-button:hover:not(:disabled) {
  background-color: #dbeafe;
}

.apply-button {
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
}

.apply-button:hover:not(:disabled) {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.unsave-button {
  color: #ffffff;
  background-color: #dc2626;
  border: 1px solid #dc2626;
}

.unsave-button:hover:not(:disabled) {
  background-color: #b91c1c;
  border-color: #b91c1c;
}

.view-pet-button:disabled,
.apply-button:disabled,
.unsave-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  padding: 0;
  margin: -1px;
  white-space: nowrap;
  border: 0;
  clip: rect(0, 0, 0, 0);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .post-row {
    grid-template-columns:
      170px minmax(0, 1fr);
  }

  .post-actions {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns:
      repeat(3, minmax(0, 1fr));
    padding: 0 18px 18px;
  }
}

@media (max-width: 650px) {
  .section-heading {
    flex-direction: column;
  }

  .refresh-button {
    width: 100%;
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
    grid-template-columns: 1fr;
    padding: 0 18px 18px;
  }
}
</style>