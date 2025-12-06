package com.example.coffeeshop.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "CoffeeShopDB"
        private const val DATABASE_VERSION = 8  // Version 8: Add vouchers table

        // Table Users
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "user_id"
        const val COL_PHONE_NUMBER = "phone_number"
        const val COL_FULL_NAME = "full_name"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_AVATAR_PATH = "avatar_path"
        const val COL_CREATED_AT = "created_at"
        const val COL_IS_LOGGED_IN = "is_logged_in"
        const val COL_AUTH_TOKEN = "auth_token"
        const val COL_IS_ADMIN = "is_admin"

        // Table Cart
        const val TABLE_CART = "cart"
        const val COL_CART_ID = "id"
        const val COL_CART_USER_ID = "user_id"  // Thêm cột user_id
        const val COL_ITEM_TITLE = "item_title"
        const val COL_ITEM_JSON = "item_json"
        const val COL_QUANTITY = "quantity"

        // Table Orders
        const val TABLE_ORDERS = "orders"
        const val COL_ORDER_ID = "order_id"
        const val COL_ORDER_USER_ID = "user_id"  // Thêm cột user_id
        const val COL_ITEMS_JSON = "items_json"
        const val COL_TOTAL_PRICE = "total_price"
        const val COL_ORDER_DATE = "order_date"
        const val COL_STATUS = "status"
        const val COL_DELIVERY_ADDRESS = "delivery_address"
        const val COL_ORDER_PHONE = "phone_number"
        const val COL_CUSTOMER_NAME = "customer_name"
        const val COL_PAYMENT_METHOD = "payment_method"

        // Table Addresses
        const val TABLE_ADDRESSES = "addresses"
        const val COL_ADDRESS_ID = "id"
        const val COL_ADDRESS_USER_ID = "user_id"  // Thêm cột user_id
        const val COL_ADDRESS_NAME = "name"
        const val COL_ADDRESS_PHONE = "phone"
        const val COL_ADDRESS = "address"
        const val COL_IS_DEFAULT = "is_default"

        // Table Wishlist
        const val TABLE_WISHLIST = "wishlist"
        const val COL_WISHLIST_ID = "id"
        const val COL_WISHLIST_USER_ID = "user_id"  // Thêm cột user_id
        const val COL_WISHLIST_ITEM_TITLE = "item_title"
        const val COL_WISHLIST_ITEM_JSON = "item_json"

        // Table Vouchers
        const val TABLE_VOUCHERS = "vouchers"
        const val COL_VOUCHER_ID = "voucher_id"
        const val COL_VOUCHER_CODE = "code"
        const val COL_VOUCHER_DISCOUNT_PERCENT = "discount_percent"
        const val COL_VOUCHER_DISCOUNT_AMOUNT = "discount_amount"
        const val COL_VOUCHER_DISCOUNT_TYPE = "discount_type"
        const val COL_VOUCHER_MIN_ORDER_AMOUNT = "min_order_amount"
        const val COL_VOUCHER_MAX_DISCOUNT_AMOUNT = "max_discount_amount"
        const val COL_VOUCHER_START_DATE = "start_date"
        const val COL_VOUCHER_END_DATE = "end_date"
        const val COL_VOUCHER_USAGE_LIMIT = "usage_limit"
        const val COL_VOUCHER_USED_COUNT = "used_count"
        const val COL_VOUCHER_IS_ACTIVE = "is_active"
        const val COL_VOUCHER_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID TEXT PRIMARY KEY,
                $COL_PHONE_NUMBER TEXT NOT NULL,
                $COL_FULL_NAME TEXT,
                $COL_EMAIL TEXT,
                $COL_PASSWORD TEXT,
                $COL_AVATAR_PATH TEXT,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_IS_LOGGED_IN INTEGER DEFAULT 0,
                $COL_AUTH_TOKEN TEXT,
                $COL_IS_ADMIN INTEGER DEFAULT 0
            )
        """.trimIndent()

        // Create Cart table
        val createCartTable = """
            CREATE TABLE $TABLE_CART (
                $COL_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CART_USER_ID TEXT NOT NULL,
                $COL_ITEM_TITLE TEXT NOT NULL,
                $COL_ITEM_JSON TEXT NOT NULL,
                $COL_QUANTITY INTEGER NOT NULL DEFAULT 1,
                UNIQUE($COL_CART_USER_ID, $COL_ITEM_TITLE)
            )
        """.trimIndent()

        // Create Orders table
        val createOrdersTable = """
            CREATE TABLE $TABLE_ORDERS (
                $COL_ORDER_ID TEXT PRIMARY KEY,
                $COL_ORDER_USER_ID TEXT NOT NULL,
                $COL_ITEMS_JSON TEXT NOT NULL,
                $COL_TOTAL_PRICE REAL NOT NULL,
                $COL_ORDER_DATE INTEGER NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_DELIVERY_ADDRESS TEXT,
                $COL_ORDER_PHONE TEXT,
                $COL_CUSTOMER_NAME TEXT,
                $COL_PAYMENT_METHOD TEXT
            )
        """.trimIndent()

        // Create Addresses table
        val createAddressesTable = """
            CREATE TABLE $TABLE_ADDRESSES (
                $COL_ADDRESS_ID TEXT PRIMARY KEY,
                $COL_ADDRESS_USER_ID TEXT NOT NULL,
                $COL_ADDRESS_NAME TEXT NOT NULL,
                $COL_ADDRESS_PHONE TEXT NOT NULL,
                $COL_ADDRESS TEXT NOT NULL,
                $COL_IS_DEFAULT INTEGER DEFAULT 0
            )
        """.trimIndent()

        // Create Wishlist table
        val createWishlistTable = """
            CREATE TABLE $TABLE_WISHLIST (
                $COL_WISHLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WISHLIST_USER_ID TEXT NOT NULL,
                $COL_WISHLIST_ITEM_TITLE TEXT NOT NULL,
                $COL_WISHLIST_ITEM_JSON TEXT NOT NULL,
                UNIQUE($COL_WISHLIST_USER_ID, $COL_WISHLIST_ITEM_TITLE)
            )
        """.trimIndent()

        // Create Vouchers table
        val createVouchersTable = """
            CREATE TABLE $TABLE_VOUCHERS (
                $COL_VOUCHER_ID TEXT PRIMARY KEY,
                $COL_VOUCHER_CODE TEXT NOT NULL UNIQUE,
                $COL_VOUCHER_DISCOUNT_PERCENT REAL DEFAULT 0,
                $COL_VOUCHER_DISCOUNT_AMOUNT REAL DEFAULT 0,
                $COL_VOUCHER_DISCOUNT_TYPE TEXT NOT NULL DEFAULT 'PERCENT',
                $COL_VOUCHER_MIN_ORDER_AMOUNT REAL DEFAULT 0,
                $COL_VOUCHER_MAX_DISCOUNT_AMOUNT REAL DEFAULT 0,
                $COL_VOUCHER_START_DATE INTEGER NOT NULL,
                $COL_VOUCHER_END_DATE INTEGER NOT NULL,
                $COL_VOUCHER_USAGE_LIMIT INTEGER DEFAULT 0,
                $COL_VOUCHER_USED_COUNT INTEGER DEFAULT 0,
                $COL_VOUCHER_IS_ACTIVE INTEGER DEFAULT 1,
                $COL_VOUCHER_DESCRIPTION TEXT
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCartTable)
        db.execSQL(createOrdersTable)
        db.execSQL(createAddressesTable)
        db.execSQL(createWishlistTable)
        db.execSQL(createVouchersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            // Tạo bảng vouchers cho version 8
            try {
                val createVouchersTable = """
                    CREATE TABLE IF NOT EXISTS $TABLE_VOUCHERS (
                        $COL_VOUCHER_ID TEXT PRIMARY KEY,
                        $COL_VOUCHER_CODE TEXT NOT NULL UNIQUE,
                        $COL_VOUCHER_DISCOUNT_PERCENT REAL DEFAULT 0,
                        $COL_VOUCHER_DISCOUNT_AMOUNT REAL DEFAULT 0,
                        $COL_VOUCHER_DISCOUNT_TYPE TEXT NOT NULL DEFAULT 'PERCENT',
                        $COL_VOUCHER_MIN_ORDER_AMOUNT REAL DEFAULT 0,
                        $COL_VOUCHER_MAX_DISCOUNT_AMOUNT REAL DEFAULT 0,
                        $COL_VOUCHER_START_DATE INTEGER NOT NULL,
                        $COL_VOUCHER_END_DATE INTEGER NOT NULL,
                        $COL_VOUCHER_USAGE_LIMIT INTEGER DEFAULT 0,
                        $COL_VOUCHER_USED_COUNT INTEGER DEFAULT 0,
                        $COL_VOUCHER_IS_ACTIVE INTEGER DEFAULT 1,
                        $COL_VOUCHER_DESCRIPTION TEXT
                    )
                """.trimIndent()
                db.execSQL(createVouchersTable)
            } catch (e: Exception) {
                // Bỏ qua nếu bảng đã tồn tại
            }
        }
        
        if (oldVersion < 7) {
            // Xóa cột role nếu tồn tại (SQLite không hỗ trợ DROP COLUMN trực tiếp)
            // Cần tạo lại bảng không có cột role
            try {
                // Tạo bảng tạm mới không có cột role
                db.execSQL("""
                    CREATE TABLE ${TABLE_USERS}_new (
                        $COL_USER_ID TEXT PRIMARY KEY,
                        $COL_PHONE_NUMBER TEXT NOT NULL,
                        $COL_FULL_NAME TEXT,
                        $COL_EMAIL TEXT,
                        $COL_PASSWORD TEXT,
                        $COL_AVATAR_PATH TEXT,
                        $COL_CREATED_AT INTEGER NOT NULL,
                        $COL_IS_LOGGED_IN INTEGER DEFAULT 0,
                        $COL_AUTH_TOKEN TEXT,
                        $COL_IS_ADMIN INTEGER DEFAULT 0
                    )
                """.trimIndent())
                
                // Copy dữ liệu từ bảng cũ sang bảng mới (bỏ qua cột role nếu có)
                db.execSQL("""
                    INSERT INTO ${TABLE_USERS}_new (
                        $COL_USER_ID, $COL_PHONE_NUMBER, $COL_FULL_NAME, $COL_EMAIL,
                        $COL_PASSWORD, $COL_AVATAR_PATH, $COL_CREATED_AT,
                        $COL_IS_LOGGED_IN, $COL_AUTH_TOKEN, $COL_IS_ADMIN
                    )
                    SELECT 
                        $COL_USER_ID, $COL_PHONE_NUMBER, $COL_FULL_NAME, $COL_EMAIL,
                        $COL_PASSWORD, $COL_AVATAR_PATH, $COL_CREATED_AT,
                        $COL_IS_LOGGED_IN, $COL_AUTH_TOKEN, $COL_IS_ADMIN
                    FROM $TABLE_USERS
                """.trimIndent())
                
                // Xóa bảng cũ
                db.execSQL("DROP TABLE $TABLE_USERS")
                
                // Đổi tên bảng mới thành tên cũ
                db.execSQL("ALTER TABLE ${TABLE_USERS}_new RENAME TO $TABLE_USERS")
            } catch (e: Exception) {
                // Nếu có lỗi, có thể cột role không tồn tại hoặc có vấn đề khác
                // Thử thêm cột is_admin nếu chưa có (cho version cũ hơn)
                try {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_IS_ADMIN INTEGER DEFAULT 0")
                } catch (e2: Exception) {
                    // Bỏ qua nếu cột đã tồn tại
                }
            }
        }
        
        if (oldVersion < 6 && oldVersion >= 4) {
            // Thêm cột is_admin nếu chưa có (thiết bị cũ từ version 4-5)
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_IS_ADMIN INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // Cột đã tồn tại hoặc có lỗi, bỏ qua để tránh crash
            }
        }

        // Nếu version quá cũ, drop và tạo lại
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ADDRESSES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_WISHLIST")
            onCreate(db)
        }
    }
}

