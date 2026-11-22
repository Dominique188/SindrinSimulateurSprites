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
    private val objet_service: ObjetService,
    private val action_selectionner_objet: (Objet?) -> Unit
) {
    private val dialogues = ObjetDialogue()

    fun creerPanneau(): VBox {
        val panneau = VBox(10.0)
        panneau.padding = Insets(10.0)

        val titre = Label("Objets")
        val bouton_ajouter = Button("Ajouter")
        val bouton_supprimer = Button("x")
        val bouton_creer = Button("Créer un objet")
        val liste_objets = ListView<Objet>()

        // Conteneur du titre et du bouton
        val entete = HBox(10.0)
        entete.children.addAll(
            titre,
            bouton_creer
        )

        val conteneur = HBox(10.0)
        conteneur.children.addAll(
            entete,
            liste_objets
        )

        VBox.setVgrow(liste_objets, Priority.ALWAYS)

        bouton_ajouter.setOnAction {
            val nom = dialogues.creerObjet()

            if (nom != null) {
                objet_service.creer(nom)
                liste_objets.items.setAll(objet_service.obtenirTout())
            }
        }

        bouton_supprimer.setOnAction {
            val objet = liste_objets.selectionModel.selectedItem

            if (objet != null) {
                val confirmation = dialogues.confirmerSuppression(objet)

                if (confirmation) {
                    objet_service.supprimerObjet(objet)
                    liste_objets.items.setAll(objet_service.obtenirTout())
                }
            }
        }

        bouton_creer.setOnAction {
            val nom = dialogues.creerObjet()

            if (nom != null) {
                objet_service.creer(nom)
                liste_objets.items.setAll(objet_service.obtenirTout())
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
            conteneur,
            titre,
            liste_objets
        )

        VBox.setVgrow(liste_objets, Priority.ALWAYS)

        liste_objets.items.setAll(objet_service.obtenirTout())

        liste_objets.setCellFactory {
            CelluleObjet(objet_service)
        }

        liste_objets.selectionModel.selectedItemProperty().addListener {
            _, _, objet_selectionne ->
            action_selectionner_objet(objet_selectionne)
        }

        return panneau
    }
}