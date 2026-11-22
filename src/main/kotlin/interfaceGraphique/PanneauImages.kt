package interfaceGraphique

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import java.io.File
import javafx.stage.FileChooser

import modele.Animation
import service.AnimationService

class PanneauImages(
    private val animation_service: AnimationService
) {

    private val zone_image = StackPane()
    private val grille_images = TilePane()
    private val image_agrandie = ImageView()
    private val bouton_retour = Button("Retour aux images")
    private val bouton_supprimer = Button("Supprimer")
    // Collection des images sélectionnées par la checkbox
    private val images_selectionnees = mutableMapOf<CheckBox, String>()
    private var animation_selectionnee: Animation? = null

    fun creerPanneau(): VBox {

        val panneau = VBox(10.0)
        panneau.padding = Insets(10.0)

        // Titre et bouton
        val titre = Label("Images")
        val bouton_ajouter = Button("Ajouter des images")

        val entete = VBox(10.0)
        entete.children.addAll(
            bouton_ajouter,
            titre
        )

        // Grille des images
        grille_images.hgap = 10.0
        grille_images.vgap = 10.0
        grille_images.padding = Insets(10.0)
        grille_images.prefTileWidth = 100.0
        grille_images.prefTileHeight = 100.0
        grille_images.alignment = Pos.TOP_LEFT

        // Image agrandie
        image_agrandie.isPreserveRatio = true
        image_agrandie.isSmooth = true
        image_agrandie.isVisible = false
        image_agrandie.isManaged = false

        // Bouton retour
        bouton_retour.isVisible = false
        bouton_retour.isManaged = false
        // Bouton supprimer
        bouton_supprimer.isVisible = true
        bouton_supprimer.isManaged = true

        // Zone d'affichage
        zone_image.children.addAll(
            grille_images,
            image_agrandie
        )

        val defilement_images = ScrollPane(zone_image)
        defilement_images.isFitToWidth = true
        defilement_images.isFitToHeight = false

        // Action du bouton retour
        bouton_retour.setOnAction {
            image_agrandie.image = null
            image_agrandie.isVisible = false
            image_agrandie.isManaged = false

            grille_images.isVisible = true
            grille_images.isManaged = true

            bouton_retour.isVisible = false
            bouton_retour.isManaged = false

            bouton_supprimer.isVisible = true
            bouton_supprimer.isManaged = true
        }

        bouton_supprimer.setOnAction {
            val images_a_supprimer = images_selectionnees
                .filter { (checkbox, _) -> checkbox.isSelected }
                .values
                .toList()

            // Vérifier qu'au moins une image est sélectionnée
            if (images_a_supprimer.isEmpty()) return@setOnAction

            // Confirmation
            val dialogue = Alert(Alert.AlertType.CONFIRMATION)

            dialogue.title = "Supprimer des images"
            dialogue.headerText = "Confirmation de suppression"
            dialogue.contentText =
                "Voulez-vous vraiment supprimer ${images_a_supprimer.size} image(s) ?"

            dialogue.buttonTypes.setAll(
                ButtonType.YES,
                ButtonType.NO
            )

            val resultat = dialogue.showAndWait()

            if (resultat.isPresent && resultat.get() == ButtonType.YES) {
                // Supprimer les images et sauvegarder les modifications
                animation_selectionnee?.let { animation ->
                    animation_service.supprimerImages(
                        animation,
                        images_a_supprimer
                    )

                    // Actualiser la grille
                    afficherImages(animation)
                }
            }
        }

        // Conteneur vertical
        val conteneur_images = VBox(10.0)
        val boutons = HBox(10.0)
        boutons.alignment = Pos.CENTER_LEFT

        boutons.children.addAll(
            bouton_supprimer,
            bouton_retour
        )

        conteneur_images.children.addAll(
            defilement_images,
            boutons
        )

        VBox.setVgrow(defilement_images, Priority.ALWAYS)

        // Assemblage final
        panneau.children.addAll(
            entete,
            conteneur_images
        )

        VBox.setVgrow(conteneur_images, Priority.ALWAYS)

        bouton_ajouter.setOnAction {

            val animation = animation_selectionnee
                ?: return@setOnAction

            val selecteur = FileChooser()

            selecteur.title = "Sélectionner des images"

            selecteur.extensionFilters.addAll(
                FileChooser.ExtensionFilter(
                    "Images",
                    "*.png",
                    "*.jpg",
                    "*.jpeg"
                )
            )

            // Permettre de sélectionner plusieurs fichiers
            val fichiers = selecteur.showOpenMultipleDialog(
                panneau.scene?.window
            ) ?: return@setOnAction

            // Récupérer les chemins des images
            val chemins = fichiers.map { it.absolutePath }

            // Ajouter les images à l'animation
            animation_service.ajouterImages(
                animation,
                chemins
            )

            // Actualiser la grille
            afficherImages(animation)
        }

        return panneau
    }

    fun afficherImages(animation: Animation?) {

        animation_selectionnee = animation

        grille_images.children.clear()
        images_selectionnees.clear()

        if (animation == null) return

        animation.images.forEach { chemin ->

            val fichier = File(chemin)

            if (!fichier.exists()) return@forEach

            val image = Image(fichier.toURI().toString())

            val apercu = ImageView(image)
            apercu.fitWidth = 100.0
            apercu.fitHeight = 100.0
            apercu.isPreserveRatio = true
            apercu.isSmooth = true

            // Checkbox
            val checkbox = CheckBox()

            images_selectionnees[checkbox] = chemin

            // Conteneur
            val conteneur_image = StackPane(apercu, checkbox)

            conteneur_image.alignment = Pos.CENTER

            StackPane.setAlignment(checkbox, Pos.TOP_RIGHT)
            StackPane.setMargin(checkbox, Insets(5.0))

            conteneur_image.minWidth = 100.0
            conteneur_image.minHeight = 100.0
            conteneur_image.prefWidth = 100.0
            conteneur_image.prefHeight = 100.0

            // Double-clic pour agrandir
            apercu.setOnMouseClicked { evenement ->

                if (evenement.clickCount == 2) {

                    image_agrandie.image = image

                    image_agrandie.fitWidthProperty().bind(zone_image.widthProperty())
                    image_agrandie.isPreserveRatio = true

                    image_agrandie.isVisible = true
                    image_agrandie.isManaged = true

                    grille_images.isVisible = false
                    grille_images.isManaged = false

                    bouton_retour.isVisible = true
                    bouton_retour.isManaged = true

                    bouton_supprimer.isVisible = false
                    bouton_supprimer.isManaged = false
                }
            }

            // Ajouter uniquement le conteneur
            grille_images.children.add(conteneur_image)
        }

        bouton_retour.setOnAction {

            image_agrandie.fitWidthProperty().unbind()
            image_agrandie.image = null

            image_agrandie.isVisible = false
            image_agrandie.isManaged = false

            grille_images.isVisible = true
            grille_images.isManaged = true

            bouton_retour.isVisible = false
            bouton_retour.isManaged = false
        }
    }
}