package com.example.nomnom.pages

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Query
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Modifier
import com.google.firebase.Timestamp

@Composable
fun ChatPage(navController: NavHostController, friendEmail: String) {
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Real-time listener for messages
    LaunchedEffect(currentUser, friendEmail) {
        currentUser?.let { user ->
            db.collection("users").whereEqualTo("email", friendEmail).get()
                .addOnSuccessListener { documents ->
                    if (documents.documents.isNotEmpty()) {
                        val friendId = documents.documents[0].id
                        val chatId = getChatId(user.uid, friendId)

                        // Listen for real-time updates on messages
                        db.collection("chats").document(chatId).collection("messages")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.w("ChatScreen", "Listen failed.", e)
                                    return@addSnapshotListener
                                }

                                // Update messages state with new data
                                messages = snapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(Message::class.java)
                                } ?: emptyList()

                                Log.d("ChatScreen", "Messages updated: ${messages.size}")
                            }
                    } else {
                        Log.w("ChatScreen", "No user found with email $friendEmail")
                    }
                }
        }
    }

    // Scroll to the latest message when new messages arrive
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Chat with $friendEmail",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            if (messages.isEmpty()) {
                item {
                    Text("No messages yet", modifier = Modifier.padding(16.dp))
                }
            }
            items(messages) { message ->
                Log.d("ChatScreen", "Displaying message: ${message.content}")
                MessageItem(message, message.senderId == currentUser?.uid)
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    currentUser?.let { user ->
                        if (newMessage.isNotBlank()) {
                            sendMessage(user.uid, friendEmail, newMessage)
                            newMessage = "" // Clear input field after sending
                        }
                    }
                },
                enabled = newMessage.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}

data class Message(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

fun getChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) "$uid1-$uid2" else "$uid2-$uid1"
}

fun sendMessage(senderId: String, receiverEmail: String, content: String) {
    val db = Firebase.firestore
    db.collection("users").whereEqualTo("email", receiverEmail).get()
        .addOnSuccessListener { documents ->
            if (documents.documents.isNotEmpty()) {
                val receiverId = documents.documents[0].id
                val chatId = getChatId(senderId, receiverId)
                val message = Message(senderId, content, Timestamp.now())
                db.collection("chats").document(chatId).collection("messages").add(message)
                    .addOnSuccessListener {
                        Log.d("ChatPage", "Message sent successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("ChatPage", "Error sending message", e)
                    }
            }
        }
}


@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = message.content,
            modifier = Modifier
                .background(
                    if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        )
    }
}