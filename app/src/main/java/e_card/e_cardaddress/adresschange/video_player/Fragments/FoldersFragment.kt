package e_card.e_cardaddress.adresschange.video_player.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import e_card.e_cardaddress.adresschange.video_player.Activitys.MainActivity
import e_card.e_cardaddress.adresschange.video_player.Adapters.FolderAdapter
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.FragmentFoldersBinding


class FoldersFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.themeList[MainActivity.themeIndex],true)
        val view = inflater.inflate(R.layout.fragment_folders, container, false)
        val binding = FragmentFoldersBinding.bind(view)


        binding.FolderRV.setHasFixedSize(true)
        binding.FolderRV.setItemViewCacheSize(11)
        binding.FolderRV.layoutManager = LinearLayoutManager(requireContext())
        binding.FolderRV.adapter = FolderAdapter(requireContext(), MainActivity.folderList)
        binding.totalVideoFA.text = "Total Video:${MainActivity.folderList.size}"

        return view
    }
}