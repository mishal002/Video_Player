package e_card.e_cardaddress.adresschange.video_player.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import e_card.e_cardaddress.adresschange.video_player.Activitys.FolderActivity
import e_card.e_cardaddress.adresschange.video_player.Activitys.MainActivity
import e_card.e_cardaddress.adresschange.video_player.Adapters.VideoAdapter
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.FragmentVideosBinding

class VideosFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        val binding = FragmentVideosBinding.bind(view)


        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = LinearLayoutManager(requireContext())
        binding.VideoRV.adapter = VideoAdapter(requireContext(), MainActivity.videoList)
        binding.totalVideoFA.text = "Total Video:${MainActivity.videoList.size}"

        return view
    }
}