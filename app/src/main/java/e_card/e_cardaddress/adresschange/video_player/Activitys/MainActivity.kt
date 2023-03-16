package e_card.e_cardaddress.adresschange.video_player.Activitys

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import e_card.e_cardaddress.adresschange.video_player.Data.Folder
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.Fragments.FoldersFragment
import e_card.e_cardaddress.adresschange.video_player.Fragments.StatusFragment
import e_card.e_cardaddress.adresschange.video_player.Fragments.VideosFragment
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.coolPinkNav)
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
        }

//
        binding.bottomNav.setOnItemSelectedListener {
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
                R.id.feedbackNav -> Toast.makeText(
                    this@MainActivity,
                    "Feedback",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.themesNav -> Toast.makeText(this@MainActivity, "themes", Toast.LENGTH_SHORT)
                    .show()
                R.id.sort_oder -> Toast.makeText(this@MainActivity, "Shor_oder", Toast.LENGTH_SHORT)
                    .show()
                R.id.aboutNav -> Toast.makeText(this@MainActivity, "about", Toast.LENGTH_SHORT)
                    .show()
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
    fun requestRuntimePermission(): Boolean {
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
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    //
    @SuppressLint("Range")
    fun getAllVideo(): ArrayList<Video> {

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
            MediaStore.Video.Media.DATE_ADDED
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
                            pahth = pathC,
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
}