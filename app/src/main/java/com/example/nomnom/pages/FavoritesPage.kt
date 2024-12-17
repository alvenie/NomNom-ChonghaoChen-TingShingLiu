package com.example.nomnom.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.example.nomnom.AuthViewModel

@Composable
fun FavoritesPage(navController: NavHostController, authViewModel: AuthViewModel) {
    val favorites by authViewModel.favorites.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.fetchFavorites()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
            }
            Text("Favorite Restaurants", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Text("You haven't added any favorites yet.")
        } else {
            LazyColumn {
                items(favorites) { restaurantId ->
                    Text(restaurantId, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp))
                    HorizontalDivider()
                }
            }
        }
    }
}
