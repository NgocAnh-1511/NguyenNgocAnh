import * as admin from 'firebase-admin';
import * as dotenv from 'dotenv';
import { join } from 'path';

// Load environment variables
dotenv.config({ path: join(__dirname, '.env') });

async function testFirebase() {
    try {
        console.log('🔥 Testing Firebase Admin SDK...\n');

        // Check environment variables
        const projectId = process.env.FIREBASE_PROJECT_ID;
        const privateKey = process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n');
        const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
        const databaseURL = process.env.FIREBASE_DATABASE_URL;

        console.log('📋 Environment Variables:');
        console.log(`  FIREBASE_PROJECT_ID: ${projectId ? '✅ Set' : '❌ Missing'}`);
        console.log(`  FIREBASE_PRIVATE_KEY: ${privateKey ? '✅ Set' : '❌ Missing'}`);
        console.log(`  FIREBASE_CLIENT_EMAIL: ${clientEmail ? '✅ Set' : '❌ Missing'}`);
        console.log(`  FIREBASE_DATABASE_URL: ${databaseURL ? '✅ Set' : '❌ Missing'}\n`);

        if (!projectId || !privateKey || !clientEmail || !databaseURL) {
            console.error('❌ Missing required Firebase environment variables!');
            process.exit(1);
        }

        // Initialize Firebase Admin
        console.log('🔧 Initializing Firebase Admin...');
        const serviceAccount = {
            projectId,
            privateKey,
            clientEmail,
        };

        if (!admin.apps.length) {
            admin.initializeApp({
                credential: admin.credential.cert(serviceAccount as admin.ServiceAccount),
                databaseURL,
            });
        }

        const db = admin.database();
        console.log('✅ Firebase Admin initialized successfully!\n');

        // Test read
        console.log('📖 Testing read from Firebase...');
        const testRef = db.ref('Category');
        const snapshot = await testRef.once('value');
        const categories = snapshot.val();
        console.log(`✅ Read successful! Found ${categories ? Object.keys(categories).length : 0} categories\n`);

        // Test write
        console.log('✍️  Testing write to Firebase...');
        const testWriteRef = db.ref('_test/admin-test');
        await testWriteRef.set({
            message: 'Firebase Admin SDK test',
            timestamp: Date.now(),
        });
        console.log('✅ Write successful!\n');

        // Clean up test data
        console.log('🧹 Cleaning up test data...');
        await testWriteRef.remove();
        console.log('✅ Cleanup complete!\n');

        console.log('🎉 All tests passed! Firebase Admin SDK is working correctly!');
        process.exit(0);
    } catch (error: any) {
        console.error('❌ Test failed:', error.message);
        console.error('Error details:', error);
        process.exit(1);
    }
}

testFirebase();

