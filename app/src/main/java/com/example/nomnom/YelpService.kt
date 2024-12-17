package com.example.nomnom

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YelpService {
    // Retrofit interface for Yelp API
    @GET("businesses/search")
    suspend fun searchRestaurants(
        // Headers and query parameters for the API request
        @Header("Authorization") authHeader: String,
        @Query("term") searchTerm: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): YelpSearchResult
}