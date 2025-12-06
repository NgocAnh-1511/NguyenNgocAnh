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
  Avatar,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Snackbar,
  Card,
  CardContent,
} from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import api from '../services/api'

interface Category {
  id?: number
  name: string
  description?: string
  imageUrl?: string
  isActive?: boolean
}

export default function Categories() {
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null)
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const queryClient = useQueryClient()

  const { data: categories, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.get('/categories').then((res) => res.data),
    staleTime: 0,
    refetchOnWindowFocus: true,
  })

  const createMutation = useMutation({
    mutationFn: (data: Partial<Category>) => api.post('/categories', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      setOpenDialog(false)
      setSnackbar({ open: true, message: 'Category created successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to create category', severity: 'error' })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Category> }) =>
      api.patch(`/categories/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      setOpenDialog(false)
      setSelectedCategory(null)
      setSnackbar({ open: true, message: 'Category updated successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to update category', severity: 'error' })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/categories/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      setOpenDeleteDialog(false)
      setSelectedCategory(null)
      setSnackbar({ open: true, message: 'Category deleted successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to delete category', severity: 'error' })
    },
  })

  const handleOpenAdd = () => {
    setSelectedCategory({
      name: '',
      description: '',
      imageUrl: '',
      isActive: true,
    })
    setOpenDialog(true)
  }

  const handleOpenEdit = (category: any) => {
    setSelectedCategory({
      id: category.id || category.categoryId,
      name: category.name || category.categoryName || '',
      description: category.description || '',
      imageUrl: category.imageUrl || category.image_url || category.image || '',
      isActive: category.isActive !== undefined ? category.isActive : (category.is_active !== undefined ? category.is_active : true),
    })
    setOpenDialog(true)
  }

  const handleOpenDelete = (category: any) => {
    setSelectedCategory({
      id: category.id || category.categoryId,
      name: category.name || category.categoryName || '',
    })
    setOpenDeleteDialog(true)
  }

  const handleSave = () => {
    if (!selectedCategory) return

    const data = {
      name: selectedCategory.name,
      description: selectedCategory.description,
      imageUrl: selectedCategory.imageUrl,
      isActive: selectedCategory.isActive,
    }

    if (selectedCategory.id) {
      updateMutation.mutate({ id: selectedCategory.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const handleDelete = () => {
    if (selectedCategory?.id) {
      deleteMutation.mutate(selectedCategory.id)
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
        Error loading categories
      </Alert>
    )
  }

  const totalCategories = categories?.length || 0
  const activeCategories = categories?.filter((c: any) => c.isActive !== false && c.is_active !== false).length || 0

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Categories Management
        </Typography>
        <Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenAdd}
            sx={{ mr: 1, boxShadow: 2 }}
          >
            Add Category
          </Button>
          <Tooltip title="Refresh categories">
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
      </Box>

      {/* Stats Cards */}
      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Card sx={{ flex: 1, bgcolor: 'primary.light', color: 'primary.contrastText' }}>
          <CardContent>
            <Typography variant="h6">Total Categories</Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {totalCategories}
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ flex: 1, bgcolor: 'success.light', color: 'success.contrastText' }}>
          <CardContent>
            <Typography variant="h6">Active Categories</Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {activeCategories}
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
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>ID</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Image</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Name</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Description</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }} align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!categories || categories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    No categories found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              categories.map((category: any) => {
                const categoryId = category.id || category.categoryId || '-'
                const name = category.name || category.categoryName || '-'
                const description = category.description || '-'
                const imageUrl = category.imageUrl || category.image_url || category.image || ''
                
                return (
                  <TableRow 
                    key={categoryId}
                    sx={{ 
                      '&:hover': { bgcolor: 'action.hover' },
                      transition: 'background-color 0.2s'
                    }}
                  >
                    <TableCell>{categoryId}</TableCell>
                    <TableCell>
                      {imageUrl ? (
                        <Avatar
                          src={imageUrl}
                          alt={name}
                          variant="rounded"
                          sx={{ width: 56, height: 56 }}
                        />
                      ) : (
                        <Avatar variant="rounded" sx={{ width: 56, height: 56, bgcolor: 'grey.300' }}>
                          No Image
                        </Avatar>
                      )}
                    </TableCell>
                    <TableCell>{name}</TableCell>
                    <TableCell>
                      {description.length > 100 ? description.substring(0, 100) + '...' : description}
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Edit Category">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenEdit(category)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete Category">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDelete(category)}
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

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 600, bgcolor: 'primary.main', color: 'white' }}>
          {selectedCategory?.id ? 'Edit Category' : 'Add Category'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
            <TextField
              label="Name"
              value={selectedCategory?.name || ''}
              onChange={(e) => setSelectedCategory({ ...selectedCategory!, name: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Description"
              value={selectedCategory?.description || ''}
              onChange={(e) => setSelectedCategory({ ...selectedCategory!, description: e.target.value })}
              fullWidth
              multiline
              rows={3}
            />
            <TextField
              label="Image URL"
              value={selectedCategory?.imageUrl || ''}
              onChange={(e) => setSelectedCategory({ ...selectedCategory!, imageUrl: e.target.value })}
              fullWidth
            />
            <FormControlLabel
              control={
                <Switch
                  checked={selectedCategory?.isActive !== false}
                  onChange={(e) => setSelectedCategory({ ...selectedCategory!, isActive: e.target.checked })}
                />
              }
              label="Active"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={createMutation.isPending || updateMutation.isPending || !selectedCategory?.name}
          >
            {createMutation.isPending || updateMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle sx={{ fontWeight: 600 }}>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete category "{selectedCategory?.name}"? This action cannot be undone.
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
