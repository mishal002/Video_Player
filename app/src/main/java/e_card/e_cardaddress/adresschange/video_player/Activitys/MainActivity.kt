package e_card.e_cardaddress.adresschange.video_player.Activitys

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import e_card.e_cardaddress.adresschange.video_player.Data.Folder
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.Fragments.FoldersFragment
import e_card.e_cardaddress.adresschange.video_player.Fragments.StatusFragment
import e_card.e_cardaddress.adresschange.video_player.Fragments.VideosFragment
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.ActivityMainBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.ThemeViewBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    var runnable: Runnable? = null
    private var sortList = arrayOf(
//        MediaStore.Video.Media.DATE_ADDED + "DESC",
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.TITLE + "DESC",
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE + "DESC",
        MediaStore.Video.Media.SIZE,
    )

    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search: Boolean = false
        private var sortValue: Int = 0
        var themeIndex: Int = 0
        var adapterChanged: Boolean? = false
        val themeList = arrayOf(
            R.style.coolPinkNav,
            R.style.coolBlueNav,
            R.style.coolPurpalNav,
            R.style.coolGreenNav,
            R.style.coolRedNav,
            R.style.coolBlackNav
        )
        var datachange: Boolean = false
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val editor = getSharedPreferences("Theme", MODE_PRIVATE)
        themeIndex = editor.getInt("themeIndex", 0)

        setTheme(themeList[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
// nav drawer

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (requestRuntimePermission()) {
            folderList = ArrayList()
            videoList = getAllVideo()
            setFagments(VideosFragment())


//            runnable = Runnable {
//                if (datachange) {
//                    videoList = getAllVideo()
//                    datachange = false
//                    adapterChanged = true
//                }
//                Handler(Looper.getMainLooper()).postDelayed(runnable!!, 100)
//            }
//            Handler(Looper.getMainLooper()).postDelayed(runnable!!, 0)
        }

//
        binding.bottomNav.setOnItemSelectedListener {
            vibrat()
            when (it.itemId) {
                R.id.videoView -> setFagments(VideosFragment())
                R.id.foldersView -> setFagments(FoldersFragment())
                R.id.statusView -> setFagments(StatusFragment())
            }
            return@setOnItemSelectedListener true
        }
//
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.themesNav -> {
                    val customeDialog =
                        LayoutInflater.from(this).inflate(R.layout.theme_view, binding.root, false)
                    val bindingTV = ThemeViewBinding.bind(customeDialog)
                    val dialog = MaterialAlertDialogBuilder(this).setView(customeDialog)
                        .setTitle("Select Theme")
                        .create()
                    dialog.show()

                    when (themeIndex) {
                        0 -> bindingTV.themePink.setBackgroundColor(Color.MAGENTA)
                        1 -> bindingTV.themeBlue.setBackgroundColor(Color.BLUE)
                        2 -> bindingTV.themePurpul.setBackgroundColor(Color.CYAN)
                        3 -> bindingTV.themeGreen.setBackgroundColor(Color.GREEN)
                        4 -> bindingTV.themeRed.setBackgroundColor(Color.RED)
                        5 -> bindingTV.themeBlack.setBackgroundColor(Color.BLACK)
                    }
                    bindingTV.themePink.setOnClickListener {
                        saveTheme(0)
                    }
                    bindingTV.themeBlue.setOnClickListener {
                        saveTheme(1)

                    }
                    bindingTV.themePurpul.setOnClickListener {
                        saveTheme(2)

                    }
                    bindingTV.themeGreen.setOnClickListener {
                        saveTheme(3)

                    }
                    bindingTV.themeRed.setOnClickListener {
                        saveTheme(4)

                    }
                    bindingTV.themeBlack.setOnClickListener {
                        saveTheme(5)

                    }
                }
                R.id.sort_oder -> {
                    val menuItem = arrayOf(
                        "Latest",
                        "Oldest",
                        "Name(A To Z)",
                        "Name(Z to A)",
                        "File Size(Smallest)",
                        "File Size(Largest)"
                    )
                    var value = sortValue
                    val dialog = MaterialAlertDialogBuilder(this)
                        .setTitle("Sort By")
                        .setPositiveButton("OK") { _, _ ->
                            val sortEditor = getSharedPreferences("Sorting", MODE_PRIVATE).edit()
                            sortEditor.putInt("sortValue", value)
                            sortEditor.apply()

                            finish()
                            startActivity(intent)
                        }
                        .setSingleChoiceItems(menuItem, sortValue) { _, pos ->
                            value = pos
                        }
                        .create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED)
                }
                R.id.aboutNav -> startActivity(Intent(this, AboutActivity::class.java))

                R.id.exitNav -> finish()
            }
            return@setNavigationItemSelectedListener true
        }
//        setFagments()
    }

    //set Fragment
    private fun setFagments(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    //    for permission
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
            return false
        }
        return true
    }

    //    for permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission allowd", Toast.LENGTH_SHORT).show()
                folderList = ArrayList()
                videoList = getAllVideo()
                setFagments(VideosFragment())
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val gradientList = arrayOf(
            R.drawable.pink_gradient,
            R.drawable.blue_gradient,
            R.drawable.purpul_gradient,
            R.drawable.green_gradient,
            R.drawable.red_gradient,
            R.drawable.black_gradient
        )

        findViewById<LinearLayout>(R.id.gradientLayout).setBackgroundResource(gradientList[themeIndex])

        vibrat()
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    //
    @SuppressLint("Range")
    fun getAllVideo(): ArrayList<Video> {

        val sortEditor = getSharedPreferences("Sorting", MODE_PRIVATE)
        sortValue = sortEditor.getInt("sortValue", 0)
        val tempList = ArrayList<Video>()
        val tempFolderList = ArrayList<String>()

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            sortList[sortValue]
        )

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folerIDC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val duration =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                            .toLong()
                    try {
                        val file = File(pathC)
                        val artUri = Uri.fromFile(file)
                        val video = Video(
                            id = idC,
                            title = titleC,
                            duration = duration,
                            folderName = folderC,
                            size = sizeC,
                            path = pathC,
                            artUri = artUri
                        )
                        if (file.exists()) tempList.add(video)
//----------------------------------------------------------------------------------------------------
                        if (!tempFolderList.contains(folderC)) {
                            tempFolderList.add(folderC)
                            folderList.add(Folder(id = folerIDC, FolderName = folderC))
                        }
                    } catch (e: Exception) {
                    }
                } while (cursor.moveToNext())
                cursor?.close()
            }
        }
        return tempList
    }

    private fun vibrat() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }


    private fun saveTheme(index: Int) {
        val editior = getSharedPreferences("Theme", MODE_PRIVATE).edit()
        editior.putInt("themeIndex", index)
        editior.apply()

        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable = null
    }
}