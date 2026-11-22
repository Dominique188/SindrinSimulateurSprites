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
import javafx.stage.FileChooser

import modele.Animation
import service.AnimationService
import kotlin.Int

class PanneauAnimations(
    private val animation_service: AnimationService,
    private val action_selectionner_animation: (Animation?) -> Unit
) {

    private var id_objet: Int? = null
    private val liste_animations = ListView<Animation>()

    fun afficherAnimations(id_objet: Int?) {
        this.id_objet = id_objet

        if (id_objet == null) {
            liste_animations.items.clear()
            action_selectionner_animation(null)
            return
        }

        liste_animations.selectionModel.clearSelection()

        liste_animations.items.setAll(
            animation_service.obtenirAnimations().filter {
                it.id_objet == id_objet
            }
        )
    }

    fun creerPanneau(): VBox {
        val panneau = VBox(10.0)
        panneau.padding = Insets(10.0)

        val titre = Label("Animations")

        val bouton_ajouter = Button("Ajouter une animation")

        // Liste des animations
        liste_animations.setCellFactory {
            CelluleAnimation(animation_service)
        }

        // Entête
        val entete = VBox(10.0)
        entete.children.addAll(
            bouton_ajouter,
            titre
        )

        // Défilement des animations
        val defilement_animations = ScrollPane(liste_animations)
        defilement_animations.isFitToWidth = true
        defilement_animations.isFitToHeight = true

        // Conteneur
        val conteneur = HBox(10.0)
        conteneur.children.addAll(
            entete,
            defilement_animations
        )

        // Dialogue de création
        val dialogue = Dialog<Animation>()

        dialogue.title = "Ajouter"
        dialogue.headerText = "Créer une nouvelle animation"

        val champ_nom = TextField()
        champ_nom.promptText = "Nom de l'animation"
        val champ_largeur = TextField()
        champ_largeur.promptText = "Largeur"
        val champ_hauteur = TextField()
        champ_hauteur.promptText = "hauteur"
        val champ_fps = TextField()
        champ_fps.promptText = "FPS"

        val contenu = VBox()
        contenu.spacing = 10.0

        val bouton_televerser = Button("Ajouter des images")

        val images_selectionnees = mutableListOf<String>()

        contenu.children.addAll(
            champ_nom,
            champ_largeur,
            champ_hauteur,
            champ_fps,
            bouton_televerser
        )

        dialogue.dialogPane.content = contenu

        dialogue.dialogPane.buttonTypes.addAll(
            ButtonType.OK,
            ButtonType.CANCEL
        )

        dialogue.setResultConverter { bouton ->
            if (bouton == ButtonType.OK) {

                val nom = champ_nom.text.trim()
                val largeur = champ_largeur.text.toIntOrNull()
                val hauteur = champ_hauteur.text.toIntOrNull()
                val fps = champ_fps.text.toIntOrNull()

                if (
                    nom.isNotBlank() &&
                    fps != null && fps in 1..60 &&
                    largeur != null && largeur > 0 &&
                    hauteur != null && hauteur > 0
                    ) {
                    Animation(
                        id = 0,
                        nom = nom,
                        largeur = largeur,
                        hauteur = hauteur,
                        fps = fps,
                        id_objet = this.id_objet
                            ?: return@setResultConverter null
                    )
                } else {
                    null
                }

            } else {
                null
            }
        }

        bouton_ajouter.setOnAction {
            val objet_id = id_objet ?: return@setOnAction

            // Réinitialiser les champs avant l'ouverture
            champ_nom.clear()
            champ_fps.clear()
            images_selectionnees.clear()

            val resultat = dialogue.showAndWait()

            resultat.ifPresent { animation_saisie ->

                val animation = animation_service.creerAnimation(
                    objet_id,
                    animation_saisie.nom,
                    animation_saisie.largeur,
                    animation_saisie.hauteur,
                    animation_saisie.fps,
                    images_selectionnees
                )

                liste_animations.items.add(animation)
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
                    "*.jpeg"
                )
            )

            val fichiers = selecteur.showOpenMultipleDialog(null)

            if (fichiers != null) {
                images_selectionnees.clear()
                images_selectionnees.addAll(
                    fichiers.map { it.absolutePath }
                )
            }
        }

        panneau.children.addAll(
            entete,
            defilement_animations
        )

        VBox.setVgrow(defilement_animations, Priority.ALWAYS)

        return panneau
    }

    init {
        liste_animations.selectionModel.selectedItemProperty().addListener {
                _, _, animation_selectionnee ->
            action_selectionner_animation(animation_selectionnee)
        }
    }
}

class CelluleAnimation(
    private val animation_service: AnimationService
) : ListCell<Animation>() {

    private val nom_animation = Label()
    private val image_crayon = ImageView(
        Image(javaClass.getResource("/images/crayon.png")!!.toExternalForm())
    )
    private val supprimer = Button("x")
    private val ligne = HBox(10.0)
    private val champ_nom = TextField()

    init {

        image_crayon.fitWidth = 21.0
        image_crayon.isPreserveRatio = true

        ligne.alignment = Pos.CENTER_LEFT

        val espace = Region()
        HBox.setHgrow(espace, Priority.ALWAYS)

        ligne.children.addAll(
            nom_animation,
            espace,
            image_crayon,
            supprimer
        )

        image_crayon.setOnMouseClicked {
            nom_animation.isVisible = false
            champ_nom.isVisible = true

            champ_nom.text = nom_animation.text
            ligne.children.setAll(champ_nom,
                espace,
                image_crayon,
                supprimer
            )

            champ_nom.requestFocus()
            champ_nom.selectAll()
        }

        champ_nom.setOnAction {
            val nouveau_nom = champ_nom.text.trim()

            if (nouveau_nom.isNotBlank()) {
                nom_animation.text = nouveau_nom
            }

            ligne.children.setAll(nom_animation,
                espace,
                image_crayon,
                supprimer
            )

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