package interfaceGraphique.simulation

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.Gdx
// Une image chargée en mémoire graphique est représentée par une classe appelée Texture
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ScreenViewport
// Préparer l'écran avant de dessiner les sprites
import com.badlogic.gdx.graphics.GL20
// Gestionnaire d'évènements
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
// Pour fabriquer des formes
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class ApplicationSimulation : ApplicationAdapter() {

    private lateinit var sprite_batch: SpriteBatch
    private lateinit var texture: Texture
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: ScreenViewport
    private lateinit var shape_renderer: ShapeRenderer

    private var deplacement_actif = false
    private var souris_x = 0
    private var souris_y = 0
    private var largeur_animation: Int? = null
    private var hauteur_animation: Int? = null

    private val gestionnaire_souris = object : InputAdapter() {
        override fun scrolled(amountX: Float, amountY: Float) : Boolean {
            val facteur = 1.1f
            // amountY > 0 = défilement vers le bas (zoom arrière)
            val nouveau_zoom = if (amountY < 0) {
                camera.zoom * facteur
            } else if (amountY > 0) {
                camera.zoom / facteur
            } else {
                return false
            }

            definirZoom(nouveau_zoom)

            return true
        }

        override fun touchDown(
            screenX: Int,
            screenY: Int,
            pointer: Int,
            button: Int
        ) : Boolean {
            if (button == Input.Buttons.LEFT) {
                deplacement_actif = true

                souris_x = screenX
                souris_y = screenY

                return true
            }

            return false
        }

        override fun touchDragged(
            screenX: Int,
            screenY: Int,
            pointer: Int
        ) : Boolean {

            if (!deplacement_actif) return false

            val delta_x = screenX - souris_x
            val delta_y = screenY - souris_y

            camera.position.x -= delta_x * camera.zoom
            camera.position.y += delta_y * camera.zoom

            souris_x = screenX
            souris_y = screenY

            camera.update()

            return true
        }

        override fun touchUp(
            screenX: Int,
            screenY: Int,
            pointer: Int,
            button: Int
        ) : Boolean {
            if (button == Input.Buttons.LEFT) {
                deplacement_actif = false
                return true
            }

            return false
        }
    }

    override fun create() {
        camera = OrthographicCamera()
        viewport = ScreenViewport(camera)
        sprite_batch = SpriteBatch()
        texture = Texture("assets/images/11/001.jpeg")
        shape_renderer = ShapeRenderer()
    }

    override fun render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        sprite_batch.projectionMatrix = camera.combined
        Gdx.input.inputProcessor = gestionnaire_souris

        sprite_batch.begin()
        dessiner()
        sprite_batch.end()

        largeur_animation?.let { largeur ->
            hauteur_animation?.let { hauteur ->
                dessinerCadre(largeur, hauteur)
            }
        }
    }

    // Libérer de la mémoire (GPU)
    override fun dispose() {
        sprite_batch.dispose()
        shape_renderer.dispose()
    }

    // Si les dimensions de la zone de visualisation sont modifiées
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)

//        println("Viewport : ${viewport.worldWidth} x ${viewport.worldHeight}")
//        println("Caméra : ${camera.position}")

        camera.position.set(
            viewport.worldWidth / 2f,
            viewport.worldHeight / 2f,
            0f
        )

        camera.update()
    }

    private fun dessiner() {
        val echelle_x = viewport.worldWidth / texture.width
        val echelle_y = viewport.worldHeight / texture.height

        val echelle = minOf(1f, echelle_x, echelle_y)

        val largeur = texture.width * echelle
        val hauteur = texture.height * echelle

        val x = camera.position.x - texture.width / 2f
        val y = camera.position.y - texture.height / 2f

        sprite_batch.draw(texture, x, y)
    }

    fun definirZoom(valeur: Float) {
        // coerceIn() limite automatiquement la valeur entre 0.1f et 5f
        camera.zoom = valeur.coerceIn(0.1f, 5f)
        camera.update()
    }

    fun reinitialiserVue() {
        camera.zoom = 1f

        camera.position.set(
            viewport.worldWidth / 2f,
            viewport.worldHeight / 2f,
            0f
        )

        camera.update()
    }

    private fun dessinerCadre(
        largeur: Int,
        hauteur: Int
    ) {
        // projectionMatrix = camera.combined : indique à ShapeRenderer d'utiliser la même caméra que celle qui affiche les images
        shape_renderer.projectionMatrix = camera.combined

        shape_renderer.begin(ShapeRenderer.ShapeType.Line)

        shape_renderer.setColor(0f, 1f, 0f, 1f)

        shape_renderer.rect(
            camera.position.x - largeur / 2f,
            camera.position.y - hauteur / 2f,
            largeur.toFloat(),
            hauteur.toFloat()
        )

        shape_renderer.end()
    }

    fun definirDimensionsAnimation(largeur: Int?, hauteur: Int?) {
        largeur_animation = largeur
        hauteur_animation = hauteur
    }


}