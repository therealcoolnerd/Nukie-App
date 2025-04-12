# Nukie Social Media Aggregator - API Integration Architecture

## Overview

This document outlines the architecture for integrating with various social media platform APIs while maintaining the privacy-first approach of the Nukie application. The integration architecture ensures that all data processing happens on the user's device, with no server-side storage of user content or credentials.

## Supported Platforms

The initial release will support the following platforms:

1. **Instagram** - Content viewing and posting
2. **TikTok** - Content viewing and posting
3. **YouTube** - Content viewing and commenting
4. **Bluesky** - Full integration (viewing and posting)
5. **Mastodon** - Full integration (viewing and posting)

## Integration Approach

### Authentication Methods

| Platform | Auth Method | Token Storage | Refresh Strategy |
|----------|-------------|---------------|------------------|
| Instagram | OAuth 2.0 | EncryptedSharedPreferences | Silent refresh with refresh token |
| TikTok | OAuth 2.0 | EncryptedSharedPreferences | Silent refresh with refresh token |
| YouTube | OAuth 2.0 | EncryptedSharedPreferences | Silent refresh with refresh token |
| Bluesky | App Password | EncryptedSharedPreferences | Manual re-auth when expired |
| Mastodon | OAuth 2.0 | EncryptedSharedPreferences | Silent refresh with refresh token |

### API Client Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    Social Repository                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                 │                 │
    ┌────────────┴─────────┐      │
    │                      │      │
    ▼                      ▼      ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│             │      │             │      │             │
│ Platform A  │      │ Platform B  │ ...  │ Platform N  │
│ API Client  │      │ API Client  │      │ API Client  │
│             │      │             │      │             │
└─────────────┘      └─────────────┘      └─────────────┘
    │                      │                     │
    ▼                      ▼                     ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│             │      │             │      │             │
│ Platform A  │      │ Platform B  │ ...  │ Platform N  │
│ Data Models │      │ Data Models │      │ Data Models │
│             │      │             │      │             │
└─────────────┘      └─────────────┘      └─────────────┘
    │                      │                     │
    └──────────────┬───────┴─────────────┬──────┘
                   │                     │
                   ▼                     ▼
         ┌─────────────────┐    ┌─────────────────┐
         │                 │    │                 │
         │ Unified Models  │    │ Local Database  │
         │                 │    │                 │
         └─────────────────┘    └─────────────────┘
```

## Platform-Specific Integration Details

### Instagram

```kotlin
class InstagramApiClient(
    private val authManager: AuthManager,
    private val apiConfig: ApiConfig
) {
    suspend fun fetchUserFeed(count: Int, maxId: String? = null): Result<List<InstagramPost>> {
        val token = authManager.getToken(Platform.INSTAGRAM)
        return withContext(Dispatchers.IO) {
            try {
                val response = apiConfig.instagramService.getUserFeed(
                    "Bearer $token",
                    count,
                    maxId
                )
                Result.success(response.items.map { it.toInstagramPost() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun postMedia(caption: String, mediaPath: String): Result<Boolean> {
        val token = authManager.getToken(Platform.INSTAGRAM)
        // Implementation for posting to Instagram
        // This would involve multiple API calls for media upload and publishing
        // ...
    }
}
```

### TikTok

```kotlin
class TikTokApiClient(
    private val authManager: AuthManager,
    private val apiConfig: ApiConfig
) {
    suspend fun fetchForYouFeed(count: Int, cursor: String? = null): Result<TikTokFeedResponse> {
        val token = authManager.getToken(Platform.TIKTOK)
        return withContext(Dispatchers.IO) {
            try {
                val response = apiConfig.tiktokService.getForYouFeed(
                    "Bearer $token",
                    count,
                    cursor
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun uploadVideo(description: String, videoPath: String): Result<Boolean> {
        val token = authManager.getToken(Platform.TIKTOK)
        // Implementation for uploading to TikTok
        // ...
    }
}
```

### Bluesky

```kotlin
class BlueskyApiClient(
    private val authManager: AuthManager,
    private val apiConfig: ApiConfig
) {
    suspend fun getTimeline(limit: Int, cursor: String? = null): Result<BlueskyTimelineResponse> {
        val credentials = authManager.getBlueskyCredentials()
        return withContext(Dispatchers.IO) {
            try {
                // First authenticate to get a session
                val session = apiConfig.blueskyService.createSession(credentials)
                
                // Then fetch the timeline
                val response = apiConfig.blueskyService.getTimeline(
                    "Bearer ${session.accessJwt}",
                    limit,
                    cursor
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun createPost(text: String, images: List<String>? = null): Result<String> {
        val credentials = authManager.getBlueskyCredentials()
        // Implementation for posting to Bluesky
        // ...
    }
}
```

## Unified Data Models

To present a consistent interface across different platforms, we'll use unified data models:

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
    val originalPlatformData: Any? // Platform-specific data for advanced features
)

data class UnifiedAuthor(
    val id: String,
    val platformId: String,
    val platformType: PlatformType,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isVerified: Boolean
)

data class UnifiedMedia(
    val id: String,
    val type: MediaType, // IMAGE, VIDEO, GIF, etc.
    val url: String,
    val previewUrl: String?,
    val width: Int?,
    val height: Int?,
    val duration: Long? // For videos
)

enum class PlatformType {
    INSTAGRAM,
    TIKTOK,
    YOUTUBE,
    BLUESKY,
    MASTODON
}

enum class MediaType {
    IMAGE,
    VIDEO,
    GIF,
    AUDIO
}
```

## Mappers

Each platform will have mapper functions to convert platform-specific models to unified models:

```kotlin
// Instagram Mapper
fun InstagramPost.toUnifiedPost(): UnifiedPost {
    return UnifiedPost(
        id = UUID.randomUUID().toString(),
        platformId = this.id,
        platformType = PlatformType.INSTAGRAM,
        author = UnifiedAuthor(
            id = UUID.randomUUID().toString(),
            platformId = this.user.pk,
            platformType = PlatformType.INSTAGRAM,
            username = this.user.username,
            displayName = this.user.fullName,
            avatarUrl = this.user.profilePicUrl,
            isVerified = this.user.isVerified
        ),
        content = this.caption?.text,
        mediaItems = this.carouselMedia?.map { it.toUnifiedMedia() } 
            ?: listOfNotNull(this.imageVersions2?.toUnifiedMedia()),
        publishedAt = this.takenAt * 1000, // Convert to milliseconds
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = 0, // Instagram doesn't provide share count
        isLiked = this.hasLiked,
        originalPlatformData = this
    )
}

// TikTok Mapper
fun TikTokVideo.toUnifiedPost(): UnifiedPost {
    // Similar implementation for TikTok
}

// Bluesky Mapper
fun BlueskyPost.toUnifiedPost(): UnifiedPost {
    // Similar implementation for Bluesky
}
```

## Social Repository

The Social Repository acts as a facade for all platform-specific API clients:

```kotlin
class SocialRepository(
    private val instagramApiClient: InstagramApiClient,
    private val tiktokApiClient: TikTokApiClient,
    private val youtubeApiClient: YouTubeApiClient,
    private val blueskyApiClient: BlueskyApiClient,
    private val mastodonApiClient: MastodonApiClient,
    private val postDao: SocialPostsDao,
    private val accountsDao: ConnectedAccountsDao
) {
    // Get aggregated feed from all connected platforms
    suspend fun getAggregatedFeed(limit: Int = 20): Flow<PagingData<UnifiedPost>> {
        return Pager(
            config = PagingConfig(
                pageSize = limit,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                AggregatedFeedPagingSource(
                    instagramApiClient,
                    tiktokApiClient,
                    youtubeApiClient,
                    blueskyApiClient,
                    mastodonApiClient,
                    accountsDao
                )
            }
        ).flow
    }
    
    // Get feed for a specific platform
    suspend fun getPlatformFeed(platform: PlatformType, limit: Int = 20): Flow<PagingData<UnifiedPost>> {
        return when (platform) {
            PlatformType.INSTAGRAM -> getInstagramFeed(limit)
            PlatformType.TIKTOK -> getTikTokFeed(limit)
            PlatformType.YOUTUBE -> getYouTubeFeed(limit)
            PlatformType.BLUESKY -> getBlueskyFeed(limit)
            PlatformType.MASTODON -> getMastodonFeed(limit)
        }
    }
    
    // Post to multiple platforms
    suspend fun createMultiPlatformPost(
        content: String,
        mediaFiles: List<String>?,
        platforms: List<PlatformType>
    ): Map<PlatformType, Result<Boolean>> {
        return coroutineScope {
            platforms.associateWith { platform ->
                async {
                    when (platform) {
                        PlatformType.INSTAGRAM -> instagramApiClient.postMedia(content, mediaFiles?.firstOrNull() ?: "")
                        PlatformType.TIKTOK -> tiktokApiClient.uploadVideo(content, mediaFiles?.firstOrNull() ?: "")
                        PlatformType.BLUESKY -> blueskyApiClient.createPost(content, mediaFiles).map { true }
                        PlatformType.MASTODON -> mastodonApiClient.createPost(content, mediaFiles).map { true }
                        else -> Result.failure(UnsupportedOperationException("Posting not supported for $platform"))
                    }
                }
            }.mapValues { it.value.await() }
        }
    }
    
    // Platform-specific feed methods
    private suspend fun getInstagramFeed(limit: Int): Flow<PagingData<UnifiedPost>> {
        // Implementation
    }
    
    private suspend fun getTikTokFeed(limit: Int): Flow<PagingData<UnifiedPost>> {
        // Implementation
    }
    
    // Additional methods for interactions (like, comment, share)
    suspend fun likePost(post: UnifiedPost): Result<Boolean> {
        // Implementation
    }
    
    suspend fun commentOnPost(post: UnifiedPost, comment: String): Result<Boolean> {
        // Implementation
    }
}
```

## Paging and Caching Strategy

To efficiently load and cache content from multiple platforms:

```kotlin
class AggregatedFeedPagingSource(
    private val instagramApiClient: InstagramApiClient,
    private val tiktokApiClient: TikTokApiClient,
    private val youtubeApiClient: YouTubeApiClient,
    private val blueskyApiClient: BlueskyApiClient,
    private val mastodonApiClient: MastodonApiClient,
    private val accountsDao: ConnectedAccountsDao
) : PagingSource<AggregatedFeedKey, UnifiedPost>() {

    override suspend fun load(params: LoadParams<AggregatedFeedKey>): LoadResult<AggregatedFeedKey, UnifiedPost> {
        val key = params.key ?: AggregatedFeedKey()
        
        try {
            // Get connected accounts
            val connectedAccounts = accountsDao.getActiveAccountsSync()
            
            // Fetch from each connected platform
            val results = mutableListOf<UnifiedPost>()
            
            // Fetch from Instagram if connected
            if (connectedAccounts.any { it.platform == "instagram" }) {
                val instagramResult = instagramApiClient.fetchUserFeed(
                    count = params.loadSize,
                    maxId = key.instagramCursor
                )
                
                if (instagramResult.isSuccess) {
                    results.addAll(instagramResult.getOrNull()?.map { it.toUnifiedPost() } ?: emptyList())
                    key.instagramCursor = instagramResult.getOrNull()?.lastOrNull()?.id
                }
            }
            
            // Similar for other platforms
            // ...
            
            // Sort all results by date
            results.sortByDescending { it.publishedAt }
            
            // Take only the requested amount
            val finalResults = results.take(params.loadSize)
            
            return LoadResult.Page(
                data = finalResults,
                prevKey = null, // Only forward pagination
                nextKey = if (finalResults.isNotEmpty()) key else null
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}

data class AggregatedFeedKey(
    var instagramCursor: String? = null,
    var tiktokCursor: String? = null,
    var youtubeCursor: String? = null,
    var blueskyCursor: String? = null,
    var mastodonCursor: String? = null
)
```

## Error Handling and Retry Strategy

```kotlin
class ApiErrorHandler {
    fun handleError(error: Throwable, platform: PlatformType): ApiError {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> ApiError.AuthenticationError(platform, "Authentication failed")
                    403 -> ApiError.PermissionError(platform, "Permission denied")
                    429 -> ApiError.RateLimitError(platform, "Rate limit exceeded")
                    else -> ApiError.NetworkError(platform, "HTTP Error: ${error.code()}")
                }
            }
            is IOException -> ApiError.NetworkError(platform, "Network error: ${error.message}")
            else -> ApiError.UnknownError(platform, error.message ?: "Unknown error")
        }
    }
}

sealed class ApiError {
    abstract val platform: PlatformType
    abstract val message: String
    
    class AuthenticationError(override val platform: PlatformType, override val message: String) : ApiError()
    class PermissionError(override val platform: PlatformType, override val message: String) : ApiError()
    class RateLimitError(override val platform: PlatformType, override val message: String) : ApiError()
    class NetworkError(override val platform: PlatformType, override val message: String) : ApiError()
    class UnknownError(override val platform: PlatformType, override val message: String) : ApiError()
}
```

## Offline Support

```kotlin
class OfflineFirstRepository(
    private val socialRepository: SocialRepository,
    private val postDao: SocialPostsDao,
    private val networkMonitor: NetworkMonitor
) {
    fun getAggregatedFeed(limit: Int = 20): Flow<PagingData<UnifiedPost>> {
        return if (networkMonitor.isOnline) {
            // Online: fetch from network and cache
            socialRepository.getAggregatedFeed(limit)
                .onEach { pagingData ->
                    // Cache the data
                    pagingData.map { post ->
                        postDao.insertOrUpdatePost(post.toSocialPost())
                        post.mediaItems.forEach { media ->
                            postDao.insertMedia(media.toPostMedia(post.id))
                        }
                    }
                }
        } else {
            // Offline: fetch from cache
            postDao.getPostsPaged(limit).map { pagingData ->
                pagingData.map { it.toUnifiedPost() }
            }
        }
    }
    
    suspend fun createPost(
        content: String,
        mediaFiles: List<String>?,
        platforms: List<PlatformType>
    ): Result<Boolean> {
        // Save draft locally first
        val draft = PostDraft(
            id = UUID.randomUUID().toString(),
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            targetPlatforms = platforms.map { it.name }.toString(),
            status = if (networkMonitor.isOnline) "publishing" else "queued"
        )
        
        // If online, publish immediately
        return if (networkMonitor.isOnline) {
            socialRepository.createMultiPlatformPost(content, mediaFiles, platforms)
                .let { results ->
                    // Update draft status based on results
                    if (results.all { it.value.isSuccess }) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Failed to post to some platforms"))
                    }
                }
        } else {
            // Queue for later when online
            Result.success(false)
        }
    }
}
```

## Conclusion

This API integration architecture ensures that Nukie can interact with multiple social media platforms while maintaining its privacy-first approach. By processing all data on the user's device and using a unified data model, the app provides a seamless experience across different platforms without compromising user privacy.

The architecture is designed to be extensible, allowing for easy addition of new platforms in the future. It also includes robust error handling and offline support to ensure a reliable user experience in various network conditions.
