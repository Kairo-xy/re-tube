# ReTube (NOVA Tube)

**ReTube (NOVA Tube)** is a privacy-focused, modular native Android streaming client built with **Kotlin**, **[Jetpack Compose](https://developer.android.com/develop/ui/compose/documentation)**, **Material 3**, and **[AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer)**. It enables ad-free browsing, background audio playback, Picture-in-Picture (PiP), [SponsorBlock](https://sponsor.ajay.app/) auto-skipping, [Return YouTube Dislike](https://returnyoutubedislike.com/) integration, local subscriptions, playlists, and offline media downloads without requiring a Google account or proprietary YouTube Data API keys.

---

## Features

- **Privacy-First Multi-Provider Streaming**:
  - Queries public **[Piped API](https://docs.piped.video/docs/api-documentation/)** instances with automatic instance failover and Invidious API v1 fallback parsing.
  - Includes a built-in **Open Media Catalog fallback** (Blender Studio Open Movies & NASA UHD streams) so video playback, quality switching, chapters, and downloads work out of the box even when public proxy instances are rate-limited or unreachable.
- **Advanced Media3 ExoPlayer Playback**:
  - Supports HLS (`.m3u8`), DASH (`.mpd`), progressive streams, and `MergingMediaSource` (combining adaptive video-only streams with high-bitrate audio).
  - Full background playback & lockscreen media controls powered by `MediaSessionService`.
  - Native **Picture-in-Picture (PiP)** support when minimizing the app during active video playback.
  - Variable playback speed (`0.25x` to `2.5x`), resolution selector (`1080p`, `720p`, `480p`, `360p`, `Audio Only`), audio-only mode, and configurable sleep timer.
- **SponsorBlock & Return YouTube Dislike**:
  - Automatically fetches crowdsourced segments from the **[SponsorBlock API](https://github.com/ajayyy/SponsorBlock/wiki/API-Docs)** (`sponsor`, `selfpromo`, `interaction`, `intro`, `outro`, `music_offtopic`), renders color-coded markers on the seekbar, and auto-skips segments during playback.
  - Restores dislike counts and like-to-dislike ratios via the **[Return YouTube Dislike API](https://returnyoutubedislike.com/docs)**.
- **Local-First Media Library ([AndroidX Room](https://developer.android.com/training/data-storage/room))**:
  - **Watch History**: Automatically tracks playback progress and resumes videos where you left off.
  - **Local Subscriptions**: Subscribe to channels locally without an account and browse a unified chronological subscription feed.
  - **Custom Local Playlists & Bookmarks**: Create custom playlists, reorder or remove tracks, and bookmark videos for offline organization.
- **Offline Media Downloader**:
  - Resumable HTTP file downloads (`Range` header support) with pause, resume, retry, and deletion controls, backed by `WorkManager` and Room.
- **Material 3 & Pure Black AMOLED UI**:
  - Supports Android 12+ Material You dynamic colors, System/Light/Dark modes, and a true `#000000` **Pure Black AMOLED** theme.

---

## Screenshots

> **Placeholder Notice**: Replace the placeholders below with actual device screenshots after building the APK in Android Studio or downloading the build artifact from GitHub Actions.

| Home & Trending Feed | Video Player & SponsorBlock | Library & Offline Downloads | Settings & AMOLED Theme |
| :---: | :---: | :---: | :---: |
| `[Screenshot Placeholder: docs/screenshots/home_feed.png]` | `[Screenshot Placeholder: docs/screenshots/player_sponsorblock.png]` | `[Screenshot Placeholder: docs/screenshots/library_downloads.png]` | `[Screenshot Placeholder: docs/screenshots/settings_amoled.png]` |

---

## Architecture Overview

ReTube is structured into 6 Gradle modules with strict unidirectional data flow:

```text
NOVA-Tube/
├── app/         # Application entry point, MainActivity, PiP lifecycle, YouTube share/deep-link intents
├── core/        # Domain models, Room Database & DAOs, DataStore Preferences, Material 3 Theme, Utilities
├── providers/   # Piped API client, Invidious fallback parser, SponsorBlock client, RYD client, OpenCatalog
├── playback/    # Media3 ExoPlayer controller, MediaSessionService, SponsorBlock skipper, Queue & Sleep Timer
├── downloads/   # Resumable HTTP downloader, WorkManager CoroutineWorker, offline file resolver
└── features/    # Jetpack Compose screens (Home, Search, Player, Channel, Subscriptions, Library, Settings)
```

---

## Installation & Build Instructions

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer.
- **JDK 17** (configured in Android Studio or via `JAVA_HOME`).
- **Android SDK Platform 35** (minSdk 26 / targetSdk 35).

### Building Locally
1. Extract `retube.zip` and open the `NOVA-Tube` folder in Android Studio, or clone your GitHub repository:
   ```bash
   git clone https://github.com/<your-username>/retube.git
   cd retube
   ```
2. If the Gradle wrapper JAR is not yet initialized locally, generate it with Gradle 8.9 or let Android Studio sync the project:
   ```bash
   gradle wrapper --gradle-version 8.9
   ./gradlew assembleDebug
   ```
3. The compiled debug APK will be generated at:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Building Automatically with GitHub Actions
Push the repository to GitHub. The included workflow at `.github/workflows/android-build.yml` automatically provisions JDK 17 and Gradle 8.9, runs unit tests (`testDebugUnitTest`), compiles the debug APK (`assembleDebug`), and uploads `retube-debug-apk` as a downloadable build artifact.

---

## Dependencies & Licenses

| Library / Service | Purpose | License |
| :--- | :--- | :--- |
| [Jetpack Compose & Material 3](https://developer.android.com/develop/ui/compose/documentation) | Declarative UI toolkit & dynamic theming | Apache-2.0 |
| [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) | Adaptive HLS/DASH/MP4 playback & `MediaSessionService` | Apache-2.0 |
| [AndroidX Room](https://developer.android.com/training/data-storage/room) | Local SQLite persistence for history, playlists, subscriptions, and downloads | Apache-2.0 |
| [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Coroutine-based user preferences storage | Apache-2.0 |
| [Coil Compose](https://github.com/coil-kt/coil) | Asynchronous thumbnail and avatar image loading | Apache-2.0 |
| [OkHttp](https://square.github.io/okhttp/) | HTTP client for API requests and resumable media downloads | Apache-2.0 |
| [Piped API](https://docs.piped.video/docs/api-documentation/) | Privacy-friendly YouTube proxy API | AGPL-3.0 |
| [SponsorBlock API](https://sponsor.ajay.app/) | Crowdsourced sponsor segment timestamps | CC BY-NC-SA 4.0 / LGPL-3.0 |
| [Return YouTube Dislike API](https://returnyoutubedislike.com/) | Community-maintained dislike count statistics | GPL-3.0 |

---

## Known Limitations & Design Alternatives

1. **Public Piped / Invidious Instance Rate Limiting**:
   - *Limitation*: Public Piped and Invidious proxy servers frequently face IP rate-limiting or CAPTCHA challenges from upstream servers.
   - *Implemented Alternative*: `PipedApiClient` automatically rotates across multiple public Piped and Invidious instances, allows users to configure a custom self-hosted instance URL in **Settings**, and seamlessly falls back to the built-in `OpenMediaCatalog` so playback and testing never break.
2. **Google Account Login / Cloud Sync**:
   - *Limitation*: Direct YouTube OAuth account mutation (posting comments or syncing private cloud playlists) requires restricted Google API credentials and compromises user anonymity.
   - *Implemented Alternative*: All subscriptions, watch history, bookmarks, and custom playlists are stored locally on-device in [AndroidX Room](https://developer.android.com/training/data-storage/room) with JSON export/import support.
3. **DRM / Widevine Protected Content**:
   - *Limitation*: Paid movies or rental streams encrypted with Widevine DRM cannot be extracted via third-party privacy proxies.
   - *Implemented Alternative*: Standard unencrypted adaptive HLS, DASH, and progressive MP4/WebM streams play natively in Media3 ExoPlayer.

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)** — see the [LICENSE](LICENSE) file for details.