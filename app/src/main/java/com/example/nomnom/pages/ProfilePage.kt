package com.example.nomnom.pages

import android.Manifest
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nomnom.AuthState
import com.example.nomnom.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp

@Composable
fun ProfilePage(navController: NavHostController, authViewModel: AuthViewModel) {

    val authState by authViewModel.authState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe username and profile picture URL
    val username by authViewModel.username.collectAsState()
    val profilePictureUrl by authViewModel.profilePictureUrl.collectAsState()
    val updateStatus by authViewModel.profilePictureUpdateStatus.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    // Launch effects for authentication and data fetching
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchUsername()
        authViewModel.fetchUserProfile()
    }

    // Handle profile picture update
    LaunchedEffect(updateStatus) {
        updateStatus.onSuccess {
            if (it) {
                isLoading = false
            }
        }.onFailure { exception ->
            isLoading = false
            scope.launch { snackbarHostState.showSnackbar("Failed to update profile picture: ${exception.message}") }
        }
    }

    // Image picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it
                isLoading = true
                authViewModel.updateProfilePicture(it)
            }
        }
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // If all permission are granted, it launches photo picker
        if (permissions.values.all { it }) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permission denied. Cannot select profile picture.") }
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Find") },
                    selected = false,
                    onClick = { navController.navigate("search") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = true,
                    onClick = { /* Already on profile page */ }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    // Utilize Coil's built-in disk caching capabilities.
                    // Coil automatically caches images on disk, so subsequent loads will be faster.
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePictureUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Profile Picture",

                    // Testing
                    onLoading = { Log.d(TAG, "Loading image...") },
                    onSuccess = { Log.d(TAG, "Image loaded successfully") },
                    onError = { Log.e(TAG, "Error loading image: $it") },
                    // Testing

                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .clickable {
                            val permissions = when {
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                                    // For Android 14 and above, request read media images permission
                                    arrayOf(
                                        Manifest.permission.READ_MEDIA_IMAGES,
                                        Manifest.permission.READ_MEDIA_VIDEO,
                                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                    )
                                }
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                    // For Android 13 and above, request read media images permission
                                    arrayOf(
                                        Manifest.permission.READ_MEDIA_IMAGES,
                                        Manifest.permission.READ_MEDIA_VIDEO
                                    )
                                }
                                else -> {
                                    // For Android 12 and below, request read external storage permission
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                            permissionLauncher.launch(permissions)
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.clickable { isDialogOpen = true }
            )

            // Pop up box to change username
            if (isDialogOpen) {
                var newUsername by remember { mutableStateOf(username) }
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    title = { Text("Edit Username") },
                    text = {
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("New Username") }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            isDialogOpen = false
                            // Call function to update username in database
                            authViewModel.updateDisplayName(newUsername)
                            scope.launch {
                                snackbarHostState.showSnackbar("Username updated successfully")
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { isDialogOpen = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = FirebaseAuth.getInstance().currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("favorites")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text("Favorite Restaurants", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("friends") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text("Friends", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.logOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text("Log Out", fontSize = 24.sp)
            }
        }
    }
}