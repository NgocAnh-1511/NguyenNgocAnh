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
  Chip,
  IconButton,
  Tooltip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  MenuItem,
  Snackbar,
  Card,
  CardContent,
  Grid,
  Divider,
  List,
  ListItem,
  ListItemText,
} from '@mui/material'
import RefreshIcon from '@mui/icons-material/Refresh'
import VisibilityIcon from '@mui/icons-material/Visibility'
import DeleteIcon from '@mui/icons-material/Delete'
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart'
import AttachMoneyIcon from '@mui/icons-material/AttachMoney'
import PendingIcon from '@mui/icons-material/Pending'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { format } from 'date-fns'
import api from '../services/api'

const getStatusColor = (status: string) => {
  switch (status) {
    case 'Completed':
    case 'Delivered':
      return 'success'
    case 'Processing':
      return 'info'
    case 'Pending':
      return 'warning'
    case 'Confirmed':
      return 'primary'
    case 'Shipping':
      return 'secondary'
    case 'Cancelled':
      return 'error'
    default:
      return 'default'
  }
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'Completed':
    case 'Delivered':
      return <CheckCircleIcon />
    case 'Pending':
      return <PendingIcon />
    default:
      return <ShoppingCartIcon />
  }
}

const statusOptions = ['Pending', 'Confirmed', 'Processing', 'Shipping', 'Delivered', 'Completed', 'Cancelled']

export default function Orders() {
  const [openDetailsDialog, setOpenDetailsDialog] = useState(false)
  const [openStatusDialog, setOpenStatusDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState<any>(null)
  const [newStatus, setNewStatus] = useState('')
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' })
  const queryClient = useQueryClient()

  const { data: orders, isLoading, error, refetch, isRefetching } = useQuery({
    queryKey: ['orders'],
    queryFn: () => api.get('/orders').then((res) => res.data),
    staleTime: 0,
    refetchOnWindowFocus: true,
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.patch(`/orders/${id}/status`, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      setOpenStatusDialog(false)
      setSelectedOrder(null)
      setSnackbar({ open: true, message: 'Order status updated successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to update order status', severity: 'error' })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/orders/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      setOpenDeleteDialog(false)
      setSelectedOrder(null)
      setSnackbar({ open: true, message: 'Order deleted successfully', severity: 'success' })
    },
    onError: () => {
      setSnackbar({ open: true, message: 'Failed to delete order', severity: 'error' })
    },
  })

  const handleOpenDetails = (order: any) => {
    setSelectedOrder(order)
    setOpenDetailsDialog(true)
  }

  const handleOpenStatus = (order: any) => {
    setSelectedOrder(order)
    setNewStatus(order.status || order.order_status || 'Pending')
    setOpenStatusDialog(true)
  }

  const handleOpenDelete = (order: any) => {
    setSelectedOrder(order)
    setOpenDeleteDialog(true)
  }

  const handleUpdateStatus = () => {
    if (selectedOrder && newStatus) {
      const orderId = selectedOrder.orderId || selectedOrder.order_id
      updateStatusMutation.mutate({ id: orderId, status: newStatus })
    }
  }

  const handleDelete = () => {
    if (selectedOrder) {
      const orderId = selectedOrder.orderId || selectedOrder.order_id
      deleteMutation.mutate(orderId)
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
        Error loading orders
      </Alert>
    )
  }

  const totalOrders = orders?.length || 0
  const totalRevenue = orders?.reduce((sum: number, order: any) => {
    const price = order.totalPrice || order.total_price || 0
    return sum + (typeof price === 'number' ? price : 0)
  }, 0) || 0
  const pendingOrders = orders?.filter((o: any) => (o.status || o.order_status) === 'Pending').length || 0

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
          Orders Management
        </Typography>
        <Tooltip title="Refresh orders">
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
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'primary.light', color: 'primary.contrastText', height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <ShoppingCartIcon />
                <Typography variant="h6">Total Orders</Typography>
              </Box>
              <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
                {totalOrders}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'success.light', color: 'success.contrastText', height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AttachMoneyIcon />
                <Typography variant="h6">Total Revenue</Typography>
              </Box>
              <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
                {typeof totalRevenue === 'number' ? totalRevenue.toLocaleString('vi-VN') + 'đ' : '0đ'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'warning.light', color: 'warning.contrastText', height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <PendingIcon />
                <Typography variant="h6">Pending Orders</Typography>
              </Box>
              <Typography variant="h4" sx={{ mt: 1, fontWeight: 600 }}>
                {pendingOrders}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {(isLoading || isRefetching) && (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}

      <TableContainer component={Paper} sx={{ mt: 2, boxShadow: 3 }}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'primary.main' }}>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Order ID</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Customer</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Total</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Status</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }}>Date</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 600 }} align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!orders || orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">
                    No orders found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              orders.map((order: any) => {
                const orderId = order.orderId || order.order_id || '-'
                const customerName = order.customerName || order.customer_name || '-'
                const totalPrice = order.totalPrice || order.total_price || 0
                const status = order.status || order.order_status || '-'
                const orderDate = order.orderDate || order.order_date
                
                let formattedDate = '-'
                if (orderDate) {
                  try {
                    const dateValue = typeof orderDate === 'number' ? orderDate : parseInt(String(orderDate))
                    if (!isNaN(dateValue)) {
                      formattedDate = format(new Date(dateValue), 'dd/MM/yyyy HH:mm')
                    }
                  } catch (e) {
                    formattedDate = '-'
                  }
                }
                
                return (
                  <TableRow 
                    key={orderId}
                    sx={{ 
                      '&:hover': { bgcolor: 'action.hover' },
                      transition: 'background-color 0.2s'
                    }}
                  >
                    <TableCell>
                      {typeof orderId === 'string' ? orderId.substring(0, 12) + '...' : String(orderId)}
                    </TableCell>
                    <TableCell>{customerName}</TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'success.main' }}>
                        {typeof totalPrice === 'number' ? totalPrice.toLocaleString('vi-VN') + 'đ' : String(totalPrice)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={status}
                        color={getStatusColor(status) as any}
                        size="small"
                        icon={getStatusIcon(status)}
                      />
                    </TableCell>
                    <TableCell>{formattedDate}</TableCell>
                    <TableCell align="center">
                      <Tooltip title="View Details">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => handleOpenDetails(order)}
                        >
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Update Status">
                        <IconButton
                          size="small"
                          color="warning"
                          onClick={() => handleOpenStatus(order)}
                        >
                          <PendingIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete Order">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenDelete(order)}
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

      {/* Order Details Dialog */}
      <Dialog open={openDetailsDialog} onClose={() => setOpenDetailsDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 600, bgcolor: 'primary.main', color: 'white' }}>
          Order Details
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          {selectedOrder && (
            <Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Order ID</Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedOrder.orderId || selectedOrder.order_id}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Status</Typography>
                  <Chip
                    label={selectedOrder.status || selectedOrder.order_status}
                    color={getStatusColor(selectedOrder.status || selectedOrder.order_status) as any}
                    sx={{ mb: 2 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Customer Name</Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedOrder.customerName || selectedOrder.customer_name || '-'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Phone Number</Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedOrder.phoneNumber || selectedOrder.phone_number || '-'}
                  </Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="subtitle2" color="text.secondary">Delivery Address</Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedOrder.deliveryAddress || selectedOrder.delivery_address || '-'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Total Price</Typography>
                  <Typography variant="h6" sx={{ mb: 2, color: 'success.main', fontWeight: 600 }}>
                    {(selectedOrder.totalPrice || selectedOrder.total_price || 0).toLocaleString('vi-VN')}đ
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">Payment Method</Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {selectedOrder.paymentMethod || selectedOrder.payment_method || '-'}
                  </Typography>
                </Grid>
              </Grid>
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" sx={{ mb: 1 }}>Order Items</Typography>
              <List>
                {selectedOrder.items?.map((item: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={item.productName || item.product_name}
                      secondary={`Quantity: ${item.quantity || 0} - Price: ${(item.price || 0).toLocaleString('vi-VN')}đ`}
                    />
                  </ListItem>
                ))}
              </List>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDetailsDialog(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Update Status Dialog */}
      <Dialog open={openStatusDialog} onClose={() => setOpenStatusDialog(false)}>
        <DialogTitle sx={{ fontWeight: 600 }}>Update Order Status</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ minWidth: 300 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Select New Status</Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {statusOptions.map((status) => (
                <Button
                  key={status}
                  variant={newStatus === status ? 'contained' : 'outlined'}
                  color={getStatusColor(status) as any}
                  onClick={() => setNewStatus(status)}
                  sx={{ justifyContent: 'flex-start' }}
                >
                  {status}
                </Button>
              ))}
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenStatusDialog(false)}>Cancel</Button>
          <Button
            onClick={handleUpdateStatus}
            variant="contained"
            disabled={updateStatusMutation.isPending || !newStatus}
          >
            {updateStatusMutation.isPending ? 'Updating...' : 'Update'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
        <DialogTitle sx={{ fontWeight: 600 }}>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete order "{selectedOrder?.orderId || selectedOrder?.order_id}"? This action cannot be undone.
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
