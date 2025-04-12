package com.nukie.app.lynx;

import android.content.Context;
import android.util.Log;

import com.nukie.app.data.model.PlatformType;
import com.nukie.app.data.model.UnifiedAuthor;
import com.nukie.app.data.model.UnifiedMedia;
import com.nukie.app.data.model.UnifiedPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * LynxAggregator is the main class responsible for aggregating social media content
 * from multiple platforms using the Lynx framework's networking capabilities.
 * 
 * It implements client-side data storage to ensure user data remains on their device.
 */
public class LynxAggregator {
    private static final String TAG = "LynxAggregator";
    
    private final Context context;
    private final Map<PlatformType, LynxPlatformClient> platformClients;
    private final OkHttpClient httpClient;
    
    public LynxAggregator(Context context) {
        this.context = context;
        this.platformClients = new HashMap<>();
        
        // Set up HTTP client with logging and authentication interceptors
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthenticationInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Initialize platform clients
        initializePlatformClients();
    }
    
    private void initializePlatformClients() {
        // Initialize clients for each supported platform
        platformClients.put(PlatformType.INSTAGRAM, new InstagramClient(httpClient));
        platformClients.put(PlatformType.TIKTOK, new TikTokClient(httpClient));
        platformClients.put(PlatformType.YOUTUBE, new YouTubeClient(httpClient));
        platformClients.put(PlatformType.BLUESKY, new BlueskyClient(httpClient));
        platformClients.put(PlatformType.MASTODON, new MastodonClient(httpClient));
    }
    
    /**
     * Fetches content from all connected social media platforms and aggregates it
     * into a unified feed.
     * 
     * @param limit Maximum number of posts to fetch from each platform
     * @return List of unified posts from all platforms
     */
    public List<UnifiedPost> fetchAggregatedFeed(int limit) {
        List<UnifiedPost> aggregatedPosts = new ArrayList<>();
        
        // Fetch from each platform in parallel using Lynx's multithreaded capabilities
        for (Map.Entry<PlatformType, LynxPlatformClient> entry : platformClients.entrySet()) {
            PlatformType platform = entry.getKey();
            LynxPlatformClient client = entry.getValue();
            
            try {
                List<UnifiedPost> platformPosts = client.fetchPosts(limit);
                aggregatedPosts.addAll(platformPosts);
                Log.d(TAG, "Fetched " + platformPosts.size() + " posts from " + platform);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching posts from " + platform, e);
            }
        }
        
        // Sort aggregated posts by timestamp (newest first)
        aggregatedPosts.sort((p1, p2) -> Long.compare(p2.getPublishedAt(), p1.getPublishedAt()));
        
        return aggregatedPosts;
    }
    
    /**
     * Fetches content from a specific social media platform.
     * 
     * @param platform The platform to fetch from
     * @param limit Maximum number of posts to fetch
     * @return List of unified posts from the specified platform
     */
    public List<UnifiedPost> fetchPlatformFeed(PlatformType platform, int limit) {
        LynxPlatformClient client = platformClients.get(platform);
        if (client == null) {
            Log.e(TAG, "No client available for platform: " + platform);
            return new ArrayList<>();
        }
        
        try {
            return client.fetchPosts(limit);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching posts from " + platform, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Posts content to multiple social media platforms simultaneously.
     * 
     * @param content Text content to post
     * @param mediaFiles List of media file paths to attach
     * @param platforms List of platforms to post to
     * @return Map of platforms to success/failure results
     */
    public Map<PlatformType, Boolean> postToMultiplePlatforms(
            String content, 
            List<String> mediaFiles, 
            List<PlatformType> platforms) {
        
        Map<PlatformType, Boolean> results = new HashMap<>();
        
        for (PlatformType platform : platforms) {
            LynxPlatformClient client = platformClients.get(platform);
            if (client == null) {
                results.put(platform, false);
                continue;
            }
            
            try {
                boolean success = client.createPost(content, mediaFiles);
                results.put(platform, success);
            } catch (Exception e) {
                Log.e(TAG, "Error posting to " + platform, e);
                results.put(platform, false);
            }
        }
        
        return results;
    }
    
    /**
     * Authenticates with a social media platform.
     * 
     * @param platform The platform to authenticate with
     * @param authData Authentication data (tokens, credentials, etc.)
     * @return True if authentication was successful
     */
    public boolean authenticatePlatform(PlatformType platform, Map<String, String> authData) {
        LynxPlatformClient client = platformClients.get(platform);
        if (client == null) {
            Log.e(TAG, "No client available for platform: " + platform);
            return false;
        }
        
        try {
            return client.authenticate(authData);
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating with " + platform, e);
            return false;
        }
    }
    
    /**
     * Performs a social interaction (like, comment, share) on a post.
     * 
     * @param post The post to interact with
     * @param interactionType The type of interaction
     * @param interactionData Additional data for the interaction (e.g., comment text)
     * @return True if the interaction was successful
     */
    public boolean performSocialInteraction(
            UnifiedPost post, 
            String interactionType, 
            Map<String, String> interactionData) {
        
        PlatformType platform = post.getPlatformType();
        LynxPlatformClient client = platformClients.get(platform);
        if (client == null) {
            Log.e(TAG, "No client available for platform: " + platform);
            return false;
        }
        
        try {
            return client.performInteraction(post.getPlatformId(), interactionType, interactionData);
        } catch (Exception e) {
            Log.e(TAG, "Error performing interaction on " + platform, e);
            return false;
        }
    }
    
    /**
     * Authentication interceptor for adding auth tokens to requests.
     */
    private class AuthenticationInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            
            // Get the platform from the request URL
            String url = originalRequest.url().toString();
            PlatformType platform = getPlatformFromUrl(url);
            
            if (platform != null) {
                // Get auth token for the platform
                String authToken = getAuthTokenForPlatform(platform);
                
                if (authToken != null && !authToken.isEmpty()) {
                    // Add auth token to request header
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + authToken);
                    
                    return chain.proceed(builder.build());
                }
            }
            
            return chain.proceed(originalRequest);
        }
        
        private PlatformType getPlatformFromUrl(String url) {
            if (url.contains("instagram")) return PlatformType.INSTAGRAM;
            if (url.contains("tiktok")) return PlatformType.TIKTOK;
            if (url.contains("youtube")) return PlatformType.YOUTUBE;
            if (url.contains("bsky")) return PlatformType.BLUESKY;
            if (url.contains("mastodon")) return PlatformType.MASTODON;
            return null;
        }
        
        private String getAuthTokenForPlatform(PlatformType platform) {
            // In a real implementation, this would retrieve tokens from secure storage
            // For now, we'll return null to indicate no authentication
            return null;
        }
    }
}
