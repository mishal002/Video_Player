package e_card.e_cardaddress.adresschange.video_player.Activitys

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    companion object {
        private lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<Video>
        var position: Int = -1
        var repeat: Boolean = false
        private var isFullscreen: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
//
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setTheme(R.style.playerActivityTheme)
        setContentView(binding.root)

//        for full screen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        }

        intializeLayout()
        intilizeBinding()
    }

    fun intializeLayout() {
        when (intent.getStringExtra("class")) {
            "AllVideo" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                createplyer()
            }
            "FolderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FolderActivity.currentFolderVideo)
            }
        }
        if (repeat) {
            binding.repeatBtn.setImageResource(R.drawable.repeat_on_btn)
        } else {
            binding.repeatBtn.setImageResource(R.drawable.repeat_off_btn)

        }
    }

    fun intilizeBinding() {
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.playPauseBtn.setOnClickListener {

            if (player.isPlaying) {
                pauseVideo()
            } else
                playVideo()
        }
        binding.nextBtn.setOnClickListener {
            nextPrevVideo()
        }
        binding.prevBtn.setOnClickListener { nextPrevVideo(isNext = false) }

        binding.repeatBtn.setOnClickListener {
            if (repeat) {
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.repeat_off_btn)
            } else {
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.repeatBtn.setImageResource(R.drawable.repeat_on_btn)
            }
        }
        binding.fullScreenBtn.setOnClickListener {
            if (isFullscreen) {
                isFullscreen = false
                playFullScreen(enable = false)
            } else {
                isFullscreen = true
                playFullScreen(enable = true)
            }
        }

    }

    fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        player.play()
    }

    fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        player.pause()
    }

    fun createplyer() {
        try {
            player.release()
        } catch (e: Exception) {
        }
        binding.videoTital.text = playerList[position].title
        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextPrevVideo()
                }
            }
        })
        playFullScreen(enable = isFullscreen)
        setVisibility()
    }

    //    next video
    fun nextPrevVideo(isNext: Boolean = true) {
        if (isNext) {
            setPostion()
        } else
            setPostion(isIncrement = false)
        createplyer()
    }

    fun setPostion(isIncrement: Boolean = true) {

        if (!repeat) {
            if (isIncrement) {
                if (playerList.size - 1 == position) {
                    position = 0
                } else {
                    ++position
                }
            } else {
                if (position == 0) {
                    position = playerList.size - 1
                } else {
                    --position
                }
            }
        }
    }

    //    full screen
    fun playFullScreen(enable: Boolean) {
        if (enable) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenBtn.setImageResource(R.drawable.full_screen_exit)
        } else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenBtn.setImageResource(R.drawable.full_screen_btn)
        }

    }

    fun setVisibility() {
        runnable = Runnable {
            if (binding.playerView.isControllerVisible) {
                changVisibilty(View.VISIBLE)
            } else {
                changVisibilty(View.GONE)
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    fun changVisibilty(visibility: Int) {
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        binding.playPauseBtn.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}