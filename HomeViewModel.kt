package com.nukie.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nukie.app.data.model.PlatformType
import com.nukie.app.data.model.UnifiedPost
import com.nukie.app.data.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

enum class FeedFilter {
    ALL, INSTAGRAM, TIKTOK, YOUTUBE, BLUESKY, MASTODON, FAVORITES
}

class HomeViewModel(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val currentFilter = MutableStateFlow(FeedFilter.ALL)
    
    val feedPosts: Flow<PagingData<UnifiedPost>> = currentFilter.flatMapLatest { filter ->
        when (filter) {
            FeedFilter.ALL -> socialRepository.getAggregatedFeed()
            FeedFilter.INSTAGRAM -> socialRepository.getPlatformFeed(PlatformType.INSTAGRAM)
            FeedFilter.TIKTOK -> socialRepository.getPlatformFeed(PlatformType.TIKTOK)
            FeedFilter.YOUTUBE -> socialRepository.getPlatformFeed(PlatformType.YOUTUBE)
            FeedFilter.BLUESKY -> socialRepository.getPlatformFeed(PlatformType.BLUESKY)
            FeedFilter.MASTODON -> socialRepository.getPlatformFeed(PlatformType.MASTODON)
            FeedFilter.FAVORITES -> socialRepository.getBookmarkedPosts()
        }
    }.cachedIn(viewModelScope)

    fun setFeedFilter(filter: FeedFilter) {
        currentFilter.value = filter
    }

    fun refreshFeed() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Refresh logic would depend on the paging implementation
                // For now, we'll just simulate a delay
                kotlinx.coroutines.delay(1000)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likePost(post: UnifiedPost) {
        viewModelScope.launch {
            socialRepository.likePost(post)
        }
    }

    fun sharePost(post: UnifiedPost) {
        viewModelScope.launch {
            socialRepository.sharePost(post)
        }
    }

    fun bookmarkPost(post: UnifiedPost, isBookmarked: Boolean) {
        viewModelScope.launch {
            socialRepository.bookmarkPost(post, isBookmarked)
        }
    }
}
