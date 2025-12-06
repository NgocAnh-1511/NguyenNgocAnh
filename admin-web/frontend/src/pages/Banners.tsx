import { Box, Typography, Paper, Alert } from '@mui/material'

export default function Banners() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Banners Management
      </Typography>
      <Paper sx={{ p: 3, mt: 2 }}>
        <Alert severity="info">
          Banners page - Coming soon. Data will be loaded from Firebase.
        </Alert>
      </Paper>
    </Box>
  )
}

