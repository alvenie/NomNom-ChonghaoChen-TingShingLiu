package com.example.nomnom

import com.google.gson.annotations.SerializedName

// Yelp API data classes
data class YelpSearchResult(
    @SerializedName("total") val total: Int,
    @SerializedName("businesses") val restaurants: List<YelpRestaurant>
)

data class YelpRestaurant(
    // SerializedName is used to map the JSON field to the Kotlin property
    @SerializedName("name") val name: String,
    // rating matches the JSON field name
    val rating: Double,
    @SerializedName("review_count") val numReviews: Int,
    @SerializedName("distance") val distanceInMeters: Double,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("categories") val categories: List<YelpCategory>,
    @SerializedName("location") val location: YelpLocation,
    @SerializedName("url") val yelpUrl: String
) {
    // Calculate distance in miles
    val distanceInMiles: Double
        get() = ((distanceInMeters / 1609.34) * 100).toInt() / 100.0
}

data class YelpCategory(
    val title: String
)

data class YelpLocation(
    @SerializedName("address1") val address: String
)

// Restaurant data class
data class Restaurant(
    val name: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val distanceInMiles: Double = 0.0,
    val yelpUrl: String = ""
)