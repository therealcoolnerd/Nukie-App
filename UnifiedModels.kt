package com.nukie.app.data.model

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
