# Nukie Social Media Aggregator - Database Schema

## Overview

This document details the database schema for the Nukie social media aggregator's client-side storage. The schema is designed to support the privacy-first approach where all user data remains on the device while enabling full functionality of a social media aggregator.

## Room Database Schema

The following schema is implemented using Android's Room persistence library, which provides an abstraction layer over SQLite.

### Tables

#### 1. User Profile

```sql
CREATE TABLE user_profile (
    id TEXT PRIMARY KEY DEFAULT 'local_user',
    display_name TEXT NOT NULL,
    username TEXT NOT NULL,
    bio TEXT,
    avatar_path TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 2. Connected Accounts

```sql
CREATE TABLE connected_accounts (
    id TEXT PRIMARY KEY,
    platform TEXT NOT NULL,
    username TEXT NOT NULL,
    display_name TEXT NOT NULL,
    avatar_path TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    last_sync_time INTEGER NOT NULL,
    
    UNIQUE(platform, username)
);
```

#### 3. Social Posts

```sql
CREATE TABLE social_posts (
    id TEXT PRIMARY KEY,
    platform TEXT NOT NULL,
    original_platform_id TEXT NOT NULL,
    author_id TEXT NOT NULL,
    author_username TEXT NOT NULL,
    author_display_name TEXT NOT NULL,
    author_avatar_path TEXT,
    content TEXT,
    published_at INTEGER NOT NULL,
    fetched_at INTEGER NOT NULL,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    share_count INTEGER NOT NULL DEFAULT 0,
    is_liked_by_user INTEGER NOT NULL DEFAULT 0,
    is_bookmarked INTEGER NOT NULL DEFAULT 0,
    
    UNIQUE(platform, original_platform_id)
);
```

#### 4. Post Media

```sql
CREATE TABLE post_media (
    id TEXT PRIMARY KEY,
    post_id TEXT NOT NULL,
    media_type TEXT NOT NULL, -- 'image', 'video', 'gif', etc.
    remote_url TEXT NOT NULL,
    local_path TEXT,
    width INTEGER,
    height INTEGER,
    duration INTEGER, -- for videos
    thumbnail_path TEXT,
    alt_text TEXT,
    
    FOREIGN KEY (post_id) REFERENCES social_posts(id) ON DELETE CASCADE
);
```

#### 5. Post Drafts

```sql
CREATE TABLE post_drafts (
    id TEXT PRIMARY KEY,
    content TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    target_platforms TEXT NOT NULL, -- JSON array of platform IDs
    scheduled_time INTEGER,
    status TEXT NOT NULL DEFAULT 'draft' -- 'draft', 'scheduled', 'publishing', 'failed'
);
```

#### 6. Draft Media

```sql
CREATE TABLE draft_media (
    id TEXT PRIMARY KEY,
    draft_id TEXT NOT NULL,
    media_type TEXT NOT NULL,
    local_path TEXT NOT NULL,
    width INTEGER,
    height INTEGER,
    duration INTEGER,
    thumbnail_path TEXT,
    alt_text TEXT,
    
    FOREIGN KEY (draft_id) REFERENCES post_drafts(id) ON DELETE CASCADE
);
```

#### 7. Token Balances

```sql
CREATE TABLE token_balances (
    token_type TEXT PRIMARY KEY,
    balance INTEGER NOT NULL DEFAULT 0,
    last_updated INTEGER NOT NULL
);
```

#### 8. Token Transactions

```sql
CREATE TABLE token_transactions (
    id TEXT PRIMARY KEY,
    token_type TEXT NOT NULL,
    amount INTEGER NOT NULL,
    description TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    related_entity_id TEXT,
    
    FOREIGN KEY (token_type) REFERENCES token_balances(token_type)
);
```

#### 9. User Interactions

```sql
CREATE TABLE user_interactions (
    id TEXT PRIMARY KEY,
    post_id TEXT NOT NULL,
    interaction_type TEXT NOT NULL, -- 'like', 'comment', 'share', 'save'
    content TEXT, -- for comments
    created_at INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending', -- 'pending', 'synced', 'failed'
    
    FOREIGN KEY (post_id) REFERENCES social_posts(id) ON DELETE CASCADE
);
```

#### 10. Feed Positions

```sql
CREATE TABLE feed_positions (
    feed_id TEXT PRIMARY KEY, -- e.g., 'home', 'instagram', 'tiktok', etc.
    last_position INTEGER NOT NULL DEFAULT 0,
    last_viewed_post_id TEXT,
    last_updated INTEGER NOT NULL
);
```

#### 11. App Settings

```sql
CREATE TABLE app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 12. Sync Status

```sql
CREATE TABLE sync_status (
    platform TEXT PRIMARY KEY,
    last_sync_time INTEGER NOT NULL,
    next_sync_time INTEGER,
    sync_interval INTEGER NOT NULL DEFAULT 3600000, -- milliseconds
    is_auto_sync INTEGER NOT NULL DEFAULT 1
);
```

## Indexes

```sql
-- For faster feed loading
CREATE INDEX idx_social_posts_platform_published ON social_posts(platform, published_at DESC);

-- For user's own content
CREATE INDEX idx_social_posts_author ON social_posts(author_username, platform);

-- For media loading
CREATE INDEX idx_post_media_post_id ON post_media(post_id);

-- For token history
CREATE INDEX idx_token_transactions_type_time ON token_transactions(token_type, timestamp DESC);

-- For interaction sync
CREATE INDEX idx_user_interactions_status ON user_interactions(status);
```

## Data Access Objects (DAOs)

The Room database implementation will include the following DAOs to provide type-safe access to the database:

### UserProfileDao

```kotlin
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
```

### ConnectedAccountsDao

```kotlin
@Dao
interface ConnectedAccountsDao {
    @Query("SELECT * FROM connected_accounts WHERE is_active = 1")
    fun getActiveAccounts(): Flow<List<ConnectedAccount>>
    
    @Query("SELECT * FROM connected_accounts WHERE platform = :platform")
    fun getAccountsForPlatform(platform: String): Flow<List<ConnectedAccount>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: ConnectedAccount)
    
    @Query("UPDATE connected_accounts SET is_active = 0 WHERE id = :accountId")
    suspend fun deactivateAccount(accountId: String)
}
```

### SocialPostsDao

```kotlin
@Dao
interface SocialPostsDao {
    @Transaction
    @Query("SELECT * FROM social_posts WHERE platform IN (:platforms) ORDER BY published_at DESC LIMIT :limit OFFSET :offset")
    fun getPostsForPlatforms(platforms: List<String>, limit: Int, offset: Int): Flow<List<SocialPostWithMedia>>
    
    @Transaction
    @Query("SELECT * FROM social_posts WHERE is_bookmarked = 1 ORDER BY published_at DESC")
    fun getBookmarkedPosts(): Flow<List<SocialPostWithMedia>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePost(post: SocialPost)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: PostMedia)
    
    @Query("UPDATE social_posts SET is_bookmarked = :isBookmarked WHERE id = :postId")
    suspend fun updateBookmarkStatus(postId: String, isBookmarked: Boolean)
}
```

### DraftsDao

```kotlin
@Dao
interface DraftsDao {
    @Transaction
    @Query("SELECT * FROM post_drafts ORDER BY updated_at DESC")
    fun getAllDrafts(): Flow<List<DraftWithMedia>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDraft(draft: PostDraft): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftMedia(media: DraftMedia)
    
    @Query("DELETE FROM post_drafts WHERE id = :draftId")
    suspend fun deleteDraft(draftId: String)
}
```

### TokensDao

```kotlin
@Dao
interface TokensDao {
    @Query("SELECT * FROM token_balances")
    fun getAllTokenBalances(): Flow<List<TokenBalance>>
    
    @Query("SELECT * FROM token_balances WHERE token_type = :tokenType")
    fun getTokenBalance(tokenType: String): Flow<TokenBalance?>
    
    @Query("SELECT * FROM token_transactions WHERE token_type = :tokenType ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getTokenTransactions(tokenType: String, limit: Int, offset: Int): Flow<List<TokenTransaction>>
    
    @Transaction
    suspend fun processTransaction(transaction: TokenTransaction) {
        insertTransaction(transaction)
        updateBalance(transaction.tokenType, transaction.amount)
    }
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TokenTransaction)
    
    @Query("UPDATE token_balances SET balance = balance + :amount, last_updated = :timestamp WHERE token_type = :tokenType")
    suspend fun updateBalance(tokenType: String, amount: Int, timestamp: Long = System.currentTimeMillis())
}
```

## Encrypted SharedPreferences

For sensitive data like authentication tokens, we'll use Android's EncryptedSharedPreferences:

```kotlin
val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

val encryptedPrefs = EncryptedSharedPreferences.create(
    "nukie_secure_prefs",
    masterKeyAlias,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// Store auth token
encryptedPrefs.edit().putString("instagram_auth_token", "token_value").apply()

// Retrieve auth token
val token = encryptedPrefs.getString("instagram_auth_token", null)
```

## File Storage Structure

The app will organize cached media files in a structured directory hierarchy:

```
/data/data/com.nukie.app/files/
├── avatars/
│   ├── user_avatar.jpg
│   └── platform_avatars/
│       ├── instagram_avatar.jpg
│       ├── tiktok_avatar.jpg
│       └── ...
├── media/
│   ├── instagram/
│   │   ├── [content_hash1].jpg
│   │   └── [content_hash2].mp4
│   ├── tiktok/
│   │   └── ...
│   └── ...
└── drafts/
    ├── [draft_id1]/
    │   ├── image1.jpg
    │   └── image2.jpg
    └── [draft_id2]/
        └── video.mp4
```

## Migration Strategy

The database schema includes version migration support to handle app updates:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration: Add a new column to social_posts
        database.execSQL("ALTER TABLE social_posts ADD COLUMN is_reposted INTEGER NOT NULL DEFAULT 0")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "nukie-db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

## Conclusion

This database schema provides a comprehensive foundation for the Nukie social media aggregator's client-side storage requirements. It supports all the necessary functionality while ensuring user data remains on their device, aligning with the app's privacy-first approach. The schema is designed to be efficient, scalable, and maintainable as the application evolves.
