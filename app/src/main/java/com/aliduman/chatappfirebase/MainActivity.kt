package com.aliduman.chatappfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliduman.chatappfirebase.ui.ChatListScreen
import com.aliduman.chatappfirebase.ui.LoginScreen
import com.aliduman.chatappfirebase.ui.ProfileScreen
import com.aliduman.chatappfirebase.ui.SignupScreen
import com.aliduman.chatappfirebase.ui.SingleChatScreen
import com.aliduman.chatappfirebase.ui.SingleStatusScreen
import com.aliduman.chatappfirebase.ui.StatusListScreen
import com.aliduman.chatappfirebase.ui.theme.ChatappFirebaseTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(val route: String) {
    object Singup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(chatId: String) = "singleChat/$chatId"
    }

    object StatusList : DestinationScreen("statusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}") {
        fun createRoute(userId: String?) = "singleStatus/$userId"
    }

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatappFirebaseTheme {
                ChatAppNavigation()
            }
        }
    }
}

@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    val vm = hiltViewModel<CAViewModel>()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.Singup.route) {
        composable(route = DestinationScreen.Singup.route) {
            SignupScreen(navController = navController, vm = vm)
        }
        composable(route = DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }
        composable(route = DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, vm = vm)
        }
        composable(route = DestinationScreen.ChatList.route) {
            ChatListScreen(navController = navController, vm = vm)
        }
        composable(route = DestinationScreen.StatusList.route) {
            StatusListScreen(navController = navController, vm = vm)

        }
        composable(route = DestinationScreen.SingleChat.route) {
            val chatId = it.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(navController = navController, vm = vm, chatId = it)
            }
        }
        composable(route = DestinationScreen.SingleStatus.route) {
            val userId = it.arguments?.getString("userId")
            userId?.let {
                SingleStatusScreen(navController = navController, vm = vm, userId = it)
            }
        }

    }

}



