package com.example.nomnom.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun ProfilePage(navController: NavHostController, authViewModel: AuthViewModel) {

    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    // Fetching the username
    LaunchedEffect(Unit) {
        authViewModel.fetchUsername()
    }

    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var isDialogOpen by remember { mutableStateOf(false) }

    val username by authViewModel.username.collectAsState()



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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp)
            )

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
                text = email,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Navigate to favorite restaurants */ }
            ) {
                Text("Favorite Restaurants")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("friends") }
            ) {
                Text("Friends")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.logOut() }
            ) {
                Text("Log Out")
            }
        }
    }
}