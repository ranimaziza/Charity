package com.example.charity_projet.api

import com.example.charity_projet.models.ApiResponse
import com.example.charity_projet.models.CommentRequest
import com.example.charity_projet.models.Commentaire
import com.example.charity_projet.models.Demande
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("demandes/liste")
    suspend fun getDemandes(): Response<List<Demande>>

    // Retrofit API
    @PUT("demandes/{id}/statut")
    suspend fun traiterDemande(
        @Path("id") id: String,
        @Query("action") action: String
    ): Response<ResponseBody>

    @DELETE("demandes/{id}")
    suspend fun supprimerDemande(@Path("id") id: String): Response<ResponseBody>


    // 🔹 Like une demande
    @PUT("demandes/{id}/like")
    suspend fun likeDemande(@Path("id") id: String): Response<Demande>

    // 🔹 Ajouter un commentaire
    @POST("demandes/{id}/comment")
    suspend fun addComment(
        @Path("id") id: String,
        @Body comment: CommentRequest
    ): Response<List<Commentaire>>

}