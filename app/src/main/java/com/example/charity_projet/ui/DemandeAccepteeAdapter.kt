package com.example.charity_projet.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.databinding.ItemDemandeAccepteeBinding
import com.example.charity_projet.models.Demande

class DemandeAccepteeAdapter(
    private var demandes: List<Demande>
) : RecyclerView.Adapter<DemandeAccepteeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDemandeAccepteeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(demande: Demande) {
            binding.tvTitre.text = "Titre : ${demande.contenu ?: "Titre non disponible"}"
            binding.tvType.text = "Type : ${demande.typeDemande ?: "Non spécifié"}"

            // Icônes interactives (optionnelles)
            binding.ivLike.setOnClickListener { /* Action Like */ }
            binding.ivShare.setOnClickListener { /* Action Share */ }
            binding.ivComment.setOnClickListener { /* Action Comment */ }
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
