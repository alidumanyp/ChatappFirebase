package com.aliduman.chatappfirebase.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.chatappfirebase.CAViewModel
import com.aliduman.chatappfirebase.CommonImage

enum class ProgressIndicatorState {
    INITIAL, ACTIVE, COMPLETE
}

@Composable
fun SingleStatusScreen(
    userId: String, vm: CAViewModel, navController: NavController
) {
    val status = vm.status.value.filter { it.user?.userId == userId }

    if (status.isNotEmpty()) {
        val currentStatus = remember { mutableIntStateOf(0) }
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                CommonImage(
                    data = status[currentStatus.intValue].imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    status.forEachIndexed { index, singleStatus ->
                        CustomProgressIndicator(
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .padding(1.dp),
                            state = if (currentStatus.intValue < index) ProgressIndicatorState.INITIAL else if (currentStatus.intValue == index) ProgressIndicatorState.ACTIVE else ProgressIndicatorState.COMPLETE,
                        ) {
                            if (currentStatus.intValue < status.size - 1) {
                                currentStatus.intValue++
                            } else {
                                navController.popBackStack()
                            }
                        }
                    }

                }
            }
        }
    }

}

@Composable
fun CustomProgressIndicator(
    modifier: Modifier,
    state: ProgressIndicatorState,
    onCompleted: () -> Unit,
) {
    var progressIndicator = if (state == ProgressIndicatorState.INITIAL) 0f else 1f
    if (state == ProgressIndicatorState.ACTIVE) {
        val toggleState = remember { mutableStateOf(false) }

        LaunchedEffect(toggleState) {
            toggleState.value = true
        }

        val p: Float by animateFloatAsState(
            if (toggleState.value) 1f else 0f,
            animationSpec = tween(5000),
            finishedListener = {
                onCompleted.invoke()
            },
        )

        progressIndicator = p
    }

    LinearProgressIndicator(
        progress = progressIndicator,
        modifier = modifier,
        color = Color.Red,
    )


}