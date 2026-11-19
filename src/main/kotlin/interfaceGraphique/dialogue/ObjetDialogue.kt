package interfaceGraphique.dialogue

import javafx.scene.control.TextInputDialog
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import modele.Objet

class ObjetDialogue {

    fun creerObjet(): String? {
        val dialogue = TextInputDialog()

        dialogue.title = "Nouvel objet"
        dialogue.headerText = "Créer un objet"
        dialogue.contentText = "Nom de l'objet :"

        var resultat = dialogue.showAndWait()

        if (resultat.isEmpty) {
            return null
        }

        val nom = resultat.get().trim()

        if (nom.isBlank()) {
            return null
        }

        return nom
    }

    fun confirmerSuppression(objet: Objet): Boolean {
        val dialogue = Alert(Alert.AlertType.CONFIRMATION)

        dialogue.title = "Supprimer un objet"
        dialogue.headerText = "Suppression de ${objet.nom}"
        dialogue.contentText = "Voulez-vous vraiment supprimer cet objet ?"

        val resultat = dialogue.showAndWait()

        return resultat.isPresent && resultat.get() == ButtonType.OK
    }
}