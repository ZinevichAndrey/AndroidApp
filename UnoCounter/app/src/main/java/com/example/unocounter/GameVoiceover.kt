package com.example.unocounter

import android.content.Context
import android.media.MediaPlayer



class GameVoiceover() {
    private lateinit var mediaPlayer: MediaPlayer


    fun init(context: Context, audio: Int, volume: Float = 1.0f) {
        this.mediaPlayer = MediaPlayer.create(context, audio)
        this.mediaPlayer.setVolume(volume, volume)

    }

    fun startPlayer() {
        this.mediaPlayer.start()
    }

    fun restartAudioTrack() {
        this.mediaPlayer.pause()
        this.mediaPlayer.seekTo(0)
    }

    fun getMediaPlayer(): MediaPlayer {
        return this.mediaPlayer
    }

    fun releasePlayer() {
        this.mediaPlayer.release()
    }


}