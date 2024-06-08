package ru.netology.nework.viewmodel

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nework.players.MPlayer
import ru.netology.nework.util.AndroidUtils

class MediaViewModel: ViewModel() {
    private val _player = MutableLiveData(MPlayer())

    private val _duration = MutableLiveData(String())
    val duration: LiveData<String>
        get() = _duration

    fun playAudio(track: String) {
        _player.value?.play(track, object : MPlayer.GetInfo{
            override fun getDuration(dut: Int) {
                _duration.value = AndroidUtils.getTimeTrack(dut)
            }

            override fun onCompletionPlay() {
                _player.value!!.stopPlayer()
                _duration.value = "STOP"
            }
        })
    }

    fun playVideo(url: String, view: VideoView){
        view.apply {
            setMediaController(MediaController(context))
            setVideoURI(
                Uri.parse(url)
            )
            setOnPreparedListener {
                start()
            }
        }
    }

    fun pauseAudio() {
        _player.value?.pausePlayer()
    }

    fun stopAudio(){
        _player.value?.stopPlayer()
    }
}