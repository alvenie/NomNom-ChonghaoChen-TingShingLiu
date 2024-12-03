package com.example.nomnom.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nomnom.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun FriendsPage(navController: NavHostController, authViewModel: AuthViewModel) {
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    var friends by remember { mutableStateOf<List<String>>(emptyList()) }
    var friendRequest by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("FriendsPage", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    friends = snapshot?.get("friends") as? List<String> ?: emptyList()
                }
        }
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Friends",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            if (friends.isEmpty()) {
                item {
                    Text("You have no friends yet.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                items(friends) { friendEmail ->
                    Text(friendEmail, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("friendRequests") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Friend Requests")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = friendRequest,
            onValueChange = { friendRequest = it },
            label = { Text("Add friend by email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                currentUser?.let { user ->
                    if (friendRequest.isNotBlank()) {
                        db.collection("users").whereEqualTo("email", friendRequest)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    errorMessage = "No user found with email $friendRequest"
                                } else if (friendRequest != currentUser.email){
                                    val friendDoc = documents.documents[0]
                                    db.collection("users").document(friendDoc.id)
                                        .collection("friendRequests")
                                        .document(user.uid)
                                        .set(mapOf(
                                            "email" to user.email,
                                            "status" to "pending"
                                        ))
                                        .addOnSuccessListener {
                                            successMessage = "Friend request sent"
                                            friendRequest = ""
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Error sending friend request: ${e.message}"
                                        }
                                } else {
                                    errorMessage = "You cannot send a friend request to yourself"
                                }
                            }
                    }
                }
            }
        ) {
            Text("Send Friend Request")
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}