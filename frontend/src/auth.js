import { reactive } from 'vue'

const TOKEN_KEY = 'oa_token'
const USER_KEY = 'oa_user'
const ME_KEY = 'oa_me'

export const auth = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
  me: JSON.parse(localStorage.getItem(ME_KEY) || 'null'),
})

export function setAuth(token, user) {
  auth.token = token
  auth.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function setMe(me) {
  auth.me = me
  localStorage.setItem(ME_KEY, JSON.stringify(me))
}

export function clearAuth() {
  auth.token = ''
  auth.user = null
  auth.me = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(ME_KEY)
}
