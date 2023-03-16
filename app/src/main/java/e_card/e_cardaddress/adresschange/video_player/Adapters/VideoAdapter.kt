package e_card.e_cardaddress.adresschange.video_player.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import e_card.e_cardaddress.adresschange.video_player.Activitys.PlayerActivity
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.VideoViewBinding

class VideoAdapter(
    val context: Context,
    val videoList: ArrayList<Video>,
    val isFolder: Boolean = false) :
    RecyclerView.Adapter<VideoAdapter.Viewdata>() {

    class Viewdata(binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val tital = binding.videoName
        val folder = binding.folderName
        val duration = binding.duration
        val image = binding.videoImg
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewdata {
        return Viewdata(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: Viewdata, position: Int) {
        holder.tital.text = videoList[position].title
        holder.folder.text = videoList[position].folderName
        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration / 1000)

        Glide
            .with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {

            when {
                isFolder -> {
                    sendIntent(pos = position, ref = "FolderActivity")
                }
                else -> {
                    sendIntent(pos = position, ref = "AllVideo")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    fun sendIntent(pos: Int, ref: String) {
        PlayerActivity.position = pos
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

}