import { Injectable, OnModuleInit, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';

@Injectable()
export class FirebaseService implements OnModuleInit {
    private readonly logger = new Logger(FirebaseService.name);
    private db: admin.database.Database;

    constructor(private configService: ConfigService) { }

    onModuleInit() {
        try {
            // Check if Firebase is already initialized
            if (admin.apps.length > 0) {
                this.logger.log('Firebase already initialized');
                this.db = admin.database();
                return;
            }

            // Get Firebase credentials from environment variables
            const projectId = this.configService.get<string>('FIREBASE_PROJECT_ID');
            const privateKey = this.configService.get<string>('FIREBASE_PRIVATE_KEY')?.replace(/\\n/g, '\n');
            const clientEmail = this.configService.get<string>('FIREBASE_CLIENT_EMAIL');
            const databaseURL = this.configService.get<string>('FIREBASE_DATABASE_URL');

            // If credentials are not provided, skip initialization (for development)
            if (!projectId || !privateKey || !clientEmail || !databaseURL) {
                this.logger.warn('Firebase credentials not found. Firebase sync will be disabled.');
                return;
            }

            // Initialize Firebase Admin
            const serviceAccount = {
                projectId,
                privateKey,
                clientEmail,
            };

            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount as admin.ServiceAccount),
                databaseURL,
            });

            this.db = admin.database();
            this.logger.log('Firebase Admin initialized successfully');
        } catch (error) {
            this.logger.error('Failed to initialize Firebase Admin', error);
        }
    }

    /**
     * Check if Firebase is available
     */
    private isAvailable(): boolean {
        return this.db !== undefined;
    }

    /**
     * Sync Category to Firebase
     * Firebase structure: Category/{id} = { categoryId, title, pic, description }
     */
    async syncCategory(category: any): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping category sync');
            return;
        }

        try {
            const categoryRef = this.db.ref(`Category/${category.id}`);
            await categoryRef.set({
                categoryId: category.id.toString(),
                title: category.name || category.title || '',
                pic: category.imageUrl || category.pic || '',
                description: category.description || '',
            });
            this.logger.log(`Category ${category.id} synced to Firebase`);
        } catch (error) {
            this.logger.error(`Failed to sync category ${category.id} to Firebase`, error);
            throw error;
        }
    }

    /**
     * Sync Product to Firebase
     * Firebase structure: 
     * - Items/{id} = { itemId, title, price, originalPrice, pic, description, categoryId, stock, isActive }
     * - Popular/{id} = same structure (if isPopular is true)
     */
    async syncProduct(product: any): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping product sync');
            return;
        }

        try {
            // Convert imageUrl to picUrl array format (as expected by Android app)
            const picUrlArray = product.imageUrl
                ? [product.imageUrl]
                : (product.picUrl || (product.pic ? [product.pic] : []));

            // Ensure price is a number, not string
            const price = typeof product.price === 'string'
                ? parseFloat(product.price) || 0
                : (product.price || 0);
            const originalPrice = typeof product.originalPrice === 'string'
                ? parseFloat(product.originalPrice) || price
                : (product.originalPrice || price);

            const productData = {
                itemId: product.id.toString(),
                title: product.name || product.title || '',
                price: price, // Ensure it's a number
                originalPrice: originalPrice, // Ensure it's a number
                picUrl: picUrlArray, // Array format as expected by Android ItemsModel
                description: product.description || '',
                categoryId: product.categoryId?.toString() || product.category?.id?.toString() || '',
                stock: product.stock || 0,
                isActive: product.isActive !== false,
                rating: product.rating || 0,
                numberInCart: 0,
                extra: '',
            };

            // Sync to Items node
            const itemsRef = this.db.ref(`Items/${product.id}`);
            await itemsRef.set(productData);
            this.logger.log(`Product ${product.id} synced to Firebase Items - Price: ${price}, Title: ${productData.title}`);

            // If product is popular, also sync to Popular node
            if (product.isPopular || product.category?.name?.toLowerCase().includes('popular')) {
                const popularRef = this.db.ref(`Popular/${product.id}`);
                await popularRef.set({
                    itemId: productData.itemId,
                    title: productData.title,
                    price: productData.price,
                    originalPrice: productData.originalPrice,
                    picUrl: productData.picUrl, // Array format
                    description: productData.description,
                    categoryId: productData.categoryId,
                });
                this.logger.log(`Product ${product.id} synced to Firebase Popular`);
            } else {
                // Remove from Popular if not popular anymore
                const popularRef = this.db.ref(`Popular/${product.id}`);
                await popularRef.remove();
            }
        } catch (error) {
            this.logger.error(`Failed to sync product ${product.id} to Firebase`, error);
            throw error;
        }
    }

    /**
     * Delete Category from Firebase
     */
    async deleteCategory(categoryId: number): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping category deletion');
            return;
        }

        try {
            await this.db.ref(`Category/${categoryId}`).remove();
            this.logger.log(`Category ${categoryId} deleted from Firebase`);
        } catch (error) {
            this.logger.error(`Failed to delete category ${categoryId} from Firebase`, error);
            throw error;
        }
    }

    /**
     * Delete Product from Firebase
     */
    async deleteProduct(productId: number): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping product deletion');
            return;
        }

        try {
            await this.db.ref(`Items/${productId}`).remove();
            await this.db.ref(`Popular/${productId}`).remove();
            this.logger.log(`Product ${productId} deleted from Firebase`);
        } catch (error) {
            this.logger.error(`Failed to delete product ${productId} from Firebase`, error);
            throw error;
        }
    }

    /**
     * Sync all categories to Firebase (for initial sync)
     */
    async syncAllCategories(categories: any[]): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping bulk category sync');
            return;
        }

        try {
            const updates: any = {};
            categories.forEach((category) => {
                updates[`Category/${category.id}`] = {
                    categoryId: category.id.toString(),
                    title: category.name || category.title || '',
                    pic: category.imageUrl || category.pic || '',
                    description: category.description || '',
                };
            });

            await this.db.ref().update(updates);
            this.logger.log(`Synced ${categories.length} categories to Firebase`);
        } catch (error) {
            this.logger.error('Failed to sync all categories to Firebase', error);
            throw error;
        }
    }

    /**
     * Sync all products to Firebase (for initial sync)
     */
    async syncAllProducts(products: any[]): Promise<void> {
        if (!this.isAvailable()) {
            this.logger.warn('Firebase not available, skipping bulk product sync');
            return;
        }

        try {
            const itemsUpdates: any = {};
            const popularUpdates: any = {};

            products.forEach((product) => {
                // Convert imageUrl to picUrl array format (as expected by Android app)
                const picUrlArray = product.imageUrl
                    ? [product.imageUrl]
                    : (product.picUrl || (product.pic ? [product.pic] : []));

                // Ensure price is a number, not string
                const price = typeof product.price === 'string'
                    ? parseFloat(product.price) || 0
                    : (product.price || 0);
                const originalPrice = typeof product.originalPrice === 'string'
                    ? parseFloat(product.originalPrice) || price
                    : (product.originalPrice || price);

                const productData = {
                    itemId: product.id.toString(),
                    title: product.name || product.title || '',
                    price: price, // Ensure it's a number
                    originalPrice: originalPrice, // Ensure it's a number
                    picUrl: picUrlArray, // Array format as expected by Android ItemsModel
                    description: product.description || '',
                    categoryId: product.categoryId?.toString() || product.category?.id?.toString() || '',
                    stock: product.stock || 0,
                    isActive: product.isActive !== false,
                    rating: product.rating || 0,
                    numberInCart: 0,
                    extra: '',
                };

                itemsUpdates[`Items/${product.id}`] = productData;

                if (product.isPopular || product.category?.name?.toLowerCase().includes('popular')) {
                    popularUpdates[`Popular/${product.id}`] = {
                        itemId: productData.itemId,
                        title: productData.title,
                        price: productData.price,
                        originalPrice: productData.originalPrice,
                        picUrl: productData.picUrl, // Array format
                        description: productData.description,
                        categoryId: productData.categoryId,
                    };
                }
            });

            await this.db.ref().update({ ...itemsUpdates, ...popularUpdates });
            this.logger.log(`Synced ${products.length} products to Firebase`);
        } catch (error) {
            this.logger.error('Failed to sync all products to Firebase', error);
            throw error;
        }
    }
}

