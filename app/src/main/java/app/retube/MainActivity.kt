package app.retube

import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.retube.core.designsystem.ReTubeTheme
import app.retube.core.util.Formatters
import app.retube.features.channel.ChannelScreen
import app.retube.features.home.HomeScreen
import app.retube.features.library.LibraryScreen
import app.retube.features.player.MiniPlayerBar
import app.retube.features.player.VideoPlayerSheet
import app.retube.features.search.SearchScreen
import app.retube.features.settings.SettingsScreen
import app.retube.features.subscriptions.SubscriptionsScreen
import app.retube.features.viewmodel.ReTubeMainViewModel
import app.retube.playback.service.ReTubePlaybackService

class MainActivity : ComponentActivity() {

    private val viewModel: ReTubeMainViewModel by viewModels {
        val app = application as ReTubeApplication
        ReTubeMainViewModel.Factory(this, app.playerController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startPlaybackServiceIfNeeded()
        handleIncomingIntent(intent)

        setContent {
            val prefs by viewModel.userPreferences.collectAsState()
            ReTubeTheme(
                themeMode = prefs.themeMode,
                dynamicColor = prefs.dynamicColor,
                pureBlackAmoled = prefs.pureBlackAmoled
            ) {
                ReTubeAppRoot(
                    viewModel = viewModel,
                    onEnterPip = { enterPipMode() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val playerState = viewModel.playerState.value
        if (playerState.isPlaying && !playerState.isAudioOnlyMode && playerState.currentVideo != null) {
            enterPipMode()
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            runCatching { enterPictureInPictureMode(params) }
        }
    }

    private fun startPlaybackServiceIfNeeded() {
        runCatching {
            val serviceIntent = Intent(this, ReTubePlaybackService::class.java)
            startService(serviceIntent)
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val rawUrl = when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return

        val extractedId = Formatters.extractYoutubeVideoId(rawUrl)
        if (extractedId.isNotBlank()) {
            viewModel.playVideoById(extractedId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReTubeAppRoot(
    viewModel: ReTubeMainViewModel,
    onEnterPip: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val homeState by viewModel.homeState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val channelState by viewModel.channelState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val playlists by viewModel.localPlaylists.collectAsState()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsState()
    val selectedPlaylistVideos by viewModel.selectedPlaylistVideos.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val subscriptionsFeed by viewModel.subscriptionsFeed.collectAsState()
    val isRefreshingSubscriptions by viewModel.isRefreshingSubscriptions.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    val activeVideo = playerState.currentVideo
    val isBookmarked = activeVideo != null && bookmarks.any { it.videoId == activeVideo.id }
    val isSubscribedToCurrent = activeVideo != null && subscriptions.any { it.channelId == activeVideo.uploaderId }

    BackHandler(enabled = activeVideo != null && !playerState.isMinimized) {
        viewModel.playerController.setMinimized(true)
    }

    BackHandler(enabled = channelState.channel != null && (activeVideo == null || playerState.isMinimized)) {
        viewModel.closeChannel()
    }

    Scaffold(
        topBar = {
            if (activeVideo == null || playerState.isMinimized) {
                TopAppBar(
                    title = {
                        Text(
                            text = "ReTube • NOVA",
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    actions = {
                        IconButton(onClick = { currentTab = 1 }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { currentTab = 4 }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (activeVideo == null || playerState.isMinimized) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (activeVideo != null && playerState.isMinimized) {
                        MiniPlayerBar(
                            state = playerState,
                            onExpand = { viewModel.playerController.setMinimized(false) },
                            onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                            onClose = { viewModel.playerController.closePlayer() }
                        )
                    }
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = {
                                viewModel.closeChannel()
                                currentTab = 0
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = {
                                viewModel.closeChannel()
                                currentTab = 1
                            },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Search") },
                            label = { Text("Explore") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = {
                                viewModel.closeChannel()
                                currentTab = 2
                            },
                            icon = { Icon(Icons.Default.Subscriptions, contentDescription = "Subscriptions") },
                            label = { Text("Subs") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = {
                                viewModel.closeChannel()
                                currentTab = 3
                            },
                            icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                            label = { Text("Library") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = {
                                viewModel.closeChannel()
                                currentTab = 4
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (channelState.channel != null) {
                val ch = channelState.channel!!
                ChannelScreen(
                    state = channelState,
                    isSubscribed = subscriptions.any { it.channelId == ch.id },
                    onBack = { viewModel.closeChannel() },
                    onToggleSubscribe = { id, name, avatar, subs ->
                        viewModel.toggleSubscription(id, name, avatar, subs)
                    },
                    onVideoClick = { viewModel.playVideo(it) }
                )
            } else {
                when (currentTab) {
                    0 -> HomeScreen(
                        state = homeState,
                        watchHistory = watchHistory,
                        playlists = playlists,
                        onSelectCategory = { viewModel.refreshHomeFeed(it) },
                        onRefresh = { viewModel.refreshHomeFeed() },
                        onVideoClick = { viewModel.playVideo(it) },
                        onChannelClick = { viewModel.openChannel(it) },
                        onAddToQueue = { viewModel.playerController.addToQueue(it) },
                        onBookmark = { viewModel.toggleBookmark(it) },
                        onCreatePlaylist = { name, desc -> viewModel.createLocalPlaylist(name, desc) },
                        onAddVideoToPlaylist = { plId, video -> viewModel.addVideoToPlaylist(plId, video) },
                        onDownload = { video -> viewModel.startDownload(video, prefs.defaultQuality, false) }
                    )

                    1 -> SearchScreen(
                        state = searchState,
                        searchHistory = searchHistory,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onSearch = { q, filter -> viewModel.executeSearch(q, filter) },
                        onDeleteHistoryQuery = { viewModel.deleteSearchHistoryItem(it) },
                        onVideoClick = { viewModel.playVideo(it) },
                        onChannelClick = { viewModel.openChannel(it) }
                    )

                    2 -> SubscriptionsScreen(
                        subscriptions = subscriptions,
                        feedVideos = subscriptionsFeed,
                        isRefreshing = isRefreshingSubscriptions,
                        onRefresh = { viewModel.refreshSubscriptionsFeed() },
                        onChannelClick = { viewModel.openChannel(it) },
                        onVideoClick = { viewModel.playVideo(it) },
                        onQuickSubscribeDefaultChannel = {
                            viewModel.toggleSubscription(
                                channelId = "UCB0W8N8V0B-blender",
                                channelName = "Blender Studio",
                                avatarUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Blender_logo_no_text.svg/240px-Blender_logo_no_text.svg.png",
                                subscriberCount = 1_150_000L
                            )
                        }
                    )

                    3 -> LibraryScreen(
                        watchHistory = watchHistory,
                        playlists = playlists,
                        selectedPlaylistId = selectedPlaylistId,
                        selectedPlaylistVideos = selectedPlaylistVideos,
                        bookmarks = bookmarks,
                        downloads = downloads,
                        onPlayVideoById = { viewModel.playVideoById(it) },
                        onDeleteHistoryItem = { viewModel.deleteWatchHistoryItem(it) },
                        onClearHistory = { viewModel.clearWatchHistory() },
                        onCreatePlaylist = { name, desc -> viewModel.createLocalPlaylist(name, desc) },
                        onDeletePlaylist = { viewModel.deleteLocalPlaylist(it) },
                        onSelectPlaylist = { viewModel.selectPlaylist(it) },
                        onRemoveVideoFromPlaylist = { plId, vId -> viewModel.removeVideoFromPlaylist(plId, vId) },
                        onRemoveBookmark = { vId ->
                            val bm = bookmarks.firstOrNull { it.videoId == vId }
                            if (bm != null) {
                                viewModel.toggleBookmark(
                                    app.retube.core.model.VideoItem(
                                        id = bm.videoId,
                                        title = bm.title,
                                        uploaderName = bm.uploaderName,
                                        uploaderId = bm.uploaderId,
                                        thumbnailUrl = bm.thumbnailUrl,
                                        durationSeconds = bm.durationSeconds
                                    )
                                )
                            }
                        },
                        onPauseDownload = { viewModel.pauseDownload(it) },
                        onResumeDownload = { viewModel.resumeDownload(it) },
                        onDeleteDownload = { viewModel.deleteDownload(it) }
                    )

                    4 -> SettingsScreen(
                        preferences = prefs,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetDynamicColor = { viewModel.setDynamicColor(it) },
                        onSetPureBlackAmoled = { viewModel.setPureBlackAmoled(it) },
                        onSetDefaultQuality = { viewModel.setDefaultQuality(it) },
                        onSetDefaultSpeed = { viewModel.setDefaultSpeed(it) },
                        onSetPipedInstanceUrl = { viewModel.setPipedInstanceUrl(it) },
                        onSetContentRegion = { viewModel.setContentRegion(it) },
                        onSetSponsorBlockEnabled = { viewModel.setSponsorBlockEnabled(it) },
                        onToggleSponsorCategory = { viewModel.toggleSponsorCategory(it) },
                        onSetRydEnabled = { viewModel.setRydEnabled(it) },
                        onSetBackgroundPlayback = { viewModel.setBackgroundPlaybackEnabled(it) },
                        onSetAutoPlayNext = { viewModel.setAutoPlayNext(it) },
                        onSetPauseWatchHistory = { viewModel.setPauseWatchHistory(it) },
                        onSetPauseSearchHistory = { viewModel.setPauseSearchHistory(it) },
                        onClearWatchHistory = { viewModel.clearWatchHistory() },
                        onClearSearchHistory = { viewModel.clearSearchHistory() }
                    )
                }
            }

            if (activeVideo != null && !playerState.isMinimized) {
                VideoPlayerSheet(
                    exoPlayer = viewModel.playerController.exoPlayer,
                    state = playerState,
                    isBookmarked = isBookmarked,
                    isSubscribed = isSubscribedToCurrent,
                    playlists = playlists,
                    onMinimize = { viewModel.playerController.setMinimized(true) },
                    onEnterPip = onEnterPip,
                    onTogglePlayPause = { viewModel.playerController.togglePlayPause() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onSeekRelative = { viewModel.playerController.seekRelative(it) },
                    onPlayNext = {
                        viewModel.playerController.playNextInQueueOrRelated { nextVideo ->
                            viewModel.playVideo(nextVideo)
                        }
                    },
                    onSelectVideo = { viewModel.playVideo(it) },
                    onSelectQuality = { viewModel.playerController.changeQuality(it) },
                    onSelectSpeed = { viewModel.playerController.setPlaybackSpeed(it) },
                    onToggleAudioOnly = { viewModel.playerController.toggleAudioOnlyMode() },
                    onStartSleepTimer = { viewModel.playerController.startSleepTimer(it) },
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onToggleSubscription = { video ->
                        viewModel.toggleSubscription(
                            channelId = video.uploaderId.ifBlank { "channel-${video.uploaderName}" },
                            channelName = video.uploaderName,
                            avatarUrl = video.uploaderAvatar.ifBlank { video.thumbnailUrl },
                            subscriberCount = video.subscriberCount
                        )
                    },
                    onOpenChannel = { channelId ->
                        viewModel.playerController.setMinimized(true)
                        viewModel.openChannel(channelId)
                    },
                    onCreatePlaylist = { name, desc -> viewModel.createLocalPlaylist(name, desc) },
                    onAddVideoToPlaylist = { plId, video -> viewModel.addVideoToPlaylist(plId, video) },
                    onDownloadVideo = { video, quality, audioOnly ->
                        viewModel.startDownload(video, quality, audioOnly)
                    }
                )
            }
        }
    }
}