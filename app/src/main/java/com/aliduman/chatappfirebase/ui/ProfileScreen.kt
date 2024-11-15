package com.aliduman.chatappfirebase.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.chatappfirebase.CAViewModel
import com.aliduman.chatappfirebase.CommonDivider
import com.aliduman.chatappfirebase.CommonImage
import com.aliduman.chatappfirebase.CommonProgressSpinner
import com.aliduman.chatappfirebase.DestinationScreen
import com.aliduman.chatappfirebase.navigateTo


@Composable
fun ProfileScreen(
    navController: NavController,
    vm: CAViewModel
) {
    val inProgress = vm.inProgress.value


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        if (inProgress) {
            CommonProgressSpinner()
        } else {
            val userData = vm.userData.value
            var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
            var number by rememberSaveable { mutableStateOf(userData?.number ?: "") }

            val scrollState = rememberScrollState()
            val focusManager = LocalFocusManager.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                ProfileContent(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(8.dp),
                    vm = vm,
                    name = name,
                    number = number,
                    onNameChanged = { name = it },
                    onNumberChanged = { number = it },
                    onSave = {
                        vm.updateProfileData(name, number)
                        focusManager.clearFocus()
                    },
                    onBack = {
                        navigateTo(navController, DestinationScreen.ChatList.route)
                        focusManager.clearFocus()
                    },
                    onLogout = {
                        vm.onLogout()
                        navigateTo(navController, DestinationScreen.Login.route)
                    }
                )

                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.PROFILE,
                    navController = navController
                )
            }

        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: CAViewModel,
    name: String,
    number: String,
    onNameChanged: (String) -> Unit,
    onNumberChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = vm.userData.value?.imageUrl

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    onBack.invoke()
                }
            ) {
                Text(text = "Back")
            }

            TextButton(
                onClick = {
                    onSave.invoke()
                }
            ) {
                Text(text = "Save")
            }
        }

        CommonDivider()

        ProfileImage(imageUrl, vm)

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChanged,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Number", modifier = Modifier.width(100.dp))
            TextField(
                value = number,
                onValueChange = onNumberChanged,
            )
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", modifier = Modifier.clickable { onLogout.invoke() })
        }

    }
}


@Composable
fun ProfileImage(imageUri: String?, vm: CAViewModel) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                vm.uploadProfileImage(it)
            }
        }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                CommonImage(
                    data = imageUri,
                    contentScale = ContentScale.Crop // Use Crop to fill circle
                )
            }
            Text(text = "Change profile picture")
        }

        val isLoading = vm.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}

