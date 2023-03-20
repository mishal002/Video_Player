package e_card.e_cardaddress.adresschange.video_player.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import e_card.e_cardaddress.adresschange.video_player.Activitys.MainActivity
import e_card.e_cardaddress.adresschange.video_player.Activitys.PlayerActivity
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.RenameFieldBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.VideoMoreFeaturesBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.VideoViewBinding

class VideoAdapter(
    val context: Context,
    var videoList: ArrayList<Video>,
    val isFolder: Boolean = false
) :
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
                    PlayerActivity.PipStatus = 1
                    sendIntent(pos = position, ref = "FolderActivity")
                }
                MainActivity.search -> {
                    PlayerActivity.PipStatus = 2
                    sendIntent(pos = position, ref = "SearchVideo")
                }
                else -> {
                    PlayerActivity.PipStatus = 2
                    sendIntent(pos = position, ref = "AllVideo")
                }
            }
        }
        holder.root.setOnLongClickListener {
            val customeDialog = LayoutInflater.from(context)
                .inflate(R.layout.video_more_features, holder.root, false)
            val bindingMF = VideoMoreFeaturesBinding.bind(customeDialog)
            val dialog = MaterialAlertDialogBuilder(context).setView(customeDialog)
                .create()
            dialog.show()

            bindingMF.renameBtn.setOnClickListener {
                dialog.dismiss()
                val customeDialogRF =
                    LayoutInflater.from(context).inflate(R.layout.rename_field, holder.root, false)
                val bindingRF = RenameFieldBinding.bind(customeDialogRF)
                val dialogRF = MaterialAlertDialogBuilder(context).setView(customeDialogRF)
                    .setCancelable(false)
                    .setPositiveButton("Rename") { self, _ ->
                        self.dismiss()
                    }.setNegativeButton("Cancel") { self, _ ->
                        self.dismiss()
                    }
                    .create()
                dialogRF.show()
                bindingRF.renameField.text = SpannableStringBuilder(videoList[position].title)
                dialogRF.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setBackgroundColor(
                        MaterialColors.getColor(context, R.attr.themeColor, Color.GREEN)
                    )
                dialogRF.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setBackgroundColor(
                        MaterialColors.getColor(context, R.attr.themeColor, Color.RED)
                    )
            }
            return@setOnLongClickListener true
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

    fun updateList(searchList: ArrayList<Video>) {
        videoList = ArrayList()
        videoList.addAll(searchList)
        notifyDataSetChanged()
    }

}