package com.example.nomnom

import com.google.gson.annotations.SerializedName

data class YelpSearchResult(
    @SerializedName("total") val total: Int,
    @SerializedName("businesses") val restaurants: List<YelpRestaurant>
)

data class YelpRestaurant(
    @SerializedName("name") val name: String,
    val rating: Double,
    @SerializedName("review_count") val numReviews: Int,
    @SerializedName("distance") val distanceInMeters: Double,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("categories") val categories: List<YelpCategory>,
    @SerializedName("location") val location: YelpLocation,
    @SerializedName("url") val yelpUrl: String
)

data class YelpCategory(
    val title: String
)

data class YelpLocation(
    @SerializedName("address1") val address: String
)

data class Restaurant(
    val name: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val distanceInMeters: Double = 0.0,
    val yelpUrl: String = ""
)