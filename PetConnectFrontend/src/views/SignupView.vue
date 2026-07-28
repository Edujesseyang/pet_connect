<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

interface SignupRequest {
  username: string
  fullname: string
  passwordHash: string
  email: string

  householdType: string
  adoptionExp: string
  gender: string
  dateOfBirth: string
  phoneNumber: string
  bio: string
  profilePhotoUrl: string
  socialMediaLinks: string

  country: string
  state: string
  city: string
  street: string
  zipCode: string
}

interface SignupResponse {
  message?: string
  userId?: number
}

const router = useRouter()

const form = reactive<SignupRequest>({
  username: '',
  fullname: '',
  passwordHash: '',
  email: '',

  householdType: '',
  adoptionExp: '',
  gender: '',
  dateOfBirth: '',
  phoneNumber: '',
  bio: '',
  profilePhotoUrl: '',
  socialMediaLinks: '',

  country: '',
  state: '',
  city: '',
  street: '',
  zipCode: '',
})

const confirmPassword = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const isLoading = ref(false)

function validateForm(): boolean {
  errorMessage.value = ''

  if (!form.username.trim()) {
    errorMessage.value = 'Username is required.'
    return false
  }

  if (!form.fullname.trim()) {
    errorMessage.value = 'Full name is required.'
    return false
  }

  if (!form.email.trim()) {
    errorMessage.value = 'Email is required.'
    return false
  }

  if (!form.passwordHash) {
    errorMessage.value = 'Password is required.'
    return false
  }

  if (form.passwordHash.length < 8) {
    errorMessage.value = 'Password must contain at least 8 characters.'
    return false
  }

  if (form.passwordHash !== confirmPassword.value) {
    errorMessage.value = 'Passwords do not match.'
    return false
  }

  if (!form.country.trim()) {
    errorMessage.value = 'Country is required.'
    return false
  }

  if (!form.state.trim()) {
    errorMessage.value = 'State is required.'
    return false
  }

  if (!form.city.trim()) {
    errorMessage.value = 'City is required.'
    return false
  }

  return true
}

async function handleSignup(): Promise<void> {
  successMessage.value = ''

  if (!validateForm()) {
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''

    const response = await fetch('http://localhost:8080/users/signup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(form),
    })

    let data: SignupResponse = {}

    const contentType = response.headers.get('content-type')

    if (contentType?.includes('application/json')) {
      data = (await response.json()) as SignupResponse
    }

    if (!response.ok) {
      throw new Error(data.message || 'Signup failed.')
    }

    successMessage.value = data.message || 'Account created successfully.'

    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (error) {
    errorMessage.value =
      error instanceof Error
        ? error.message
        : 'Unexpected signup error.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <section class="signup-page">
    <form class="signup-card" @submit.prevent="handleSignup">
      <header class="form-header">
        <h1>Create Account</h1>
        <p>Register your Pet Connect account.</p>
      </header>

      <section class="form-section">
        <h2>Account Information</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="username">Username</label>
            <input
              id="username"
              v-model.trim="form.username"
              type="text"
              autocomplete="username"
              required
            />
          </div>

          <div class="form-group">
            <label for="fullname">Full Name</label>
            <input
              id="fullname"
              v-model.trim="form.fullname"
              type="text"
              autocomplete="name"
              required
            />
          </div>

          <div class="form-group full-width">
            <label for="email">Email</label>
            <input
              id="email"
              v-model.trim="form.email"
              type="email"
              autocomplete="email"
              required
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              v-model="form.passwordHash"
              type="password"
              autocomplete="new-password"
              required
            />
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              autocomplete="new-password"
              required
            />
          </div>
        </div>
      </section>

      <section class="form-section">
        <h2>User Profile</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="householdType">Household Type</label>
            <select
              id="householdType"
              v-model="form.householdType"
            >
              <option value="">Select household type</option>
              <option value="apartment">Apartment</option>
              <option value="house">House</option>
              <option value="farm">Farm</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div class="form-group">
            <label for="adoptionExp">Adoption Experience</label>
            <select
              id="adoptionExp"
              v-model="form.adoptionExp"
            >
              <option value="">Select experience</option>
              <option value="none">None</option>
              <option value="beginner">Beginner</option>
              <option value="experienced">Experienced</option>
              <option value="professional">Professional</option>
            </select>
          </div>

          <div class="form-group">
            <label for="gender">Gender</label>
            <select
              id="gender"
              v-model="form.gender"
            >
              <option value="">Select gender</option>
              <option value="male">Male</option>
              <option value="female">Female</option>
              <option value="non-binary">Non-binary</option>
              <option value="prefer-not-to-say">
                Prefer not to say
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="dateOfBirth">Date of Birth</label>
            <input
              id="dateOfBirth"
              v-model="form.dateOfBirth"
              type="date"
            />
          </div>

          <div class="form-group">
            <label for="phoneNumber">Phone Number</label>
            <input
              id="phoneNumber"
              v-model.trim="form.phoneNumber"
              type="tel"
              autocomplete="tel"
            />
          </div>

          <div class="form-group">
            <label for="profilePhotoUrl">Profile Photo URL</label>
            <input
              id="profilePhotoUrl"
              v-model.trim="form.profilePhotoUrl"
              type="url"
              placeholder="https://example.com/photo.jpg"
            />
          </div>

          <div class="form-group full-width">
            <label for="socialMediaLinks">Social Media Links</label>
            <input
              id="socialMediaLinks"
              v-model.trim="form.socialMediaLinks"
              type="text"
              placeholder="https://..."
            />
          </div>

          <div class="form-group full-width">
            <label for="bio">Bio</label>
            <textarea
              id="bio"
              v-model.trim="form.bio"
              rows="4"
              placeholder="Tell us about yourself."
            />
          </div>
        </div>
      </section>

      <section class="form-section">
        <h2>Address</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="country">Country</label>
            <input
              id="country"
              v-model.trim="form.country"
              type="text"
              autocomplete="country-name"
              required
            />
          </div>

          <div class="form-group">
            <label for="state">State</label>
            <input
              id="state"
              v-model.trim="form.state"
              type="text"
              autocomplete="address-level1"
              required
            />
          </div>

          <div class="form-group">
            <label for="city">City</label>
            <input
              id="city"
              v-model.trim="form.city"
              type="text"
              autocomplete="address-level2"
              required
            />
          </div>

          <div class="form-group">
            <label for="zipCode">ZIP Code</label>
            <input
              id="zipCode"
              v-model.trim="form.zipCode"
              type="text"
              autocomplete="postal-code"
            />
          </div>

          <div class="form-group full-width">
            <label for="street">Street</label>
            <input
              id="street"
              v-model.trim="form.street"
              type="text"
              autocomplete="street-address"
            />
          </div>
        </div>
      </section>

      <p
        v-if="errorMessage"
        class="message error-message"
      >
        {{ errorMessage }}
      </p>

      <p
        v-if="successMessage"
        class="message success-message"
      >
        {{ successMessage }}
      </p>

      <button
        class="submit-button"
        type="submit"
        :disabled="isLoading"
      >
        {{ isLoading ? 'Creating account...' : 'Create Account' }}
      </button>

      <p class="login-link">
        Already have an account?
        <RouterLink to="/login">
          Login
        </RouterLink>
      </p>
    </form>
  </section>
</template>

<style scoped>
.signup-page {
  min-height: 100vh;
  padding: 48px 20px;
  background-color: #f4f6f8;
}

.signup-card {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 36px;
  background-color: white;
  border: 1px solid #dddddd;
  border-radius: 12px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);
}

.form-header {
  margin-bottom: 32px;
  text-align: center;
}

.form-header h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.form-header p {
  margin: 0;
  color: #666666;
}

.form-section {
  margin-bottom: 32px;
}

.form-section h2 {
  margin-bottom: 18px;
  padding-bottom: 10px;
  font-size: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.full-width {
  grid-column: 1 / -1;
}

label {
  font-weight: 600;
}

input,
select,
textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  font: inherit;
  background-color: white;
  border: 1px solid #cccccc;
  border-radius: 6px;
  outline: none;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #2563eb;
}

textarea {
  resize: vertical;
}

.message {
  padding: 12px;
  border-radius: 6px;
}

.error-message {
  color: #b91c1c;
  background-color: #fee2e2;
}

.success-message {
  color: #166534;
  background-color: #dcfce7;
}

.submit-button {
  width: 100%;
  padding: 13px;
  color: white;
  font-size: 16px;
  background-color: #2563eb;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.submit-button:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.login-link {
  margin-top: 20px;
  text-align: center;
}

.login-link a {
  color: #2563eb;
  text-decoration: none;
}

@media (max-width: 700px) {
  .signup-card {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }
}
</style>