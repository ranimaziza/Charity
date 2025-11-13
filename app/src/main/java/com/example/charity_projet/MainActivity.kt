package com.example.charity_projet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.models.Demande
import com.example.charity_projet.ui.DemandeAdapter
import com.example.charity_projet.ui.DemandesAccepteesActivity
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRefresh: Button
    private lateinit var btnVoirAcceptees: Button // ✅ bouton ajouté ici

    private lateinit var demandeAdapter: DemandeAdapter
    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Initialisation des vues
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnVoirAcceptees = findViewById(R.id.btnVoirAcceptees) // ✅ ajouté

        setupRecyclerView()
        setupClickListeners()
        loadDemandes()
    }

    private fun setupRecyclerView() {
        demandeAdapter = DemandeAdapter(emptyList()) { demande, action ->
            showConfirmationDialog(demande, action) // Utiliser la confirmation
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = demandeAdapter
        }
    }

    private fun setupClickListeners() {
        // ✅ Bouton actualiser
        btnRefresh.setOnClickListener {
            loadDemandes()
        }

        // ✅ Bouton voir les demandes acceptées
        btnVoirAcceptees.setOnClickListener {
            val intent = Intent(this, DemandesAccepteesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDemandes() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = apiService.getDemandes()
                if (response.isSuccessful) {
                    val toutesLesDemandes = response.body() ?: emptyList()
                    val demandesEnAttente = toutesLesDemandes.filter { it.etat == "EN_ATTENTE" }

                    showDemandes(demandesEnAttente)

                    Toast.makeText(
                        this@MainActivity,
                        "${demandesEnAttente.size} demandes en attente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError("Erreur: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                showError("Erreur réseau: ${e.message}")
                Log.e("NETWORK_ERROR", "Exception: ${e.message}", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun traiterDemande(demande: Demande, action: String) {
        val demandeId = demande.getId() ?: return

        lifecycleScope.launch {
            try {
                if (action == "refuser") {
                    // Utiliser la suppression définitive
                    val response: Response<ResponseBody> = apiService.supprimerDemande(demandeId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Demande refusée et supprimée définitivement", Toast.LENGTH_SHORT).show()
                        demandeAdapter.removeDemande(demande)
                    } else {
                        // Fallback: utiliser l'ancienne méthode si DELETE échoue
                        val fallbackResponse: Response<ResponseBody> = apiService.traiterDemande(demandeId, action)
                        if (fallbackResponse.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Demande refusée", Toast.LENGTH_SHORT).show()
                            demandeAdapter.removeDemande(demande)
                        } else {
                            val errorBody = fallbackResponse.errorBody()?.string()
                            Log.e("REFUS_ERROR", "Code: ${fallbackResponse.code()}, Body: $errorBody")
                            Toast.makeText(
                                this@MainActivity,
                                "Erreur lors du refus: ${fallbackResponse.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // Pour "accepter", utiliser l'ancienne méthode
                    val response: Response<ResponseBody> = apiService.traiterDemande(demandeId, action)
                    if (response.isSuccessful) {
                        val messageServeur = response.body()?.string() ?: "Demande acceptée"
                        Toast.makeText(this@MainActivity, messageServeur, Toast.LENGTH_SHORT).show()
                        demandeAdapter.removeDemande(demande)
                        if (action == "accepter") {
                            // Optionnel: naviguer vers les demandes acceptées
                            val intent = Intent(this@MainActivity, DemandesAccepteesActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ACCEPT_ERROR", "Code: ${response.code()}, Body: $errorBody")
                        Toast.makeText(
                            this@MainActivity,
                            "Erreur lors de l'acceptation: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ACTION_NETWORK_ERROR", "Exception: ${e.message}", e)
                Toast.makeText(
                    this@MainActivity,
                    "Erreur réseau: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun showConfirmationDialog(demande: Demande, action: String) {
        val message = if (action == "refuser") {
            "Êtes-vous sûr de vouloir refuser et supprimer définitivement cette demande ? Cette action est irréversible."
        } else {
            "Êtes-vous sûr de vouloir accepter cette demande ?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage(message)
            .setPositiveButton("Oui") { dialog, which ->
                traiterDemande(demande, action)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    private fun showDemandes(demandes: List<Demande>) {
        demandeAdapter.updateData(demandes)
        if (demandes.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        tvEmpty.text = message
        tvEmpty.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
}
