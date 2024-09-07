package com.aliduman.chatappfirebase.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.chatappfirebase.CAViewModel
import com.aliduman.chatappfirebase.CommonDivider
import com.aliduman.chatappfirebase.CommonImage
import com.aliduman.chatappfirebase.data.Message

@Composable
fun SingleChatScreen(
    navController: NavController,
    vm: CAViewModel,
    chatId: String,
) {

    LaunchedEffect(key1 = Unit) {
        vm.populateChat(chatId)
    }

    BackHandler {
        vm.dePopulateChat()
        navController.popBackStack()
    }

    var reply by rememberSaveable { mutableStateOf("") }
    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }
    val chatMessages = vm.chatMessages.value

    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val myUser = vm.userData.value
    val otherUser = if (currentChat.user1.userId == myUser?.userId) {
        currentChat.user2
    } else {
        currentChat.user1
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ChatHeader(name = otherUser.name ?: "", imageUrl = otherUser.imageUrl ?: "") {
                navController.popBackStack()
                vm.dePopulateChat()
            }

            Messages(
                modifier = Modifier.weight(1f),
                chatMessages = chatMessages,
                currentUserId = myUser?.userId ?: ""
            )

            ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)

        }
    }

}


@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "back arrow"
            )
        }
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

    }

}

@Composable
fun Messages(modifier: Modifier, chatMessages: List<Message>, currentUserId: String) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sentBy == currentUserId) {
                Alignment.End
            } else {
                Alignment.Start
            }
            val color = if (msg.sentBy == currentUserId) {
                Color.Blue
            } else {
                Color.Gray
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp)
                )

            }

        }

    }

}

@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
            Button(onClick = onSendReply) {
                Text(text = "Send")
            }

        }

    }
}