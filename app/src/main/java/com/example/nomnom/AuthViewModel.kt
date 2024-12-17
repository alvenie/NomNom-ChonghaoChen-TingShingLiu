package com.example.nomnom

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateLastLogin(task.result?.user)
                    fetchUsername()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }

            }
    }

    private fun updateLastLogin(user: FirebaseUser?) {
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(it.uid)
            userRef.update("lastLoginAt", FieldValue.serverTimestamp())
        }
    }

    fun signup(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _authState.value = AuthState.Error("Email, password, and display name cannot be empty")
            return
        }
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        updateDisplayName(displayName)
                        createUserDocument(user)
                        _authState.value = AuthState.Authenticated
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun logOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    private fun createUserDocument(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)
        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "favorites" to listOf<String>(),
            "friends" to listOf<String>()
        )
        userRef.set(userData)
            .addOnSuccessListener {
                // Document created successfully
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { e ->
                // Handle any errors
                _authState.value = AuthState.Error("Failed to create user document: ${e.message}")
            }
    }

    // USERNAME
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()


    fun updateDisplayName(newDisplayName: String) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = newDisplayName
        }
        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _username.value = newDisplayName
                    updateUserDocumentDisplayName(newDisplayName)
                } else {
                    _authState.value = AuthState.Error("Failed to update display name: ${task.exception?.message}")
                }
            }
    }

    private fun updateUserDocumentDisplayName(newDisplayName: String) {
        val user = Firebase.auth.currentUser
        user?.let { firebaseUser ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(firebaseUser.uid)
            userRef.update("displayName", newDisplayName)
                .addOnSuccessListener {
                    Log.d(TAG, "User display name updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating user display name in Firestore", e)
                }
        }
    }

    fun fetchUsername() {
        val user = Firebase.auth.currentUser
        _username.value = user?.displayName ?: ""
    }

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    fun addToFavorites(restaurantId: String) {
        val user = Firebase.auth.currentUser
        user?.let { firebaseUser ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(firebaseUser.uid)
            userRef.update("favorites", FieldValue.arrayUnion(restaurantId))
                .addOnSuccessListener {
                    _toastMessage.value = "Restaurant added to favorites"
                }
                .addOnFailureListener { e ->
                    _toastMessage.value = "Failed to add restaurant to favorites"
                }
        }
    }
    fun clearToastMessage() {
        _toastMessage.value = null
    }
}

sealed class AuthState {

    data object Unauthenticated : AuthState()
    data object Authenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()

}
