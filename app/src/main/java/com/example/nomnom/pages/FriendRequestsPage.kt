package com.example.nomnom.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nomnom.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

@Composable
fun FriendRequestsPage(navController: NavHostController, authViewModel: AuthViewModel) {
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    var friendRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .collection("friendRequests")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        // Handle error
                        return@addSnapshotListener
                    }
                    friendRequests = snapshot?.documents?.mapNotNull { doc ->
                        FriendRequest(doc.id, doc.getString("email") ?: "", doc.getString("status") ?: "")
                    } ?: emptyList()
                }
        }
    }

    val errorMessage by remember { mutableStateOf<String?>(null) }

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
            Text("Friend Requests", style = MaterialTheme.typography.headlineMedium)
        }
        friendRequests.forEach { request ->
            FriendRequestItem(request) { accepted ->
                if (accepted) {
                    acceptFriendRequest(db, currentUser?.uid, request)
                } else {
                    rejectFriendRequest(db, currentUser?.uid, request)
                }
            }
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun FriendRequestItem(request: FriendRequest, onResponse: (Boolean) -> Unit) {
    Column {
        Text(request.email, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 32.sp))
        Row {
            Button(onClick = { onResponse(true) }) {
                Text("Accept")
            }
            Button(onClick = { onResponse(false) }) {
                Text("Reject")
            }
        }
    }
}

fun acceptFriendRequest(db: FirebaseFirestore, currentUserId: String?, request: FriendRequest) {
    currentUserId?.let { userId ->
        db.runTransaction { transaction ->
            val currentUserRef = db.collection("users").document(userId)
            val friendRef = db.collection("users").document(request.id)

            // Add friend's email to current user's friends list
            transaction.update(currentUserRef, "friends", FieldValue.arrayUnion(request.email))

            // Add current user's email to friend's friends list
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
            if (currentUserEmail != null) {
                transaction.update(friendRef, "friends", FieldValue.arrayUnion(currentUserEmail))
            }

            // Remove friend request
            val requestRef = currentUserRef.collection("friendRequests").document(request.id)
            transaction.delete(requestRef)
        }
    }
}

fun rejectFriendRequest(db: FirebaseFirestore, currentUserId: String?, request: FriendRequest) {
    currentUserId?.let { userId ->
        db.collection("users").document(userId)
            .collection("friendRequests")
            .document(request.id)
            .delete()
    }
}

data class FriendRequest(val id: String, val email: String, val status: String)