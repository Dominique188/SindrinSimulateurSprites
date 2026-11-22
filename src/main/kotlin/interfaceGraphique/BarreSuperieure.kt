package interfaceGraphique

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem

class BarreSuperieure(
    private val action_dimensions_defaut: () -> Unit
) {

    fun creerMenu(): MenuBar {

        val menu_barre = MenuBar()

        // Menus
        val menu_fichier = Menu("Fichier")
        val menu_edition = Menu("Édition")
        val menu_affichage = Menu("Affichage")

        // Option : dimensions par défaut
        val zoom_100 = MenuItem(
            "Zoom 100 %"
        )

        zoom_100.setOnAction {
            action_dimensions_defaut()
        }

        menu_affichage.items.add(zoom_100)

        // Ajouter les menus à la barre supérieure
        menu_barre.menus.addAll(
            menu_fichier,
            menu_edition,
            menu_affichage
        )

        return menu_barre
    }
}