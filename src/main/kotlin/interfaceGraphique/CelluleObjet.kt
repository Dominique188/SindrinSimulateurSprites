package interfaceGraphique

import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import modele.Objet
import service.ObjetService

class CelluleObjet(
    private val objet_service: ObjetService
) : ListCell<Objet>() {

    private val nom_objet = Label()

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
            nom_objet,
            espace,
            image_crayon,
            supprimer
        )

        // Renommer
        image_crayon.setOnMouseClicked {
            champ_nom.text = nom_objet.text

            ligne.children.setAll(
                champ_nom,
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
                item?.let { objet ->
                    objet_service.renommerObjet(objet, nouveau_nom)
                    nom_objet.text = nouveau_nom
                }
            }

            ligne.children.setAll(
                nom_objet,
                espace,
                image_crayon,
                supprimer
            )
        }

        // Supprimer
        supprimer.setOnAction {
            val dialogue = Alert(Alert.AlertType.CONFIRMATION)

            dialogue.title = "Supprimer un objet"
            dialogue.headerText = "Confirmation"
            dialogue.contentText =
                "Voulez-vous vraiment supprimer cet objet ?"

            dialogue.buttonTypes.setAll(
                ButtonType.YES,
                ButtonType.NO
            )

            val resultat = dialogue.showAndWait()

            if (resultat.isPresent && resultat.get() == ButtonType.YES) {
                item?.let { objet ->
                    objet_service.supprimerObjet(objet)
                    listView.items.remove(objet)
                }
            }
        }
    }

    override fun updateItem(item: Objet?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
        } else {
            nom_objet.text = item.nom
            graphic = ligne
        }
    }
}