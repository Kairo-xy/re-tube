# Contributing to ReTube (NOVA Tube)

Thank you for your interest in contributing to **ReTube (NOVA Tube)**!

## Architecture & Module Guidelines

ReTube follows a strict multi-module clean architecture:

- **`:app`** — Application entry point, `MainActivity`, adaptive navigation scaffold, Picture-in-Picture lifecycle, and deep-link intent filters.
- **`:core`** — Domain models, [AndroidX Room](https://developer.android.com/training/data-storage/room) database entities & DAOs, [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore) user preferences, Material 3 dynamic/AMOLED theme, and shared utilities.
- **`:providers`** — Network data sources including [Piped API](https://docs.piped.video/docs/api-documentation/) with multi-instance failover, Invidious fallback parser, [SponsorBlock API](https://sponsor.ajay.app/), [Return YouTube Dislike API](https://returnyoutubedislike.com/), and an offline-capable Open Media Catalog fallback.
- **`:playback`** — [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) controller, `MediaSessionService` for background audio and lockscreen controls, SponsorBlock segment skipper, sleep timer, and queue management.
- **`:downloads`** — Resumable HTTP stream downloader (`Range` header support), `CoroutineWorker` background execution, and offline playback file resolver.
- **`:features`** — [Jetpack Compose](https://developer.android.com/develop/ui/compose/documentation) UI screens (Home, Search, Video Player Sheet, Channel Details, Subscriptions, Library, and Settings) and `ReTubeMainViewModel`.

## Pull Request Checklist

1. **No Proprietary Keys or Secrets**: Never commit API keys, signing keystores, or proprietary assets.
2. **No Dead UI Stubs**: Every interactive button or toggle in Compose screens must be wired to a real state flow, Room DAO action, or player command.
3. **Code Formatting**: Follow official Kotlin style conventions (`kotlin.code.style=official`).
4. **Tests**: Add or update unit tests in `:core` or `:providers` when modifying parsers, formatters, or repository logic.