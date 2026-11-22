package service

import donnees.AnimationDAO
import modele.Animation

import java.nio.file.Files
import java.nio.file.Path

class AnimationService {

    private val animation_dao = AnimationDAO()
    private var prochain_id = 1
    private val animations = mutableListOf<Animation>()

    fun creerAnimation(
        id_objet: Int,
        nom: String,
        largeur: Int,
        hauteur: Int,
        fps: Int,
        images_selectionnees: List<String>
    ): Animation {

        val animation = Animation(
            id = prochain_id,
            nom = nom,
            largeur = largeur,
            hauteur = hauteur,
            fps = fps,
            id_objet = id_objet
        )

        val dossier_images = Path.of("assets/images", animation.id.toString())
        Files.createDirectories(dossier_images)
        println("Dossier créé : ${dossier_images.toAbsolutePath()}")

        images_selectionnees.forEachIndexed { index, chemin ->
            val fichier_source = Path.of(chemin)
            val extension = fichier_source.fileName.toString()
                .substringAfterLast(".", "")

            val nom_fichier = "%03d.%s".format(index + 1, extension)
            val destination = dossier_images.resolve(nom_fichier)
            Files.copy(fichier_source, destination)
            animation.images.add(destination.toString())
        }

        animations.add(animation)
        prochain_id++
        animation_dao.enregistrer(prochain_id, animations)

        return animation
    }

    fun obtenirAnimations(): List<Animation> {
        return animations
    }

    fun supprimerAnimation(animation: Animation) {

        val dossier_images = Path.of("assets/images", animation.id.toString())

        if (Files.exists(dossier_images)) {
            // Parcourt le dossier et tous ses sous-dossiers
            Files.walk(dossier_images).use { chemins ->
                // Inverse l'ordre pour supprimer les fichiers avant les dossiers
                chemins.sorted(Comparator.reverseOrder())
                    .forEach { Files.delete(it) }
            }
        }
        animations.remove(animation)
        animation_dao.enregistrer(prochain_id, animations)
    }

    fun supprimerImages(animation: Animation, chemins: List<String>) {
        chemins.forEach { chemin ->
            val fichier = Path.of(chemin)

            // Supprimer le fichier s'il existe
            Files.deleteIfExists(fichier)

            // Retirer le chemin de l'animation
            animation.images.remove(chemin)
        }

        // Enregistrer les modifications dans animations.json
        animation_dao.enregistrer(prochain_id, animations)
    }

    fun ajouterImages(
        animation: Animation,
        images_selectionnees: List<String>
    ) {

        val dossier_images = Path.of(
            "assets/images",
            animation.id.toString()
        )

        Files.createDirectories(dossier_images)

        var prochain_index = animation.images.size + 1

        images_selectionnees.forEach { chemin ->

            val fichier_source = Path.of(chemin)

            val extension = fichier_source.fileName.toString()
                .substringAfterLast(".", "")

            val nom_fichier = "%03d.%s".format(
                prochain_index,
                extension
            )

            val destination = dossier_images.resolve(nom_fichier)

            Files.copy(fichier_source, destination)

            animation.images.add(destination.toString())

            prochain_index++
        }

        animation_dao.enregistrer(prochain_id, animations)
    }

    init {

        val donnees = animation_dao.charger()

        prochain_id = donnees.get("prochain_id").asInt

        animations.addAll(
            animation_dao.obtenirAnimations(donnees)
        )
    }
}