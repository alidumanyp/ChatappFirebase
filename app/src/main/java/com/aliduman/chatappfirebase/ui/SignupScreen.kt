package com.aliduman.chatappfirebase.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aliduman.chatappfirebase.CAViewModel
import com.aliduman.chatappfirebase.CheckSignedIn
import com.aliduman.chatappfirebase.CommonProgressSpinner
import com.aliduman.chatappfirebase.DestinationScreen
import com.aliduman.chatappfirebase.R
import com.aliduman.chatappfirebase.navigateTo

@Composable
fun SignupScreen(navController: NavController, vm: CAViewModel) {
    CheckSignedIn(vm = vm, navController = navController)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val nameState = remember { mutableStateOf(TextFieldValue()) }
                val numberState = remember { mutableStateOf(TextFieldValue()) }
                val emailState = remember { mutableStateOf(TextFieldValue()) }
                val passwordState = remember { mutableStateOf(TextFieldValue()) }

                val focus = LocalFocusManager.current

                Image(
                    painter = painterResource(id = R.drawable.chat),
                    contentDescription = "Chat App Logo",
                    modifier = Modifier
                        .width(200.dp)
                        .padding(top = 16.dp)
                        .padding(8.dp)
                )

                Text(
                    text = "Signup",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif
                )

                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    modifier = Modifier.padding(8.dp),
                    label = { Text(text = "Name") })

                OutlinedTextField(
                    value = numberState.value,
                    onValueChange = { numberState.value = it },
                    modifier = Modifier.padding(8.dp),
                    label = { Text(text = "Number") })

                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    modifier = Modifier.padding(8.dp),
                    label = { Text(text = "Email") })

                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    modifier = Modifier.padding(8.dp),
                    label = { Text(text = "Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        focus.clearFocus(force = true)
                        vm.onSignUp(
                            name = nameState.value.text,
                            number = numberState.value.text,
                            email = emailState.value.text,
                            password = passwordState.value.text
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "SIGN UP")
                }

                Text(text = "Already a user? Go to login ->",
                    color = Color.Blue,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { navigateTo(navController, DestinationScreen.Login.route) }
                )
            }

            val isLoading = vm.inProgress.value
            if (isLoading) {
                CommonProgressSpinner()
            }

        }
    }

}