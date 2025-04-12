# Nukie Social Media Aggregator
## Developer Documentation

### Architecture Overview

Nukie is a social media aggregator built for Android that uses the Lynx framework for cross-platform integration. The application follows a client-side data storage approach, ensuring all user data remains on the device.

#### Key Components

1. **UI Layer**
   - Implements MVVM architecture with ViewModels and LiveData
   - Features a custom dot matrix styling throughout the interface
   - Uses Navigation component for screen transitions

2. **Data Layer**
   - Room database for local storage
   - Repository pattern for data access
   - Unified data models for cross-platform compatibility

3. **Social Media Integration**
   - Lynx framework for network operations
   - Platform-specific clients for API interactions
   - Authentication and token management

4. **Testing Framework**
   - Functional testing with NukieTestRunner
   - UI testing with NukieUITestRunner
   - Test orchestration with NukieTestExecutor

### Project Structure

```
nukie-app/
├── android/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/nukie/app/
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── db/
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   ├── lynx/
│   │   │   │   │   ├── test/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── adapter/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   ├── post/
│   │   │   │   ├── res/
│   │   │   │   │   ├── drawable/
│   │   │   │   │   ├── layout/
│   │   │   │   │   ├── menu/
│   │   │   │   │   ├── navigation/
│   │   │   │   │   ├── values/
├── design/
│   ├── color_palette.css
│   ├── dot_matrix_font.css
│   ├── dot_matrix_style_guide.md
│   ├── mockups/
├── docs/
```

### Setup and Configuration

#### Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer
- JDK 11 or newer
- Android SDK 31 (Android 12) or newer
- Lynx SDK 1.0.0 or newer

#### Building the Project

1. Clone the repository:
   ```
   git clone https://github.com/nukie/nukie-app.git
   ```

2. Open the project in Android Studio:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository and select the `android` directory

3. Configure API keys:
   - Create a `secrets.properties` file in the project root
   - Add your API keys for each platform:
     ```
     INSTAGRAM_CLIENT_ID=your_instagram_client_id
     INSTAGRAM_CLIENT_SECRET=your_instagram_client_secret
     TIKTOK_CLIENT_KEY=your_tiktok_client_key
     YOUTUBE_API_KEY=your_youtube_api_key
     BLUESKY_APP_PASSWORD=your_bluesky_app_password
     MASTODON_CLIENT_ID=your_mastodon_client_id
     MASTODON_CLIENT_SECRET=your_mastodon_client_secret
     ```

4. Build the project:
   - Select "Build > Make Project" from the menu
   - Or use the keyboard shortcut Ctrl+F9 (Windows/Linux) or Cmd+F9 (Mac)

### Core Components

#### Data Models

The app uses unified data models to represent content from different platforms:

```kotlin
data class UnifiedPost(
    val id: String,
    val platformId: String,
    val platformType: PlatformType,
    val author: UnifiedAuthor,
    val content: String?,
    val mediaItems: List<UnifiedMedia>,
    val publishedAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val originalPlatformData: Any?
)
```

#### Database Schema

The Room database includes the following entities:

- UserProfile
- ConnectedAccount
- SocialPost
- PostMedia
- PostDraft
- DraftMedia
- TokenBalance
- TokenTransaction
- UserInteraction
- FeedPosition
- AppSetting
- SyncStatus

#### Lynx Integration

The Lynx integration is handled by three main components:

1. **LynxAggregator**: Manages the aggregation of content from multiple platforms
2. **LynxPlatformClients**: Platform-specific implementations for API interactions
3. **LynxIntegrationManager**: Bridge between Android components and Lynx functionality

### UI Components

#### Dot Matrix Styling

The dot matrix styling is implemented using custom drawables and styles:

- `dot_matrix_background.xml`: Background drawable with dot pattern
- `dot_shape.xml`: Individual dot shape definition
- `styles.xml`: Custom styles for text and components

#### Custom Views

- `DotMatrixTextView`: Custom TextView with dot matrix rendering
- `DotMatrixButton`: Custom Button with dot matrix styling
- `DotMatrixCardView`: Custom CardView with dot matrix border

### Social Media Integration

#### Authentication Flow

1. User selects a platform to connect
2. App initiates OAuth flow for the selected platform
3. User authenticates with the platform
4. Platform returns access token
5. App securely stores the token locally
6. App uses the token for subsequent API requests

#### Content Aggregation

1. App fetches content from each connected platform
2. Content is transformed into unified data models
3. Content is stored in the local database
4. UI displays content from the database

#### Cross-Platform Posting

1. User creates a post in the app
2. User selects platforms to post to
3. App sends the post to each selected platform
4. App stores the post in the local database
5. UI updates to show the new post

### Testing

#### Running Tests

1. Open the TestActivity:
   - Navigate to the app in the emulator or device
   - Go to Settings > Developer Options > Testing
   - Tap "Open Test Suite"

2. Run tests from the command line:
   ```
   ./gradlew connectedAndroidTest
   ```

#### Adding New Tests

1. Create a new test class in the appropriate package
2. Extend the appropriate base test class
3. Implement test methods with JUnit annotations
4. Run the tests to verify functionality

### Deployment

#### Generating a Release Build

1. Update the version code and name in `build.gradle`:
   ```gradle
   android {
       defaultConfig {
           versionCode 1
           versionName "1.0.0"
       }
   }
   ```

2. Create a signing configuration:
   - Create a keystore file if you don't have one:
     ```
     keytool -genkey -v -keystore nukie.keystore -alias nukie -keyalg RSA -keysize 2048 -validity 10000
     ```
   - Add the signing configuration to `build.gradle`:
     ```gradle
     android {
         signingConfigs {
             release {
                 storeFile file("nukie.keystore")
                 storePassword "your_keystore_password"
                 keyAlias "nukie"
                 keyPassword "your_key_password"
             }
         }
         buildTypes {
             release {
                 signingConfig signingConfigs.release
                 minifyEnabled true
                 proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
             }
         }
     }
     ```

3. Generate the APK:
   - Select "Build > Generate Signed Bundle / APK" from the menu
   - Follow the wizard to create a signed APK or App Bundle

#### Publishing to Google Play

1. Create a Google Play Developer account if you don't have one
2. Create a new application in the Google Play Console
3. Upload the signed APK or App Bundle
4. Fill in the store listing information
5. Set up pricing and distribution
6. Submit for review

### Extending Nukie

#### Adding a New Platform

1. Create a new platform type in `PlatformType.kt`:
   ```kotlin
   enum class PlatformType {
       INSTAGRAM,
       TIKTOK,
       YOUTUBE,
       BLUESKY,
       MASTODON,
       NEW_PLATFORM
   }
   ```

2. Create a new platform client in `LynxPlatformClients.kt`:
   ```kotlin
   class NewPlatformClient implements LynxPlatformClient {
       // Implement the required methods
   }
   ```

3. Add the new client to the `LynxAggregator`:
   ```kotlin
   private void initializePlatformClients() {
       // Existing platforms
       platformClients.put(PlatformType.NEW_PLATFORM, new NewPlatformClient(httpClient));
   }
   ```

4. Update the UI to include the new platform

#### Customizing the Dot Matrix Style

1. Modify the dot matrix parameters in `dot_matrix_background.xml`:
   ```xml
   <item name="dot_size">2dp</item>
   <item name="dot_spacing">4dp</item>
   <item name="dot_color">@color/primary</item>
   ```

2. Update the color palette in `colors.xml`:
   ```xml
   <color name="primary">#FF5722</color>
   <color name="primary_variant">#E64A19</color>
   <color name="secondary">#FFC107</color>
   ```

3. Adjust the text styles in `styles.xml`:
   ```xml
   <style name="TextAppearance.Nukie.DotMatrix" parent="TextAppearance.MaterialComponents.Body1">
       <item name="android:fontFamily">@font/dot_matrix</item>
       <item name="android:textSize">16sp</item>
   </style>
   ```

### Troubleshooting

#### Common Issues

1. **API Authentication Failures**
   - Check that API keys are correctly configured in `secrets.properties`
   - Verify that the OAuth redirect URI is correctly set up in the platform's developer console
   - Check that the device has a valid internet connection

2. **Database Migration Errors**
   - Ensure that database migrations are properly defined for schema changes
   - Consider using Room's fallback migration strategy for development:
     ```kotlin
     Room.databaseBuilder(context, NukieDatabase::class.java, "nukie-db")
         .fallbackToDestructiveMigration()
         .build()
     ```

3. **UI Rendering Issues**
   - Check that the dot matrix resources are correctly defined
   - Verify that the custom views are properly initialized
   - Test on different screen sizes and densities

#### Debugging Tips

1. Enable verbose logging:
   ```kotlin
   if (BuildConfig.DEBUG) {
       Timber.plant(Timber.DebugTree())
   }
   ```

2. Use the test suite to isolate issues:
   ```
   ./gradlew connectedAndroidTest --tests "com.nukie.app.test.NukieTestRunner"
   ```

3. Monitor database operations with Room's debug mode:
   ```kotlin
   Room.databaseBuilder(context, NukieDatabase::class.java, "nukie-db")
       .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
       .build()
   ```

### Support and Resources

- GitHub Repository: https://github.com/nukie/nukie-app
- Issue Tracker: https://github.com/nukie/nukie-app/issues
- Developer Forum: https://forum.nukieapp.com
- API Documentation: https://docs.nukieapp.com/api

For additional support, contact the development team at dev@nukieapp.com.
