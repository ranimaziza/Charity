package com.example.charity_projet.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.api.RetrofitClient.apiService
import com.example.charity_projet.databinding.ItemDemandeAccepteeBinding
import com.example.charity_projet.models.CommentRequest
import com.example.charity_projet.models.Demande
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DemandeAccepteeAdapter(
    private var demandes: List<Demande>,
    private val context: Context
) : RecyclerView.Adapter<DemandeAccepteeAdapter.ViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Main)

    inner class ViewHolder(private val binding: ItemDemandeAccepteeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(demande: Demande) {
            binding.tvTitre.text = "Titre : ${demande.contenu ?: "Titre non disponible"}"
            binding.tvType.text = "Type : ${demande.typeDemande ?: "Non spécifié"}"

            // Like
            binding.ivLike.setOnClickListener {
                adapterScope.launch {
                    try {
                        val response = apiService.likeDemande(demande.getId() ?: "")
                        if (response.isSuccessful) {
                            // Mettre à jour le nombre de likes dans l'objet
                            demande.likes = response.body()?.likes ?: demande.likes

                            // Mettre à jour l'affichage du compteur de likes
                            binding.tvLikeCount.text = "${demande.likes} Likes"

                            // Notifier l'adapter pour rebind si nécessaire
                            notifyItemChanged(adapterPosition)

                            Toast.makeText(context, "Vous avez aimé cette demande !", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erreur lors du like", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            // Share
            binding.ivShare.setOnClickListener {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Demande: ${demande.contenu}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
            }

            // Commentaire
            binding.ivComment.setOnClickListener {
                val editText = EditText(context)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Ajouter un commentaire")
                    .setView(editText)
                    .setPositiveButton("Envoyer") { _, _ ->
                        val contenuCommentaire = editText.text.toString()
                        if (contenuCommentaire.isNotBlank()) {
                            adapterScope.launch {
                                try {
                                    val response = apiService.addComment(
                                        demande.getId() ?: "",
                                        CommentRequest(contenuCommentaire)
                                    )
                                    if (response.isSuccessful) {
                                        val nouveauCommentaire = response.body()?.lastOrNull()
                                        nouveauCommentaire?.let {
                                            // Ajouter le commentaire à la liste locale
                                            demande.comments = demande.comments + it

                                            // Mettre à jour le compteur de commentaires
                                            binding.tvCommentCount.text = "${demande.comments.size} Commentaires"

                                            notifyItemChanged(adapterPosition)
                                        }
                                        Toast.makeText(context, "Commentaire ajouté", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Erreur lors de l'ajout du commentaire", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .setNegativeButton("Annuler", null)
                    .create()
                dialog.show()
            }


            // Affichage compteur de likes initial
            binding.tvLikeCount.text = "${demande.likes} Likes"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDemandeAccepteeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(demandes[position])
    }

    override fun getItemCount(): Int = demandes.size

    fun updateData(newList: List<Demande>) {
        demandes = newList
        notifyDataSetChanged()
    }
}
