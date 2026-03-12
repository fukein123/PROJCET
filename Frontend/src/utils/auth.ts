const TOKEN_KEY = 'cvs_token'
const ROLE_KEY = 'cvs_role'
const USERNAME_KEY = 'cvs_username'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRole(): string {
  return localStorage.getItem(ROLE_KEY) ?? ''
}

export function setRole(role: string) {
  localStorage.setItem(ROLE_KEY, role)
}

export function clearRole() {
  localStorage.removeItem(ROLE_KEY)
}

export function getUsername(): string {
  return localStorage.getItem(USERNAME_KEY) ?? ''
}

export function setUsername(username: string) {
  localStorage.setItem(USERNAME_KEY, username)
}

export function clearUsername() {
  localStorage.removeItem(USERNAME_KEY)
}

export function clearAuthStorage() {
  clearToken()
  clearRole()
  clearUsername()
}

