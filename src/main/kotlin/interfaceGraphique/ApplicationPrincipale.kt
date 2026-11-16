package interfaceGraphique

import donnees.ParametresDAO
import modele.ParametresAffichage
import service.AnimationService

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class ApplicationPrincipale : Application() {

    override fun start(stage: Stage) {
        // Conteneur principal
        val contenu = BorderPane()
        // Instance du service
        val animation_service = AnimationService()
        // Panneau des animations
        val panneau_animations = PanneauAnimations(animation_service).creerPanneau()

        val titre_animations = Label("Animations")
        val liste_animations = ListView<String>()

        // Zone de visualisation
        val zone_visualisation = StackPane()

        zone_visualisation.children.add(
            Label("Zone de visualisation")
        )

        // ligne_de_temps
        val ligne_de_temps = VBox(10.0)
        ligne_de_temps.padding = Insets(10.0)

        ligne_de_temps.children.add(
            Label("Ligne de temps")
        )

        // SplitPane horizontal : Animations / Visualisation
        val conteneur_superieur = SplitPane()

        conteneur_superieur.orientation = Orientation.HORIZONTAL

        conteneur_superieur.items.addAll(
            panneau_animations,
            zone_visualisation
        )

        // SplitPane vertical : Partie supérieure / ligne_de_temps
        val disposition = SplitPane()

        disposition.orientation = Orientation.VERTICAL

        disposition.items.addAll(
            conteneur_superieur,
            ligne_de_temps
        )

        // Chargement des paramètres
        val parametres_dao = ParametresDAO()
        var parametres = parametres_dao.obtenirParametres()

        // Positions initiales des séparateurs
        conteneur_superieur.setDividerPositions(
            parametres.position_animations
        )

        disposition.setDividerPositions(
            parametres.position_ligne_de_temps
        )

        // Sauvegarder les changements de dimensions
        conteneur_superieur.dividers[0].positionProperty().addListener {
                _, _, nouvelle_position ->

            parametres = parametres.copy(
                position_animations = nouvelle_position.toDouble()
            )

            parametres_dao.enregistrerParametres(parametres)
        }

        disposition.dividers[0].positionProperty().addListener {
                _, _, nouvelle_position ->

            parametres = parametres.copy(
                position_ligne_de_temps = nouvelle_position.toDouble()
            )

            parametres_dao.enregistrerParametres(parametres)
        }

        // Barre supérieure
        val barre_superieure = BarreSuperieure(
            action_dimensions_defaut = {
                conteneur_superieur.setDividerPositions(0.30)
                disposition.setDividerPositions(0.75)
                parametres = ParametresAffichage()
                parametres_dao.enregistrerParametres(parametres)
            }
        )

        contenu.top = barre_superieure.creerMenu()

        // Ajouter la disposition au conteneur principal
        contenu.center = disposition

        // Création de la scène
        val scene = Scene(contenu, 1200.0, 800.0)

        // Configuration de la fenêtre
        stage.title = "Simulateur"
        stage.scene = scene
        stage.show()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            launch(ApplicationPrincipale::class.java, *args)
        }
    }
}