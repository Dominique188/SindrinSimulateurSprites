package donnees

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path

import modele.ParametresAffichage

class ParametresDAO {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val fichier_config = Path.of("parametres.json")

    fun obtenirParametres(): ParametresAffichage {

        if (!Files.exists(fichier_config)) {
            return ParametresAffichage()
        }

        return try {
            val json = Files.readString(fichier_config)

            gson.fromJson(
                json,
                ParametresAffichage::class.java
            )
        } catch (e: Exception) {
            ParametresAffichage()
        }
    }
    // Enregistrer les paramètres
    fun enregistrerParametres(parametres: ParametresAffichage) {

        val json = gson.toJson(parametres)
        Files.writeString(fichier_config, json)
    }
}