package com.example.charity_projet.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.models.Demande
import kotlinx.coroutines.launch

class DemandesAccepteesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRefresh: Button
    private lateinit var demandeAdapter: DemandeAccepteeAdapter

    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_view_acceptees)

        recyclerView = findViewById(R.id.recyclerViewAcceptees)
        tvEmpty = findViewById(R.id.tvEmptyAcceptees)
        progressBar = findViewById(R.id.progressBarAcceptees)
        btnRefresh = findViewById(R.id.btnRefreshAcceptees)

        setupRecyclerView()
        setupClickListeners()
        loadDemandesAcceptees()
    }

    private fun setupRecyclerView() {
        demandeAdapter = DemandeAccepteeAdapter(emptyList()) // ✅ nouvel adapter
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DemandesAccepteesActivity)
            adapter = demandeAdapter
        }
    }


    private fun setupClickListeners() {
        btnRefresh.setOnClickListener { loadDemandesAcceptees() }
    }

    private fun loadDemandesAcceptees() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getDemandes()
                if (response.isSuccessful) {
                    val accepted = response.body()?.filter { it.etat == "ACCEPTEE" } ?: emptyList()
                    showDemandes(accepted)
                } else {
                    showError("Erreur: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                showError("Erreur réseau: ${e.message}")
                Log.e("LOAD_ACCEPTED", "Exception: ${e.message}", e)
            } finally {
                showLoading(false)
            }
        }
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
        tvEmpty.text = message
        tvEmpty.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadDemandesAcceptees() // Recharge la liste à chaque retour sur l'activité
    }
}
