# Changelog

All notable changes to **ReTube (NOVA Tube)** are documented in this file.

## [1.0.0] - 2026-11-06

### Added
- **Multi-Module Architecture**: Created `:app`, `:core`, `:providers`, `:playback`, `:downloads`, and `:features` Gradle modules using Kotlin 2.0 and [Jetpack Compose](https://developer.android.com/develop/ui/compose/documentation) Material 3.
- **Privacy-First Streaming**: Integrated [Piped API](https://docs.piped.video/docs/api-documentation/) with automatic multi-instance failover, Invidious API v1 fallback parsing, and a zero-key Open Media Catalog fallback featuring Blender Studio open movies and NASA UHD science streams.
- **Media3 ExoPlayer & Background Service**: Implemented `ReTubePlayerController` and `ReTubePlaybackService` (`MediaSessionService`) using [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) with HLS, DASH, progressive MP4, and `MergingMediaSource` support, background audio playback, lockscreen controls, and Picture-in-Picture (PiP) mode.
- **SponsorBlock & Return YouTube Dislike**: Added live [SponsorBlock](https://sponsor.ajay.app/) segment fetching, timeline markers, and automatic category-filtered skipping, plus [Return YouTube Dislike](https://returnyoutubedislike.com/) vote count and ratio restoration.
- **Local Media Library**: Built [AndroidX Room](https://developer.android.com/training/data-storage/room) persistence for Watch History (with resume position), Local Playlists, Bookmarked videos, Local Channel Subscriptions, Search History, and Offline Downloads.
- **Resumable Media Downloader**: Implemented `DownloadManagerController` and `MediaDownloadWorker` with HTTP `Range` support for video and audio-only offline downloads.
- **Customization & Privacy Controls**: Added [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore) preferences for System/Light/Dark themes, Material You Dynamic Color, Pure Black AMOLED mode, default resolution, playback speed, custom Piped instance URLs, and history pause/clear controls.