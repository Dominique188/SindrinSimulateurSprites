package interfaceGraphique

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.Dialog
import javafx.scene.control.ListCell
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.layout.HBox
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.stage.FileChooser

import modele.Animation
import service.AnimationService
import java.io.File
import java.nio.file.Path

class PanneauAnimations(
    private val animation_service: AnimationService
) {

    fun creerPanneau(): VBox {
        val panneau = VBox(10.0)
        panneau.padding = Insets(10.0)

        val titre = Label("Animations")

        val liste_animations = ListView<Animation>()
        liste_animations.items.addAll(
            animation_service.obtenirAnimations()
        )

        liste_animations.setCellFactory {
            CelluleAnimation(animation_service)
        }
        // Grille pour les images de l'animation
        val grille_images = TilePane()

        grille_images.hgap = 10.0
        grille_images.vgap = 10.0
        grille_images.padding = Insets(10.0)
        grille_images.prefTileWidth = 100.0
        grille_images.prefTileHeight = 100.0
        grille_images.alignment = Pos.TOP_LEFT

        val defilement_images = ScrollPane(grille_images)

        defilement_images.isFitToWidth = true
        defilement_images.isFitToHeight = false

        val bouton_retour = Button("Retour aux animations")

        val panneau_images = VBox(10.0)
        panneau_images.children.addAll(
            bouton_retour,
            defilement_images
        )

        VBox.setVgrow(defilement_images, Priority.ALWAYS)

        val zone_contenu = StackPane()
        zone_contenu.children.addAll(
            liste_animations,
            panneau_images
        )

        panneau_images.isVisible = false
        panneau_images.isManaged = false

        liste_animations.isVisible = true
        liste_animations.isManaged = true

        val dialogue = Dialog<Animation>()

        dialogue.title = "Nouvelle animation"
        dialogue.headerText = "Créer une nouvelle animation"
        dialogue.contentText = "Nom de l'animation"

        val champ_nom = TextField()
        champ_nom.promptText = "Nom de l'animation"

        val champ_fps = TextField()

        val contenu = GridPane()

        val bouton_televerser = Button("Ajouter des images")

        val images_selectionnees = mutableListOf<String>()

        contenu.hgap = 10.0
        contenu.vgap = 10.0

        contenu.add(Label("Nom :"), 0, 0)
        contenu.add(champ_nom, 1, 0)

        contenu.add(Label("FPS :"), 0, 1)
        contenu.add(champ_fps, 1, 1)

        contenu.add(bouton_televerser, 1, 2)

        dialogue.dialogPane.content = contenu

        dialogue.dialogPane.buttonTypes.addAll(
            ButtonType.OK,
            ButtonType.CANCEL
        )

        val bouton_ajouter = Button("Ajouter une animation")

        bouton_ajouter.setOnAction {
            val resultat = dialogue.showAndWait()

            resultat.ifPresent { animation_saisie ->

                val animation = animation_service.creerAnimation(
                    animation_saisie.nom,
                    animation_saisie.fps,
                    images_selectionnees
                )

                liste_animations.items.add(animation)
            }
        }

        dialogue.setResultConverter { bouton ->

            if (bouton == ButtonType.OK) {

                val nom = champ_nom.text.trim()
                val fps = champ_fps.text.toIntOrNull()

                if (nom.isNotBlank() && fps != null && fps in 1..60) {
                    Animation(
                        id = 0,
                        nom = nom,
                        fps = fps
                    )
                } else {
                    null
                }

            } else {
                null
            }
        }

        bouton_televerser.setOnAction {

            val selecteur = FileChooser()

            selecteur.title = "Sélectionner des images"

            selecteur.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "Images",
                    "*.jpg",
                    "*.png",
                    "*jpeg"
                )
            )

            val fichiers = selecteur.showOpenMultipleDialog(null)

            if (fichiers != null) {
                images_selectionnees.addAll(
                    fichiers.map { it.absolutePath }
                )
            }
        }

        bouton_retour.setOnAction {
            panneau_images.isVisible = false
            panneau_images.isManaged = false
            liste_animations.isVisible = true
            liste_animations.isManaged = true
        }

        liste_animations.setOnMouseClicked { e ->

            if (e.clickCount == 2) {

                val animation = liste_animations.selectionModel.selectedItem

                if (animation != null) {
                    liste_animations.isVisible = false
                    liste_animations.isManaged = false
                    panneau_images.isVisible = true
                    panneau_images.isManaged = true

                    grille_images.children.clear()

                    animation.images.forEach { chemin ->
                        val image = Image(Path.of(chemin).toUri().toString())

                        val miniature = ImageView(image)

                        miniature.fitWidth = 120.0
                        miniature.fitHeight = 120.0
                        miniature.isPreserveRatio = true
                        miniature.isSmooth = true

                        grille_images.children.add(miniature)
                    }
                }
            }
        }

        panneau.children.addAll(
            titre,
            bouton_ajouter,
            zone_contenu
        )

        VBox.setVgrow(zone_contenu, Priority.ALWAYS)

        return panneau
    }
}

class CelluleAnimation(
    private val animation_service: AnimationService
) : ListCell<Animation>() {

    private val nom_animation = Label()
    private val image_crayon = ImageView(
        Image(javaClass.getResource("/images/crayon.png")!!.toExternalForm())
    )
    private val image_dossier = ImageView(
        Image(javaClass.getResource("/images/dossier-petit.png")!!.toExternalForm())
    )
    private val supprimer = Button("x")
    private val ligne = HBox(10.0)
    private val champ_nom = TextField()

    init {

        image_crayon.fitWidth = 21.0
        image_crayon.isPreserveRatio = true

        image_dossier.fitWidth = 21.0
        image_dossier.isPreserveRatio = true

        ligne.alignment = Pos.CENTER_LEFT

        val espace = Region()
        HBox.setHgrow(espace, Priority.ALWAYS)

        ligne.children.addAll(
            nom_animation,
            espace,
            image_crayon,
            image_dossier,
            supprimer
        )

        image_crayon.setOnMouseClicked {
            nom_animation.isVisible = false
            champ_nom.isVisible = true

            champ_nom.text = nom_animation.text
            ligne.children.setAll(champ_nom, espace, image_crayon, image_dossier, supprimer)

            champ_nom.requestFocus()
            champ_nom.selectAll()
        }

        champ_nom.setOnAction {
            val nouveau_nom = champ_nom.text.trim()

            if (nouveau_nom.isNotBlank()) {
                nom_animation.text = nouveau_nom
            }

            ligne.children.setAll(nom_animation, espace, image_crayon, image_dossier, supprimer)

            nom_animation.isVisible = true
            champ_nom.isVisible = false
        }

        supprimer.setOnAction {

            val dialogue = Alert(Alert.AlertType.CONFIRMATION)

            dialogue.title = "Supprimer une animation"
            dialogue.headerText = "Confirmation"
            dialogue.contentText = "Voulez-vous vraiment supprimer cette animation et son contenu?"

            dialogue.buttonTypes.setAll(
                ButtonType.YES,
                ButtonType.NO
            )

            val resultat = dialogue.showAndWait()

            if (resultat.isPresent && resultat.get() == ButtonType.YES) {
                item?.let { animation ->
                    animation_service.supprimerAnimation(animation)
                    listView.items.remove(animation)
                }
            }
        }
    }

    override fun updateItem(item: Animation?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
        } else {
            nom_animation.text = item.nom
            graphic = ligne
        }
    }

}