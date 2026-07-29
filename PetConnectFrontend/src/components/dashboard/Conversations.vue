<script setup lang="ts">
import {
  computed,
  nextTick,
  onActivated,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
} from "vue";

import type { User } from "../../types/api";

interface Message {
  senderId: number;
  content: string;
  timestamp: string;
  conversationId: number;
  senderName: string;
}

interface Conversation {
  fromUser: number;
  toUser: number;
  petId: number;
  conversationId: number;
  messages: Message[];
}

interface ApiResponse<T> {
  data: T;
  message: string;
  state: boolean;
}

interface StoredUser {
  userId?: number;
  id?: number;
  fullname?: string;
  name?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
}

const props = defineProps<{
  user: User | null;
}>();

const conversations = ref<Conversation[]>([]);
const selectedConversationId = ref<number | null>(null);

const messageInput = ref("");

const isLoading = ref(false);
const isRefreshing = ref(false);
const isSending = ref(false);

const endingConversationId = ref<number | null>(null);

const errorMessage = ref("");
const successMessage = ref("");

const messageContainer = ref<HTMLElement | null>(null);

const AUTO_REFRESH_INTERVAL = 5000;

let refreshTimer: ReturnType<typeof setInterval> | null = null;

const currentUserId = computed(() => {
  if (props.user?.userId) {
    return props.user.userId;
  }

  const storedUser = getStoredUser();

  return storedUser?.userId ?? storedUser?.id ?? 0;
});

const currentUserName = computed(() => {
  const propUser = props.user as {
    fullname?: string;
    name?: string;
    username?: string;
    firstName?: string;
    lastName?: string;
  } | null;

  if (propUser?.fullname) {
    return propUser.fullname;
  }

  if (propUser?.name) {
    return propUser.name;
  }

  if (propUser?.username) {
    return propUser.username;
  }

  const propFullName = [propUser?.firstName, propUser?.lastName]
    .filter(Boolean)
    .join(" ");

  if (propFullName) {
    return propFullName;
  }

  const storedUser = getStoredUser();

  if (storedUser?.fullname) {
    return storedUser.fullname;
  }

  if (storedUser?.name) {
    return storedUser.name;
  }

  if (storedUser?.username) {
    return storedUser.username;
  }

  const storedFullName = [storedUser?.firstName, storedUser?.lastName]
    .filter(Boolean)
    .join(" ");

  return storedFullName || "User";
});

/**
 * Conversation sorting:
 *
 * 1. Conversations with zero messages appear first.
 * 2. Empty conversations are sorted by conversationId descending.
 * 3. Conversations with messages are sorted by their latest message time.
 * 4. Newer message time appears before older message time.
 */
const sortedConversations = computed(() => {
  return [...conversations.value].sort(
    (firstConversation, secondConversation) => {
      const firstHasNoMessages = firstConversation.messages.length === 0;

      const secondHasNoMessages = secondConversation.messages.length === 0;

      if (firstHasNoMessages && !secondHasNoMessages) {
        return -1;
      }

      if (!firstHasNoMessages && secondHasNoMessages) {
        return 1;
      }

      if (firstHasNoMessages && secondHasNoMessages) {
        return (
          secondConversation.conversationId - firstConversation.conversationId
        );
      }

      const firstTimestamp =
        getConversationLastTimestamp(firstConversation) ?? 0;

      const secondTimestamp =
        getConversationLastTimestamp(secondConversation) ?? 0;

      if (firstTimestamp !== secondTimestamp) {
        return secondTimestamp - firstTimestamp;
      }

      return (
        secondConversation.conversationId - firstConversation.conversationId
      );
    },
  );
});

const selectedConversation = computed(() => {
  if (selectedConversationId.value === null) {
    return null;
  }

  return (
    conversations.value.find(
      (conversation) =>
        conversation.conversationId === selectedConversationId.value,
    ) ?? null
  );
});

const selectedMessages = computed(() => {
  if (!selectedConversation.value) {
    return [];
  }

  return [...selectedConversation.value.messages].sort(
    (firstMessage, secondMessage) => {
      const firstTimestamp = new Date(firstMessage.timestamp).getTime();

      const secondTimestamp = new Date(secondMessage.timestamp).getTime();

      if (Number.isNaN(firstTimestamp) || Number.isNaN(secondTimestamp)) {
        return 0;
      }

      return firstTimestamp - secondTimestamp;
    },
  );
});

const selectedOtherUserName = computed(() => {
  if (!selectedConversation.value) {
    return "Conversation";
  }

  return getOtherUserName(selectedConversation.value);
});

function getStoredUser(): StoredUser | null {
  const storedUserText = localStorage.getItem("user");

  if (!storedUserText) {
    return null;
  }

  try {
    return JSON.parse(storedUserText) as StoredUser;
  } catch {
    return null;
  }
}

function getConversationLastTimestamp(
  conversation: Conversation,
): number | null {
  if (conversation.messages.length === 0) {
    return null;
  }

  const validTimestamps = conversation.messages
    .map((message) => new Date(message.timestamp).getTime())
    .filter((timestamp) => !Number.isNaN(timestamp));

  if (validTimestamps.length === 0) {
    return null;
  }

  return Math.max(...validTimestamps);
}

function getLastMessage(conversation: Conversation): Message | null {
  if (conversation.messages.length === 0) {
    return null;
  }

  return (
    [...conversation.messages].sort((firstMessage, secondMessage) => {
      const firstTimestamp = new Date(firstMessage.timestamp).getTime();

      const secondTimestamp = new Date(secondMessage.timestamp).getTime();

      if (Number.isNaN(firstTimestamp) || Number.isNaN(secondTimestamp)) {
        return 0;
      }

      return secondTimestamp - firstTimestamp;
    })[0] ?? null
  );
}

function getOtherUserName(conversation: Conversation): string {
  const otherUserId = getOtherUserId(conversation);

  const otherUserMessage = conversation.messages.find(
    (message) => message.senderId === otherUserId && message.senderName?.trim(),
  );

  if (!otherUserMessage) {
    return "New Asking";
  }

  return otherUserMessage.senderName.trim();
}

function hasOtherUserReplied(conversation: Conversation): boolean {
  const otherUserId = getOtherUserId(conversation);

  return conversation.messages.some(
    (message) => message.senderId === otherUserId,
  );
}

function getLastMessagePreview(conversation: Conversation): string {
  if (!hasOtherUserReplied(conversation)) {
    return "Waiting for reply";
  }

  const lastMessage = getLastMessage(conversation);

  return lastMessage?.content || "Waiting for reply";
}

function getOtherUserId(conversation: Conversation): number {
  if (conversation.fromUser === currentUserId.value) {
    return conversation.toUser;
  }

  return conversation.fromUser;
}

function getConversationInitial(conversation: Conversation): string {
  return getOtherUserName(conversation).charAt(0).toUpperCase();
}

function formatTimestamp(timestamp: string): string {
  if (!timestamp) {
    return "";
  }

  const date = new Date(timestamp);

  if (Number.isNaN(date.getTime())) {
    return timestamp;
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(date);
}

function formatConversationTime(conversation: Conversation): string {
  if (!hasOtherUserReplied(conversation)) {
    return "New";
  }

  const lastMessage = getLastMessage(conversation);

  if (!lastMessage) {
    return "New";
  }

  return formatTimestamp(lastMessage.timestamp);
}

async function scrollToBottom(): Promise<void> {
  await nextTick();

  if (!messageContainer.value) {
    return;
  }

  messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
}

async function readApiResponse<T>(response: Response): Promise<ApiResponse<T>> {
  const responseText = await response.text();

  if (!responseText) {
    throw new Error(
      `The server returned an empty response. Status: ${response.status}`,
    );
  }

  try {
    return JSON.parse(responseText) as ApiResponse<T>;
  } catch {
    throw new Error("The server returned an invalid JSON response.");
  }
}

async function readFlexibleResponse(response: Response): Promise<unknown> {
  const responseText = await response.text();

  if (!responseText) {
    return null;
  }

  try {
    return JSON.parse(responseText) as unknown;
  } catch {
    return responseText;
  }
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

function responseStateIsFalse(responseBody: unknown): boolean {
  return (
    typeof responseBody === "object" &&
    responseBody !== null &&
    "state" in responseBody &&
    responseBody.state === false
  );
}

function updateSelectedConversation(updatedConversation: Conversation): void {
  const conversationIndex = conversations.value.findIndex(
    (conversation) =>
      conversation.conversationId === updatedConversation.conversationId,
  );

  if (conversationIndex === -1) {
    conversations.value = [updatedConversation, ...conversations.value];

    return;
  }

  const updatedConversations = [...conversations.value];

  updatedConversations[conversationIndex] = updatedConversation;

  conversations.value = updatedConversations;
}

async function loadAllConversations(
  options: {
    showLoading?: boolean;
    scroll?: boolean;
  } = {},
): Promise<void> {
  const { showLoading = false, scroll = false } = options;

  if (isRefreshing.value || currentUserId.value <= 0) {
    return;
  }

  isRefreshing.value = true;

  if (showLoading && conversations.value.length === 0) {
    isLoading.value = true;
  }

  try {
    errorMessage.value = "";

    const userId = currentUserId.value;

    const response = await fetch(
      `http://localhost:8080/message/get_all/${userId}?userId=${userId}`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
          "Cache-Control": "no-cache",
          Pragma: "no-cache",
        },
        cache: "no-store",
      },
    );

    const result = await readApiResponse<Conversation[]>(response);

    if (!response.ok) {
      throw new Error(
        result.message ||
          `Failed to load conversations. Status: ${response.status}`,
      );
    }

    if (!result.state) {
      throw new Error(result.message || "Failed to load conversations.");
    }

    const previousConversation = selectedConversation.value;

    const previousMessageCount = previousConversation?.messages.length ?? 0;

    conversations.value = result.data ?? [];

    if (conversations.value.length === 0) {
      selectedConversationId.value = null;
      return;
    }

    const selectedStillExists =
      selectedConversationId.value !== null &&
      conversations.value.some(
        (conversation) =>
          conversation.conversationId === selectedConversationId.value,
      );

    if (!selectedStillExists) {
      selectedConversationId.value =
        sortedConversations.value[0]?.conversationId ?? null;
    }

    const newMessageCount = selectedConversation.value?.messages.length ?? 0;

    if (scroll || newMessageCount > previousMessageCount) {
      await scrollToBottom();
    }
  } catch (error) {
    console.error("Failed to load conversations:", error);

    errorMessage.value =
      error instanceof Error ? error.message : "Failed to load conversations.";
  } finally {
    isLoading.value = false;
    isRefreshing.value = false;
  }
}

async function selectConversation(conversationId: number): Promise<void> {
  selectedConversationId.value = conversationId;

  errorMessage.value = "";
  successMessage.value = "";

  await scrollToBottom();
}

async function sendMessage(): Promise<void> {
  const content = messageInput.value.trim();

  if (
    !content ||
    isSending.value ||
    endingConversationId.value !== null ||
    !selectedConversation.value
  ) {
    return;
  }

  isSending.value = true;
  errorMessage.value = "";
  successMessage.value = "";

  try {
    const activeConversation = selectedConversation.value;

    const response = await fetch("http://localhost:8080/message/send_message", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      cache: "no-store",
      body: JSON.stringify({
        senderId: currentUserId.value,
        content,
        senderName: currentUserName.value,
        conversationId: activeConversation.conversationId,
        timestamp: new Date().toISOString(),
      }),
    });

    const result = await readApiResponse<Conversation>(response);

    if (!response.ok) {
      throw new Error(
        result.message || `Failed to send message. Status: ${response.status}`,
      );
    }

    if (!result.state) {
      throw new Error(result.message || "Failed to send message.");
    }

    messageInput.value = "";

    if (result.data) {
      updateSelectedConversation(result.data);
    }

    await loadAllConversations({
      scroll: true,
    });

    await scrollToBottom();
  } catch (error) {
    console.error("Failed to send message:", error);

    errorMessage.value =
      error instanceof Error ? error.message : "Failed to send message.";
  } finally {
    isSending.value = false;
  }
}

async function endConversation(conversationId: number): Promise<void> {
  if (endingConversationId.value !== null || isSending.value) {
    return;
  }

  const shouldEnd = window.confirm(
    "End this conversation? It will be removed from your conversation list.",
  );

  if (!shouldEnd) {
    return;
  }

  try {
    endingConversationId.value = conversationId;

    errorMessage.value = "";
    successMessage.value = "";

    const response = await fetch(
      `http://localhost:8080/message/end_conversation/${conversationId}?conversationId=${conversationId}`,
      {
        method: "POST",
        headers: {
          Accept: "*/*",
        },
        cache: "no-store",
      },
    );

    const responseBody = await readFlexibleResponse(response);

    if (!response.ok) {
      throw new Error(
        getResponseMessage(responseBody, "Failed to end the conversation."),
      );
    }

    if (responseStateIsFalse(responseBody)) {
      throw new Error(
        getResponseMessage(responseBody, "Failed to end the conversation."),
      );
    }

    conversations.value = conversations.value.filter(
      (conversation) => conversation.conversationId !== conversationId,
    );

    if (selectedConversationId.value === conversationId) {
      selectedConversationId.value =
        sortedConversations.value[0]?.conversationId ?? null;
    }

    messageInput.value = "";

    successMessage.value = "Conversation ended successfully.";

    await loadAllConversations();
  } catch (error) {
    console.error("Failed to end conversation:", error);

    errorMessage.value =
      error instanceof Error
        ? error.message
        : "Failed to end the conversation.";
  } finally {
    endingConversationId.value = null;
  }
}

function startAutoRefresh(): void {
  stopAutoRefresh();

  refreshTimer = setInterval(() => {
    if (
      document.visibilityState === "visible" &&
      !isSending.value &&
      endingConversationId.value === null
    ) {
      void loadAllConversations();
    }
  }, AUTO_REFRESH_INTERVAL);
}

function stopAutoRefresh(): void {
  if (!refreshTimer) {
    return;
  }

  clearInterval(refreshTimer);
  refreshTimer = null;
}

function handleWindowFocus(): void {
  void loadAllConversations();
}

function handleVisibilityChange(): void {
  if (document.visibilityState === "visible") {
    void loadAllConversations();
    startAutoRefresh();

    return;
  }

  stopAutoRefresh();
}

function handleConversationCreated(): void {
  void loadAllConversations({
    scroll: true,
  });
}

onMounted(() => {
  window.addEventListener("focus", handleWindowFocus);

  window.addEventListener("conversation-created", handleConversationCreated);

  document.addEventListener("visibilitychange", handleVisibilityChange);

  void loadAllConversations({
    showLoading: true,
    scroll: true,
  });

  startAutoRefresh();
});

onActivated(() => {
  void loadAllConversations();
  startAutoRefresh();
});

onDeactivated(() => {
  stopAutoRefresh();
});

onBeforeUnmount(() => {
  stopAutoRefresh();

  window.removeEventListener("focus", handleWindowFocus);

  window.removeEventListener("conversation-created", handleConversationCreated);

  document.removeEventListener("visibilitychange", handleVisibilityChange);
});
</script>

<template>
  <section class="conversation-layout">
    <aside class="conversation-sidebar">
      <header class="conversation-sidebar-header">
        <div>
          <h2>Conversations</h2>

          <span v-if="isRefreshing && !isLoading" class="refresh-status">
            Refreshing...
          </span>
        </div>

        <button
          type="button"
          class="refresh-button"
          :disabled="isRefreshing || endingConversationId !== null"
          @click="
            loadAllConversations({
              scroll: false,
            })
          "
        >
          {{ isRefreshing ? "Loading..." : "Refresh" }}
        </button>
      </header>

      <div class="conversation-list-scroll">
        <div v-if="isLoading" class="conversation-status">
          <div class="loading-spinner"></div>

          <p>Loading conversations...</p>
        </div>

        <div
          v-else-if="sortedConversations.length > 0"
          class="conversation-items"
        >
          <button
            v-for="item in sortedConversations"
            :key="item.conversationId"
            type="button"
            class="conversation-item"
            :class="{
              active: item.conversationId === selectedConversationId,
              empty: item.messages.length === 0,
            }"
            :disabled="endingConversationId !== null"
            @click="selectConversation(item.conversationId)"
          >
            <div class="conversation-avatar">
              {{ getConversationInitial(item) }}
            </div>

            <div class="conversation-summary">
              <div class="conversation-title-row">
                <span class="conversation-name">
                  {{ getOtherUserName(item) }}
                </span>

                <time class="conversation-time">
                  {{ formatConversationTime(item) }}
                </time>
              </div>

              <span class="conversation-preview">
                {{ getLastMessagePreview(item) }}
              </span>

              <span class="conversation-details">
                Pet #{{ item.petId }} · Conversation #{{ item.conversationId }}
              </span>
            </div>
          </button>
        </div>

        <div v-else class="conversation-status">
          <h3>No conversations yet</h3>

          <p>New conversations will appear here.</p>
        </div>
      </div>
    </aside>

    <main class="message-stage">
      <div
        v-if="errorMessage && !selectedConversation"
        class="center-message error-state"
      >
        <h3>Unable to load conversations</h3>

        <p>{{ errorMessage }}</p>

        <button
          type="button"
          class="retry-button"
          @click="
            loadAllConversations({
              showLoading: true,
              scroll: true,
            })
          "
        >
          Try again
        </button>
      </div>

      <template v-else-if="selectedConversation">
        <header class="message-header">
          <div class="message-header-user">
            <div class="header-avatar">
              {{ selectedOtherUserName.charAt(0).toUpperCase() }}
            </div>

            <div class="message-header-details">
              <h3>
                {{ selectedOtherUserName }}
              </h3>

              <p>
                Pet #{{ selectedConversation.petId }} · Conversation #{{
                  selectedConversation.conversationId
                }}
              </p>
            </div>
          </div>

          <div class="message-header-actions">
            <button
              type="button"
              class="header-refresh-button"
              :disabled="isRefreshing || endingConversationId !== null"
              @click="
                loadAllConversations({
                  scroll: false,
                })
              "
            >
              {{ isRefreshing ? "Refreshing..." : "Refresh" }}
            </button>

            <button
              type="button"
              class="end-conversation-button"
              :disabled="isSending || endingConversationId !== null"
              @click="endConversation(selectedConversation.conversationId)"
            >
              {{
                endingConversationId === selectedConversation.conversationId
                  ? "Ending..."
                  : "End Conversation"
              }}
            </button>
          </div>
        </header>

        <div v-if="successMessage || errorMessage" class="message-notice">
          <div v-if="successMessage" class="success-message" role="status">
            {{ successMessage }}
          </div>

          <div v-if="errorMessage" class="send-error" role="alert">
            {{ errorMessage }}
          </div>
        </div>

        <section class="message-display">
          <!-- 真正负责滚动的消息列表 -->
          <div ref="messageContainer" class="message-list">
            <div v-if="selectedMessages.length === 0" class="empty-messages">
              <h3>No messages yet</h3>

              <p>Send the first message to start this conversation.</p>
            </div>

            <article
              v-for="(message, index) in selectedMessages"
              :key="`${selectedConversation.conversationId}-${message.timestamp}-${message.senderId}-${index}`"
              class="message-row"
              :class="{
                own: message.senderId === currentUserId,
              }"
            >
              <div class="message-content">
                <div class="message-meta">
                  <span>
                    {{ message.senderName || `User ${message.senderId}` }}
                  </span>

                  <time :datetime="message.timestamp">
                    {{ formatTimestamp(message.timestamp) }}
                  </time>
                </div>

                <div class="message-bubble">
                  {{ message.content }}
                </div>
              </div>
            </article>
          </div>
        </section>

        <form class="message-composer" @submit.prevent="sendMessage">
          <textarea
            v-model="messageInput"
            maxlength="1000"
            rows="3"
            placeholder="Type a message..."
            :disabled="isSending || endingConversationId !== null"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>

          <button
            type="submit"
            :disabled="
              isSending ||
              endingConversationId !== null ||
              messageInput.trim().length === 0
            "
          >
            {{ isSending ? "Sending..." : "Send" }}
          </button>
        </form>
      </template>

      <div v-else class="center-message">
        <h3>Select a conversation</h3>

        <p>Choose a conversation from the list to view its messages.</p>
      </div>
    </main>
  </section>
</template>

<style scoped>
.conversation-layout {
  display: grid;
  grid-template-columns: 330px minmax(0, 1fr);

  width: 100%;
  height: 960px;
  min-height: 960px;
  max-height: 960px;

  overflow: hidden;

  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

/* =========================
   Left conversation sidebar
   ========================= */

.conversation-sidebar {
  display: grid;
  min-width: 0;
  min-height: 0;

  grid-template-rows: auto minmax(0, 1fr);

  overflow: hidden;

  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
}

.conversation-sidebar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;

  flex: 0 0 auto;

  padding: 18px 18px 14px;

  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.conversation-sidebar-header h2 {
  margin: 0;

  color: #0f172a;
  font-size: 20px;
}

.refresh-status {
  display: block;

  margin-top: 4px;

  color: #64748b;
  font-size: 11px;
}

.conversation-list-scroll {
  min-width: 0;
  min-height: 0;

  padding: 12px;

  overflow-x: hidden;
  overflow-y: auto;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.conversation-list-scroll::-webkit-scrollbar,
.message-list::-webkit-scrollbar,
.message-composer textarea::-webkit-scrollbar {
  width: 8px;
}

.conversation-list-scroll::-webkit-scrollbar-track,
.message-list::-webkit-scrollbar-track,
.message-composer textarea::-webkit-scrollbar-track {
  background: transparent;
}

.conversation-list-scroll::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb,
.message-composer textarea::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.conversation-list-scroll::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover,
.message-composer textarea::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.conversation-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.conversation-item {
  display: flex;
  width: 100%;
  min-width: 0;
  gap: 12px;
  align-items: center;

  padding: 12px;

  text-align: left;
  cursor: pointer;

  background: transparent;
  border: 1px solid transparent;
  border-radius: 11px;

  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.conversation-item:hover:not(:disabled) {
  background: #f1f5f9;
  border-color: #dbe3ed;
}

.conversation-item.active {
  background: #e0ecff;
  border-color: #93c5fd;
}

.conversation-item.empty:not(.active) {
  background: #ffffff;
  border-color: #dbeafe;
}

.conversation-item:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.conversation-avatar,
.header-avatar {
  display: flex;
  flex: 0 0 auto;
  width: 44px;
  height: 44px;

  align-items: center;
  justify-content: center;

  color: #ffffff;
  font-weight: 800;

  background: #334155;
  border-radius: 50%;
}

.conversation-summary {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.conversation-title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.conversation-name {
  overflow: hidden;

  color: #0f172a;
  font-weight: 700;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  flex: 0 0 auto;

  color: #94a3b8;
  font-size: 10px;
}

.conversation-item.empty .conversation-time {
  color: #2563eb;
  font-weight: 700;
}

.conversation-preview {
  overflow: hidden;

  color: #64748b;
  font-size: 13px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-details {
  overflow: hidden;

  color: #94a3b8;
  font-size: 10px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-status {
  display: flex;
  min-height: 180px;

  align-items: center;
  justify-content: center;
  flex-direction: column;

  padding: 30px 15px;

  color: #64748b;
  text-align: center;

  background: #ffffff;
  border-radius: 10px;
}

.conversation-status h3 {
  margin: 0;
  color: #0f172a;
}

.conversation-status p {
  margin: 8px 0 0;
}

/* =========================
   Right message section
   ========================= */

.message-stage {
  display: grid;
  min-width: 0;
  min-height: 0;
  height: 100%;

  grid-template-rows:
    auto
    auto
    minmax(0, 1fr)
    auto
    104px;

  overflow: hidden;
  background: #ffffff;
}

.message-header {
  display: flex;
  min-width: 0;
  flex: 0 0 auto;

  align-items: center;
  justify-content: space-between;
  gap: 18px;

  padding: 14px 20px;

  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.message-header-user {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.message-header-details {
  min-width: 0;
}

.message-header h3 {
  overflow: hidden;

  margin: 0 0 3px;

  color: #0f172a;
  font-size: 17px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-header p {
  overflow: hidden;

  margin: 0;

  color: #64748b;
  font-size: 13px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-header-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
}

.refresh-button,
.header-refresh-button {
  flex: 0 0 auto;

  padding: 7px 10px;

  color: #1d4ed8;
  font: inherit;
  font-size: 12px;
  font-weight: 700;

  cursor: pointer;

  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 7px;
}

.refresh-button:hover:not(:disabled),
.header-refresh-button:hover:not(:disabled) {
  background: #dbeafe;
}

.refresh-button:disabled,
.header-refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.end-conversation-button {
  padding: 7px 11px;

  color: #b91c1c;
  font: inherit;
  font-size: 12px;
  font-weight: 700;

  cursor: pointer;

  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 7px;
}

.end-conversation-button:hover:not(:disabled) {
  color: #ffffff;
  background: #dc2626;
  border-color: #dc2626;
}

.end-conversation-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.success-message {
  flex: 0 0 auto;

  padding: 9px 20px;

  color: #166534;
  font-size: 14px;

  background: #f0fdf4;
  border-bottom: 1px solid #bbf7d0;
}

/* Fixed message display region */
.message-list {
  display: flex;
  min-width: 0;
  min-height: 0;

  flex-direction: column;
  gap: 14px;

  padding: 20px;

  overflow-x: hidden;
  overflow-y: auto;

  background: #f8fafc;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.message-row {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-start;
}

.message-row.own {
  justify-content: flex-end;
}

.message-content {
  max-width: min(72%, 640px);
}

.message-meta {
  display: flex;
  gap: 10px;
  align-items: center;

  margin: 0 6px 5px;

  color: #64748b;
  font-size: 12px;
}

.message-row.own .message-meta {
  justify-content: flex-end;
}

.message-bubble {
  padding: 11px 14px;

  color: #1e293b;
  line-height: 1.5;

  overflow-wrap: anywhere;
  white-space: pre-wrap;

  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 4px 14px 14px 14px;
}

.message-row.own .message-bubble {
  color: #ffffff;
  background: #2563eb;
  border-color: #2563eb;
  border-radius: 14px 4px 14px 14px;
}

.empty-messages,
.center-message {
  display: flex;

  align-self: center;
  justify-self: center;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  color: #64748b;
  text-align: center;
}

.empty-messages {
  width: 100%;
  min-height: 100%;
}

.empty-messages h3,
.center-message h3 {
  margin: 0;
  color: #0f172a;
}

.empty-messages p,
.center-message p {
  margin: 7px 0 0;
}

.error-state {
  color: #b91c1c;
}

.send-error {
  flex: 0 0 auto;

  padding: 8px 20px;

  color: #b91c1c;
  font-size: 14px;

  background: #fef2f2;
  border-top: 1px solid #fecaca;
}

/* =========================
   Fixed composer area
   ========================= */

.message-composer {
  display: grid;
  min-width: 0;
  min-height: 104px;
  max-height: 104px;
  height: 80px;

  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 12px;
  align-items: stretch;

  padding: 14px 20px;

  box-sizing: border-box;

  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}

.message-composer textarea {
  width: 100%;
  min-width: 0;
  min-height: 0;
  height: 80px;

  padding: 10px 14px;

  box-sizing: border-box;

  color: #0f172a;
  font: inherit;
  line-height: 1.4;

  resize: none;

  overflow-x: hidden;
  overflow-y: auto;

  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  outline: none;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.message-composer textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgb(37 99 235 / 12%);
}

.message-composer textarea:disabled {
  cursor: not-allowed;
  background: #f8fafc;
}

.message-composer button,
.retry-button {
  color: #ffffff;
  font: inherit;
  font-weight: 700;

  cursor: pointer;

  background: #2563eb;
  border: 0;
  border-radius: 9px;
}

.message-composer button {
  width: 96px;
  min-width: 96px;
  height: 100%;
  padding: 0 12px;
}

.message-composer button:hover:not(:disabled),
.retry-button:hover:not(:disabled) {
  background: #1d4ed8;
}

.message-composer button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.retry-button {
  margin-top: 12px;
  padding: 10px 18px;
}

.loading-spinner {
  width: 30px;
  height: 30px;

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
/* =========================
   Right-side main layout
   ========================= */

.message-stage {
  position: relative;

  display: flex;
  min-width: 0;
  min-height: 0;
  height: 100%;

  flex-direction: column;

  overflow: hidden;

  background: #ffffff;
}

/* =========================
   Fixed header
   ========================= */

.message-header {
  display: flex;
  min-width: 0;
  min-height: 74px;
  flex: 0 0 auto;

  align-items: center;
  justify-content: space-between;
  gap: 18px;

  padding: 14px 20px;
  box-sizing: border-box;

  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.message-header-user {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.message-header-details {
  min-width: 0;
}

.message-header h3 {
  overflow: hidden;

  margin: 0 0 3px;

  color: #0f172a;
  font-size: 17px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-header p {
  overflow: hidden;

  margin: 0;

  color: #64748b;
  font-size: 13px;

  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-header-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
}

.header-refresh-button {
  flex: 0 0 auto;

  padding: 7px 10px;

  color: #1d4ed8;
  font: inherit;
  font-size: 12px;
  font-weight: 700;

  cursor: pointer;

  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 7px;
}

.header-refresh-button:hover:not(:disabled) {
  background: #dbeafe;
}

.header-refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.end-conversation-button {
  flex: 0 0 auto;

  padding: 7px 11px;

  color: #b91c1c;
  font: inherit;
  font-size: 12px;
  font-weight: 700;

  cursor: pointer;

  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 7px;
}

.end-conversation-button:hover:not(:disabled) {
  color: #ffffff;
  background: #dc2626;
  border-color: #dc2626;
}

.end-conversation-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

/* =========================
   Notice region
   ========================= */

.message-notice {
  display: flex;
  min-width: 0;
  flex: 0 0 auto;
  flex-direction: column;
}

.success-message {
  padding: 9px 20px;

  color: #166534;
  font-size: 14px;

  background: #f0fdf4;
  border-bottom: 1px solid #bbf7d0;
}

.send-error {
  padding: 9px 20px;

  color: #b91c1c;
  font-size: 14px;

  background: #fef2f2;
  border-bottom: 1px solid #fecaca;
}

/* =========================
   Fixed message display box
   ========================= */

/*
  message-display 是固定的中间外框。

  flex: 1 1 auto:
  占据 header、notice、composer 之外的全部剩余空间。

  min-height: 0:
  允许它在 flex 布局中正确缩小，而不是被消息撑大。

  overflow: hidden:
  消息不能突破这个外框。
*/
.message-display {
  position: relative;

  display: block;
  min-width: 0;
  min-height: 0;
  flex: 1 1 auto;

  overflow: hidden;

  background: #f8fafc;
}

/*
  message-list 填满 message-display。

  消息少时：
  它仍然占满整个中间区域。

  消息多时：
  只在它内部产生滚动条。
*/
.message-list {
  position: absolute;
  inset: 0;

  display: flex;
  min-width: 0;
  min-height: 0;

  flex-direction: column;
  gap: 14px;

  padding: 20px;
  box-sizing: border-box;

  overflow-x: hidden;
  overflow-y: auto;

  background: #f8fafc;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.message-list::-webkit-scrollbar {
  width: 8px;
}

.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* =========================
   Message rows
   ========================= */

.message-row {
  display: flex;
  width: 100%;
  min-width: 0;
  flex: 0 0 auto;

  justify-content: flex-start;
}

.message-row.own {
  justify-content: flex-end;
}

.message-content {
  min-width: 0;
  max-width: min(72%, 640px);
}

.message-meta {
  display: flex;
  gap: 10px;
  align-items: center;

  margin: 0 6px 5px;

  color: #64748b;
  font-size: 12px;
}

.message-row.own .message-meta {
  justify-content: flex-end;
}

.message-bubble {
  padding: 11px 14px;

  color: #1e293b;
  line-height: 1.5;

  overflow-wrap: anywhere;
  white-space: pre-wrap;

  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 4px 14px 14px 14px;
}

.message-row.own .message-bubble {
  color: #ffffff;

  background: #2563eb;
  border-color: #2563eb;
  border-radius: 14px 4px 14px 14px;
}

/* =========================
   Empty message state
   ========================= */

.empty-messages {
  display: flex;
  width: 100%;
  min-height: 100%;
  flex: 1 0 auto;

  align-items: center;
  justify-content: center;
  flex-direction: column;

  color: #64748b;
  text-align: center;
}

.empty-messages h3 {
  margin: 0;
  color: #0f172a;
}

.empty-messages p {
  margin: 7px 0 0;
}

/* =========================
   Fixed composer
   ========================= */

.message-composer {
  display: grid;
  width: 100%;
  min-width: 0;

  height: 104px;
  min-height: 104px;
  max-height: 104px;
  flex: 0 0 104px;

  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 12px;
  align-items: stretch;

  padding: 14px 20px;
  box-sizing: border-box;

  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}

.message-composer textarea {
  display: block;

  width: 100%;
  min-width: 0;
  height: 100%;
  min-height: 0;

  padding: 10px 14px;
  box-sizing: border-box;

  color: #0f172a;
  font: inherit;
  line-height: 1.4;

  resize: none;

  overflow-x: hidden;
  overflow-y: auto;

  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  outline: none;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.message-composer textarea:focus {
  border-color: #2563eb;

  box-shadow: 0 0 0 3px rgb(37 99 235 / 12%);
}

.message-composer textarea:disabled {
  cursor: not-allowed;
  background: #f8fafc;
}

.message-composer button {
  width: 96px;
  min-width: 96px;
  height: 100%;

  padding: 0 12px;

  color: #ffffff;
  font: inherit;
  font-weight: 700;

  cursor: pointer;

  background: #2563eb;
  border: 0;
  border-radius: 9px;
}

.message-composer button:hover:not(:disabled) {
  background: #1d4ed8;
}

.message-composer button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

/* =========================
   General states
   ========================= */

.center-message {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex: 1;

  align-items: center;
  justify-content: center;
  flex-direction: column;

  padding: 20px;

  color: #64748b;
  text-align: center;
}

.center-message h3 {
  margin: 0;
  color: #0f172a;
}

.center-message p {
  margin: 7px 0 0;
}

.error-state {
  color: #b91c1c;
}

.retry-button {
  margin-top: 12px;
  padding: 10px 18px;

  color: #ffffff;
  font: inherit;
  font-weight: 700;

  cursor: pointer;

  background: #2563eb;
  border: 0;
  border-radius: 9px;
}

/* =========================
   Responsive layout
   ========================= */

@media (max-width: 850px) {
  .conversation-layout {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .message-content {
    max-width: 82%;
  }

  .message-header-actions {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 650px) {
  .conversation-layout {
    grid-template-columns: 1fr;
    grid-template-rows: 240px minmax(0, 1fr);

    height: 820px;
    min-height: 820px;
    max-height: 820px;
  }

  .conversation-sidebar {
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
  }

  .message-stage {
    grid-template-rows:
      auto
      auto
      minmax(0, 1fr)
      auto
      96px;
  }

  .message-header {
    padding: 12px 14px;
  }

  .header-refresh-button {
    display: none;
  }

  .message-header-actions {
    flex-direction: row;
  }

  .message-list {
    padding: 14px;
  }

  .message-content {
    max-width: 88%;
  }

  .message-composer {
    height: 96px;
    min-height: 96px;
    max-height: 96px;

    grid-template-columns: minmax(0, 1fr) 80px;

    padding: 12px 14px;
  }

  .message-composer button {
    width: 80px;
    min-width: 80px;
  }
}

@media (max-width: 430px) {
  .conversation-layout {
    height: 780px;
    min-height: 780px;
    max-height: 780px;
  }

  .conversation-sidebar-header {
    padding: 12px 14px;
  }

  .conversation-list-scroll {
    padding: 8px;
  }

  .message-header {
    align-items: flex-start;
  }

  .message-header-user {
    max-width: calc(100% - 54px);
  }

  .message-header-actions {
    flex: 0 0 auto;
  }

  .end-conversation-button {
    width: 38px;
    height: 38px;
    padding: 0;

    overflow: hidden;
    font-size: 0;
  }

  .end-conversation-button::before {
    content: "×";

    font-size: 24px;
    line-height: 1;
  }

  .message-composer {
    grid-template-columns: minmax(0, 1fr) 70px;
    gap: 8px;
  }

  .message-composer button {
    width: 70px;
    min-width: 70px;
  }

  .message-content {
    max-width: 94%;
  }
}
</style>
