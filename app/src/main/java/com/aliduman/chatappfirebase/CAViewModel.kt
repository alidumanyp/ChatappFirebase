package com.aliduman.chatappfirebase

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.aliduman.chatappfirebase.data.COLLECTION_CHATS
import com.aliduman.chatappfirebase.data.COLLECTION_MESSAGES
import com.aliduman.chatappfirebase.data.COLLECTION_STATUS
import com.aliduman.chatappfirebase.data.COLLECTION_USERS
import com.aliduman.chatappfirebase.data.ChatData
import com.aliduman.chatappfirebase.data.ChatUser
import com.aliduman.chatappfirebase.data.Event
import com.aliduman.chatappfirebase.data.Message
import com.aliduman.chatappfirebase.data.Status
import com.aliduman.chatappfirebase.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CAViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    private var lastClickTime = 0L

    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressMessages = mutableStateOf(false)
    private var currentChatMessagesListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    private var currentStatusListener: ListenerRegistration? = null


    init {
        //onLogout()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
        createOrUpdateProfile()
    }

    fun onLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            popupNotification.value = Event("Please enter all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        getUserData(uid)
                    }
                } else {
                    handleException(task.exception, "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }

    fun onSignUp(name: String, number: String, email: String, password: String) {
        if (name.isBlank() || number.isBlank() || email.isBlank() || password.isBlank()) {
            popupNotification.value = Event("Please enter all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USERS).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(name = name, number = number)
                            } else {
                                handleException(task.exception)
                            }
                        }

                } else {
                    popupNotification.value = Event("User with this number already exists")
                    inProgress.value = false
                }
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUri: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageUri ?: userData.value?.imageUrl
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        //update user
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                            }

                    } else {
                        //create user
                        db.collection(COLLECTION_USERS).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid) //get user data after creating user
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot retrieve user")
                }
        }
    }

    fun updateProfileData(name: String, number: String) {
        createOrUpdateProfile(name = name, number = number)
        auth.currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USERS).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot retrieve user")
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    //val userData = value.toObject(UserData::class.java)
                    val user = value.toObject<UserData>()
                    this.userData.value = user
                    inProgress.value = false
                    populateChats()
                    populateStatuses()
                }
            }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out successfully")
        chats.value = listOf()
        status.value = listOf()

        // Clean up all listeners and in-progress state
        inProgress.value = false
        inProgressChats.value = false
        inProgressMessages.value = false
        inProgressStatus.value = false

        chatListener?.remove()
        currentStatusListener?.remove()
        dePopulateChat()
    }

    private fun handleException(e: Exception? = null, customMessage: String = "") {
        Log.e("ChatApp", "Exception: ${e?.localizedMessage}", e)
        e?.printStackTrace()

        val errorMsg = when (e) {
            is FirebaseAuthException -> "Authentication error: ${e.message}"
            is FirebaseFirestoreException -> "Firestore error: ${e.message}"
            else -> e?.localizedMessage ?: "Unknown error"
        }

        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
    }


    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it, "Cannot upload image")
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUri = it.toString())
        }
    }

    fun onAddChat(number: String) {
        if (number.isBlank() || !number.isDigitsOnly()) {
            handleException(customMessage = "Invalid number")
        } else {
            inProgressChats.value = true
            db.collection(COLLECTION_CHATS)
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("user1.number", number),
                            Filter.equalTo("user2.number", userData.value?.number)
                        ),
                        Filter.and(
                            Filter.equalTo("user1.number", userData.value?.number),
                            Filter.equalTo("user2.number", number)
                        )
                    )
                )
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        db.collection(COLLECTION_USERS).whereEqualTo("number", number)
                            .get()
                            .addOnSuccessListener {
                                if (it.isEmpty) {
                                    handleException(customMessage = "User not found number with $number")
                                } else {
                                    val chatPartner = it.documents[0].toObject<UserData>()
                                    val id = db.collection(COLLECTION_CHATS).document().id
                                    val chat = ChatData(
                                        chatId = id,
                                        user1 = ChatUser(
                                            userId = userData.value?.userId,
                                            name = userData.value?.name,
                                            imageUrl = userData.value?.imageUrl,
                                            number = userData.value?.number
                                        ),
                                        user2 = ChatUser(
                                            userId = chatPartner?.userId,
                                            name = chatPartner?.name,
                                            imageUrl = chatPartner?.imageUrl,
                                            number = chatPartner?.number
                                        )
                                    )
                                    db.collection(COLLECTION_CHATS).document(id).set(chat)
                                }
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot retrieve user")
                            }

                    } else {
                        handleException(customMessage = "Chat already exists")
                    }
                }

        }
    }

    private fun populateChats() {
        inProgressChats.value = true
        chatListener = db.collection(COLLECTION_CHATS)
            .where(
                Filter.or(
                    Filter.equalTo("user1.userId", userData.value?.userId),
                    Filter.equalTo("user2.userId", userData.value?.userId)
                )
            )
            .limit(50)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot retrieve chats")
                    return@addSnapshotListener
                }
                if (value != null) {
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                }
                inProgressChats.value = false
            }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(
            sentBy = userData.value?.userId,
            message = message,
            timestamp = time
        )
        db.collection(COLLECTION_CHATS).document(chatId)
            .collection(COLLECTION_MESSAGES)
            .document()
            .set(msg)
    }

    fun populateChat(chatId: String) {
        inProgressMessages.value = true
        currentChatMessagesListener = db.collection(COLLECTION_CHATS)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot retrieve messages")
                }
                if (value != null) {
                    chatMessages.value = value.documents
                        .mapNotNull { it.toObject<Message>() }
                        .sortedBy { it.timestamp }
                }
                inProgressMessages.value = false
            }
    }

    fun dePopulateChat() {
        chatMessages.value = listOf()
        currentChatMessagesListener?.remove()
    }

    private fun createStatus(imageUrl: String) {
        val newStatus = Status(
            user = ChatUser(
                userId = userData.value?.userId,
                name = userData.value?.name,
                number = userData.value?.number,
                imageUrl = userData.value?.imageUrl
            ),
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        db.collection(COLLECTION_STATUS).document().set(newStatus)
    }

    fun uploadStatus(imageUri: Uri) {
        uploadImage(imageUri) {
            createStatus(imageUrl = it.toString())
        }
    }

    private fun populateStatuses() {
        inProgressStatus.value = true
        val milliTimeDelta = 24L * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - milliTimeDelta

        currentStatusListener = db.collection(COLLECTION_CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot retrieve chats")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val currentConnections = arrayListOf(userData.value?.userId)
                    val chats = value.toObjects<ChatData>()
                    chats.forEach { chat ->
                        if (chat.user1.userId == userData.value?.userId) {
                            currentConnections.add(chat.user2.userId)
                        } else {
                            currentConnections.add(chat.user1.userId)
                        }
                    }
                    db.collection(COLLECTION_STATUS)
                        .whereGreaterThan("timestamp", cutoff)
                        .whereIn("user.userId", currentConnections)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                handleException(error, "Cannot retrieve statuses")
                            }
                            if (value != null) {
                                status.value = value.toObjects()
                            }
                            inProgressStatus.value = false
                        }
                }
            }
    }


}