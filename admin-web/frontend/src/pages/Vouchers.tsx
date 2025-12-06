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
  MenuItem,
  FormControlLabel,
  Switch,
  Snackbar,
  Chip,
  Card,
  CardContent,
} from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import api from '../services/api'

interface Voucher {
  id?: string
  code: string
  name: string
  description?: string
  type?: 'PERCENT' | 'FIXED'
  value: number
  minPurchaseAmount?: number
  maxDiscountAmount?: number
  usageLimit?: number
  startDate?: string
  endDate?: string
  isActive?: boolean
}

export default function Vouchers() {
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedVoucher, setSelectedVoucher] = useState<Voucher | null>(null)
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const queryClient = useQueryClient()

  const { data: vouchers, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['vouchers'],
    queryFn: () => api.get('/vouchers').then((res) => res.data),
    staleTime: 0,
    refetchOnWindowFocus: true,
  })

  const createMutation = useMutation({
    mutationFn: (data: Partial<Voucher>) => api.post('/vouchers', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vouchers'] })
      setOpenDialog(false)
      setSnackbar({ open: true, message: 'Voucher created successfully', severity: 'success' })
    },
    onError: (error: any) => {
      console.error('Create voucher error:', error)
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to create voucher'
      setSnackbar({ open: true, message: errorMessage, severity: 'error' })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<Voucher> }) =>
      api.patch(`/vouchers/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vouchers'] })
      setOpenDialog(false)
      setSelectedVoucher(null)
      setSnackbar({ open: true, message: 'Voucher updated successfully', severity: 'success' })
    },
    onError: (error: any) => {
      console.error('Update voucher error:', error)
      console.error('Error response:', error?.response?.data)
      console.error('Error status:', error?.response?.status)
      
      // Try to get detailed error message
      let errorMessage = 'Failed to update voucher'
      if (error?.response?.data) {
        const errorData = error.response.data
        if (errorData.message) {
          errorMessage = errorData.message
        } else if (Array.isArray(errorData.message)) {
          errorMessage = errorData.message.join(', ')
        } else if (errorData.error) {
          errorMessage = errorData.error
        }
      } else if (error?.message) {
        errorMessage = error.message
      }
      
      setSnackbar({ open: true, message: errorMessage, severity: 'error' })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/vouchers/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vouchers'] })
      setOpenDeleteDialog(false)
      setSelectedVoucher(null)
      setSnackbar({ open: true, message: 'Voucher deleted successfully', severity: 'success' })
    },
    onError: (error: any) => {
      console.error('Delete voucher error:', error)
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to delete voucher'
      setSnackbar({ open: true, message: errorMessage, severity: 'error' })
    },
  })

  const handleOpenAdd = () => {
    setSelectedVoucher({
      code: '',
      name: '',
      description: '',
      type: 'PERCENT',
      value: 0,
      minPurchaseAmount: 0,
      maxDiscountAmount: 0,
      usageLimit: 0,
      isActive: true,
    })
    setOpenDialog(true)
  }

  const handleOpenEdit = (voucher: any) => {
    const voucherId = voucher.voucherId || voucher.voucher_id || voucher.id
    
    // Helper function to safely parse date
    const parseDate = (dateValue: any): string | undefined => {
      // Handle null, undefined, or empty values
      if (dateValue === null || dateValue === undefined || dateValue === '') {
        return undefined
      }
      
      // Handle string values that might be invalid
      if (typeof dateValue === 'string') {
        const trimmed = dateValue.trim()
        if (trimmed === '' || trimmed === 'null' || trimmed === 'undefined' || trimmed === 'Invalid Date' || trimmed.toLowerCase() === 'null' || trimmed === '0') {
          return undefined
        }
      }
      
      // Handle number (timestamp) - backend stores dates as bigint timestamps
      let date: Date
      if (typeof dateValue === 'number') {
        // Check if it's a valid timestamp (must be positive and reasonable)
        if (dateValue <= 0 || isNaN(dateValue) || !isFinite(dateValue)) {
          return undefined
        }
        // Check if timestamp is reasonable (not too far in past or future)
        // Valid range: between year 1970 and 2100
        if (dateValue < 0 || dateValue > 4102444800000) {
          return undefined
        }
        date = new Date(dateValue)
      } else if (typeof dateValue === 'string') {
        // Try to parse as date string
        try {
          date = new Date(dateValue)
        } catch {
          return undefined
        }
      } else {
        return undefined
      }
      
      // Check if date is valid
      if (!date || isNaN(date.getTime()) || date.getTime() === 0) {
        return undefined
      }
      
      // Final validation - check if date is reasonable
      const year = date.getFullYear()
      if (year < 1970 || year > 2100) {
        return undefined
      }
      
      try {
        return date.toISOString().split('T')[0]
      } catch (e) {
        console.warn('Error converting date to ISO string:', e, dateValue)
        return undefined
      }
    }
    
    // Map voucher type - handle AMOUNT as FIXED
    const rawType = voucher.type || voucher.discountType || voucher.discount_type || 'PERCENT'
    const mappedType = rawType === 'AMOUNT' ? 'FIXED' : (rawType === 'PERCENT' || rawType === 'FIXED' ? rawType : 'PERCENT')
    
    setSelectedVoucher({
      id: voucherId,
      code: voucher.code || '',
      name: voucher.name || '',
      description: voucher.description || '',
      type: mappedType as 'PERCENT' | 'FIXED',
      value: voucher.value || voucher.discountPercent || voucher.discount_percent || voucher.discountAmount || voucher.discount_amount || 0,
      minPurchaseAmount: voucher.minPurchaseAmount || voucher.min_order_amount || voucher.minOrderAmount || 0,
      maxDiscountAmount: voucher.maxDiscountAmount || voucher.max_discount_amount || 0,
      usageLimit: voucher.usageLimit || voucher.usage_limit || 0,
      startDate: parseDate(voucher.startDate || voucher.start_date),
      endDate: parseDate(voucher.endDate || voucher.end_date),
      isActive: voucher.isActive !== undefined ? voucher.isActive : (voucher.is_active !== undefined ? voucher.is_active : true),
    })
    setOpenDialog(true)
  }

  const handleOpenDelete = (voucher: any) => {
    const voucherId = voucher.voucherId || voucher.voucher_id || voucher.id
    setSelectedVoucher({
      id: voucherId,
      code: voucher.code || '',
      name: voucher.name || '',
    })
    setOpenDeleteDialog(true)
  }

  const handleSave = () => {
    if (!selectedVoucher) return

    // Validate required fields
    if (!selectedVoucher.code || !selectedVoucher.code.trim()) {
      setSnackbar({ open: true, message: 'Code is required', severity: 'error' })
      return
    }
    if (!selectedVoucher.name || !selectedVoucher.name.trim()) {
      setSnackbar({ open: true, message: 'Name is required', severity: 'error' })
      return
    }

    // Map FIXED to AMOUNT for backend (backend uses AMOUNT, not FIXED)
    const backendType = selectedVoucher.type === 'FIXED' ? 'AMOUNT' : selectedVoucher.type

    const data: any = {
      code: selectedVoucher.code.trim(),
      name: selectedVoucher.name.trim(),
      type: backendType,
      value: Number(selectedVoucher.value),
      // Convert to boolean (backend expects boolean, not number)
      isActive: selectedVoucher.isActive !== undefined 
        ? Boolean(selectedVoucher.isActive) 
        : true,
    }

    // Only include description if it has a value
    if (selectedVoucher.description && selectedVoucher.description.trim() !== '') {
      data.description = selectedVoucher.description.trim()
    }

    // Only include optional numeric fields if they have valid values
    if (selectedVoucher.minPurchaseAmount && Number(selectedVoucher.minPurchaseAmount) > 0) {
      data.minPurchaseAmount = Number(selectedVoucher.minPurchaseAmount)
    }
    if (selectedVoucher.maxDiscountAmount && Number(selectedVoucher.maxDiscountAmount) > 0) {
      data.maxDiscountAmount = Number(selectedVoucher.maxDiscountAmount)
    }
    if (selectedVoucher.usageLimit && Number(selectedVoucher.usageLimit) > 0) {
      data.usageLimit = Number(selectedVoucher.usageLimit)
    }

    // Safely convert dates to ISO string (backend expects ISO 8601 string format)
    if (selectedVoucher.startDate) {
      try {
        const startDate = new Date(selectedVoucher.startDate)
        if (!isNaN(startDate.getTime()) && startDate.getTime() > 0) {
          data.startDate = startDate.toISOString()
        }
      } catch (e) {
        console.error('Invalid start date:', e, selectedVoucher.startDate)
      }
    }
    if (selectedVoucher.endDate) {
      try {
        const endDate = new Date(selectedVoucher.endDate)
        if (!isNaN(endDate.getTime()) && endDate.getTime() > 0) {
          data.endDate = endDate.toISOString()
        }
      } catch (e) {
        console.error('Invalid end date:', e, selectedVoucher.endDate)
      }
    }

    console.log('Saving voucher data:', data)

    if (selectedVoucher.id) {
      updateMutation.mutate({ id: selectedVoucher.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const handleDelete = () => {
    if (selectedVoucher?.id) {
      deleteMutation.mutate(selectedVoucher.id)
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
        Error loading vouchers
      </Alert>
    )
  }

  const totalVouchers = vouchers?.length || 0
  const activeVouchers = vouchers?.filter((v: any) => v.isActive !== false && v.is_active !== false).length || 0

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Vouchers Management
        </Typography>
        <Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenAdd}
            sx={{ mr: 1, boxShadow: 2 }}
          >
            Add Voucher
          </Button>
          <Tooltip title="Refresh vouchers">
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
            <Typography variant="h6">Total Vouchers</Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {totalVouchers}
            </Typography>
          </CardContent>
        </Card>
        <Card sx={{ flex: 1, bgcolor: 'success.light', color: 'success.contrastText' }}>
          <CardContent>
            <Typography variant="h6">Active Vouchers</Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
              {activeVouchers}
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
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Code</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Name</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Type</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Value</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Min Order</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Used</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Status</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }} align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!vouchers || vouchers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    No vouchers found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              vouchers.map((voucher: any) => {
                const voucherId = voucher.voucherId || voucher.voucher_id || voucher.id || '-'
                const code = voucher.code || '-'
                const name = voucher.name || '-'
                // Map AMOUNT to FIXED for display (backend uses AMOUNT, UI uses FIXED)
                const rawType = voucher.type || voucher.discountType || voucher.discount_type || 'PERCENT'
                const discountType = rawType === 'AMOUNT' ? 'FIXED' : rawType
                const discountPercent = voucher.discountPercent || voucher.discount_percent || 0
                const discountAmount = voucher.discountAmount || voucher.discount_amount || 0
                const value = voucher.value || (rawType === 'PERCENT' || rawType === 'AMOUNT' ? (rawType === 'PERCENT' ? discountPercent : discountAmount) : discountPercent)
                const minOrderAmount = voucher.minPurchaseAmount || voucher.min_order_amount || voucher.minOrderAmount || 0
                const usedCount = voucher.usedCount || voucher.used_count || 0
                const usageLimit = voucher.usageLimit || voucher.usage_limit || 0
                const isActive = voucher.isActive !== undefined ? voucher.isActive : (voucher.is_active !== undefined ? voucher.is_active : true)
                
                return (
                  <TableRow 
                    key={voucherId}
                    sx={{ 
                      '&:hover': { bgcolor: 'action.hover' },
                      transition: 'background-color 0.2s'
                    }}
                  >
                    <TableCell>{code}</TableCell>
                    <TableCell>{name}</TableCell>
                    <TableCell>{discountType}</TableCell>
                    <TableCell>
                      {discountType === 'PERCENT'
                        ? `${value}%`
                        : `${typeof value === 'number' ? value.toLocaleString('vi-VN') : value}đ`}
                    </TableCell>
                    <TableCell>
                      {typeof minOrderAmount === 'number' ? minOrderAmount.toLocaleString('vi-VN') + 'đ' : minOrderAmount}
                    </TableCell>
                    <TableCell>
                      {usedCount} / {usageLimit || '∞'}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={isActive ? 'Active' : 'Inactive'}
                        color={isActive ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Edit Voucher">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenEdit(voucher)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete Voucher">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDelete(voucher)}
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
          {selectedVoucher?.id ? 'Edit Voucher' : 'Add Voucher'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
            <TextField
              label="Code"
              value={selectedVoucher?.code || ''}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, code: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Name"
              value={selectedVoucher?.name || ''}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, name: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Description"
              value={selectedVoucher?.description || ''}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, description: e.target.value })}
              fullWidth
              multiline
              rows={2}
            />
            <TextField
              select
              label="Type"
              value={selectedVoucher?.type || 'PERCENT'}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, type: e.target.value as 'PERCENT' | 'FIXED' })}
              fullWidth
            >
              <MenuItem value="PERCENT">Percent</MenuItem>
              <MenuItem value="FIXED">Fixed Amount</MenuItem>
            </TextField>
            <TextField
              label={selectedVoucher?.type === 'PERCENT' ? 'Discount Percent (%)' : 'Discount Amount'}
              type="number"
              value={selectedVoucher?.value || 0}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, value: Number(e.target.value) })}
              fullWidth
              required
            />
            <TextField
              label="Min Purchase Amount"
              type="number"
              value={selectedVoucher?.minPurchaseAmount || 0}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, minPurchaseAmount: Number(e.target.value) })}
              fullWidth
            />
            <TextField
              label="Max Discount Amount"
              type="number"
              value={selectedVoucher?.maxDiscountAmount || 0}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, maxDiscountAmount: Number(e.target.value) })}
              fullWidth
            />
            <TextField
              label="Usage Limit"
              type="number"
              value={selectedVoucher?.usageLimit || 0}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, usageLimit: Number(e.target.value) })}
              fullWidth
            />
            <TextField
              label="Start Date"
              type="date"
              value={selectedVoucher?.startDate || ''}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, startDate: e.target.value })}
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="End Date"
              type="date"
              value={selectedVoucher?.endDate || ''}
              onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, endDate: e.target.value })}
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={selectedVoucher?.isActive !== false}
                  onChange={(e) => setSelectedVoucher({ ...selectedVoucher!, isActive: e.target.checked })}
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
            disabled={createMutation.isPending || updateMutation.isPending || !selectedVoucher?.code || !selectedVoucher?.name}
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
            Are you sure you want to delete voucher "{selectedVoucher?.code}"? This action cannot be undone.
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
