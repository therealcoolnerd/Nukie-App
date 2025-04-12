package com.nukie.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nukie.app.data.db.dao.*
import com.nukie.app.data.db.entity.*
import com.nukie.app.data.db.converter.DateConverter
import com.nukie.app.data.db.converter.StringListConverter

@Database(
    entities = [
        UserProfile::class,
        ConnectedAccount::class,
        SocialPost::class,
        PostMedia::class,
        PostDraft::class,
        DraftMedia::class,
        TokenBalance::class,
        TokenTransaction::class,
        UserInteraction::class,
        FeedPosition::class,
        AppSetting::class,
        SyncStatus::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class NukieDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun connectedAccountsDao(): ConnectedAccountsDao
    abstract fun socialPostsDao(): SocialPostsDao
    abstract fun draftsDao(): DraftsDao
    abstract fun tokensDao(): TokensDao
    abstract fun userInteractionsDao(): UserInteractionsDao
    abstract fun feedPositionsDao(): FeedPositionsDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun syncStatusDao(): SyncStatusDao
}
