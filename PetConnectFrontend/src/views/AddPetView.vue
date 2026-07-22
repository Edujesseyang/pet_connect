<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { addPet } from "../api/pets";
import type { AddPetRequest } from "../types";

const router = useRouter();

const form = reactive<AddPetRequest>({
  name: "",
  species: "",
  breed: "",
  sex: "",
  size: "",
  description: "",
  city: "",
  state: "",
});

const error = ref("");
const success = ref(false);
const loading = ref(false);

async function handleSubmit() {
  error.value = "";
  loading.value = true;
  try {
    const pet = await addPet(form);
    success.value = true;
    setTimeout(() => router.push({ name: "pet-detail", params: { id: pet.petId } }), 800);
  } catch {
    error.value = "Couldn't create the listing. Is the backend running?";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="pc-container new-pet">
    <form class="new-pet__card pc-card" @submit.prevent="handleSubmit">
      <h1>List a pet</h1>
      <p class="new-pet__subtitle">Tell us about the pet you're rehoming.</p>

      <div class="new-pet__row">
        <div class="pc-field">
          <label for="name">Name</label>
          <input id="name" v-model="form.name" type="text" required />
        </div>
        <div class="pc-field">
          <label for="species">Species</label>
          <input id="species" v-model="form.species" type="text" placeholder="Dog, Cat…" required />
        </div>
      </div>

      <div class="new-pet__row">
        <div class="pc-field">
          <label for="breed">Breed</label>
          <input id="breed" v-model="form.breed" type="text" required />
        </div>
        <div class="pc-field">
          <label for="sex">Sex</label>
          <select id="sex" v-model="form.sex">
            <option value="">—</option>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
          </select>
        </div>
      </div>

      <div class="new-pet__row">
        <div class="pc-field">
          <label for="city">City</label>
          <input id="city" v-model="form.city" type="text" />
        </div>
        <div class="pc-field">
          <label for="state">State</label>
          <input id="state" v-model="form.state" type="text" />
        </div>
      </div>

      <div class="pc-field">
        <label for="description">Description</label>
        <textarea id="description" v-model="form.description" rows="4"></textarea>
      </div>

      <p v-if="error" class="pc-error">{{ error }}</p>
      <p v-if="success" class="new-pet__success">Listing created! Taking you there…</p>

      <button class="pc-btn pc-btn--primary new-pet__submit" type="submit" :disabled="loading">
        {{ loading ? "Saving…" : "Create listing" }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.new-pet {
  display: flex;
  justify-content: center;
  padding: 48px 24px 60px;
}

.new-pet__card {
  width: 100%;
  max-width: 520px;
  padding: 32px;
}

.new-pet__subtitle {
  color: var(--pc-muted);
  margin: 0 0 24px;
}

.new-pet__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.new-pet__submit {
  width: 100%;
  margin-top: 8px;
}

.new-pet__success {
  color: var(--pc-primary-light);
  font-weight: 600;
  font-size: 0.9rem;
}
</style>
