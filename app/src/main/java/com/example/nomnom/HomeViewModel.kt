package com.example.nomnom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class HomeViewModel : ViewModel() {
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val yelpRepository = YelpRepository()
    private val firestoreRepository = FirestoreRepository()

    fun clearRestaurants() {
        _restaurants.value = emptyList()
        viewModelScope.launch {
            firestoreRepository.clearRestaurants()
        }
    }

    fun searchAndFilterRestaurants(latitude: Double, longitude: Double, radiusInMeters: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            if (radiusInMeters <= 0) {
                // If radius is 0 or negative, clear restaurants and return
                clearRestaurants()
                _isLoading.value = false
                return@launch
            }
            val yelpRestaurants = yelpRepository.searchRestaurants(latitude, longitude, radiusInMeters)

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

    private fun handleError(message: String, exception: Exception? = null) {
        Log.e("HomeViewModel", message, exception)
        // You can also update a state to show an error message in the UI
    }
}