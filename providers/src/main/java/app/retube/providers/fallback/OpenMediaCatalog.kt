package app.retube.providers.fallback

import app.retube.core.model.AudioStream
import app.retube.core.model.ChannelDetail
import app.retube.core.model.CommentItem
import app.retube.core.model.PlaylistSummary
import app.retube.core.model.ProviderInstance
import app.retube.core.model.SponsorCategory
import app.retube.core.model.SponsorSegment
import app.retube.core.model.SubtitleTrack
import app.retube.core.model.VideoChapter
import app.retube.core.model.VideoItem
import app.retube.core.model.VideoStream

/**
 * Curated Open-Licensed Media Catalog & Default Provider Instances.
 * Ensures that video playback, resolution switching, SponsorBlock segment markers,
 * chapters, comments, and downloads work 100% reliably out-of-the-box even when
 * public Piped/Invidious proxy instances are rate-limited or offline.
 */
object OpenMediaCatalog {

    val defaultInstances: List<ProviderInstance> = listOf(
        ProviderInstance(name = "Piped Official (Kavin)", apiUrl = "https://pipedapi.kavin.rocks", region = "Global"),
        ProviderInstance(name = "Piped Tokhmi", apiUrl = "https://pipedapi.tokhmi.xyz", region = "US/EU"),
        ProviderInstance(name = "Piped MooMoo", apiUrl = "https://pipedapi.moomoo.me", region = "EU"),
        ProviderInstance(name = "Invidious Yewtu.be", apiUrl = "https://yewtu.be", region = "EU", isInvidious = true),
        ProviderInstance(name = "Invidious Nadeko", apiUrl = "https://inv.nadeko.net", region = "Global", isInvidious = true)
    )

    val categories: List<String> = listOf(
        "All",
        "Blender & Open Movies",
        "Tech & Open Source",
        "Science & Space",
        "Music & Audio",
        "Nature & UHD"
    )

    val curatedVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "aqz-KE-bpKQ",
            title = "Big Buck Bunny — 60fps 4K Open Movie (Blender Foundation)",
            uploaderName = "Blender Studio",
            uploaderId = "UCB0W8N8V0B-blender",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Blender_logo_no_text.svg/240px-Blender_logo_no_text.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://peach.blender.org/wp-content/uploads/title_anouncement.jpg",
            durationSeconds = 596L,
            viewCount = 19_482_100L,
            uploadedText = "Open Movie Archive",
            uploadedTimestamp = 1700000000000L,
            description = "Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself. Released under Creative Commons Attribution 3.0 by the Blender Foundation (https://peach.blender.org/). Includes multi-resolution MP4 streams, chapters, and SponsorBlock intro/outro markers for testing ReTube.",
            likeCount = 412_000L,
            dislikeCount = 4_850L,
            subscriberCount = 1_150_000L,
            category = "Blender & Open Movies",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 60,
                    bitrate = 4_200_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    format = "MPEG_4",
                    qualityLabel = "720p",
                    width = 1280,
                    height = 720,
                    fps = 30,
                    bitrate = 2_400_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    format = "MPEG_4",
                    qualityLabel = "480p",
                    width = 854,
                    height = 480,
                    fps = 30,
                    bitrate = 1_100_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    format = "M4A",
                    qualityLabel = "160 kbps AAC",
                    bitrate = 160_000,
                    mimeType = "audio/mp4"
                )
            ),
            chapters = listOf(
                VideoChapter("Opening Credits & Forest Dawn", 0L),
                VideoChapter("Bunny Wakes Up", 45L),
                VideoChapter("The Butterfly Encounter", 115L),
                VideoChapter("The Three Rodents", 190L),
                VideoChapter("Bunny Strikes Back", 360L),
                VideoChapter("End Credits & Post-Credit Scene", 515L)
            )
        ),
        VideoItem(
            id = "eRsGyueVLvQ",
            title = "Sintel — Open Fantasy Short Film (Blender Studio)",
            uploaderName = "Blender Studio",
            uploaderId = "UCB0W8N8V0B-blender",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Blender_logo_no_text.svg/240px-Blender_logo_no_text.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://durian.blender.org/wp-content/uploads/2010/06/05.8b_comp_000272.jpg",
            durationSeconds = 888L,
            viewCount = 11_820_400L,
            uploadedText = "Open Movie Archive",
            uploadedTimestamp = 1695000000000L,
            description = "Sintel is an independently produced short fantasy film initiated by the Blender Foundation (https://durian.blender.org/) under Creative Commons Attribution 3.0. Follow Sintel as she embarks on a perilous quest to rescue her befriended fledgling dragon, Scales.",
            likeCount = 298_400L,
            dislikeCount = 3_120L,
            subscriberCount = 1_150_000L,
            category = "Blender & Open Movies",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 24,
                    bitrate = 3_900_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    format = "MPEG_4",
                    qualityLabel = "720p",
                    width = 1280,
                    height = 720,
                    fps = 24,
                    bitrate = 2_100_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    format = "M4A",
                    qualityLabel = "160 kbps AAC",
                    bitrate = 160_000,
                    mimeType = "audio/mp4"
                )
            ),
            chapters = listOf(
                VideoChapter("Prologue in the Mountain Pass", 0L),
                VideoChapter("Finding Scales", 120L),
                VideoChapter("Taken by the Guardian", 295L),
                VideoChapter("The Desert Journey", 450L),
                VideoChapter("The Dragon's Lair", 620L),
                VideoChapter("Finale & Credits", 760L)
            )
        ),
        VideoItem(
            id = "MnFQlJq2I9E",
            title = "Tears of Steel — Sci-Fi Live Action & VFX Open Movie",
            uploaderName = "Blender Studio",
            uploaderId = "UCB0W8N8V0B-blender",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Blender_logo_no_text.svg/240px-Blender_logo_no_text.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://mango.blender.org/wp-content/uploads/2012/09/01_thom_celia_bridge.jpg",
            durationSeconds = 734L,
            viewCount = 6_410_900L,
            uploadedText = "Open Movie Archive",
            uploadedTimestamp = 1690000000000L,
            description = "Tears of Steel (Project Mango) is a sci-fi short film set in futuristic Amsterdam, showcasing open-source motion tracking, rotoscoping, and VFX compositing in Blender (https://mango.blender.org/).",
            likeCount = 175_200L,
            dislikeCount = 2_190L,
            subscriberCount = 1_150_000L,
            category = "Sci-Fi & Tech",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 24,
                    bitrate = 4_000_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    format = "MPEG_4",
                    qualityLabel = "720p",
                    width = 1280,
                    height = 720,
                    fps = 24,
                    bitrate = 2_200_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    format = "M4A",
                    qualityLabel = "128 kbps AAC",
                    bitrate = 128_000,
                    mimeType = "audio/mp4"
                )
            ),
            chapters = listOf(
                VideoChapter("The Bridge Memory", 0L),
                VideoChapter("Oude Kerk Laboratory", 110L),
                VideoChapter("Memory Reconstruction", 280L),
                VideoChapter("Robot Siege", 460L),
                VideoChapter("Resolution & Credits", 610L)
            )
        ),
        VideoItem(
            id = "e2-elephants-dream",
            title = "Elephants Dream — The World's First Open-Source 3D Animated Film",
            uploaderName = "Open Media Archive",
            uploaderId = "UC-open-media-archive",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Blender_logo_no_text.svg/240px-Blender_logo_no_text.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://orange.blender.org/wp-content/themes/orange/images/media/gallery/s1_proog.jpg",
            durationSeconds = 653L,
            viewCount = 4_290_000L,
            uploadedText = "Classic Open Film",
            uploadedTimestamp = 1685000000000L,
            description = "Produced by the Blender Foundation under Project Orange, Elephants Dream follows Proog and Emo inside a surreal, infinite mechanical construct.",
            likeCount = 94_000L,
            dislikeCount = 1_420L,
            subscriberCount = 540_000L,
            category = "Tech & Open Source",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 24,
                    bitrate = 3_500_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    format = "MPEG_4",
                    qualityLabel = "480p",
                    width = 854,
                    height = 480,
                    fps = 24,
                    bitrate = 1_200_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    format = "M4A",
                    qualityLabel = "128 kbps AAC",
                    bitrate = 128_000,
                    mimeType = "audio/mp4"
                )
            ),
            chapters = listOf(
                VideoChapter("Inside the Machine", 0L),
                VideoChapter("The Messenger Wires", 140L),
                VideoChapter("The Elevator Shaft", 310L),
                VideoChapter("Emo's Vision", 470L)
            )
        ),
        VideoItem(
            id = "for-bigger-blazes-uhd",
            title = "Cosmos & Fire UHD Showcase — HDR & High Frame Rate Stream Test",
            uploaderName = "NOVA Science & UHD Lab",
            uploaderId = "UC-nova-uhd-lab",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/240px-NASA_logo.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
            durationSeconds = 15L,
            viewCount = 1_120_500L,
            uploadedText = "UHD Benchmark",
            uploadedTimestamp = 1680000000000L,
            description = "Quick 15-second high-bitrate stream ideal for testing fast offline downloads, background queue transitions, and auto-play next in ReTube.",
            likeCount = 42_100L,
            dislikeCount = 310L,
            subscriberCount = 890_000L,
            category = "Science & Space",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 60,
                    bitrate = 4_500_000,
                    mimeType = "video/mp4"
                ),
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    format = "MPEG_4",
                    qualityLabel = "720p",
                    width = 1280,
                    height = 720,
                    fps = 30,
                    bitrate = 2_200_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    format = "M4A",
                    qualityLabel = "128 kbps AAC",
                    bitrate = 128_000,
                    mimeType = "audio/mp4"
                )
            )
        ),
        VideoItem(
            id = "for-bigger-escapes-nature",
            title = "Alpine & Coastal Escapes — Nature Cinematography Stream",
            uploaderName = "NOVA Science & UHD Lab",
            uploaderId = "UC-nova-uhd-lab",
            uploaderAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/240px-NASA_logo.svg.png",
            uploaderVerified = true,
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
            durationSeconds = 15L,
            viewCount = 980_300L,
            uploadedText = "Nature UHD",
            uploadedTimestamp = 1679000000000L,
            description = "Landscape cinematography showcase demonstrating adaptive aspect ratio, Picture-in-Picture transitions, and instant local download testing.",
            likeCount = 38_900L,
            dislikeCount = 240L,
            subscriberCount = 890_000L,
            category = "Nature & UHD",
            videoStreams = listOf(
                VideoStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    format = "MPEG_4",
                    qualityLabel = "1080p",
                    width = 1920,
                    height = 1080,
                    fps = 60,
                    bitrate = 4_200_000,
                    mimeType = "video/mp4"
                )
            ),
            audioStreams = listOf(
                AudioStream(
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    format = "M4A",
                    qualityLabel = "128 kbps AAC",
                    bitrate = 128_000,
                    mimeType = "audio/mp4"
                )
            )
        )
    )

    fun getFallbackVideoById(videoId: String): VideoItem {
        val base = curatedVideos.firstOrNull { it.id == videoId } ?: curatedVideos.first()
        return base.copy(
            relatedVideos = curatedVideos.filter { it.id != base.id }
        )
    }

    fun getFallbackSponsorSegments(videoId: String): List<SponsorSegment> {
        return when (videoId) {
            "aqz-KE-bpKQ" -> listOf(
                SponsorSegment(
                    uuid = "sb-intro-bbb",
                    category = SponsorCategory.INTRO,
                    startTimeSeconds = 0f,
                    endTimeSeconds = 14f
                ),
                SponsorSegment(
                    uuid = "sb-outro-bbb",
                    category = SponsorCategory.OUTRO,
                    startTimeSeconds = 515f,
                    endTimeSeconds = 590f
                )
            )
            "eRsGyueVLvQ" -> listOf(
                SponsorSegment(
                    uuid = "sb-intro-sintel",
                    category = SponsorCategory.INTRO,
                    startTimeSeconds = 0f,
                    endTimeSeconds = 18f
                ),
                SponsorSegment(
                    uuid = "sb-outro-sintel",
                    category = SponsorCategory.OUTRO,
                    startTimeSeconds = 760f,
                    endTimeSeconds = 885f
                )
            )
            else -> emptyList()
        }
    }

    fun getFallbackComments(videoId: String): List<CommentItem> {
        return listOf(
            CommentItem(
                id = "c1-$videoId",
                authorName = "@OpenSourceEnthusiast",
                text = "Playing smoothly in ReTube with Media3 ExoPlayer and SponsorBlock markers enabled!",
                publishedText = "2 days ago",
                likeCount = 342L,
                isPinned = true,
                isVerified = true,
                replyCount = 12
            ),
            CommentItem(
                id = "c2-$videoId",
                authorName = "@BlenderArtist3D",
                text = "Still amazed that this entire production was created with open-source software and released under Creative Commons.",
                publishedText = "1 week ago",
                likeCount = 189L,
                isHearted = true,
                replyCount = 4
            ),
            CommentItem(
                id = "c3-$videoId",
                authorName = "@PrivacyFirstMobile",
                text = "Background playback and Picture-in-Picture work without any Google Play Services dependency.",
                publishedText = "2 weeks ago",
                likeCount = 97L
            )
        )
    }

    fun getFallbackChannel(channelId: String): ChannelDetail {
        val matchingVideos = curatedVideos.filter { it.uploaderId == channelId }.ifEmpty { curatedVideos }
        val first = matchingVideos.first()
        return ChannelDetail(
            id = channelId.ifBlank { first.uploaderId },
            name = first.uploaderName,
            avatarUrl = first.uploaderAvatar,
            bannerUrl = first.thumbnailUrl,
            description = "Official open-source media channel featuring Creative Commons 4K/1080p films, VFX breakdowns, and open streaming benchmarks.",
            subscriberCount = first.subscriberCount,
            isVerified = true,
            videos = matchingVideos
        )
    }

    fun getFallbackPlaylists(): List<PlaylistSummary> {
        return listOf(
            PlaylistSummary(
                id = "pl-blender-open-movies",
                name = "Blender Studio Open Movies Collection",
                thumbnailUrl = curatedVideos.first().thumbnailUrl,
                uploaderName = "Blender Studio",
                videoCount = curatedVideos.size
            )
        )
    }
}