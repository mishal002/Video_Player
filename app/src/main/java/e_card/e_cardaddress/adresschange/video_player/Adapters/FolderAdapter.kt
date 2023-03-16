package e_card.e_cardaddress.adresschange.video_player.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import e_card.e_cardaddress.adresschange.video_player.Activitys.FolderActivity
import e_card.e_cardaddress.adresschange.video_player.Data.Folder
import e_card.e_cardaddress.adresschange.video_player.databinding.FolderViewBinding

class FolderAdapter(val context: Context, val folderList: ArrayList<Folder>) :
    RecyclerView.Adapter<FolderAdapter.Viewdata>() {

    class Viewdata(binding: FolderViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.folderNameFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewdata {
        return Viewdata(
            FolderViewBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Viewdata, position: Int) {
        holder.folderName.text = folderList[position].FolderName
        holder.root.setOnClickListener {
            val i = Intent(context, FolderActivity::class.java)
            i.putExtra("position", position)
            ContextCompat.startActivity(context, i, null)
        }
    }

    override fun getItemCount(): Int {
        return folderList.size
    }
}