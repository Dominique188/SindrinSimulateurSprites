package service

import donnees.ObjetDAO
import modele.Objet
import kotlin.collections.remove

class ObjetService {

    private val objet_dao = ObjetDAO()
    private var prochain_id = 1
    private val objets = mutableListOf<Objet>()

    private fun enregistrerObjet() {
        objet_dao.enregistrer(prochain_id, objets)
    }

    fun obtenirObjets(): List<Objet> {
        return objets
    }

    fun creerObjet(nom: String): Objet {
        val objet = Objet(
            id = prochain_id,
            nom = nom
        )

        objets.add(objet)

        prochain_id++

        enregistrerObjet()

        return objet
    }

    fun supprimerObjet(objet: Objet) {
        objets.remove(objet)
        enregistrerObjet()
    }

    init {
        val donnees = objet_dao.charger()

        prochain_id = donnees.get("prochain_id").asInt

        objets.addAll(objet_dao.obtenirObjets(donnees))
    }

}