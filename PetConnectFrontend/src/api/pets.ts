import { apiClient } from "./client";
import type { AddPetRequest, InnerRespond, Pet, PetSearchFilters } from "../types";

// EXISTING on backend today (PetController.java)
export async function addPet(payload: AddPetRequest) {
  const { data } = await apiClient.post<Pet>("/pets/add", payload);
  return data;
}

// NOT YET ON BACKEND — see API_CONTRACT.md items #2-#6.
// Written against the shape the frontend needs; once Jesse builds these,
// nothing else in the app has to change.

export async function searchPets(filters: PetSearchFilters) {
  const { data } = await apiClient.get<InnerRespond<Pet[]>>("/pets/search", { params: filters });
  return data;
}

export async function getPetById(petId: number) {
  const { data } = await apiClient.get<InnerRespond<Pet>>(`/pets/${petId}`);
  return data;
}

export async function updatePet(petId: number, payload: Partial<AddPetRequest>) {
  const { data } = await apiClient.put<InnerRespond<Pet>>(`/pets/${petId}`, payload);
  return data;
}

export async function deletePet(petId: number) {
  const { data } = await apiClient.delete<InnerRespond<null>>(`/pets/${petId}`);
  return data;
}

export async function getPetsByOwner(userId: number) {
  const { data } = await apiClient.get<InnerRespond<Pet[]>>(`/pets/owner/${userId}`);
  return data;
}

export async function addFavorite(petId: number) {
  const { data } = await apiClient.post<InnerRespond<null>>(`/favorites/${petId}`);
  return data;
}

export async function removeFavorite(petId: number) {
  const { data } = await apiClient.delete<InnerRespond<null>>(`/favorites/${petId}`);
  return data;
}

export async function getFavorites() {
  const { data } = await apiClient.get<InnerRespond<Pet[]>>("/favorites");
  return data;
}
