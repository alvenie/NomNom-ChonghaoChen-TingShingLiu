package com.example.nomnom

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")

    // Save restaurants to Firestore
    suspend fun saveRestaurants(restaurants: List<YelpRestaurant>) {
        restaurants.forEach { restaurant ->
            // Check if the restaurant already exists in Firestore
            val query = restaurantsCollection.whereEqualTo("name", restaurant.name)
                .whereEqualTo("address", restaurant.location.address)
                .get()
                .await()

            // If the restaurant doesn't exist in Firestore, add it
            if (query.isEmpty) {
                val restaurantData = hashMapOf(
                    "name" to restaurant.name,
                    "address" to restaurant.location.address,
                    "imageUrl" to restaurant.imageUrl,
                    "rating" to restaurant.rating,
                    "distanceInMiles" to restaurant.distanceInMiles,
                    "yelpUrl" to restaurant.yelpUrl
                )
                restaurantsCollection.add(restaurantData)
            }
        }
    }

    // Retrieve restaurants from Firestore
    suspend fun getRestaurants(): List<Restaurant> {
        return try {
            // Fetch restaurants from Firestore
            val restaurants = restaurantsCollection.get().await().documents.mapNotNull { document ->
                document.toObject(Restaurant::class.java)
            }
            Log.d("FirestoreRepository", "Retrieved ${restaurants.size} restaurants from Firestore")
            restaurants
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching restaurants: ${e.message}")
            emptyList()
        }
    }

    // Clear all restaurants from Firestore
    suspend fun clearRestaurants() {
        try {
            val batch = db.batch()
            val documents = restaurantsCollection.get().await()
            for (document in documents) {
                batch.delete(document.reference)
            }
            // Commit the batch to delete all restaurants
            batch.commit().await()
            Log.d("FirestoreRepository", "All restaurants cleared from Firestore")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error clearing restaurants: ${e.message}")
        }
    }

}