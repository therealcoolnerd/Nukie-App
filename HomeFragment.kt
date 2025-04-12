package com.nukie.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nukie.app.R
import com.nukie.app.databinding.FragmentHomeBinding
import com.nukie.app.ui.adapter.FeedAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupChipGroup()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            onPostClick = { post ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(post.id)
                )
            },
            onLikeClick = { post ->
                viewModel.likePost(post)
            },
            onCommentClick = { post ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(post.id)
                )
            },
            onShareClick = { post ->
                viewModel.sharePost(post)
            },
            onBookmarkClick = { post, isBookmarked ->
                viewModel.bookmarkPost(post, isBookmarked)
            }
        )
        
        binding.feedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshFeed()
        }
    }

    private fun setupChipGroup() {
        binding.feedFiltersChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipAllFeeds -> viewModel.setFeedFilter(FeedFilter.ALL)
                R.id.chipInstagram -> viewModel.setFeedFilter(FeedFilter.INSTAGRAM)
                R.id.chipTiktok -> viewModel.setFeedFilter(FeedFilter.TIKTOK)
                R.id.chipYoutube -> viewModel.setFeedFilter(FeedFilter.YOUTUBE)
                R.id.chipBluesky -> viewModel.setFeedFilter(FeedFilter.BLUESKY)
                R.id.chipMastodon -> viewModel.setFeedFilter(FeedFilter.MASTODON)
                R.id.chipFavorites -> viewModel.setFeedFilter(FeedFilter.FAVORITES)
            }
        }
    }

    private fun setupButtons() {
        binding.refreshButton.setOnClickListener {
            viewModel.refreshFeed()
        }
        
        binding.postButton.setOnClickListener {
            findNavController().navigate(R.id.postCreateFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.feedPosts.collectLatest { pagingData ->
                feedAdapter.submitData(pagingData)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
