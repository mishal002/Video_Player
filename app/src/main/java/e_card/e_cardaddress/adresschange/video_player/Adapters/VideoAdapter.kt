package e_card.e_cardaddress.adresschange.video_player.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import e_card.e_cardaddress.adresschange.video_player.Activitys.FolderActivity
import e_card.e_cardaddress.adresschange.video_player.Activitys.MainActivity
import e_card.e_cardaddress.adresschange.video_player.Activitys.PlayerActivity
import e_card.e_cardaddress.adresschange.video_player.Data.Video
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.DetailsViewBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.RenameFieldBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.VideoMoreFeaturesBinding
import e_card.e_cardaddress.adresschange.video_player.databinding.VideoViewBinding
import java.io.File

class VideoAdapter(
    private val context: Context,
    private var videoList: ArrayList<Video>,
    private val isFolder: Boolean = false
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
//
            bindingMF.renameBtn.setOnClickListener {
                requestPermissionR()
                dialog.dismiss()
                val customeDialogRF =
                    LayoutInflater.from(context).inflate(R.layout.rename_field, holder.root, false)
                val bindingRF = RenameFieldBinding.bind(customeDialogRF)
                val dialogRF = MaterialAlertDialogBuilder(context).setView(customeDialogRF)
                    .setCancelable(false)
                    .setPositiveButton("Rename") { self, _ ->
                        val currentFile = File(videoList[position].path)
                        val newName = bindingRF.renameField.text
                        Log.e("TAG", "onBindViewHolder: $newName")
                        if (newName != null && currentFile.exists() && newName.toString()
                                .isEmpty()
                        ) {
                            val newFile = File(
                                currentFile.parentFile,
                                newName.toString() + "." + currentFile.extension
                            )
                            if (currentFile.renameTo(newFile)) {
                                MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(newFile.toString()),
                                    arrayOf("video/*"),
                                    null
                                )
                                when {
                                    MainActivity.search -> {
                                        MainActivity.searchList[position].title =
                                            newName.toString()
                                        MainActivity.searchList[position].path =
                                            newFile.path
                                        MainActivity.searchList[position].artUri =
                                            Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                    }
                                    isFolder -> {
                                        FolderActivity.currentFolderVideo[position].title =
                                            newName.toString()
                                        FolderActivity.currentFolderVideo[position].path =
                                            newFile.path
                                        FolderActivity.currentFolderVideo[position].artUri =
                                            Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                        MainActivity.datachange = true
                                    }
                                    else -> {
                                        MainActivity.videoList[position].title = newName.toString()
                                        MainActivity.videoList[position].path = newFile.path
                                        MainActivity.videoList[position].artUri =
                                            Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Permission Denied!!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
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
            bindingMF.shareBtn.setOnClickListener {
                dialog.dismiss()
                val shareIntent = Intent()
                shareIntent.type = "video/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoList[position].path))
                shareIntent.action = Intent.ACTION_SEND
                ContextCompat.startActivity(
                    context,
                    Intent.createChooser(shareIntent, "Shareing Video File!!"), null
                )
            }
            bindingMF.infoBtn.setOnClickListener {
                dialog.dismiss()
                val customDialogIF =
                    LayoutInflater.from(context).inflate(R.layout.details_view, holder.root, false)
                val bindingIF = DetailsViewBinding.bind(customDialogIF)
                val dialogIF = MaterialAlertDialogBuilder(context).setView(customDialogIF)
                    .setCancelable(false)
                    .setPositiveButton("Ok") { self, _ ->
                        self.dismiss()
                    }
                    .create()
                dialogIF.show()
                val infoText = SpannableStringBuilder().bold { append("DETAILS\n\nName: ") }
                    .append(videoList[position].title)
                    .bold { append("\n\nDuration: ") }
                    .append(DateUtils.formatElapsedTime(videoList[position].duration / 1000))
                    .bold { append("\n\nFile Size: ") }.append(
                        Formatter.formatShortFileSize(
                            context,
                            videoList[position].size.toLong()
                        )
                    )
                    .bold { append("\n\nLocation: ") }.append(videoList[position].path)

                bindingIF.detailTV.text = infoText
                dialogIF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor, Color.RED)
                )
            }
            bindingMF.deleteBtn.setOnClickListener {
                requestPermissionR()
                dialog.dismiss()
                val dialogDF = MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Video?")
                    .setMessage(videoList[position].title)
                    .setCancelable(false)
                    .setPositiveButton("Yes") { self, _ ->
                        val file = File(videoList[position].path)
                        if (file.exists() && file.delete()) {
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(file.path),
                                arrayOf("video/*"),
                                null
                            )
                            when {
                                MainActivity.search -> {
                                    MainActivity.datachange = true
                                    videoList.removeAt(position)
                                    notifyDataSetChanged()
                                }
                                isFolder -> {
                                    FolderActivity.currentFolderVideo.removeAt(position)
                                    notifyDataSetChanged()
                                }
                                else -> {
                                    MainActivity.videoList.removeAt(position)
                                    notifyDataSetChanged()
                                }
                            }
                            MainActivity.videoList.removeAt(position)
                            notifyDataSetChanged()
                        } else {
                            Toast.makeText(context, "Permission Denied!!", Toast.LENGTH_SHORT)
                                .show()
                        }
                        self.dismiss()
                    }
                    .setNegativeButton("No") { self, _ ->
                        self.dismiss()
                    }
                    .create()
                dialogDF.show()
                dialogDF.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setBackgroundColor(
                        MaterialColors.getColor(context, R.attr.themeColor, Color.GREEN)
                    )
                dialogDF.getButton(AlertDialog.BUTTON_NEGATIVE)
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(searchList: ArrayList<Video>) {
        videoList = ArrayList()
        videoList.addAll(searchList)
        notifyDataSetChanged()
    }

    //request by android 11 Permission
    private fun requestPermissionR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                i.addCategory("android.intent.category.DEFAULT")
                i.data = Uri.parse("package:${context.applicationContext.packageName}")
                ContextCompat.startActivity(context, i, null)
            } else {
            }
        }
    }

}