package com.example.charity_projet.models

import com.google.gson.annotations.SerializedName

// Modèle User
data class User(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("identifiant")
    val identifiant: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("role")
    val role: String? = null
)

// Modèle Demande
data class Demande(
    @SerializedName("id")
    private val idServer: String? = null, // renommé pour éviter le conflit JVM

    @SerializedName("_id")
    private val idAlt: String? = null,    // reste privé

    @SerializedName("contenu")
    val contenu: String? = null,

    @SerializedName("typeDemande")
    val typeDemande: String? = null,

    @SerializedName("etat")
    val etat: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("dateCreation")
    val dateCreation: String? = null
) {
    // Getter unique pour récupérer l'ID
    fun getId(): String? = idServer ?: idAlt
}

// Modèle ApiResponse
data class ApiResponse(
    @SerializedName("statut")
    val statut: String? = null,

    @SerializedName("notifications")
    val notifications: List<NotificationResponse>? = null
)

// Modèle NotificationResponse
data class NotificationResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("destinataire")
    val destinataire: String? = null
)
