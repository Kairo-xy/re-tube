package app.retube

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import app.retube.core.database.ReTubeDatabase
import app.retube.playback.ReTubePlayerController
import app.retube.playback.service.ReTubePlaybackService

class ReTubeApplication : Application() {

    lateinit var playerController: ReTubePlayerController
        private set

    override fun onCreate() {
        super.onCreate()
        val database = ReTubeDatabase.getInstance(this)
        playerController = ReTubePlayerController(this, database.watchHistoryDao())
        ReTubePlaybackService.sharedPlayerProvider = { playerController.exoPlayer }
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val playbackChannel = NotificationChannel(
            "retube_playback",
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background audio and video playback controls"
        }
        val downloadChannel = NotificationChannel(
            "retube_downloads",
            "Offline Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Progress notifications for offline video and audio downloads"
        }
        manager.createNotificationChannel(playbackChannel)
        manager.createNotificationChannel(downloadChannel)
    }
}