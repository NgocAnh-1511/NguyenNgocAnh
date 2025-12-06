import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Snackbar,
  Avatar,
  Chip,
  Card,
  CardContent,
} from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings'
import PersonIcon from '@mui/icons-material/Person'
import api from '../services/api'

interface User {
  userId?: string
  phoneNumber: string
  fullName: string
  email?: string
  avatarPath?: string
  isAdmin?: boolean
}

export default function Users() {
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const queryClient = useQueryClient()

  const { data: users, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get('/users').then((res) => res.data),
    staleTime: 0,
    refetchOnWindowFocus: true,
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<User> }) =>
      api.patch(`/users/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      setOpenDialog(false)
      setSelectedUser(null)
      setSnackbar({ open: true, message: 'User updated successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to update user', severity: 'error' })
    },
  })

  const toggleAdminMutation = useMutation({
    mutationFn: ({ id, isAdmin }: { id: string; isAdmin: boolean }) =>
      api.patch(`/users/${id}`, { isAdmin }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      setSnackbar({ open: true, message: 'Admin status updated successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to update admin status', severity: 'error' })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/users/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      setOpenDeleteDialog(false)
      setSelectedUser(null)
      setSnackbar({ open: true, message: 'User deleted successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to delete user', severity: 'error' })
    },
  })

  const handleOpenEdit = (user: any) => {
    setSelectedUser({
      userId: user.userId || user.user_id,
      phoneNumber: user.phoneNumber || user.phone_number || '',
      fullName: user.fullName || user.full_name || '',
      email: user.email || '',
      avatarPath: user.avatarPath || user.avatar_path || '',
      isAdmin: user.isAdmin !== undefined ? user.isAdmin : (user.is_admin !== undefined ? user.is_admin : false),
    })
    setOpenDialog(true)
  }

  const handleOpenDelete = (user: any) => {
    setSelectedUser({
      userId: user.userId || user.user_id,
      fullName: user.fullName || user.full_name || '',
    })
    setOpenDeleteDialog(true)
  }

  const handleToggleAdmin = (user: any) => {
    const userId = user.userId || user.user_id
    const currentAdminStatus = user.isAdmin !== undefined ? user.isAdmin : (user.is_admin !== undefined ? user.is_admin : false)
    toggleAdminMutation.mutate({ id: userId, isAdmin: !currentAdminStatus })
  }

  const handleSave = () => {
    if (!selectedUser?.userId) return

    const data = {
      fullName: selectedUser.fullName,
      email: selectedUser.email,
      avatarPath: selectedUser.avatarPath,
      isAdmin: selectedUser.isAdmin,
    }

    updateMutation.mutate({ id: selectedUser.userId, data })
  }

  const handleDelete = () => {
    if (selectedUser?.userId) {
      deleteMutation.mutate(selectedUser.userId)
    }
  }

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        Error loading users
      </Alert>
    )
  }

  const adminCount = users?.filter((u: any) => u.isAdmin || u.is_admin).length || 0
  const totalUsers = users?.length || 0

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Users Management
        </Typography>
        <Tooltip title="Refresh users">
          <span>
            <IconButton 
              onClick={() => refetch()} 
              disabled={isRefetching}
              color="primary"
              sx={{ 
                bgcolor: 'primary.light',
                '&:hover': { bgcolor: 'primary.main', color: 'white' }
              }}
            >
              <RefreshIcon />
            </IconButton>
          </span>
        </Tooltip>
      </Box>

      {/* Stats Cards */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Card sx={{ flex: 1, bgcolor: 'primary.light', color: 'primary.contrastText' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PersonIcon />
              <Typography variant="h6">Total Users</Typography>
            </Box>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {totalUsers}
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ flex: 1, bgcolor: 'warning.light', color: 'warning.contrastText' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AdminPanelSettingsIcon />
              <Typography variant="h6">Admins</Typography>
            </Box>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {adminCount}
            </Typography>
          </CardContent>
        </Card>
      </Box>

      {(isLoading || isRefetching) && (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}

      <TableContainer component={Paper} sx={{ mt: 2, boxShadow: 3 }}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'primary.main' }}>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Avatar</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>User ID</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Phone Number</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Full Name</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Email</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Role</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }} align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!users || users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    No users found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              users.map((user: any) => {
                const userId = user.userId || user.user_id
                const isAdmin = user.isAdmin !== undefined ? user.isAdmin : (user.is_admin !== undefined ? user.is_admin : false)
                
                return (
                  <TableRow 
                    key={userId}
                    sx={{ 
                      '&:hover': { bgcolor: 'action.hover' },
                      transition: 'background-color 0.2s'
                    }}
                  >
                    <TableCell>
                      <Avatar 
                        src={user.avatarPath || user.avatar_path}
                        sx={{ bgcolor: 'primary.main' }}
                      >
                        {(user.fullName || user.full_name || '').charAt(0).toUpperCase()}
                      </Avatar>
                    </TableCell>
                    <TableCell>{userId}</TableCell>
                    <TableCell>{user.phoneNumber || user.phone_number || '-'}</TableCell>
                    <TableCell>{user.fullName || user.full_name || '-'}</TableCell>
                    <TableCell>{user.email || '-'}</TableCell>
                    <TableCell>
                      <Chip
                        label={isAdmin ? 'Admin' : 'User'}
                        color={isAdmin ? 'warning' : 'default'}
                        size="small"
                        icon={isAdmin ? <AdminPanelSettingsIcon /> : <PersonIcon />}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Toggle Admin">
                        <IconButton
                          size="small"
                          color={isAdmin ? 'warning' : 'default'}
                          onClick={() => handleToggleAdmin(user)}
                          disabled={toggleAdminMutation.isPending}
                        >
                          <AdminPanelSettingsIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Edit User">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenEdit(user)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete User">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDelete(user)}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                )
              })
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Edit Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 600, bgcolor: 'primary.main', color: 'white' }}>
          Edit User
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="Phone Number"
              value={selectedUser?.phoneNumber || ''}
              disabled
              fullWidth
            />
            <TextField
              label="Full Name"
              value={selectedUser?.fullName || ''}
              onChange={(e) => setSelectedUser({ ...selectedUser!, fullName: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Email"
              type="email"
              value={selectedUser?.email || ''}
              onChange={(e) => setSelectedUser({ ...selectedUser!, email: e.target.value })}
              fullWidth
            />
            <TextField
              label="Avatar URL"
              value={selectedUser?.avatarPath || ''}
              onChange={(e) => setSelectedUser({ ...selectedUser!, avatarPath: e.target.value })}
              fullWidth
            />
            <FormControlLabel
              control={
                <Switch
                  checked={selectedUser?.isAdmin || false}
                  onChange={(e) => setSelectedUser({ ...selectedUser!, isAdmin: e.target.checked })}
                />
              }
              label="Admin"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={updateMutation.isPending || !selectedUser?.fullName}
          >
            {updateMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle sx={{ fontWeight: 600 }}>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete user "{selectedUser?.fullName}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
          <Button
            onClick={handleDelete}
            variant="contained"
            color="error"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  )
}
