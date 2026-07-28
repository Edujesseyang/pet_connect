<script setup lang="ts">
import { computed, onMounted, ref, type Component } from "vue";
import { useRouter } from "vue-router";

import type { User } from "../types/api";

import CommunityPosts from "../components/dashboard/CommunityPosts.vue";
import MyProfile from "../components/dashboard/MyProfile.vue";
import MyPets from "../components/dashboard/MyPets.vue";
import MyPosts from "../components/dashboard/MyPosts.vue";
import SavedPosts from "../components/dashboard/SavedPosts.vue";
import AppliedPosts from "../components/dashboard/AppliedPosts.vue";
import Conversations from "../components/dashboard/Conversations.vue";

type DashboardSection =
  | "dashboard"
  | "profile"
  | "pets"
  | "posts"
  | "savedPosts"
  | "appliedPosts"
  | "conversations";

interface MenuItem {
  key: DashboardSection;
  label: string;
  description: string;
  component: Component;
}

const router = useRouter();

const currentUser = ref<User | null>(null);
const activeSection = ref<DashboardSection>("dashboard");

const defaultMenuItem: MenuItem = {
  key: "dashboard",
  label: "Dashboard",
  description: "Community posts",
  component: CommunityPosts,
};

const menuItems: MenuItem[] = [
  defaultMenuItem,
  {
    key: "profile",
    label: "My Profile",
    description: "Personal information",
    component: MyProfile,
  },
  {
    key: "pets",
    label: "My Pets",
    description: "Manage owned pets",
    component: MyPets,
  },
  {
    key: "posts",
    label: "My Posts",
    description: "Manage published posts",
    component: MyPosts,
  },
  {
    key: "savedPosts",
    label: "Saved Posts",
    description: "View saved posts",
    component: SavedPosts,
  },
  {
    key: "appliedPosts",
    label: "Applied Posts",
    description: "Track applications",
    component: AppliedPosts,
  },
  {
    key: "conversations",
    label: "Conversations",
    description: "Messages and contacts",
    component: Conversations,
  },
];

const activeMenuItem = computed<MenuItem>(() => {
  return (
    menuItems.find(
      (item) => item.key === activeSection.value,
    ) ?? defaultMenuItem
  );
});

const activeComponent = computed<Component>(() => {
  return activeMenuItem.value.component;
});

const displayName = computed(() => {
  return (
    currentUser.value?.fullname ||
    currentUser.value?.username ||
    "User"
  );
});

const userInitial = computed(() => {
  return displayName.value.charAt(0).toUpperCase();
});

onMounted(() => {
  const storedUser = localStorage.getItem("user");

  if (!storedUser) {
    router.replace("/login");
    return;
  }

  try {
    currentUser.value = JSON.parse(storedUser) as User;
  } catch {
    localStorage.removeItem("user");
    router.replace("/login");
  }
});

function selectSection(section: DashboardSection): void {
  activeSection.value = section;
}

async function logout(): Promise<void> {
  localStorage.removeItem("user");
  await router.push("/login");
}
</script>

<template>
  <div class="dashboard-page">
    <div class="dashboard-layout">
      <aside class="sidebar">
        <div class="user-summary">
          <div class="avatar">
            <img
              v-if="currentUser?.userProfile?.profilePhotoUrl"
              :src="currentUser.userProfile.profilePhotoUrl"
              :alt="displayName"
            />

            <span v-else>
              {{ userInitial }}
            </span>
          </div>

          <div class="user-summary-text">
            <strong>
              {{ displayName }}
            </strong>

            <span>
              @{{ currentUser?.username || "user" }}
            </span>
          </div>
        </div>

        <nav class="sidebar-menu">
          <button
            v-for="item in menuItems"
            :key="item.key"
            type="button"
            class="menu-item"
            :class="{
              active: activeSection === item.key,
            }"
            @click="selectSection(item.key)"
          >
            <span class="menu-indicator"></span>

            <span class="menu-text">
              <strong>
                {{ item.label }}
              </strong>

              <small>
                {{ item.description }}
              </small>
            </span>
          </button>
        </nav>

        <button
          type="button"
          class="logout-button"
          @click="logout"
        >
          Log Out
        </button>
      </aside>

      <main class="workspace">
        <header class="workspace-header">
          <div>
            <p class="workspace-label">
              Pet Connect Workspace
            </p>

            <h1>
              {{ activeMenuItem.label }}
            </h1>

            <p class="workspace-description">
              {{ activeMenuItem.description }}
            </p>
          </div>

          <div class="account-status">
            <span class="status-dot"></span>
            Signed in
          </div>
        </header>

        <section class="stage">
          <Transition name="stage" mode="out-in">
            <component
              :is="activeComponent"
              :key="activeSection"
              :user="currentUser"
              @user-updated="currentUser = $event"
            />
          </Transition>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  min-height: calc(100vh - 70px);
  padding: 28px;
  background:
    linear-gradient(
      135deg,
      rgba(37, 99, 235, 0.05),
      transparent 35%
    ),
    #f4f7fb;
}

.dashboard-layout {
  display: grid;
  grid-template-columns: 270px minmax(0, 1fr);
  width: 100%;
  max-width: 1500px;
  min-height: calc(100vh - 126px);
  margin: 0 auto;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #e3e8ef;
  border-radius: 18px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}

.sidebar {
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
  background-color: #111827;
}

.user-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.avatar {
  display: flex;
  flex: 0 0 46px;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  background-color: #ffffff;
  border-radius: 50%;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-summary-text {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.user-summary-text strong {
  overflow: hidden;
  color: #ffffff;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-summary-text span {
  overflow: hidden;
  margin-top: 3px;
  color: #94a3b8;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-menu {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 7px;
  margin-top: 24px;
}

.menu-item {
  display: grid;
  grid-template-columns: 4px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  width: 100%;
  padding: 12px 10px;
  color: #cbd5e1;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 10px;
  cursor: pointer;
}

.menu-item:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.07);
}

.menu-item.active {
  color: #ffffff;
  background-color: rgba(37, 99, 235, 0.22);
}

.menu-indicator {
  width: 4px;
  height: 36px;
  background-color: transparent;
  border-radius: 999px;
}

.menu-item.active .menu-indicator {
  background-color: #60a5fa;
}

.menu-text {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.menu-text strong {
  font-size: 14px;
}

.menu-text small {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 11px;
}

.logout-button {
  width: 100%;
  padding: 12px;
  color: #fecaca;
  background-color: rgba(220, 38, 38, 0.12);
  border: 1px solid rgba(248, 113, 113, 0.25);
  border-radius: 10px;
  cursor: pointer;
}

.logout-button:hover {
  color: #ffffff;
  background-color: rgba(220, 38, 38, 0.25);
}

.workspace {
  min-width: 0;
  padding: 30px;
  background-color: #f8fafc;
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 26px;
}

.workspace-label {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workspace-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
}

.workspace-description {
  margin: 7px 0 0;
  color: #64748b;
}

.account-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 13px;
  color: #166534;
  font-size: 13px;
  font-weight: 600;
  background-color: #dcfce7;
  border-radius: 999px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background-color: #22c55e;
  border-radius: 50%;
}

.stage {
  min-height: 500px;
}

.stage-enter-active,
.stage-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.stage-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.stage-leave-to {
  opacity: 0;
  transform: translateX(-16px);
}

@media (max-width: 800px) {
  .dashboard-page {
    padding: 14px;
  }

  .dashboard-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-menu {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .logout-button {
    margin-top: 18px;
  }

  .workspace {
    padding: 20px;
  }

  .workspace-header {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .sidebar-menu {
    grid-template-columns: 1fr;
  }
}
</style>