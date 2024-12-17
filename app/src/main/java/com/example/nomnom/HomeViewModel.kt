package com.example.nomnom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // StateFlow for restaurants
    // StateFlow is a type of Flow in Kotlin that represents an observable state holder.
    // It's designed to efficiently handle and emit state updates to multiple collectors.
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Initialize YelpRepository and FirestoreRepository
    private val yelpRepository = YelpRepository()
    private val firestoreRepository = FirestoreRepository()

    fun clearRestaurants() {
        // Clear the list of restaurants
        _restaurants.value = emptyList()
        viewModelScope.launch {
            // Clear restaurants from Firestore
            firestoreRepository.clearRestaurants()
        }
    }

    // Search and filter restaurants based on latitude, longitude, and radius
    fun searchAndFilterRestaurants(latitude: Double, longitude: Double, radiusInMeters: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            if (radiusInMeters <= 0) {
                // If radius is 0 or negative, clear restaurants and return
                clearRestaurants()
                _isLoading.value = false
                return@launch
            }

            // Search restaurants using YelpRepository
            val yelpRestaurants = yelpRepository.searchRestaurants(latitude, longitude, radiusInMeters)

            // Save and retrieve restaurants from Firestore
            if (yelpRestaurants.isNotEmpty()) {
                firestoreRepository.saveRestaurants(yelpRestaurants)
                _restaurants.value = firestoreRepository.getRestaurants()
            } else {
                // If no restaurants found, clear the list
                clearRestaurants()
            }

            _isLoading.value = false
        }
    }

    private var selectedRestaurant: Restaurant? = null

    // Select a random restaurant from the list
    fun selectRandomRestaurant(): Restaurant? {
        selectedRestaurant = restaurants.value.randomOrNull()
        return selectedRestaurant
    }

    // Get the selected restaurant
    fun getSelectedRestaurant(): Restaurant? = selectedRestaurant
}