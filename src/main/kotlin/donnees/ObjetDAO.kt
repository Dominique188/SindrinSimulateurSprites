package donnees

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.nio.file.Files
import java.nio.file.Path

import modele.Objet

class ObjetDAO {

    private val chemin_fichier = Path.of("objets.json")
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun charger(): JsonObject {
        if (!Files.exists(chemin_fichier)) {

            enregistrer(1, emptyList())

            val donnees = JsonObject()
            donnees.addProperty("prochain_id", 1)
            donnees.add("objets", gson.toJsonTree(emptyList<Objet>()))

            return donnees
        }

        return Files.newBufferedReader(chemin_fichier).use {
            gson.fromJson(it, JsonObject::class.java)
        }
    }

    fun enregistrer(prochain_id: Int, objets: List<Objet>) {
        val donnees = JsonObject()

        donnees.addProperty("prochain_id", prochain_id)
        donnees.add("objets", gson.toJsonTree(objets))


        Files.newBufferedWriter(chemin_fichier).use {
            gson.toJson(donnees, it)
        }
    }

    fun obtenirObjets(donnees: JsonObject): List<Objet> {
        val tableau = donnees.getAsJsonArray("objets")

        return gson.fromJson(
               tableau,
            object : com.google.gson.reflect.TypeToken<List<Objet>>() {}.type
        )
    }
}

// Files.exists() vérifie l'existence du fichier.
// JsonObject() crée un objet JSON.
// addProperty() ajoute une propriété simple.
// toJsonTree() convertit une liste Kotlin en structure JSON.
// newBufferedReader() ouvre le fichier pour le lire.
// use ferme automatiquement le lecteur après utilisation.