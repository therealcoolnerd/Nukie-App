package com.nukie.app.data.db.dao

import androidx.lifecycle.Flow
import androidx.room.*
import com.nukie.app.data.db.entity.UserProfile

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

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
    
    @Query("SELECT * FROM connected_accounts WHERE is_active = 1")
    suspend fun getActiveAccountsSync(): List<ConnectedAccount>
}

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
    
    @Query("SELECT * FROM social_posts ORDER BY published_at DESC LIMIT :limit")
    fun getPostsPaged(limit: Int): Flow<PagingData<SocialPostWithMedia>>
}

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

@Dao
interface UserInteractionsDao {
    @Query("SELECT * FROM user_interactions WHERE status = :status")
    fun getInteractionsByStatus(status: String): Flow<List<UserInteraction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserInteraction)
    
    @Query("UPDATE user_interactions SET status = :newStatus WHERE id = :interactionId")
    suspend fun updateInteractionStatus(interactionId: String, newStatus: String)
}

@Dao
interface FeedPositionsDao {
    @Query("SELECT * FROM feed_positions WHERE feed_id = :feedId")
    fun getFeedPosition(feedId: String): Flow<FeedPosition?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFeedPosition(position: FeedPosition)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun getSetting(key: String): Flow<AppSetting?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSetting)
}

@Dao
interface SyncStatusDao {
    @Query("SELECT * FROM sync_status")
    fun getAllSyncStatus(): Flow<List<SyncStatus>>
    
    @Query("SELECT * FROM sync_status WHERE platform = :platform")
    fun getSyncStatusForPlatform(platform: String): Flow<SyncStatus?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSyncStatus(syncStatus: SyncStatus)
}
