package e_card.e_cardaddress.adresschange.video_player.Activitys

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import e_card.e_cardaddress.adresschange.video_player.Class.DoubleClickListener
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.ActivityPlayerBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.BoosterBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.MoreFeaturesBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.SpeedDialogBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    lateinit var runnable: Runnable
    var isSubtital: Boolean = true
    private var moreTime: Int = 0

    companion object {
        private var timer: Timer? = null
        private lateinit var player: ExoPlayer
        private lateinit var playerList: ArrayList<Video>
        var position: Int = -1
        var repeat: Boolean = false
        private var isFullscreen: Boolean = false
        private var isLocked: Boolean = false
        private lateinit var loudnessEnhancer: LoudnessEnhancer

        @SuppressLint("StaticFieldLeak")
        private lateinit var trackSelector: DefaultTrackSelector
        private var speed: Float = 1.0f
        var PipStatus: Int = 0
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
        binding.forwedFl.setOnClickListener(DoubleClickListener(callback = object :
            DoubleClickListener.Callback {
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.forwedBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition + 10000)
                moreTime = 0
            }
        }))

        binding.rewideFL.setOnClickListener(DoubleClickListener(callback = object :
            DoubleClickListener.Callback {
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.rewindBtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition - 10000)
                moreTime = 0
            }
        }))
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
                createplyer()
            }
            "SearchVideo" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.searchList)
                createplyer()
            }
        }
        if (repeat) {
            binding.repeatBtn.setImageResource(R.drawable.repeat_on_btn)
        } else {
            binding.repeatBtn.setImageResource(R.drawable.repeat_off_btn)

        }
    }

    //All Click
    @SuppressLint("SetTextI18n")
    fun intilizeBinding() {
        binding.backBtn.setOnClickListener {
            vibrat()
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
            vibrat()
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
                player.repeatMode = com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.repeat_off_btn)
            } else {
                repeat = true
                player.repeatMode = com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
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
        binding.lockBtn.setOnClickListener {
            when (!isLocked) {
                true -> {
                    isLocked = true
                    binding.playerView.hideController()
                    binding.playerView.useController = false
                    binding.lockBtn.setImageResource(R.drawable.close_lock)
                }
                false -> {
                    isLocked = false
                    binding.playerView.useController = true
                    binding.playerView.showController()
                    binding.lockBtn.setImageResource(R.drawable.un_lock)
                }
            }
        }
/**/
        binding.moreFeatureBtn.setOnClickListener {
            pauseVideo()
            val customeDialog =
                LayoutInflater.from(this).inflate(R.layout.more_features, binding.root, false)
            val bindingMF = MoreFeaturesBinding.bind(customeDialog)
            val dialog = MaterialAlertDialogBuilder(
                this
            ).setView(customeDialog)
                .setOnCancelListener { playVideo() }
                .setBackground(ColorDrawable(0x803700B3.toInt()))
                .create()
            dialog.show()
//
            bindingMF.audioTrack.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTrack = ArrayList<String>()
                for (i in 0 until player.currentTrackGroups.length) {
                    if (player.currentTrackGroups.get(i)
                            .getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT
                    ) {
                        audioTrack.add(
                            Locale(
                                player.currentTrackGroups.get(i).getFormat(0).language.toString()
                            ).displayLanguage
                        )
                    }
                }
                val tempTrack = audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Language")
                    .setOnCancelListener { playVideo() }
                    .setBackground(ColorDrawable(0x803700B3.toInt()))
                    .setItems(tempTrack) { _, position ->
                        Toast.makeText(this, audioTrack[position] + "Selected", Toast.LENGTH_SHORT)
                            .show()
                        trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                .setPreferredAudioLanguage(audioTrack[position])
                        )
                    }
                    .create()
                    .show()
            }
//
            bindingMF.subTital.setOnClickListener {
                if (isSubtital) {
                    trackSelector.parameters =
                        DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, true
                        ).build()
                    Toast.makeText(this, "Subtitles Off", Toast.LENGTH_SHORT).show()
                    isSubtital = false
                } else {
                    trackSelector.parameters =
                        DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, false
                        ).build()
                    Toast.makeText(this, "Subtitles On", Toast.LENGTH_SHORT).show()
                    isSubtital = true
                }
                dialog.dismiss()
                playVideo()
            }
/*Haf Code*/
            bindingMF.audioBooster.setOnClickListener {
                dialog.dismiss()
                val customeDialogB =
                    LayoutInflater.from(this).inflate(R.layout.booster, binding.root, false)
                val bindingB = BoosterBinding.bind(customeDialogB)
                val dialogB = MaterialAlertDialogBuilder(this).setView(customeDialogB)
                    .setOnCancelListener { playVideo() }
                    .setPositiveButton("OK") { self, _ ->
                        loudnessEnhancer.setTargetGain(bindingB.verticalBar.progress * 100)
                        playVideo()
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x803700B3.toInt()))
                    .create()
                dialogB.show()
                bindingB.verticalBar.progress = loudnessEnhancer.targetGain.toInt() / 100
                bindingB.progressText.text =
                    "Audio Boost\n\n${loudnessEnhancer.targetGain.toInt() / 10}"
                playVideo()
            }
            /*Haf Code*/

            bindingMF.speedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val customeDialogS =
                    LayoutInflater.from(this).inflate(R.layout.speed_dialog, binding.root, false)
                val bindingS = SpeedDialogBinding.bind(customeDialogS)
                val dialogS = MaterialAlertDialogBuilder(this).setView(customeDialogS)
                    .setCancelable(false)
                    .setPositiveButton("OK") { self, _ ->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x803700B3.toInt()))
                    .create()
                dialogS.show()
                bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                bindingS.minusBtn.setOnClickListener {
                    ChangeSpeed(isIncrement = false)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
                bindingS.plusBtn.setOnClickListener {
                    ChangeSpeed(isIncrement = true)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
            }
//
            bindingMF.sleepTimer.setOnClickListener {
                dialog.dismiss()
                if (timer != null) {
                    Toast.makeText(this, "Timer Already Running!!", Toast.LENGTH_SHORT).show()
                } else {
                    playVideo()
                    var sleepTime = 15
                    val customeDialogS =
                        LayoutInflater.from(this)
                            .inflate(R.layout.speed_dialog, binding.root, false)
                    val bindingS = SpeedDialogBinding.bind(customeDialogS)
                    val dialogS = MaterialAlertDialogBuilder(this).setView(customeDialogS)
                        .setCancelable(false)
                        .setPositiveButton("OK") { self, _ ->
                            timer = Timer()
                            val task = object : TimerTask() {
                                override fun run() {
                                    moveTaskToBack(true)
                                    exitProcess(1)
                                }
                            }
                            timer!!.schedule(task, sleepTime * 60 * 1000.toLong())
                            self.dismiss()
                            playVideo()
                        }
                        .setBackground(ColorDrawable(0x803700B3.toInt()))
                        .create()
                    dialogS.show()
                    bindingS.speedText.text = "$sleepTime Min"
                    bindingS.minusBtn.setOnClickListener {
                        if (sleepTime > 15) sleepTime -= 15
                        bindingS.speedText.text = "$sleepTime Min"
                    }
                    bindingS.plusBtn.setOnClickListener {
                        if (sleepTime < 120) sleepTime += 15
                        bindingS.speedText.text = "$sleepTime Min"
                    }
                }
            }
            bindingMF.pipModeBtn.setOnClickListener {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        android.os.Process.myUid(),
                        packageName
                    ) == AppOpsManager.MODE_ALLOWED
                } else {
                    false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (status) {
                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        binding.playerView.hideController()
                        playVideo()
                        PipStatus = 0
                    } else {
                        val i = Intent(
                            "android.settings.PICTURE_IN_PICTURE_SETTING",
                            Uri.parse("packege : $packageName")
                        )
                        startActivity(i)
                    }
                } else {
                    Toast.makeText(this, "Feature not Supported!!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    playVideo()

                }
            }
        }
    }

    fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.play_btn_white)
        player.play()
    }

    fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.pause_btn_white)
        player.pause()
    }

    fun createplyer() {
        try {
            player.release()
        } catch (e: Exception) {
        }
        speed = 1.0f
        trackSelector = DefaultTrackSelector(this)
        binding.videoTital.text = playerList[position].title
        binding.videoTital.isSelected = true
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        player.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    nextPrevVideo()
                }
            }
        })
        playFullScreen(enable = isFullscreen)
        setVisibility()
        loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
        loudnessEnhancer.enabled = true

    }

    fun setVisibility() {
        runnable = Runnable {
            if (binding.playerView.isControllerVisible) {
                changVisibilty(View.VISIBLE)
            } else {
                changVisibilty(View.INVISIBLE)
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 100)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
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

    fun changVisibilty(visibility: Int) {
        binding.topController.visibility = visibility
        binding.bottomController.visibility = visibility
        binding.playPauseBtn.visibility = visibility

        if (isLocked) binding.lockBtn.visibility = View.VISIBLE
        else binding.lockBtn.visibility = visibility

//        skip video
        if (moreTime == 2) {
            binding.rewindBtn.visibility = View.GONE
            binding.forwedBtn.visibility = View.GONE
        } else {
            ++moreTime
//            for lockscreen--hiding double tap
//            binding.rewideFL.visibility = visibility
//            binding.forwedFl.visibility = visibility
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun vibrat() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(150)
        }
    }

    private fun ChangeSpeed(isIncrement: Boolean) {
        if (isIncrement) {
            if (speed <= 2.9f) {
                speed += 0.10f//speed = speed +0.10f
            }
        } else {
            if (speed > 0.20f) {
                speed -= 0.10f
            }
        }
        player.setPlaybackSpeed(speed)
    }

    @SuppressLint("MissingSuperCall")
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        if (PipStatus != 0) {
            finish()
            val intent = Intent(this, PlayerActivity::class.java)
            when (PipStatus) {
                1 -> intent.putExtra("class", "FolderActivity")
                2 -> intent.putExtra("class", "searchVideo")
                3 -> intent.putExtra("class", "AllVideos")
            }
            ContextCompat.startActivity(this, intent, null)
        }
    }

}
/*

* */