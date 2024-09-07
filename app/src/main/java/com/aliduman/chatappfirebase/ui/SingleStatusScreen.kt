package com.aliduman.chatappfirebase.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SingleStatusScreen(statusId: String) {


    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Text(text = "single status screen $statusId")

        }
    }

}