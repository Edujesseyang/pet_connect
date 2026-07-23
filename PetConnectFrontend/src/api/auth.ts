import { apiClient } from "./client";
import type { InnerRespond, User } from "../types";

// Matches SignupRequest.java exactly
export interface SignupPayload {
  username: string;
  fullname: string;
  passwordHash: string; // backend hashes on receipt, but field is named this on the DTO
  email: string;
  householdType?: string;
  adoptionExp?: string;
  gender?: string;
  dateOfBirth?: string; // yyyy-MM-dd
  phoneNumber?: string;
  bio?: string;
  profilePhotoUrl?: string;
  socialMediaLinks?: string;
  country?: string;
  state?: string;
  city?: string;
  street?: string;
  zipCode?: string;
}

export async function signup(payload: SignupPayload) {
  const { data } = await apiClient.post<InnerRespond<User>>("/users/signup", payload);
  return data;
}

// NOTE: current backend takes username/password as query params, not a JSON body,
// and POST /users/login does not yet return a JWT (see API_CONTRACT.md item #1).
export async function login(username: string, password: string) {
  const { data } = await apiClient.post<InnerRespond<User>>("/users/login", null, {
    params: { username, password },
  });
  return data;
}

export async function changePassword(username: string, oldPassword: string, newPassword: string) {
  const { data } = await apiClient.post<InnerRespond<User>>("/users/changePassword", {
    username,
    oldPassword,
    newPassword,
  });
  return data;
}

export async function getUserByUsername(username: string) {
  const { data } = await apiClient.get<InnerRespond<User>>(`/users/getUserByUsername/${username}`);
  return data;
}

export async function getUserByEmail(email: string) {
  const { data } = await apiClient.get<InnerRespond<User>>(`/users/getUserByEmail/${email}`);
  return data;
}
