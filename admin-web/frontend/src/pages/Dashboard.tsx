import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  LinearProgress,
  Avatar,
  IconButton,
  Tooltip,
} from '@mui/material'
import PeopleIcon from '@mui/icons-material/People'
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart'
import LocalOfferIcon from '@mui/icons-material/LocalOffer'
import InventoryIcon from '@mui/icons-material/Inventory'
import AttachMoneyIcon from '@mui/icons-material/AttachMoney'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import TrendingDownIcon from '@mui/icons-material/TrendingDown'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import PendingIcon from '@mui/icons-material/Pending'
import RefreshIcon from '@mui/icons-material/Refresh'
import { format } from 'date-fns'
import api from '../services/api'

const StatCard = ({ title, value, icon, color, gradient, trend, trendValue }: any) => (
  <Card
    sx={{
      background: gradient || `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)`,
      color: 'white',
      height: '100%',
      transition: 'transform 0.2s, box-shadow 0.2s',
      '&:hover': {
        transform: 'translateY(-4px)',
        boxShadow: 6,
      },
      position: 'relative',
      overflow: 'hidden',
    }}
  >
    <Box
      sx={{
        position: 'absolute',
        top: -20,
        right: -20,
        width: 120,
        height: 120,
        borderRadius: '50%',
        bgcolor: 'rgba(255,255,255,0.1)',
      }}
    />
    <CardContent>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Box>
          <Typography variant="body2" sx={{ opacity: 0.9, mb: 0.5 }}>
            {title}
          </Typography>
          <Typography variant="h3" sx={{ fontWeight: 700, mb: 1 }}>
            {value}
          </Typography>
          {trend && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              {trend === 'up' ? (
                <TrendingUpIcon sx={{ fontSize: 16 }} />
              ) : (
                <TrendingDownIcon sx={{ fontSize: 16 }} />
              )}
              <Typography variant="caption" sx={{ opacity: 0.9 }}>
                {trendValue}
              </Typography>
            </Box>
          )}
        </Box>
        <Box
          sx={{
            bgcolor: 'rgba(255,255,255,0.2)',
            borderRadius: '50%',
            p: 1.5,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
)

const SimpleBarChart = ({ data, color }: any) => {
  const maxValue = Math.max(...data.map((d: any) => d.value), 1)
  
  return (
    <Box sx={{ display: 'flex', alignItems: 'flex-end', gap: 1, height: 200, mt: 2 }}>
      {data.map((item: any, index: number) => (
        <Box key={index} sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <Box
            sx={{
              width: '100%',
              height: `${(item.value / maxValue) * 100}%`,
              bgcolor: color,
              borderRadius: '4px 4px 0 0',
              minHeight: '4px',
              transition: 'height 0.3s',
              position: 'relative',
              '&:hover': {
                opacity: 0.8,
              },
            }}
          >
            <Tooltip title={`${item.label}: ${item.value}`}>
              <Box sx={{ width: '100%', height: '100%' }} />
            </Tooltip>
          </Box>
          <Typography variant="caption" sx={{ mt: 0.5, fontSize: '0.7rem' }}>
            {item.label}
          </Typography>
        </Box>
      ))}
    </Box>
  )
}

const StatusDistribution = ({ orders }: any) => {
  const statusCounts = useMemo(() => {
    const counts: any = {}
    orders?.forEach((order: any) => {
      const status = order.status || order.order_status || 'Unknown'
      counts[status] = (counts[status] || 0) + 1
    })
    return counts
  }, [orders])

  const statusColors: any = {
    Completed: 'success',
    Delivered: 'success',
    Processing: 'info',
    Pending: 'warning',
    Confirmed: 'primary',
    Shipping: 'secondary',
    Cancelled: 'error',
  }

  const total = Object.values(statusCounts).reduce((sum: number, count: any) => sum + count, 0)

  return (
    <Box sx={{ mt: 2 }}>
      {Object.entries(statusCounts).map(([status, count]: [string, any]) => (
        <Box key={status} sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
            <Typography variant="body2">{status}</Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {count} ({total > 0 ? Math.round((count / total) * 100) : 0}%)
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={total > 0 ? (count / total) * 100 : 0}
            sx={{
              height: 8,
              borderRadius: 4,
              bgcolor: 'grey.200',
              '& .MuiLinearProgress-bar': {
                bgcolor: statusColors[status] || 'grey',
              },
            }}
          />
        </Box>
      ))}
    </Box>
  )
}

export default function Dashboard() {
  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get('/users').then((res) => res.data),
  })

  const { data: orders, isLoading: ordersLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: () => api.get('/orders').then((res) => res.data),
  })

  const { data: vouchers, isLoading: vouchersLoading } = useQuery({
    queryKey: ['vouchers'],
    queryFn: () => api.get('/vouchers').then((res) => res.data),
  })

  const { data: products, isLoading: productsLoading } = useQuery({
    queryKey: ['products'],
    queryFn: () => api.get('/products').then((res) => res.data),
  })

  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.get('/categories').then((res) => res.data),
  })

  const stats = useMemo(() => {
    if (!orders || !users || !products || !vouchers) return null

    const totalRevenue = orders.reduce((sum: number, order: any) => {
      const price = order.totalPrice || order.total_price || 0
      return sum + (typeof price === 'number' ? price : 0)
    }, 0)

    const completedOrders = orders.filter(
      (o: any) => (o.status || o.order_status) === 'Completed' || (o.status || o.order_status) === 'Delivered'
    ).length

    const pendingOrders = orders.filter((o: any) => (o.status || o.order_status) === 'Pending').length

    const activeProducts = products.filter((p: any) => p.isActive !== false && p.is_active !== false).length

    const activeVouchers = vouchers.filter((v: any) => v.isActive !== false && v.is_active !== false).length

    // Revenue by day (last 7 days)
    const revenueByDay: any = {}
    orders.forEach((order: any) => {
      const orderDate = order.orderDate || order.order_date
      if (orderDate) {
        try {
          const date = new Date(typeof orderDate === 'number' ? orderDate : parseInt(String(orderDate)))
          const dateKey = format(date, 'MM/dd')
          const price = order.totalPrice || order.total_price || 0
          revenueByDay[dateKey] = (revenueByDay[dateKey] || 0) + (typeof price === 'number' ? price : 0)
        } catch (e) {
          // Ignore invalid dates
        }
      }
    })

    const last7Days = Array.from({ length: 7 }, (_, i) => {
      const date = new Date()
      date.setDate(date.getDate() - (6 - i))
      return format(date, 'MM/dd')
    })

    const revenueChartData = last7Days.map((day) => ({
      label: day,
      value: revenueByDay[day] || 0,
    }))

    // Recent orders (last 5)
    const recentOrders = [...orders]
      .sort((a: any, b: any) => {
        const dateA = a.orderDate || a.order_date || 0
        const dateB = b.orderDate || b.order_date || 0
        return (typeof dateB === 'number' ? dateB : parseInt(String(dateB))) - 
               (typeof dateA === 'number' ? dateA : parseInt(String(dateA)))
      })
      .slice(0, 5)

    // Top products (by order count - simplified)
    const productCounts: any = {}
    orders.forEach((order: any) => {
      if (order.items && Array.isArray(order.items)) {
        order.items.forEach((item: any) => {
          const productName = item.productName || item.product_name || 'Unknown'
          productCounts[productName] = (productCounts[productName] || 0) + (item.quantity || 1)
        })
      }
    })

    const topProducts = Object.entries(productCounts)
      .map(([name, count]: [string, any]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5)

    return {
      totalRevenue,
      completedOrders,
      pendingOrders,
      activeProducts,
      activeVouchers,
      revenueChartData,
      recentOrders,
      topProducts,
    }
  }, [orders, users, products, vouchers])

  const isLoading = usersLoading || ordersLoading || vouchersLoading || productsLoading || categoriesLoading

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    )
  }

  if (!stats) {
    return (
      <Alert severity="info" sx={{ m: 2 }}>
        Loading dashboard data...
      </Alert>
    )
  }

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

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, background: 'linear-gradient(45deg, #1976d2 30%, #42a5f5 90%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          Dashboard Overview
        </Typography>
        <Tooltip title="Refresh data">
          <IconButton
            color="primary"
            onClick={() => {
              window.location.reload()
            }}
          >
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {/* Main Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Revenue"
            value={typeof stats.totalRevenue === 'number' ? stats.totalRevenue.toLocaleString('vi-VN') + 'đ' : '0đ'}
            icon={<AttachMoneyIcon sx={{ fontSize: 32 }} />}
            gradient="linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
            trend="up"
            trendValue="+12%"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Orders"
            value={orders?.length || 0}
            icon={<ShoppingCartIcon sx={{ fontSize: 32 }} />}
            gradient="linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
            trend="up"
            trendValue={`${stats.completedOrders} completed`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={users?.length || 0}
            icon={<PeopleIcon sx={{ fontSize: 32 }} />}
            gradient="linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
            trend="up"
            trendValue="+5%"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Products"
            value={stats.activeProducts}
            icon={<InventoryIcon sx={{ fontSize: 32 }} />}
            gradient="linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
            trend="up"
            trendValue={`${products?.length || 0} total`}
          />
        </Grid>
      </Grid>

      {/* Secondary Stats */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'warning.light', color: 'warning.contrastText' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <PendingIcon />
                <Typography variant="h6">Pending Orders</Typography>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 700 }}>
                {stats.pendingOrders}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'success.light', color: 'success.contrastText' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <CheckCircleIcon />
                <Typography variant="h6">Completed Orders</Typography>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 700 }}>
                {stats.completedOrders}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'info.light', color: 'info.contrastText' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <LocalOfferIcon />
                <Typography variant="h6">Active Vouchers</Typography>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 700 }}>
                {stats.activeVouchers}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'primary.light', color: 'primary.contrastText' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <InventoryIcon />
                <Typography variant="h6">Categories</Typography>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 700 }}>
                {categories?.length || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts and Tables Row */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* Revenue Chart */}
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%', boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Revenue (Last 7 Days)
              </Typography>
              <SimpleBarChart data={stats.revenueChartData} color="primary.main" />
            </CardContent>
          </Card>
        </Grid>

        {/* Order Status Distribution */}
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Order Status Distribution
              </Typography>
              <StatusDistribution orders={orders} />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Orders and Top Products */}
      <Grid container spacing={3}>
        {/* Recent Orders */}
        <Grid item xs={12} md={7}>
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Recent Orders
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Order ID</TableCell>
                      <TableCell>Customer</TableCell>
                      <TableCell>Total</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Date</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {stats.recentOrders.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5} align="center">
                          <Typography color="text.secondary">No recent orders</Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      stats.recentOrders.map((order: any) => {
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
                              formattedDate = format(new Date(dateValue), 'MM/dd HH:mm')
                            }
                          } catch (e) {
                            formattedDate = '-'
                          }
                        }

                        return (
                          <TableRow key={orderId} hover>
                            <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                              {typeof orderId === 'string' ? orderId.substring(0, 8) + '...' : String(orderId)}
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
                              />
                            </TableCell>
                            <TableCell>{formattedDate}</TableCell>
                          </TableRow>
                        )
                      })
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Top Products */}
        <Grid item xs={12} md={5}>
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Top Products
              </Typography>
              {stats.topProducts.length === 0 ? (
                <Typography color="text.secondary" align="center" sx={{ py: 3 }}>
                  No product data available
                </Typography>
              ) : (
                <Box>
                  {stats.topProducts.map((product: any, index: number) => (
                    <Box
                      key={index}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        p: 1.5,
                        mb: 1,
                        borderRadius: 1,
                        bgcolor: index % 2 === 0 ? 'grey.50' : 'transparent',
                        transition: 'background-color 0.2s',
                        '&:hover': {
                          bgcolor: 'action.hover',
                        },
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Avatar
                          sx={{
                            bgcolor: 'primary.main',
                            width: 32,
                            height: 32,
                            fontSize: '0.875rem',
                          }}
                        >
                          {index + 1}
                        </Avatar>
                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                          {product.name}
                        </Typography>
                      </Box>
                      <Chip
                        label={`${product.count} sold`}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}
