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
  MenuItem,
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

interface Product {
  id?: number
  name: string
  description?: string
  price: number
  originalPrice?: number
  imageUrl?: string
  stock?: number
  isActive?: boolean
  categoryId?: number
  category?: {
    id: number
    name: string
  }
}

export default function Products() {
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const queryClient = useQueryClient()

  const { data: products, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['products'],
    queryFn: () => api.get('/products').then((res) => res.data),
    staleTime: 0,
    refetchOnWindowFocus: true,
  })

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.get('/categories').then((res) => res.data),
  })

  const createMutation = useMutation({
    mutationFn: (data: Partial<Product>) => api.post('/products', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      setOpenDialog(false)
      setSnackbar({ open: true, message: 'Product created successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to create product', severity: 'error' })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Product> }) =>
      api.patch(`/products/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      setOpenDialog(false)
      setSelectedProduct(null)
      setSnackbar({ open: true, message: 'Product updated successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to update product', severity: 'error' })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/products/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      setOpenDeleteDialog(false)
      setSelectedProduct(null)
      setSnackbar({ open: true, message: 'Product deleted successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to delete product', severity: 'error' })
    },
  })

  const handleOpenAdd = () => {
    setSelectedProduct({
      name: '',
      description: '',
      price: 0,
      originalPrice: 0,
      imageUrl: '',
      stock: 0,
      isActive: true,
      categoryId: undefined,
    })
    setOpenDialog(true)
  }

  const handleOpenEdit = (product: any) => {
    setSelectedProduct({
      id: product.id,
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      originalPrice: product.originalPrice || product.original_price || 0,
      imageUrl: product.imageUrl || product.image_url || product.image || '',
      stock: product.stock || 0,
      isActive: product.isActive !== undefined ? product.isActive : (product.is_active !== undefined ? product.is_active : true),
      categoryId: product.categoryId || product.category_id || product.category?.id,
    })
    setOpenDialog(true)
  }

  const handleOpenDelete = (product: any) => {
    setSelectedProduct({
      id: product.id,
      name: product.name || '',
    })
    setOpenDeleteDialog(true)
  }

  const handleSave = () => {
    if (!selectedProduct) return

    const data = {
      name: selectedProduct.name,
      description: selectedProduct.description,
      price: Number(selectedProduct.price),
      originalPrice: selectedProduct.originalPrice ? Number(selectedProduct.originalPrice) : undefined,
      imageUrl: selectedProduct.imageUrl,
      stock: selectedProduct.stock ? Number(selectedProduct.stock) : undefined,
      isActive: selectedProduct.isActive,
      categoryId: selectedProduct.categoryId ? Number(selectedProduct.categoryId) : undefined,
    }

    if (selectedProduct.id) {
      updateMutation.mutate({ id: selectedProduct.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const handleDelete = () => {
    if (selectedProduct?.id) {
      deleteMutation.mutate(selectedProduct.id)
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
        Error loading products
      </Alert>
    )
  }

  const totalProducts = products?.length || 0
  const activeProducts = products?.filter((p: any) => p.isActive !== false && p.is_active !== false).length || 0

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Products Management
        </Typography>
        <Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenAdd}
            sx={{ mr: 1, boxShadow: 2 }}
          >
            Add Product
          </Button>
          <Tooltip title="Refresh products">
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
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="h6">Total Products</Typography>
            </Box>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {totalProducts}
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ flex: 1, bgcolor: 'success.light', color: 'success.contrastText' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="h6">Active Products</Typography>
            </Box>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {activeProducts}
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
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Category</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Price</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Description</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }} align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!products || products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    No products found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              products.map((product: any) => {
                const productId = product.id || product.productId || '-'
                const name = product.name || product.productName || '-'
                const price = product.price || 0
                const description = product.description || '-'
                const imageUrl = product.imageUrl || product.image_url || product.image || ''
                const categoryName = product.category?.name || product.categoryName || '-'
                
                return (
                  <TableRow 
                    key={productId}
                    sx={{ 
                      '&:hover': { bgcolor: 'action.hover' },
                      transition: 'background-color 0.2s'
                    }}
                  >
                    <TableCell>{productId}</TableCell>
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
                    <TableCell>{categoryName}</TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'success.main' }}>
                        {typeof price === 'number' ? price.toLocaleString('vi-VN') + 'đ' : String(price)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {description.length > 50 ? description.substring(0, 50) + '...' : description}
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Edit Product">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenEdit(product)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete Product">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDelete(product)}
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
          {selectedProduct?.id ? 'Edit Product' : 'Add Product'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
            <TextField
              label="Name"
              value={selectedProduct?.name || ''}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, name: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Description"
              value={selectedProduct?.description || ''}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, description: e.target.value })}
              fullWidth
              multiline
              rows={3}
            />
            <TextField
              label="Price"
              type="number"
              value={selectedProduct?.price || 0}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, price: Number(e.target.value) })}
              fullWidth
              required
            />
            <TextField
              label="Original Price"
              type="number"
              value={selectedProduct?.originalPrice || 0}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, originalPrice: Number(e.target.value) })}
              fullWidth
            />
            <TextField
              label="Image URL"
              value={selectedProduct?.imageUrl || ''}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, imageUrl: e.target.value })}
              fullWidth
            />
            <TextField
              label="Stock"
              type="number"
              value={selectedProduct?.stock || 0}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, stock: Number(e.target.value) })}
              fullWidth
            />
            <TextField
              select
              label="Category"
              value={selectedProduct?.categoryId || ''}
              onChange={(e) => setSelectedProduct({ ...selectedProduct!, categoryId: Number(e.target.value) })}
              fullWidth
            >
              <MenuItem value="">None</MenuItem>
              {categories?.map((cat: any) => (
                <MenuItem key={cat.id || cat.categoryId} value={cat.id || cat.categoryId}>
                  {cat.name || cat.categoryName}
                </MenuItem>
              ))}
            </TextField>
            <FormControlLabel
              control={
                <Switch
                  checked={selectedProduct?.isActive !== false}
                  onChange={(e) => setSelectedProduct({ ...selectedProduct!, isActive: e.target.checked })}
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
            disabled={createMutation.isPending || updateMutation.isPending || !selectedProduct?.name}
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
            Are you sure you want to delete product "{selectedProduct?.name}"? This action cannot be undone.
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
