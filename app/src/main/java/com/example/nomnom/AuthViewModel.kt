package com.example.nomnom

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl.asStateFlow()

    private val _profilePictureUpdateStatus = MutableStateFlow(Result.success(false))
    val profilePictureUpdateStatus: StateFlow<Result<Boolean>> = _profilePictureUpdateStatus.asStateFlow()

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

    // User Login
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
                    fetchUserProfile()
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

    // User document creation
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
            "friends" to listOf<String>(),
            "profilePictureUrl" to "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=monsterid"
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
    fun updateDisplayName(newDisplayName: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = userProfileChangeRequest { displayName = newDisplayName }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _username.value = newDisplayName
                updateUserDocumentDisplayName(newDisplayName)
            } else {
                _authState.value = AuthState.Error("Failed to update display name: ${task.exception?.message}")
            }
        }
    }

    private fun updateUserDocumentDisplayName(newDisplayName: String) {
        val user = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(user.uid)

        userRef.update("displayName", newDisplayName)
            .addOnSuccessListener { Log.d(TAG, "User display name updated in Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Error updating user display name in Firestore", e) }
    }

    fun fetchUsername() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                var username = user.displayName

                if (username.isNullOrBlank()) {
                    val userDoc = firestore.collection("users").document(user.uid).get().await()
                    username = userDoc.getString("displayName")
                }

                _username.value = username ?: "No username set"
                Log.d(TAG, "Username fetched: ${_username.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching username", e)
                _username.value = "Error fetching username"
            }
        }
    }

    // Profile image processing
    fun updateProfilePicture(uri: Uri?) {
        viewModelScope.launch {
            try {
                uri ?: throw IllegalArgumentException("URI cannot be null")
                val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")

                val profilePicRef = storage.reference.child("profile_pictures/${user.uid}.jpg")
                profilePicRef.putFile(uri).await()

                val downloadUrl = profilePicRef.downloadUrl.await()
                Log.d(TAG, "Download URL: $downloadUrl")

                val updateSuccess = updateUserDocumentProfilePicture(downloadUrl.toString())
                if (updateSuccess) {
                    _profilePictureUrl.value = downloadUrl.toString()
                    _profilePictureUpdateStatus.value = Result.success(true)
                } else {
                    _profilePictureUpdateStatus.value = Result.failure(Exception("Failed to update Firestore"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile picture", e)
                _profilePictureUpdateStatus.value = Result.failure(e)
            }
        }
    }

    private suspend fun updateUserDocumentProfilePicture(photoUrl: String): Boolean {
        return try {
            val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
            val userRef = firestore.collection("users").document(user.uid)
            userRef.update("profilePictureUrl", photoUrl).await()
            Log.d(TAG, "User profile picture URL updated in Firestore: $photoUrl")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile picture URL in Firestore", e)
            false
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val userDoc = firestore.collection("users").document(user.uid).get().await()
                val profilePicUrl = userDoc.getString("profilePictureUrl")
                Log.d(TAG, "Fetched profile picture URL: $profilePicUrl")
                _profilePictureUrl.value = profilePicUrl
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile", e)
            }
        }
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

    private val _favorites = MutableStateFlow<List<String>>(emptyList())
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    fun fetchFavorites() {
        val user = Firebase.auth.currentUser
        user?.let { firebaseUser ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(firebaseUser.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val favoritesArray = document.get("favorites") as? List<String>
                    _favorites.value = favoritesArray ?: emptyList()
                }
            }
        }
    }
}

sealed class AuthState {

    data object Unauthenticated : AuthState()
    data object Authenticated : AuthState()
    data object Loading : AuthState()
    data class Error(val message: String) : AuthState()

}
