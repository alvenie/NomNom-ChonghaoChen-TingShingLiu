package com.example.nomnom.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nomnom.AuthViewModel

@Composable
fun FavoritesPage(navController: NavHostController, authViewModel: AuthViewModel) {
    val favorites by authViewModel.favorites.collectAsState()
    // Track whether to show the remove dialog
    var showRemoveDialog by remember { mutableStateOf(false) }
    // Track the selected favorite
    var selectedFavorite by remember { mutableStateOf<AuthViewModel.Favorite?>(null) }
    // Get the context
    val context = LocalContext.current

    // Fetch favorites when the page is loaded
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
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
            }
            Text("Favorite Restaurants", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display favorites or a message if there are none
        if (favorites.isEmpty()) {
            Text("You haven't added any favorites yet.")
        } else {
            LazyColumn {
                items(favorites) { favorite ->
                    RestaurantItem(
                        favorite = favorite,
                        onTap = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(favorite.yelpUrl))
                            context.startActivity(intent)
                        },
                        onLongPress = {
                            selectedFavorite = favorite
                            showRemoveDialog = true
                        }
                    )
                }
            }
        }
    }

    // Show the remove dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove from Favorites") },
            text = { Text("Do you want to remove ${selectedFavorite?.name} from your favorites?") },
            confirmButton = {
                Button(
                    // Remove the selected favorite
                    onClick = {
                        selectedFavorite?.let { authViewModel.removeFromFavorites(it.yelpUrl) }
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                // Cancel removing the favorite
                Button(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Restaurant item
@Composable
fun RestaurantItem(
    favorite: AuthViewModel.Favorite,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = favorite.imageUrl,
                contentDescription = "Restaurant image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = favorite.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = "Rating: ${favorite.rating}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}