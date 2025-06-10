package tg.univlome.epl.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tg.univlome.epl.R

/**
 * Adapter ImageAdapter : Affichage en carrousel d'une galerie d'images
 *
 * Description :
 * Cet adaptateur est utilisé dans un `ViewPager2` pour afficher une galerie d'images (URL) sous forme
 * de carrousel dans les activités de visualisation (`BatimentActivity`, `SalleActivity`, etc.).
 * Chaque image est affichée en plein écran avec ajustement automatique.
 *
 * Composants :
 * - `ImageViewHolder` : ViewHolder contenant une seule `ImageView` plein écran.
 * - `Glide` : Chargement des images depuis une URL avec gestion de placeholder et fallback.
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement et la mise en cache des images
 * - `RecyclerView + ViewPager2` pour le carrousel d’images
 *
 * @param images Liste d'URL des images à afficher
 *
 * @see tg.univlome.epl.ui.batiment.BatimentActivity
 * @see tg.univlome.epl.ui.infrastructure.InfraActivity
 * @see tg.univlome.epl.ui.home.SalleActivity
 */
class ImageAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    /**
     * ViewHolder contenant une ImageView plein écran.
     */
    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    /**
     * Crée dynamiquement un ImageView en plein écran pour le carrousel.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_XY
        }
        return ImageViewHolder(imageView)
    }

    /**
     * Lie une URL d’image au `ImageView` avec chargement asynchrone.
     *
     * @param holder ViewHolder de l'image
     * @param position Position dans la liste d’images
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.img) // image de chargement par défaut
            .error(R.drawable.img)       // image si erreur de chargement
            .into(holder.imageView)
    }

    /**
     * Retourne le nombre total d'images à afficher.
     */
    override fun getItemCount(): Int = images.size
}