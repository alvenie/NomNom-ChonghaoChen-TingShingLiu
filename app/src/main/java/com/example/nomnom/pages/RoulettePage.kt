package com.example.nomnom.pages
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nomnom.HomeViewModel
import coil.compose.AsyncImage
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.collectAsState
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.example.nomnom.AuthViewModel
import com.example.nomnom.R
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun RoulettePage(navController: NavHostController, homeViewModel: HomeViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var randomRestaurant by remember { mutableStateOf(homeViewModel.getSelectedRestaurant()) }
    var showAnimation by remember { mutableStateOf(true) }
    var animationKey by remember { mutableStateOf(0) }
    var isAnimationComplete by remember { mutableStateOf(false) }
    val toastMessage by authViewModel.toastMessage.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearToastMessage()
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.wheelspin))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = showAnimation,
        restartOnPlay = true
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            isAnimationComplete = true
            showAnimation = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showAnimation || !isAnimationComplete) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                randomRestaurant?.let { restaurant ->
                    IconButton(
                        onClick = { authViewModel.addToFavorites(restaurant.name) },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Add to Favorites",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(160.dp)
                        )
                    }

                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = "Restaurant image",
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                    )

                    Text(
                        text = "Selected: ${restaurant.name}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.yelpUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        Text("View Restaurant Details", fontSize = 24.sp)
                    }

                } ?: Text("No restaurants available")

                Button(
                    onClick = {
                        randomRestaurant = homeViewModel.selectRandomRestaurant()
                        showAnimation = true
                        isAnimationComplete = false
                        animationKey++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Reroll", fontSize = 24.sp)
                }

                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Home", fontSize = 24.sp)
                }
            }
        }
    }
}
