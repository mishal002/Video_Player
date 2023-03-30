package e_card.e_cardaddress.adresschange.video_player.Data

import android.net.Uri

data class Video(
    var id: String,
    var title: String,
    var duration: Long = 0,
    var folderName: String,
    var size: String,
    var path: String,
    var artUri: Uri
)

data class Folder(val id: String, val FolderName: String)
