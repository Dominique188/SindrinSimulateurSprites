package interfaceGraphique

import interfaceGraphique.dialogue.ObjetDialogue
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

import modele.Objet
import service.ObjetService

class PanneauObjets (
    private val objet_service: ObjetService
) {
    private val dialogues = ObjetDialogue()

    fun creerPanneau(): VBox {
        val panneau = VBox(10.0)
        panneau.padding = Insets(10.0)

        val titre = Label("Objets")

        val liste_objets = ListView<Objet>()

        val bouton_ajouter = Button("Ajouter")
        val bouton_supprimer = Button("x")
        val bouton_creer = Button("Créer un objet")

        val boutons = HBox(10.0)
        boutons.children.addAll(
            titre,
            liste_objets,
            bouton_creer
        )

        VBox.setVgrow(boutons, Priority.ALWAYS)

        bouton_ajouter.setOnAction {
            val nom = dialogues.creerObjet()

            if (nom != null) {
                objet_service.creerObjet(nom)
                liste_objets.items.setAll(objet_service.obtenirObjets())
            }
        }

        bouton_supprimer.setOnAction {
            val objet = liste_objets.selectionModel.selectedItem

            if (objet != null) {
                val confirmation = dialogues.confirmerSuppression(objet)

                if (confirmation) {
                    objet_service.supprimerObjet(objet)
                    liste_objets.items.setAll(objet_service.obtenirObjets())
                }
            }
        }

        bouton_creer.setOnAction {
            val nom = dialogues.creerObjet()

            if (nom != null) {
                objet_service.creerObjet(nom)
                liste_objets.items.setAll(objet_service.obtenirObjets())
            }
        }

        liste_objets.setCellFactory {
            object : ListCell<Objet>() {
                override fun updateItem(objet: Objet?, empty: Boolean) {
                    super.updateItem(objet, empty)

                    text = if (empty || objet == null) {
                        null
                    } else {
                        objet.nom
                    }
                }
            }
        }

        panneau.children.addAll(
            boutons,
            titre,
            liste_objets
        )

        VBox.setVgrow(liste_objets, Priority.ALWAYS)

        liste_objets.items.setAll(objet_service.obtenirObjets())

        return panneau
    }
}