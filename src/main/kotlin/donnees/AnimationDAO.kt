package donnees

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import modele.Animation
import java.nio.file.Files
import java.nio.file.Path
import com.google.gson.reflect.TypeToken

class AnimationDAO {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    private val fichier_animations = Path.of("animations.json")

    fun charger(): JsonObject {

        if (!Files.exists(fichier_animations) || Files.size(fichier_animations) == 0L) {
            return JsonObject().apply {
                addProperty("prochain_id", 1)
                // gson.toJsonTree(emptyList<Animation>()) => transforme une liste Kotlin en structure JSON
                add("animations", gson.toJsonTree(emptyList<Animation>()))
            }
        }

        val json = Files.readString(fichier_animations)

        return gson.fromJson(json, JsonObject::class.java)

    }

    fun enregistrer(prochain_id: Int, animations: List<Animation>) {

        val json = JsonObject()

        json.addProperty("prochain_id", prochain_id)
        json.add("animations", gson.toJsonTree(animations))

        Files.writeString(
            fichier_animations,
            gson.toJson(json)
        )
    }

    fun obtenirAnimations(json: JsonObject): List<Animation> {

        val type_liste = object : TypeToken<List<Animation>>() {}.type

        return gson.fromJson(
            json.getAsJsonArray("animations"),
            type_liste
        )
    }
}