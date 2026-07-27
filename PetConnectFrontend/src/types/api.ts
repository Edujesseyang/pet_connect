export interface Address {
  addressId: number;
  country: string;
  state: string;
  city: string;
  street: string;
  zipCode: string;
}

export interface UserProfile {
  userId: number;
  householdType: string;
  adoptionExp: string;
  gender: string;
  dateOfBirth: string;
  phoneNumber: string;
  bio: string;
  profilePhotoUrl: string;
  socialMediaLinks: string;
}

export interface Photo {
  photoId: number;
  url: string;
  description: string;
  uploaderId: number;
}

export interface MedicalRecord {
  petId: number;
  vaccination: string;
  allergies: string;
  medications: string;
  surgeries: string;
  labResults: string;
  imagingResults: string;
  note: string;
  specialCare: string;
  spayedNeutered: boolean;
}

export interface PetProfile {
  petId: number;
  sex: string;
  dateOfBirth: string;
  color: string;
  microchipNumber: string;
  size: string;
  weight: number;
  description: string;
  friendlyLevel: number;
  isTrained: boolean;
}

export interface Pet {
  petId: number;
  name: string;
  petProfile: PetProfile | null;
  medicalRecord: MedicalRecord | null;
  photos: Photo[];
  address: Address;
  species: string;
  breed: string;
}

export interface Post {
  postId: number;
  userId: number;
  petId: number;
  createdAt: string;
  title: string;
  content: string;
  type: string;
  adoptionFee: number | null;
  firstPhotoUrl: string | null;
  pickupLocation: Address;
}

export interface User {
  userId: number;
  username: string;
  fullname: string;
  email: string;
  role: string;
  address: Address | null;
  userProfile: UserProfile | null;
  posts: Post[];
  ownedPets: Pet[];
  savedPosts: Post[];
  appliedPosts: Post[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AddPetRequest {
  name: string;
  species: string;
  breed: string;
  sex: string;
  dateOfBirth: string;
  color: string;
  microchipNumber: string;
  size: string;
  weight: number | null;
  description: string;
  friendlyLevel: number | null;
  isTrained: boolean;

  country: string;
  state: string;
  city: string;
  street: string;
  zipCode: string;

  vaccination: string;
  allergies: string;
  medications: string;
  surgeries: string;
  labResults: string;
  imagingResults: string;
  note: string;
  specialCare: string;
  spayedNeutered: boolean;
}

export interface ApiResponse<T> {
  state?: boolean;
  message?: string;
  data: T;
}
