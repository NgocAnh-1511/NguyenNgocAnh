import api from './api'

export interface LoginDto {
  phoneNumber: string
  password: string
}

export interface User {
  userId: string
  phoneNumber: string
  fullName: string
  email: string
  isAdmin: boolean
}

export interface LoginResponse {
  access_token: string
  user: User
}

export const authService = {
  login: async (data: LoginDto): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', data)
    localStorage.setItem('token', response.data.access_token)
    localStorage.setItem('user', JSON.stringify(response.data.user))
    return response.data
  },

  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  getToken: (): string | null => {
    return localStorage.getItem('token')
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token')
  },
}

