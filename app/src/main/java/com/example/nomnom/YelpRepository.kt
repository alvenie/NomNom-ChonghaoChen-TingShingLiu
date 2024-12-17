package com.example.nomnom

import android.util.Log

class YelpRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun searchRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int,
        limit: Int = 50 // Increased to 50, which is Yelp's maximum
    ): List<YelpRestaurant> {
        val authHeader = "Bearer hufhi7GLZUXp3XZl0kuRUD6pSCIR4Bm4zU2-vszNHxUdgnSkj5bmTpPKXBjPGu7Cb7f8GvWPRrYACIEOl4xfUVi9wDBdypbPITEeXLL5e5x7-EmYa0Hs2_3JkmVOZ3Yx"
        val allRestaurants = mutableListOf<YelpRestaurant>()
        var offset = 0
        var total = Int.MAX_VALUE

        while (offset < total) {
            try {
                Log.d("YelpRepository", "Sending request to Yelp API with offset $offset")
                val response = apiService.searchRestaurants(
                    authHeader,
                    "restaurants",
                    latitude,
                    longitude,
                    radius,
                    limit,
                    offset
                )
                allRestaurants.addAll(response.restaurants)
                total = response.total
                offset += limit
                Log.d("YelpRepository", "Received ${response.restaurants.size} restaurants. Total: $total")
            } catch (e: Exception) {
                Log.e("YelpRepository", "Error fetching restaurants: ${e.message}")
                break
            }
        }

        return allRestaurants.filter { restaurant ->
            restaurant.distanceInMeters <= radius
        }

    }
}