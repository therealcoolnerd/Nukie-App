# Nukie Social Media Aggregator - Client-Side Data Storage Architecture

## Overview

The Nukie social media aggregator is designed with privacy as a core principle, ensuring that user data remains on their devices rather than being stored on central servers. This document outlines the architecture for implementing client-side data storage that supports this privacy-first approach while enabling the full functionality of a social media aggregator.

## Core Principles

1. **Data Sovereignty**: Users maintain complete control over their data
2. **Zero Server Storage**: No personal data or credentials stored on Nukie servers
3. **Secure Authentication**: Safe handling of platform credentials without exposing them
4. **Offline Functionality**: Core features work without constant internet connection
5. **Cross-Platform Compatibility**: Architecture supports Android initially with potential for expansion

## Data Categories

The client-side storage architecture needs to handle several categories of data:

### 1. User Profile Data
- Basic profile information
- App preferences and settings
- Theme and display preferences
- Connected account information (securely stored)

### 2. Social Media Content
- Cached posts from connected platforms
- User's own posts and drafts
- Interaction history (likes, comments, shares)
- Media files (images, videos) from feeds

### 3. Token System Data
- Cool Points balance and history
- Cool Creds balance and transactions
- Cool Pass inventory and usage records

### 4. Application State
- UI state and navigation history
- Feed positions and scroll states
- Filter preferences and search history

## Storage Technologies

For Android implementation, we'll utilize a combination of storage technologies:

### 1. Encrypted SharedPreferences
- For sensitive data like authentication tokens
- Encrypted using Android's EncryptedSharedPreferences
- Used for small key-value pairs that need secure storage

### 2. Room Database (SQLite)
- Primary structured data storage
- Provides offline capabilities with robust querying
- Supports complex data relationships
- Enables efficient data access patterns

### 3. DataStore
- For user preferences and app configuration
- Type-safe, asynchronous API
- Better performance than SharedPreferences for complex data

### 4. File Storage
- For media caching (images, videos)
- Organized directory structure in app's private storage
- Content-addressable storage for deduplication

### 5. Secure Keystore
- For storing encryption keys
- Utilizes Android Keystore System
- Hardware-backed security when available

## Data Models

### User Profile Schema
```kotlin
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "local_user",
    val displayName: String,
    val username: String,
    val bio: String,
    val avatarPath: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

### Connected Account Schema
```kotlin
@Entity(tableName = "connected_accounts")
data class ConnectedAccount(
    @PrimaryKey val id: String,
    val platform: String,
    val username: String,
    val displayName: String,
    val avatarPath: String,
    // Auth tokens stored in EncryptedSharedPreferences, not here
    val isActive: Boolean,
    val lastSyncTime: Long
)
```

### Social Post Schema
```kotlin
@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey val id: String,
    val platform: String,
    val originalPlatformId: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarPath: String,
    val content: String,
    val mediaUrls: List<String>,
    val localMediaPaths: List<String>,
    val publishedAt: Long,
    val fetchedAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val isLikedByUser: Boolean,
    val isBookmarked: Boolean
)
```

### Token System Schema
```kotlin
@Entity(tableName = "token_balances")
data class TokenBalance(
    @PrimaryKey val tokenType: String, // "cool_points", "cool_creds", "cool_pass"
    val balance: Int,
    val lastUpdated: Long
)

@Entity(tableName = "token_transactions")
data class TokenTransaction(
    @PrimaryKey val id: String,
    val tokenType: String,
    val amount: Int, // positive for credits, negative for debits
    val description: String,
    val timestamp: Long,
    val relatedEntityId: String? // e.g., post ID that earned points
)
```

## Data Flow Architecture

### 1. Authentication Flow
1. User authenticates with social platforms via OAuth
2. Access tokens stored in EncryptedSharedPreferences
3. Refresh tokens securely stored for token renewal
4. Authentication state maintained locally

### 2. Content Synchronization
1. App fetches content from connected platforms via APIs
2. Content parsed and stored in local Room database
3. Media files downloaded and cached in app's private storage
4. Sync timestamps tracked to optimize future requests

### 3. Post Creation and Publishing
1. User creates post in Nukie app
2. Draft saved locally in Room database
3. When published, app directly communicates with selected platforms
4. Success/failure status stored locally
5. No server-side processing or storage of user content

### 4. Token System Processing
1. Actions that earn tokens processed locally
2. Token balances updated in local database
3. Transaction history maintained for transparency
4. Premium features unlocked based on local token balance

## Offline Capabilities

The architecture supports robust offline functionality:

1. **Cached Content Access**: Previously synced content available offline
2. **Draft Creation**: Users can create posts offline to be published later
3. **Interaction Queueing**: Likes, comments, etc. queued locally when offline
4. **Synchronization on Reconnect**: Automatic sync when connection restored

## Security Considerations

1. **Encryption at Rest**: Sensitive data encrypted using industry-standard algorithms
2. **Secure Authentication**: OAuth tokens handled securely without exposing credentials
3. **Data Isolation**: App sandbox prevents unauthorized access to stored data
4. **Secure Deletion**: Clear data removal process when user chooses to disconnect

## Implementation Strategy

### Phase 1: Core Storage Implementation
1. Set up Room database with primary entities
2. Implement EncryptedSharedPreferences for sensitive data
3. Create repository layer for data access abstraction
4. Establish file storage structure for media caching

### Phase 2: Synchronization Mechanisms
1. Develop platform-specific API clients
2. Create sync workers for background synchronization
3. Implement conflict resolution strategies
4. Build caching policies for optimal performance

### Phase 3: Offline Support
1. Add offline indicators in UI
2. Implement action queueing system
3. Create background sync scheduling
4. Add data usage controls and preferences

## Conclusion

This client-side data storage architecture enables Nukie to deliver on its promise of a privacy-first social media aggregator where user data remains on their devices. By leveraging Android's storage technologies and following best practices for secure data handling, we can provide a robust, performant experience while respecting user privacy and data sovereignty.

The architecture is designed to be scalable and adaptable, allowing for future expansion to additional platforms beyond the initial Android implementation.
