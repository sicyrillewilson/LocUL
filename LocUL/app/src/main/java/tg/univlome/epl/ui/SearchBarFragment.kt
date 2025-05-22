package tg.univlome.epl.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import tg.univlome.epl.R

/**
 * Fragment SearchBarFragment : Barre de recherche dynamique
 *
 * Description :
 * Ce fragment fournit une interface utilisateur permettant à l'utilisateur de taper du texte
 * pour effectuer une recherche. Il déclenche dynamiquement des mises à jour de contenu
 * via une interface `SearchListener`.
 * Cette classe a donc pour but de :
 * - Fournir une zone de saisie pour filtrer dynamiquement des éléments (bâtiments, salles, infrastructures, etc.)
 * - Notifier les fragments hébergeurs des changements en temps réel
 *
 * Composants :
 * - `fragment_search_bar.xml` : contient un `EditText` pour la recherche
 *
 * Bibliothèques utilisées :
 * - AndroidX Fragment
 *
 * @see SearchListener — interface de communication pour la transmission des requêtes
 * @see tg.univlome.epl.ui.batiment.AllBatimentFragment
 * @see tg.univlome.epl.ui.infrastructure.AllInfraFragment
 * @see tg.univlome.epl.ui.home.ViewAllSalleFragment
 */
class SearchBarFragment : Fragment() {

    /**
     * Interface utilisée pour transmettre le texte saisi à un écouteur externe.
     */
    interface SearchListener {
        /**
         * Appelée à chaque fois que le texte dans la barre de recherche est modifié.
         * @param query texte saisi par l'utilisateur
         */
        fun onSearch(query: String)
    }

    private var searchListener: SearchListener? = null

    /**
     * Enregistre un écouteur pour les événements de recherche.
     *
     * @param listener l’objet implémentant SearchListener
     */
    fun setSearchListener(listener: SearchListener) {
        searchListener = listener
    }

    /**
     * Récupère l'écouteur actuellement enregistré.
     *
     * @return le SearchListener actif, ou null s'il n'est pas défini
     */
    fun getSearchListener(): SearchListener? {
        return this.searchListener
    }

    /**
     * Gonfle la vue associée au fragment.
     *
     * @param inflater le LayoutInflater
     * @param container le conteneur parent
     * @param savedInstanceState état antérieur du fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search_bar, container, false)
    }

    /**
     * Initialise les interactions après que la vue est créée. Ajoute notamment un `TextWatcher`
     * à l'élément `EditText` pour capturer les recherches.
     *
     * @param view vue racine du fragment
     * @param savedInstanceState état antérieur du fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEditText = view.findViewById<EditText>(R.id.search_edit_text)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchListener?.onSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}