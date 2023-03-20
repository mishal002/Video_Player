package e_card.e_cardaddress.adresschange.video_player.Activitys

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import e_card.e_cardaddress.adresschange.video_player.Adapters.VideoAdapter
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.ActivityFolderBinding
import java.io.File

class FolderActivity : AppCompatActivity() {
    lateinit var binding: ActivityFolderBinding

    companion object {
        lateinit var currentFolderVideo: ArrayList<Video>
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setTheme(MainActivity.themeList[MainActivity.themeIndex])
        setContentView(binding.root)

//      drawer
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
        val position = intent.getIntExtra("position", 0)
        supportActionBar?.title = MainActivity.folderList[position].FolderName
//
        currentFolderVideo = getAllVideo(MainActivity.folderList[position].id)
//
        binding.VideoRVFA.setHasFixedSize(true)
        binding.VideoRVFA.setItemViewCacheSize(10)
        binding.VideoRVFA.layoutManager = LinearLayoutManager(this)
        binding.VideoRVFA.adapter = VideoAdapter(this, currentFolderVideo, true)

        binding.totalVideoFA.text = "Total Video:${currentFolderVideo.size}"
    }

    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    //
    @SuppressLint("Range")
    fun getAllVideo(folderId: String): ArrayList<Video> {

        val tempList = ArrayList<Video>()
        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "


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
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf(folderId),
            MediaStore.Video.Media.DATE_ADDED
        )/*+ "DESC"*/
        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
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
                    } catch (e: Exception) {
                    }
                } while (cursor.moveToNext())
                cursor?.close()
            }
        }
        return tempList
    }
}