package com.royalenfield.ff.dashprototype

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NowPlaying(val title: String, val artist: String, val playing: Boolean)

// Notification listener. Android only lets an app read and control other music apps once the rider
// grants notification access (Settings > Notifications > Notification access).
class MediaListener : NotificationListenerService()

// Reads and controls whichever music app is playing (F-07).
object MusicHub {
    private val _now = MutableStateFlow<NowPlaying?>(null)
    val now: StateFlow<NowPlaying?> = _now.asStateFlow()

    private var controller: MediaController? = null

    private fun sessionManager(context: Context) =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private fun listenerName(context: Context) = ComponentName(context, MediaListener::class.java)

    // True when the rider has granted notification access, so media sessions can be read.
    fun hasAccess(context: Context): Boolean = try {
        sessionManager(context).getActiveSessions(listenerName(context))
        true
    } catch (e: SecurityException) {
        false
    }

    // Reads the first active music session into [now]. Called about once a second while music is shown.
    fun refresh(context: Context) {
        try {
            val session = sessionManager(context).getActiveSessions(listenerName(context)).firstOrNull()
            controller = session
            if (session == null) {
                _now.value = null
                return
            }
            val metadata: MediaMetadata? = session.metadata
            _now.value = NowPlaying(
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Nothing playing",
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                playing = session.playbackState?.state == PlaybackState.STATE_PLAYING,
            )
        } catch (e: SecurityException) {
            _now.value = null
        }
    }

    fun playPause() {
        val session = controller ?: return
        if (_now.value?.playing == true) session.transportControls?.pause() else session.transportControls?.play()
    }

    fun next() {
        controller?.transportControls?.skipToNext()
    }

    fun previous() {
        controller?.transportControls?.skipToPrevious()
    }
}
