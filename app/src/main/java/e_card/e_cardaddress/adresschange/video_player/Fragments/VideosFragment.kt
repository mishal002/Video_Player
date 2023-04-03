package e_card.e_cardaddress.adresschange.video_player.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import e_card.e_cardaddress.adresschange.video_player.Activitys.MainActivity
import e_card.e_cardaddress.adresschange.video_player.Adapters.VideoAdapter
import e_card.e_cardaddress.adresschange.video_player.Data.getAllVideo
import e_card.e_cardaddress.adresschange.video_player.R
import e_card.e_cardaddress.adresschange.video_player.databinding.FragmentVideosBinding

class VideosFragment : Fragment() {

    lateinit var adapter: VideoAdapter
    lateinit var binding: FragmentVideosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.themeList[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        val binding = FragmentVideosBinding.bind(view)
        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        binding.VideoRV.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(requireContext(), MainActivity.videoList)
        binding.VideoRV.adapter = adapter
        binding.totalVideoFA.text = "Total Video:${MainActivity.videoList.size}"


//        refreshLayout
        binding.root.setOnRefreshListener {

            MainActivity.videoList = getAllVideo(requireContext())
            adapter.updateList(MainActivity.videoList)
            binding.totalVideoFA.text = "Total Video:${MainActivity.videoList.size}"

            binding.root.isRefreshing = false

        }

        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view, menu)
        val serchView = menu.findItem(R.id.searchView)?.actionView as SearchView
        serchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    MainActivity.searchList = ArrayList()
                    for (video in MainActivity.videoList) {
                        if (video.title.lowercase().contains(newText.lowercase())) {
                            MainActivity.searchList.add(video)
                        }
                        MainActivity.search = true
                        adapter.updateList(searchList = MainActivity.searchList)
                    }
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//        if (PlayerActivity.position != -1) binding.nowPlayingBtn.visibility = View.VISIBLE
        if (MainActivity.adapterChanged!!) adapter.notifyDataSetChanged()
        MainActivity.adapterChanged = false
    }
}