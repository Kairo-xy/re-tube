package app.retube.playback.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * AndroidX Media3 MediaSessionService providing system media notification,
 * lockscreen transport controls, Bluetooth/headset button handling, and
 * uninterrupted background audio playback.
 */
class ReTubePlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        @Volatile
        var sharedPlayerProvider: (() -> Player)? = null
    }

    override fun onCreate() {
        super.onCreate()
        val player = sharedPlayerProvider?.invoke() ?: return
        mediaSession = MediaSession.Builder(this, player)
            .setId("ReTubeMediaSession")
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}