<script setup lang="ts">
import {
  computed,
  onMounted,
  ref,
  watch,
} from "vue";
import { useRouter } from "vue-router";

import type {
  ApiResponse,
  Post,
  User,
} from "../../types/api";

type PostTypeFilter =
  | "all"
  | "rehoming"
  | "breeder"
  | "adoption";

interface StoredUser {
  userId?: number;
  id?: number;
}

const props = defineProps<{
  user: User | null;
}>();

const router = useRouter();

const searchKeyword = ref("");
const activeSearchKeyword = ref("");
const selectedType = ref<PostTypeFilter>("all");

const allPosts = ref<Post[]>([]);
const isLoading = ref(false);
const errorMessage = ref("");

const savingPostId = ref<number | null>(null);
const savedPostIds = ref<Set<number>>(new Set());

const currentPage = ref(1);

const POSTS_PER_PAGE = 20;

const SAVED_POST_STORAGE_PREFIX =
  "community-saved-post-ids";

const DEFAULT_PET_IMAGE =
  "https://placehold.co/300x220?text=No+Pet+Photo";

const postTypeFilters: Array<{
  label: string;
  value: PostTypeFilter;
}> = [
  {
    label: "All",
    value: "all",
  },
  {
    label: "Rehoming",
    value: "rehoming",
  },
  {
    label: "Breeder",
    value: "breeder",
  },
  {
    label: "Adoption",
    value: "adoption",
  },
];

const currentUserId = computed(() => {
  if (props.user?.userId) {
    return props.user.userId;
  }

  const storedUser = getStoredUser();

  return storedUser?.userId ?? storedUser?.id ?? 0;
});

const filteredPosts = computed(() => {
  if (selectedType.value === "all") {
    return allPosts.value;
  }

  return allPosts.value.filter((post) => {
    return (
      post.type?.trim().toLowerCase() ===
      selectedType.value
    );
  });
});

const totalPages = computed(() => {
  return Math.max(
    1,
    Math.ceil(
      filteredPosts.value.length /
        POSTS_PER_PAGE,
    ),
  );
});

const paginatedPosts = computed(() => {
  const startIndex =
    (currentPage.value - 1) *
    POSTS_PER_PAGE;

  const endIndex =
    startIndex + POSTS_PER_PAGE;

  return filteredPosts.value.slice(
    startIndex,
    endIndex,
  );
});

const visiblePageNumbers = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const maximumVisiblePages = 5;

  if (total <= maximumVisiblePages) {
    return Array.from(
      {
        length: total,
      },
      (_, index) => index + 1,
    );
  }

  let startPage = Math.max(
    1,
    current - 2,
  );

  const endPage = Math.min(
    total,
    startPage +
      maximumVisiblePages -
      1,
  );

  if (
    endPage - startPage + 1 <
    maximumVisiblePages
  ) {
    startPage = Math.max(
      1,
      endPage -
        maximumVisiblePages +
        1,
    );
  }

  return Array.from(
    {
      length:
        endPage - startPage + 1,
    },
    (_, index) => startPage + index,
  );
});

const firstVisiblePage = computed(() => {
  return visiblePageNumbers.value[0] ?? 1;
});

const lastVisiblePage = computed(() => {
  return (
    visiblePageNumbers.value[
      visiblePageNumbers.value.length - 1
    ] ?? 1
  );
});

const displayedRange = computed(() => {
  if (filteredPosts.value.length === 0) {
    return {
      start: 0,
      end: 0,
    };
  }

  const start =
    (currentPage.value - 1) *
      POSTS_PER_PAGE +
    1;

  const end = Math.min(
    currentPage.value *
      POSTS_PER_PAGE,
    filteredPosts.value.length,
  );

  return {
    start,
    end,
  };
});

watch(selectedType, () => {
  currentPage.value = 1;
});

watch(totalPages, (newTotalPages) => {
  if (currentPage.value > newTotalPages) {
    currentPage.value =
      newTotalPages;
  }
});

watch(currentUserId, () => {
  loadSavedPostIds();
});

onMounted(() => {
  loadSavedPostIds();
  void fetchAllPosts();
});

function getStoredUser(): StoredUser | null {
  const storedUserText =
    localStorage.getItem("user");

  if (!storedUserText) {
    return null;
  }

  try {
    return JSON.parse(
      storedUserText,
    ) as StoredUser;
  } catch {
    return null;
  }
}

function getSavedPostStorageKey(): string {
  return `${SAVED_POST_STORAGE_PREFIX}-${currentUserId.value}`;
}

function loadSavedPostIds(): void {
  if (currentUserId.value <= 0) {
    savedPostIds.value = new Set();

    return;
  }

  const storedValue =
    localStorage.getItem(
      getSavedPostStorageKey(),
    );

  if (!storedValue) {
    savedPostIds.value = new Set();

    return;
  }

  try {
    const parsedValue =
      JSON.parse(storedValue) as unknown;

    if (!Array.isArray(parsedValue)) {
      savedPostIds.value = new Set();

      return;
    }

    const validPostIds =
      parsedValue.filter(
        (
          value,
        ): value is number => {
          return (
            typeof value === "number" &&
            Number.isInteger(value) &&
            value > 0
          );
        },
      );

    savedPostIds.value =
      new Set(validPostIds);
  } catch {
    savedPostIds.value = new Set();
  }
}

function saveSavedPostIds(): void {
  if (currentUserId.value <= 0) {
    return;
  }

  localStorage.setItem(
    getSavedPostStorageKey(),
    JSON.stringify([
      ...savedPostIds.value,
    ]),
  );
}

function isPostSaved(
  postId: number,
): boolean {
  return savedPostIds.value.has(postId);
}

function markPostAsSaved(
  postId: number,
): void {
  const updatedPostIds =
    new Set(savedPostIds.value);

  updatedPostIds.add(postId);

  savedPostIds.value =
    updatedPostIds;

  saveSavedPostIds();
}

async function readPostsResponse(
  response: Response,
): Promise<ApiResponse<Post[]>> {
  const responseText =
    await response.text();

  if (!responseText) {
    throw new Error(
      `The server returned an empty response. Status: ${response.status}`,
    );
  }

  try {
    return JSON.parse(
      responseText,
    ) as ApiResponse<Post[]>;
  } catch {
    throw new Error(
      "The server returned an invalid JSON response.",
    );
  }
}

async function handleSearch(): Promise<void> {
  const keyword =
    searchKeyword.value.trim();

  searchKeyword.value = keyword;
  currentPage.value = 1;

  if (!keyword) {
    activeSearchKeyword.value = "";

    await fetchAllPosts();

    return;
  }

  await searchPosts(keyword);
}

async function searchPosts(
  keyword: string,
): Promise<void> {
  const normalizedKeyword =
    keyword.trim();

  if (!normalizedKeyword) {
    activeSearchKeyword.value = "";

    await fetchAllPosts();

    return;
  }

  try {
    isLoading.value = true;
    errorMessage.value = "";

    const encodedKeyword =
      encodeURIComponent(
        normalizedKeyword,
      );

    const endpoint =
      `http://localhost:8080/post/search/` +
      `${encodedKeyword}` +
      `?keyword=${encodedKeyword}`;

    const response = await fetch(
      endpoint,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
        cache: "no-store",
      },
    );

    const responseData =
      await readPostsResponse(response);

    if (
      !response.ok ||
      !responseData.state
    ) {
      throw new Error(
        responseData.message ||
          `Unable to search posts. Status: ${response.status}`,
      );
    }

    allPosts.value =
      responseData.data ?? [];

    activeSearchKeyword.value =
      normalizedKeyword;

    currentPage.value = 1;
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to search posts.";

    allPosts.value = [];
    currentPage.value = 1;
  } finally {
    isLoading.value = false;
  }
}

async function clearFilters(): Promise<void> {
  selectedType.value = "all";
  searchKeyword.value = "";
  activeSearchKeyword.value = "";
  currentPage.value = 1;

  await fetchAllPosts();
}

function selectPostType(
  type: PostTypeFilter,
): void {
  selectedType.value = type;
  currentPage.value = 1;
}

function goToPage(page: number): void {
  if (
    page < 1 ||
    page > totalPages.value
  ) {
    return;
  }

  currentPage.value = page;

  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
}

function goToPreviousPage(): void {
  goToPage(
    currentPage.value - 1,
  );
}

function goToNextPage(): void {
  goToPage(
    currentPage.value + 1,
  );
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
    Number.isNaN(date.getTime())
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

function formatLocation(
  post: Post,
): string {
  const address =
    post.pickupLocation;

  if (!address) {
    return "Location unavailable";
  }

  const cityState = [
    address.city,
    address.state,
  ]
    .filter(Boolean)
    .join(", ");

  return [
    cityState,
    address.zipCode,
  ]
    .filter(Boolean)
    .join(" ");
}

function handleImageError(
  event: Event,
): void {
  const image =
    event.target as HTMLImageElement;

  image.onerror = null;
  image.src = DEFAULT_PET_IMAGE;
}

async function fetchAllPosts(): Promise<void> {
  try {
    isLoading.value = true;
    errorMessage.value = "";

    const response = await fetch(
      "http://localhost:8080/post/get_all_post",
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
        cache: "no-store",
      },
    );

    const responseData =
      await readPostsResponse(response);

    if (
      !response.ok ||
      !responseData.state
    ) {
      throw new Error(
        responseData.message ||
          `Unable to load posts. Status: ${response.status}`,
      );
    }

    allPosts.value =
      responseData.data ?? [];

    activeSearchKeyword.value = "";
    currentPage.value = 1;
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to load posts.";

    allPosts.value = [];
    currentPage.value = 1;
  } finally {
    isLoading.value = false;
  }
}

async function refreshPosts(): Promise<void> {
  if (activeSearchKeyword.value) {
    await searchPosts(
      activeSearchKeyword.value,
    );

    return;
  }

  await fetchAllPosts();
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

async function savePost(
  post: Post,
): Promise<void> {
  const userId =
    currentUserId.value;

  if (!userId) {
    errorMessage.value =
      "You must be logged in to save a post.";

    return;
  }

  if (
    savingPostId.value !== null ||
    isPostSaved(post.postId)
  ) {
    return;
  }

  try {
    savingPostId.value =
      post.postId;

    errorMessage.value = "";

    const endpoint =
      `http://localhost:8080/post/save_post/` +
      `${post.postId}/${userId}` +
      `?post_id=${post.postId}` +
      `&user_id=${userId}`;

    const response = await fetch(
      endpoint,
      {
        method: "POST",
        headers: {
          Accept: "*/*",
        },
        cache: "no-store",
      },
    );

    const responseText =
      await response.text();

    let responseData:
      | ApiResponse<unknown>
      | null = null;

    if (responseText) {
      try {
        responseData =
          JSON.parse(
            responseText,
          ) as ApiResponse<unknown>;
      } catch {
        responseData = null;
      }
    }

    if (!response.ok) {
      throw new Error(
        responseData?.message ||
          `Unable to save post. Status: ${response.status}`,
      );
    }

    if (
      responseData?.state === false
    ) {
      throw new Error(
        responseData.message ||
          "Unable to save post.",
      );
    }

    markPostAsSaved(
      post.postId,
    );
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Unable to save post.";
  } finally {
    savingPostId.value = null;
  }
}
</script>

<template>
  <div class="community-posts">
    <form
      class="search-box"
      @submit.prevent="handleSearch"
    >
      <input
        v-model="searchKeyword"
        type="search"
        placeholder="Search by species, breed, title, content, type, pet ID, or location..."
        :disabled="isLoading"
      />

      <button
        type="submit"
        :disabled="isLoading"
      >
        {{
          isLoading
            ? "Searching..."
            : "Search"
        }}
      </button>
    </form>

    <section class="filter-bar">
      <div class="filter-heading">
        <div>
          <p class="filter-label">
            Post Type
          </p>

          <p class="filter-description">
            Filter community posts by listing type.
          </p>
        </div>

        <button
          v-if="
            selectedType !== 'all' ||
            activeSearchKeyword
          "
          type="button"
          class="clear-filter-button"
          :disabled="isLoading"
          @click="clearFilters"
        >
          Clear Filters
        </button>
      </div>

      <div class="filter-options">
        <button
          v-for="filter in postTypeFilters"
          :key="filter.value"
          type="button"
          class="filter-button"
          :class="{
            active:
              selectedType ===
              filter.value,
          }"
          :disabled="isLoading"
          @click="
            selectPostType(
              filter.value,
            )
          "
        >
          {{ filter.label }}

          <span>
            {{
              filter.value === "all"
                ? allPosts.length
                : allPosts.filter(
                    (post) =>
                      post.type
                        ?.toLowerCase() ===
                      filter.value,
                  ).length
            }}
          </span>
        </button>
      </div>
    </section>

    <section class="welcome-card">
      <div>
        <p class="section-label">
          Community
        </p>

        <h2>
          Welcome back,
          {{
            props.user?.fullname ||
            props.user?.username ||
            "User"
          }}
        </h2>

        <p>
          Browse available pets and community
          adoption posts.
        </p>
      </div>
    </section>

    <section class="content-card">
      <div class="section-heading">
        <div>
          <p class="section-label">
            Discover
          </p>

          <h2>
            <template
              v-if="activeSearchKeyword"
            >
              Search results for
              “{{ activeSearchKeyword }}”
            </template>

            <template v-else>
              {{
                selectedType === "all"
                  ? "All Posts"
                  : `${formatPostType(
                      selectedType,
                    )} Posts`
              }}
            </template>
          </h2>
        </div>

        <div class="heading-actions">
          <span class="post-count">
            {{ filteredPosts.length }}
            {{
              filteredPosts.length === 1
                ? "post"
                : "posts"
            }}
          </span>

          <button
            type="button"
            class="refresh-button"
            :disabled="isLoading"
            @click="refreshPosts"
          >
            {{
              isLoading
                ? "Loading..."
                : "Refresh"
            }}
          </button>
        </div>
      </div>

      <p
        v-if="errorMessage"
        class="error-message"
      >
        {{ errorMessage }}
      </p>

      <div
        v-if="isLoading"
        class="empty-state"
      >
        <div class="loading-spinner"></div>

        <p>
          {{
            activeSearchKeyword
              ? "Searching community posts..."
              : "Loading community posts..."
          }}
        </p>
      </div>

      <template
        v-else-if="
          paginatedPosts.length
        "
      >
        <div class="results-summary">
          Showing

          <strong>
            {{ displayedRange.start }}
          </strong>

          -

          <strong>
            {{ displayedRange.end }}
          </strong>

          of

          <strong>
            {{ filteredPosts.length }}
          </strong>

          posts
        </div>

        <div class="post-list">
          <article
            v-for="
              post in paginatedPosts
            "
            :key="post.postId"
            class="post-row"
            :data-post-id="post.postId"
            :data-pet-id="post.petId"
            :data-user-id="post.userId"
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
              User ID:
              {{ post.userId }}
            </span>

            <div class="post-image-wrapper">
              <img
                class="post-image"
                :src="
                  post.firstPhotoUrl ||
                  DEFAULT_PET_IMAGE
                "
                :alt="
                  `${post.title} cover image`
                "
                @error="
                  handleImageError
                "
              />

              <span
                class="post-type"
                :class="
                  `post-type-${post.type}`
                "
              >
                {{
                  formatPostType(
                    post.type,
                  )
                }}
              </span>
            </div>

            <div class="post-information">
              <div class="post-title-row">
                <h3>
                  {{ post.title }}
                </h3>
              </div>

              <div class="post-content-section">
                <p class="post-content">
                  {{
                    post.content ||
                    "No content provided."
                  }}
                </p>
              </div>

              <div class="post-meta">
                <span class="meta-item">
                  <span class="meta-label">
                    Location
                  </span>

                  <strong>
                    {{
                      formatLocation(
                        post,
                      )
                    }}
                  </strong>
                </span>

                <span class="meta-item">
                  <span class="meta-label">
                    Posted
                  </span>

                  <strong>
                    {{
                      formatDate(
                        post.createdAt,
                      )
                    }}
                  </strong>
                </span>
              </div>
            </div>

            <div class="post-actions">
              <button
                type="button"
                class="view-pet-button"
                @click="
                  viewPet(post.petId)
                "
              >
                View Pet
              </button>

              <button
                type="button"
                class="save-button"
                :class="{
                  saved:
                    isPostSaved(
                      post.postId,
                    ),
                }"
                :disabled="
                  savingPostId !== null ||
                  isPostSaved(
                    post.postId,
                  )
                "
                @click="savePost(post)"
              >
                {{
                  isPostSaved(
                    post.postId,
                  )
                    ? "Saved"
                    : savingPostId ===
                        post.postId
                      ? "Saving..."
                      : "Save"
                }}
              </button>
            </div>
          </article>
        </div>

        <nav
          v-if="totalPages > 1"
          class="pagination"
          aria-label="Post pagination"
        >
          <button
            type="button"
            class="pagination-button pagination-navigation"
            :disabled="
              currentPage === 1
            "
            @click="
              goToPreviousPage
            "
          >
            Previous
          </button>

          <button
            v-if="
              firstVisiblePage > 1
            "
            type="button"
            class="pagination-button"
            @click="goToPage(1)"
          >
            1
          </button>

          <span
            v-if="
              firstVisiblePage > 2
            "
            class="pagination-ellipsis"
          >
            ...
          </span>

          <button
            v-for="
              page in visiblePageNumbers
            "
            :key="page"
            type="button"
            class="pagination-button"
            :class="{
              active:
                currentPage === page,
            }"
            :aria-current="
              currentPage === page
                ? 'page'
                : undefined
            "
            @click="goToPage(page)"
          >
            {{ page }}
          </button>

          <span
            v-if="
              lastVisiblePage <
              totalPages - 1
            "
            class="pagination-ellipsis"
          >
            ...
          </span>

          <button
            v-if="
              lastVisiblePage <
              totalPages
            "
            type="button"
            class="pagination-button"
            @click="
              goToPage(totalPages)
            "
          >
            {{ totalPages }}
          </button>

          <button
            type="button"
            class="pagination-button pagination-navigation"
            :disabled="
              currentPage ===
              totalPages
            "
            @click="goToNextPage"
          >
            Next
          </button>
        </nav>
      </template>

      <div
        v-else
        class="empty-state"
      >
        <h3>
          No posts found
        </h3>

        <p
          v-if="
            activeSearchKeyword ||
            selectedType !== 'all'
          "
        >
          No posts match the current search or
          selected filters.
        </p>

        <p v-else>
          There are currently no community posts
          available.
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.community-posts {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.search-box {
  display: grid;
  grid-template-columns:
    minmax(0, 1fr) auto;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #dbe2ea;
  border-radius: 14px;
  box-shadow:
    0 5px 18px
    rgba(15, 23, 42, 0.05);
}

.search-box input {
  min-width: 0;
  padding: 16px 18px;
  color: #0f172a;
  font: inherit;
  background-color: transparent;
  border: 0;
  outline: none;
}

.search-box input::placeholder {
  color: #94a3b8;
}

.search-box input:disabled {
  cursor: not-allowed;
  background-color: #f8fafc;
}

.search-box button {
  padding: 0 28px;
  color: #ffffff;
  font-weight: 700;
  background-color: #2563eb;
  border: 0;
  cursor: pointer;
}

.search-box button:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.search-box button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.filter-bar {
  padding: 20px 22px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow:
    0 5px 18px
    rgba(15, 23, 42, 0.04);
}

.filter-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.filter-label {
  margin: 0;
  color: #0f172a;
  font-weight: 800;
}

.filter-description {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.filter-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  color: #475569;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  background-color: #f8fafc;
  border: 1px solid #dbe2ea;
  border-radius: 999px;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.filter-button span {
  display: inline-flex;
  min-width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  color: #64748b;
  font-size: 11px;
  background-color: #e2e8f0;
  border-radius: 999px;
}

.filter-button:hover:not(:disabled) {
  color: #1d4ed8;
  background-color: #eff6ff;
  border-color: #bfdbfe;
}

.filter-button.active {
  color: #ffffff;
  background-color: #2563eb;
  border-color: #2563eb;
}

.filter-button.active span {
  color: #1d4ed8;
  background-color: #ffffff;
}

.filter-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.clear-filter-button {
  padding: 8px 12px;
  color: #b91c1c;
  font: inherit;
  font-size: 12px;
  font-weight: 700;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  cursor: pointer;
}

.clear-filter-button:hover:not(:disabled) {
  background-color: #fee2e2;
}

.clear-filter-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.welcome-card {
  padding: 28px;
  color: #ffffff;
  background:
    linear-gradient(
      120deg,
      #1d4ed8,
      #2563eb 55%,
      #0ea5e9
    );
  border-radius: 16px;
  box-shadow:
    0 12px 30px
    rgba(37, 99, 235, 0.16);
}

.welcome-card h2 {
  margin: 0;
  font-size: 28px;
}

.welcome-card p:last-child {
  margin: 10px 0 0;
  color: #dbeafe;
}

.section-label {
  margin: 0 0 7px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.welcome-card .section-label {
  color: #bfdbfe;
}

.content-card {
  padding: 24px;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 14px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.section-heading h2 {
  margin: 0;
  color: #0f172a;
}

.heading-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-count {
  padding: 7px 11px;
  color: #64748b;
  font-size: 12px;
  background-color: #f1f5f9;
  border-radius: 999px;
}

.refresh-button {
  padding: 8px 13px;
  color: #1d4ed8;
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
  opacity: 0.65;
}

.results-summary {
  margin-top: 20px;
  color: #64748b;
  font-size: 13px;
}

.results-summary strong {
  color: #334155;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 14px;
}

.post-row {
  position: relative;
  display: grid;
  grid-template-columns:
    190px minmax(0, 1fr)
    auto;
  gap: 22px;
  align-items: stretch;
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
  min-height: 180px;
  overflow: hidden;
  background-color: #e2e8f0;
}

.post-image {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 180px;
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
  backdrop-filter: blur(4px);
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

.post-title-row h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.35;
}

.post-content-section {
  margin: 14px 0 18px;
}

.post-content {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #64748b;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 28px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.meta-item strong {
  color: #334155;
  font-size: 13px;
}

.post-actions {
  display: flex;
  min-width: 135px;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 22px 22px 22px 0;
}

.view-pet-button,
.save-button {
  width: 100%;
  padding: 11px 16px;
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

.view-pet-button:hover {
  background-color: #dbeafe;
}

.save-button {
  color: #ffffff;
  background-color: #2563eb;
  border: 1px solid #2563eb;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    opacity 0.2s ease;
}

.save-button:hover:not(:disabled) {
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.save-button.saved {
  color: #ffffff;
  background-color: #111827;
  border-color: #111827;
  cursor: not-allowed;
  opacity: 1;
}

.save-button.saved:hover {
  background-color: #111827;
  border-color: #111827;
}

.save-button:disabled:not(.saved) {
  cursor: not-allowed;
  opacity: 0.65;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid #e2e8f0;
}

.pagination-button {
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  color: #475569;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  background-color: #ffffff;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
  cursor: pointer;
}

.pagination-button:hover:not(:disabled) {
  color: #1d4ed8;
  background-color: #eff6ff;
  border-color: #bfdbfe;
}

.pagination-button.active {
  color: #ffffff;
  background-color: #2563eb;
  border-color: #2563eb;
}

.pagination-button:disabled {
  color: #94a3b8;
  background-color: #f8fafc;
  cursor: not-allowed;
}

.pagination-navigation {
  min-width: 90px;
}

.pagination-ellipsis {
  padding: 0 3px;
  color: #94a3b8;
}

.empty-state {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  margin-top: 22px;
  padding: 30px;
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

.error-message {
  margin-top: 20px;
  padding: 12px 14px;
  color: #b91c1c;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 9px;
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
      160px minmax(0, 1fr);
  }

  .post-actions {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns:
      repeat(
        2,
        minmax(0, 1fr)
      );
    padding: 0 18px 18px;
  }
}

@media (max-width: 650px) {
  .search-box {
    grid-template-columns: 1fr;
  }

  .search-box button {
    padding: 13px;
  }

  .filter-heading,
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .clear-filter-button {
    width: 100%;
  }

  .filter-options {
    display: grid;
    grid-template-columns:
      repeat(
        2,
        minmax(0, 1fr)
      );
  }

  .filter-button {
    justify-content: space-between;
    border-radius: 9px;
  }

  .heading-actions {
    width: 100%;
    justify-content: space-between;
  }

  .post-row {
    grid-template-columns: 1fr;
  }

  .post-image-wrapper,
  .post-image {
    min-height: 220px;
    max-height: 260px;
  }

  .post-information {
    padding: 4px 18px 0;
  }

  .post-actions {
    grid-column: auto;
  }

  .pagination {
    justify-content: flex-start;
  }

  .pagination-navigation {
    flex: 1;
  }
}

@media (max-width: 440px) {
  .filter-options,
  .post-actions {
    grid-template-columns: 1fr;
  }

  .pagination-button:not(
      .pagination-navigation
    ) {
    min-width: 36px;
    height: 36px;
    padding: 0 9px;
  }
}
</style>