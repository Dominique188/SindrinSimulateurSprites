package modele

import java.util.UUID

data class Animation(
    val id: Int,
    var nom: String,
    var largeur: Int,
    var hauteur: Int,
    var fps: Int = 12,
    val images: MutableList<String> = mutableListOf(),
    val id_objet: Int
)