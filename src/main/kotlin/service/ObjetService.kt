package service

import donnees.ObjetDAO
import modele.Objet
import kotlin.collections.remove

class ObjetService {

    private val objet_dao = ObjetDAO()
    private var prochain_id = 1
    private val objets = mutableListOf<Objet>()

    fun renommerObjet(objet: Objet, nouveau_nom: String) {
        val objet_existant = objets.find { it.id == objet.id }

        if (objet_existant != null) {
            objet_existant.nom = nouveau_nom
            enregistrer()
        }
    }

    private fun enregistrer() {
        objet_dao.enregistrer(prochain_id, objets)
    }

    fun obtenirTout(): List<Objet> {
        return objets
    }

    fun creer(nom: String): Objet {
        val objet = Objet(
            id = prochain_id,
            nom = nom
        )

        objets.add(objet)

        prochain_id++

        enregistrer()

        return objet
    }

    fun supprimerObjet(objet: Objet) {
        objets.remove(objet)
        enregistrer()
    }

    init {
        val donnees = objet_dao.charger()

        prochain_id = donnees.get("prochain_id").asInt

        objets.addAll(objet_dao.obtenirObjets(donnees))
    }

}