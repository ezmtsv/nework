package ru.netology.nework.players

import android.media.MediaPlayer
//import ru.netology.mediaplayer.activity.URL_LOAD_SONG
import java.io.IOException

class MPlayer {
    private var player: MediaPlayer? = null

    interface GetInfo {
        fun getDuration(dut: Int)
        fun onCompletionPlay()
    }

    fun play(link: String, dt: GetInfo) {
        try {
            if (player == null) {
                player = MediaPlayer().apply {
                    this.setDataSource(
//                        "$URL_LOAD_SONG$link"
                        link
                    )
                    this.setOnPreparedListener {
                        dt.getDuration(it.duration)
                        it.start()
                        //it.seekTo(472000-5000)
                    }
                    this.prepareAsync()
                    this.setOnCompletionListener{
                        dt.onCompletionPlay()
                    }
                }
            } else {
                player?.start()
            }

        } catch (e: IOException) {
            println(e)
        }
    }

    fun pausePlayer() {
        try {
            if (player?.isPlaying == true) player?.pause()
        } catch (e: IOException) {
            println(e)
        }

    }

    fun stopPlayer() {
        try {
            if (player != null) {
                player!!.release()
                player = null
            }
        } catch (e: IOException) {
            println(e)
        }

    }

}