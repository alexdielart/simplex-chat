package chat.simplex.common.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.common.AppScreen
import chat.simplex.common.model.ChatController.appPrefs
import chat.simplex.common.ui.theme.SimpleXTheme
import chat.simplex.common.views.helpers.DefaultProgressView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccessControlledAppScreen() {
  val accessState = AccessManager.uiState.collectAsState().value

  when (accessState) {
    AccessUiState.ACTIVE -> AppScreen()

    AccessUiState.CHECKING -> {
      AccessGateSurface {
        DefaultProgressView("Checking access…")
      }
    }

    AccessUiState.DISABLED -> {
      ActivationScreen(
        title = "Access is not active",
        description = "Activate your access to continue using the app."
      )
    }

    AccessUiState.PENDING -> {
      ActivationScreen(
        title = "Activation pending",
        description = "Your activation has not been confirmed yet."
      )
    }

    AccessUiState.UNAVAILABLE -> {
      AccessUnavailableScreen()
    }
  }
}

@Composable
private fun ActivationScreen(
  title: String,
  description: String
) {
  val installationId = appPrefs.installationId.get().orEmpty()
  val clipboard = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  AccessGateSurface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(12.dp))

      Text(
        text = description,
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(32.dp))

      Text(
        text = "Installation ID",
        style = MaterialTheme.typography.subtitle1
      )

      Spacer(Modifier.height(8.dp))

      SelectionContainer {
        Text(
          text = installationId,
          fontFamily = FontFamily.Monospace,
          fontSize = 14.sp,
          textAlign = TextAlign.Center
        )
      }

      Spacer(Modifier.height(12.dp))

      OutlinedButton(
        onClick = {
          if (installationId.isNotEmpty()) {
            clipboard.setText(AnnotatedString(installationId))
            copied = true

            scope.launch {
              delay(2_000)
              copied = false
            }
          }
        },
        enabled = installationId.isNotEmpty()
      ) {
        Text(if (copied) "✓ Copied" else "Copy")
      }

      Spacer(Modifier.height(24.dp))

      Button(
        onClick = { AccessManager.retry() }
      ) {
        Text("Check activation")
      }
    }
  }
}

@Composable
private fun AccessUnavailableScreen() {
  AccessGateSurface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "Unable to verify access",
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(12.dp))

      Text(
        text = "The access server is unavailable. Check your connection and try again.",
        textAlign = TextAlign.Center
      )

      Spacer(Modifier.height(24.dp))

      Button(
        onClick = { AccessManager.retry() }
      ) {
        Text("Retry")
      }
    }
  }
}

@Composable
private fun AccessGateSurface(
  content: @Composable () -> Unit
) {
  SimpleXTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colors.background,
      contentColor = MaterialTheme.colors.onBackground
    ) {
      content()
    }
  }
}