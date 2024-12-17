package com.example.nomnom.pages

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nomnom.HomeViewModel
import coil.compose.AsyncImage
import com.example.nomnom.Restaurant
import android.net.Uri
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.nomnom.R

@Composable
fun SearchPage(navController: NavHostController, homeViewModel: HomeViewModel) {
    val restaurants by homeViewModel.restaurants.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(restaurants) {
        Log.d("SearchPage", "Number of restaurants to display: ${restaurants.size}")
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                homeViewModel.clearRestaurants()
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Restaurant List",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                )
            }
        } else if (restaurants.isEmpty()) {
            Text(text = "No restaurants found. Try again!", style = MaterialTheme.typography.bodyMedium)
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    homeViewModel.selectRandomRestaurant()
                    navController.navigate("roulette")
                },
                modifier = Modifier.fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text("Random Restaurant", fontSize = 20.sp)
            }
            LazyColumn {
                items(restaurants) { restaurant ->
                    RestaurantItem(
                        restaurant = restaurant,
                        onItemClick = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantItem(restaurant: Restaurant, onItemClick: (String) -> Unit) {

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                // Open the Yelp URL when the card is clicked
                onItemClick(restaurant.yelpUrl)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = "Restaurant image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = restaurant.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = "Rating: ${restaurant.rating}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Address: ${restaurant.address}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Distance: ${restaurant.distanceInMiles} miles", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}