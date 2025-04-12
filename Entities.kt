package com.nukie.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = "local_user",
    val displayName: String,
    val username: String,
    val bio: String?,
    val avatarPath: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "connected_accounts")
data class ConnectedAccount(
    @PrimaryKey
    val id: String,
    val platform: String,
    val username: String,
    val displayName: String,
    val avatarPath: String?,
    val isActive: Boolean = true,
    val lastSyncTime: Long
)

@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey
    val id: String,
    val platform: String,
    val originalPlatformId: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarPath: String?,
    val content: String?,
    val publishedAt: Long,
    val fetchedAt: Long,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isLikedByUser: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "post_media")
data class PostMedia(
    @PrimaryKey
    val id: String,
    val postId: String,
    val mediaType: String, // 'image', 'video', 'gif', etc.
    val remoteUrl: String,
    val localPath: String?,
    val width: Int?,
    val height: Int?,
    val duration: Int?, // for videos
    val thumbnailPath: String?,
    val altText: String?
)

@Entity(tableName = "post_drafts")
data class PostDraft(
    @PrimaryKey
    val id: String,
    val content: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val targetPlatforms: String, // JSON array of platform IDs
    val scheduledTime: Long?,
    val status: String = "draft" // 'draft', 'scheduled', 'publishing', 'failed'
)

@Entity(tableName = "draft_media")
data class DraftMedia(
    @PrimaryKey
    val id: String,
    val draftId: String,
    val mediaType: String,
    val localPath: String,
    val width: Int?,
    val height: Int?,
    val duration: Int?,
    val thumbnailPath: String?,
    val altText: String?
)

@Entity(tableName = "token_balances")
data class TokenBalance(
    @PrimaryKey
    val tokenType: String,
    val balance: Int = 0,
    val lastUpdated: Long
)

@Entity(tableName = "token_transactions")
data class TokenTransaction(
    @PrimaryKey
    val id: String,
    val tokenType: String,
    val amount: Int,
    val description: String,
    val timestamp: Long,
    val relatedEntityId: String?
)

@Entity(tableName = "user_interactions")
data class UserInteraction(
    @PrimaryKey
    val id: String,
    val postId: String,
    val interactionType: String, // 'like', 'comment', 'share', 'save'
    val content: String?, // for comments
    val createdAt: Long,
    val status: String = "pending" // 'pending', 'synced', 'failed'
)

@Entity(tableName = "feed_positions")
data class FeedPosition(
    @PrimaryKey
    val feedId: String, // e.g., 'home', 'instagram', 'tiktok', etc.
    val lastPosition: Int = 0,
    val lastViewedPostId: String?,
    val lastUpdated: Long
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long
)

@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey
    val platform: String,
    val lastSyncTime: Long,
    val nextSyncTime: Long?,
    val syncInterval: Long = 3600000, // milliseconds
    val isAutoSync: Boolean = true
)
