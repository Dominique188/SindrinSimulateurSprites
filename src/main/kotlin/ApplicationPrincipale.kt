import donnees.ParametresDAO
import modele.ParametresAffichage
import service.AnimationService
import service.ObjetService
import interfaceGraphique.BarreSuperieure
import interfaceGraphique.PanneauObjets
import interfaceGraphique.PanneauAnimations
import interfaceGraphique.PanneauImages
import interfaceGraphique.simulation.ApplicationSimulation

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

import com.huskerdev.openglfx.libgdx.LibGDXCanvas

class ApplicationPrincipale : Application() {

    override fun start(stage: Stage) {

        // Conteneur principal
        val contenu = BorderPane()

        // Services
        val animation_service = AnimationService()
        val objet_service = ObjetService()
        val application_simulation = ApplicationSimulation()
        val canvas_simulation = LibGDXCanvas(application_simulation)

        val panneau_images_vue = PanneauImages(animation_service)
        val panneau_images = panneau_images_vue.creerPanneau()

        // Onglets
        val onglets = TabPane()
        val onglet_images = Tab("Images", panneau_images)

        val panneau_animations_vue = PanneauAnimations(animation_service) { animation ->
            onglet_images.isDisable = animation == null

            panneau_images_vue.afficherImages(animation)

            if (animation != null) {
                application_simulation.definirDimensionsAnimation(
                    animation.largeur,
                    animation.hauteur
                )
            } else {
                application_simulation.definirDimensionsAnimation(null, null)
            }
        }

        val panneau_animations = panneau_animations_vue.creerPanneau()

        val onglet_objets = Tab("Objets")
        val onglet_animations = Tab("Animations", panneau_animations)

        // Désactiver les onglets tant qu'aucun objet n'est sélectionné
        onglet_animations.isDisable = true
        onglet_images.isDisable = true

        // Empêcher la fermeture des onglets
        onglet_objets.isClosable = false
        onglet_animations.isClosable = false
        onglet_images.isClosable = false

        // Panneau des objets avec gestion de la sélection (onglets disabled ou non)
        val panneau_objets_vue = PanneauObjets(objet_service) { objet ->

            onglet_animations.isDisable = objet == null
            onglet_images.isDisable = true

            panneau_animations_vue.afficherAnimations(objet?.id)
        }

        val panneau_objets = panneau_objets_vue.creerPanneau()

        onglet_objets.content = panneau_objets

        // Ajouter les onglets
        onglets.tabs.addAll(
            onglet_objets,
            onglet_animations,
            onglet_images
        )

        contenu.center = onglets

        // Zone de visualisation
        val zone_visualisation = StackPane()

        zone_visualisation.children.add(canvas_simulation)

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
            onglets,
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
                application_simulation.definirZoom(1f)
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