export function isStrongPassword(password: string): boolean {
  return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,30}$/.test(password)
}

export function isPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

