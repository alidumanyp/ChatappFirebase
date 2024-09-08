package com.aliduman.chatappfirebase.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.chatappfirebase.CAViewModel
import com.aliduman.chatappfirebase.CommonProgressSpinner
import com.aliduman.chatappfirebase.CommonRow
import com.aliduman.chatappfirebase.DestinationScreen
import com.aliduman.chatappfirebase.TitleText
import com.aliduman.chatappfirebase.navigateTo

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatListScreen(
    navController: NavController,
    vm: CAViewModel
) {
    val inProgress = vm.inProgress.value

    if (inProgress) {
        CommonProgressSpinner()
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember { mutableStateOf(false) }
        val onFabClicked = { showDialog.value = true }
        val onDismiss = { showDialog.value = false }
        val onAddChat = { number: String ->
            showDialog.value = false
            vm.onAddChat(number)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime),
            floatingActionButton = {
                FAB(
                    showDialog = showDialog.value,
                    onFabClicked = onFabClicked,
                    onDismiss = onDismiss,
                    onAddChat = onAddChat
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                TitleText(text = "Chats")

                if (chats.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "No chats available")
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(chats) { chat ->
                            val chatUser =
                                if (chat.user1.userId == userData?.userId) chat.user2 else chat.user1
                            CommonRow(
                                imageUrl = chatUser.imageUrl ?: "",
                                name = chatUser.name ?: ""
                            ) {
                                chat.chatId?.let { id ->
                                    navigateTo(
                                        navController = navController,
                                        dest = DestinationScreen.SingleChat.createRoute(id)
                                    )
                                }

                            }
                        }
                    }


                }

                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.CHATLIST,
                    navController = navController
                )

            }
        }
    }

}

@Composable
fun FAB(
    showDialog: Boolean,
    onFabClicked: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
                addChatNumber.value = ""
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddChat(addChatNumber.value)
                        addChatNumber.value = ""
                    }
                ) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(text = "Chat Number") },
                    placeholder = { Text(text = "Chat Number") },
                    singleLine = true
                )
            }
        )
    }

    FloatingActionButton(
        onClick = onFabClicked,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add", tint = Color.White)
    }

}