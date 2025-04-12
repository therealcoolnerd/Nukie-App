package com.nukie.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nukie.app.R
import com.nukie.app.data.model.MediaType
import com.nukie.app.data.model.UnifiedPost
import com.nukie.app.databinding.ItemFeedPostBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedAdapter(
    private val onPostClick: (UnifiedPost) -> Unit,
    private val onLikeClick: (UnifiedPost) -> Unit,
    private val onCommentClick: (UnifiedPost) -> Unit,
    private val onShareClick: (UnifiedPost) -> Unit,
    private val onBookmarkClick: (UnifiedPost, Boolean) -> Unit
) : PagingDataAdapter<UnifiedPost, FeedAdapter.PostViewHolder>(POST_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemFeedPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let { post ->
            holder.bind(post)
        }
    }

    inner class PostViewHolder(
        private val binding: ItemFeedPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: UnifiedPost) {
            // Set platform icon
            val platformIconRes = when (post.platformType) {
                PlatformType.INSTAGRAM -> R.drawable.ic_instagram
                PlatformType.TIKTOK -> R.drawable.ic_tiktok
                PlatformType.YOUTUBE -> R.drawable.ic_youtube
                PlatformType.BLUESKY -> R.drawable.ic_bluesky
                PlatformType.MASTODON -> R.drawable.ic_mastodon
            }
            binding.platformIcon.setImageResource(platformIconRes)
            
            // Set author info
            binding.authorName.text = post.author.displayName
            binding.authorUsername.text = "@${post.author.username}"
            
            // Load author avatar
            Glide.with(binding.authorAvatar)
                .load(post.author.avatarUrl)
                .placeholder(R.drawable.placeholder_avatar)
                .circleCrop()
                .into(binding.authorAvatar)
            
            // Set post content
            binding.postContent.text = post.content
            
            // Format and set post date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
            binding.postDate.text = dateFormat.format(Date(post.publishedAt))
            
            // Handle media content
            if (post.mediaItems.isNotEmpty()) {
                binding.mediaContainer.visibility = View.VISIBLE
                val firstMedia = post.mediaItems.first()
                
                when (firstMedia.type) {
                    MediaType.IMAGE -> {
                        binding.mediaImage.visibility = View.VISIBLE
                        binding.videoIndicator.visibility = View.GONE
                        
                        Glide.with(binding.mediaImage)
                            .load(firstMedia.url)
                            .placeholder(R.drawable.placeholder_image)
                            .into(binding.mediaImage)
                    }
                    MediaType.VIDEO, MediaType.GIF -> {
                        binding.mediaImage.visibility = View.VISIBLE
                        binding.videoIndicator.visibility = View.VISIBLE
                        
                        Glide.with(binding.mediaImage)
                            .load(firstMedia.previewUrl ?: firstMedia.url)
                            .placeholder(R.drawable.placeholder_video)
                            .into(binding.mediaImage)
                    }
                    else -> {
                        binding.mediaContainer.visibility = View.GONE
                    }
                }
                
                // Show media count indicator if there are multiple media items
                if (post.mediaItems.size > 1) {
                    binding.mediaCountIndicator.visibility = View.VISIBLE
                    binding.mediaCountIndicator.text = "+${post.mediaItems.size - 1}"
                } else {
                    binding.mediaCountIndicator.visibility = View.GONE
                }
            } else {
                binding.mediaContainer.visibility = View.GONE
            }
            
            // Set interaction counts
            binding.likeCount.text = formatCount(post.likeCount)
            binding.commentCount.text = formatCount(post.commentCount)
            binding.shareCount.text = formatCount(post.shareCount)
            
            // Set button states
            binding.likeButton.isSelected = post.isLiked
            binding.bookmarkButton.isSelected = post.isBookmarked
            
            // Set click listeners
            binding.root.setOnClickListener { onPostClick(post) }
            binding.likeButton.setOnClickListener { onLikeClick(post) }
            binding.commentButton.setOnClickListener { onCommentClick(post) }
            binding.shareButton.setOnClickListener { onShareClick(post) }
            binding.bookmarkButton.setOnClickListener { 
                val newState = !binding.bookmarkButton.isSelected
                binding.bookmarkButton.isSelected = newState
                onBookmarkClick(post, newState) 
            }
        }
        
        private fun formatCount(count: Int): String {
            return when {
                count < 1000 -> count.toString()
                count < 10000 -> String.format("%.1fK", count / 1000.0)
                count < 1000000 -> String.format("%dK", count / 1000)
                else -> String.format("%.1fM", count / 1000000.0)
            }
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<UnifiedPost>() {
            override fun areItemsTheSame(oldItem: UnifiedPost, newItem: UnifiedPost): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UnifiedPost, newItem: UnifiedPost): Boolean {
                return oldItem == newItem
            }
        }
    }
}
