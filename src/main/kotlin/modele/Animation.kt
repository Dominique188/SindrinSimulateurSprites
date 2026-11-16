package modele

import java.util.UUID

data class Animation(
    val id: Int,
    var nom: String,
    val images: MutableList<String> = mutableListOf(),
    var fps: Int = 12
)