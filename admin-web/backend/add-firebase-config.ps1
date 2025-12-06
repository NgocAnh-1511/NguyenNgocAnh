# Script to add Firebase config to .env file

$envPath = Join-Path $PSScriptRoot ".env"

# Firebase configuration
$firebaseConfig = @"

# Firebase Admin SDK
FIREBASE_PROJECT_ID=coffeeshop1-9d0f0
FIREBASE_PRIVATE_KEY_ID=a8b37dfb2074036629ec03acde2d1be9c17b32c4
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDycYolc0RUdzvp\nau0nzSajq9sZVuHzHrhT6bxekz+Pu9FaULavR4Lo7tIo+rS+MUF8iQa2GNKDCcG8\nL0oVm79EH1UYvx7zuVDlVj+aGsjND7DfSp2ryPDELGL8k0lXGkzwd8JXnEvDKYzy\nsrt6iYQIjUmURuuhp7hVf5/JrcdRpdm7tspEBc41/i7qdIMbCPRejk0ZUZL4TZCj\nZotSJjof5S7jcFft+y04GWHqETA3XIzCBeD/1pcTrD/wtQL1RyT/WvKjJKGOlvSM\n2y20ivuWEwBRcBzL5m095nnQy7Ha6t3fZXA+QVDEhvOAiVMsPAV8Qwdl6fmtYcCk\nZiqZ1PGPAgMBAAECggEAKF01Of6itzSyFwQUNOayfVh3nhSO13qGB4PS4U9pqzAC\nxHh2BIuxj2oc4ka56y+/LpE6ZVHk+9JsMhJmJrUrVxenrtA5woEtfJPrUJMPWxi2\n2A46XvyRnU+jMVhXmGFgGD8QGPmuzBuHBGyJIHcfB4oiYHCdpWiLnOx7zsjty46w\nKE8PaNfw+T48Xo7z+q08gZpwAiO/jYPPOWlvULw1q7ABsAmU91y3Z48e7QX2zXN5\ng6Swvq/DiGiLcz7MA7+4nu2qxjw3tzfeUXsOXZ9mbr9Dvku7wrGdeJrA02Scx1Wj\n6A9stdREi2csq1WZJ9a3E6sYi/Or4tKpUZes8zvHrQKBgQD6iA37kAPDxWsiP4SA\nzQ1urb5+hz0zQJYDrSsggGjQWf6AOS/X6hkFFKds459kcZDxsrvD/yEjxbl5iqNo\nW9Mn/08oJV3RHv76UvCRCDuGcwvVg4YqInaeWC7tfLtX8JQ7eIDLIZDaWOZgSuOk\nBvlQMNrq4HK+yz1S4BbvKB01PQKBgQD3vEpT7X5uEFHZa1vwK1DimdDyNTjOYfdD\nsyhiU243/hnELsma8npekHYwQYtitvzzkW/24Cug1zBM1I/nFMrRL1bPfrYdOq01\nutwesEfuxu4pHxj3/0+zQPLKtwBjlUMwtwxDKsRjjIQgG5IM6el8wN277yVnzUiF\npTjjKngmuwKBgE1RpA8ZWTAYQ4IkXD5sClM4J+xgzJB+xE2KKh8zwiVbQOJNw+g9\nBU++GhXPAig7rGILqUtJ9+5UXXOXR+szO63Ca6mWLqgViteytS/tfIF4DBTvLH7g\nd/9Sc9kzf5YaV4UrIYSPbWcVvLPHKQ48YMFQ8p8+eetMSjbDlhBlLu55AoGAdI0o\nZv34Gm4hI+onkgG/By0Yqasyc56Rh09Vs5TO6RbqJmtgvD5SJgVPJXrgyeuPbERD\naeGjhQNYnPzwSuZ0WSAtdmeF5JCDhyDHiMSkwwjiaYRQ6fQ7Gfl3gLnCPBewSc4E\nveD0DmXAj56JAvxVtjTC3TUw4voooX/PGMkVSBsCgYEA4A8Z/V4KgS0M9F6fMtKC\nCl+JlZMjtRw1+OCKi53oQRfbrys11pH/Ym8nyLlXTNJZjMuq4wDkbueS7oF8wWII\nOz2P7uAY2vFEKt6hY9vojZ4qhVw9sZvbG+ggNreaLoND4vpJ+xgNZxsTYdnaKgyZ\nr8/LApp+7A6C/wVlBi5b2q0=\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-fbsvc@coffeeshop1-9d0f0.iam.gserviceaccount.com
FIREBASE_CLIENT_ID=105118494699544004864
FIREBASE_CLIENT_X509_CERT_URL=https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40coffeeshop1-9d0f0.iam.gserviceaccount.com
FIREBASE_DATABASE_URL=https://coffeeshop1-9d0f0-default-rtdb.firebaseio.com
"@

# Check if .env exists
if (Test-Path $envPath) {
    Write-Host "File .env đã tồn tại. Đang kiểm tra Firebase config..."
    
    # Check if Firebase config already exists
    $content = Get-Content $envPath -Raw
    if ($content -match "FIREBASE_PROJECT_ID") {
        Write-Host "⚠️  Firebase config đã tồn tại trong .env"
        Write-Host "Bạn có muốn ghi đè không? (y/n)"
        $response = Read-Host
        if ($response -ne "y") {
            Write-Host "Đã hủy."
            exit
        }
        # Remove old Firebase config
        $lines = Get-Content $envPath
        $newLines = @()
        $skip = $false
        foreach ($line in $lines) {
            if ($line -match "^# Firebase|^FIREBASE_") {
                $skip = $true
            }
            elseif ($skip -and $line -eq "") {
                $skip = $false
            }
            elseif (-not $skip) {
                $newLines += $line
            }
        }
        $newLines | Set-Content $envPath
    }
    
    # Append Firebase config
    Add-Content -Path $envPath -Value $firebaseConfig
    Write-Host "✅ Đã thêm Firebase config vào .env"
}
else {
    # Create new .env file
    Set-Content -Path $envPath -Value $firebaseConfig
    Write-Host "✅ Đã tạo file .env với Firebase config"
}

Write-Host "`n📋 Để test Firebase, chạy: npm run test:firebase"

