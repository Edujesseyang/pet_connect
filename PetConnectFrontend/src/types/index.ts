// Types mirrored from PetConnect.drawio (ER diagram) and the backend entities
// that already exist in PetConnectBackend/src/main/java/.../entity

export interface Address {
  addressId?: number;
  country?: string;
  state?: string;
  city?: string;
  street?: string;
  zipCode?: string;
}

export interface UserProfile {
  profileId?: number;
  householdType?: string;
  adoptionExperience?: string;
  gender?: string;
  dateOfBirth?: string;
  phoneNumber?: string;
  bio?: string;
  profilePhotoUrl?: string;
  socialMediaLinks?: string;
}

export interface User {
  userId: number;
  username: string;
  fullname: string;
  email: string;
  role?: string;
  address?: Address;
  userProfile?: UserProfile;
}

export interface MedicalRecord {
  medicalRecordId?: number;
  vaccination?: string;
  allergies?: string;
  medications?: string;
  surgeries?: string;
  labResults?: string;
  imagingResults?: string;
  note?: string;
  specialCare?: string;
  spayedNeutered?: boolean;
  recordedAt?: string;
}

export interface PetPhoto {
  photoId?: number;
  photoUrl: string;
  caption?: string;
  displayOrder?: number;
}

// Matches AddPetRequest.java fields (flattened) — used when creating a listing
export interface AddPetRequest {
  name: string;
  species: string;
  breed: string;
  sex?: string;
  dateOfBirth?: string;
  color?: string;
  microchipNumber?: string;
  size?: string;
  weight?: number;
  description?: string;
  friendlyLevel?: number;
  isTrained?: boolean;
  country?: string;
  state?: string;
  city?: string;
  street?: string;
  zipCode?: string;
  vaccination?: string;
  allergies?: string;
  medications?: string;
  surgeries?: string;
  labResults?: string;
  imagingResults?: string;
  note?: string;
  specialCare?: string;
  spayedNeutered?: boolean;
}

// Full pet record as we'd expect it back from the browse/detail endpoints
// (these endpoints don't exist on the backend yet — see API_CONTRACT.md)
export interface Pet {
  petId: number;
  name: string;
  species: string;
  breed: string;
  sex?: string;
  age?: number;
  dateOfBirth?: string;
  color?: string;
  size?: string;
  weight?: number;
  description?: string;
  friendlyLevel?: number;
  isTrained?: boolean;
  listingStatus?: "AVAILABLE" | "PENDING" | "ADOPTED";
  owner?: Pick<User, "userId" | "username" | "fullname">;
  address?: Address;
  photos?: PetPhoto[];
  medicalRecord?: MedicalRecord;
  listedAt?: string;
  isFavorited?: boolean;
}

export interface PetSearchFilters {
  species?: string;
  breed?: string;
  minAge?: number;
  maxAge?: number;
  sex?: string;
  city?: string;
  state?: string;
  page?: number;
  pageSize?: number;
}

export interface Conversation {
  conversationId: number;
  pet: Pick<Pet, "petId" | "name" | "photos">;
  starter: Pick<User, "userId" | "username">;
  listingOwner: Pick<User, "userId" | "username">;
  lastMessage?: Message;
  unreadCount?: number;
}

export interface Message {
  messageId: number;
  conversationId: number;
  sender: Pick<User, "userId" | "username">;
  messageText: string;
  sentAt: string;
  readAt?: string;
}

// Generic backend envelope — matches InnerRespond<T>.java exactly
export interface InnerRespond<T> {
  state: boolean;
  message: string;
  data: T | null;
}
