package chat.simplex.common.views.newchat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import chat.simplex.common.model.*
import chat.simplex.common.platform.*
import chat.simplex.common.views.helpers.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Android proof of concept for Laravel Directory API.
 *
 * Device registration in the existing UITP web account remains unchanged.
 * UUID is created once on this device and must match the Messenger UUID
 * entered there. No additional tokens or account login are involved.
 */
@Composable
fun UITPNumberPoC(rhId: Long?, close: () -> Unit) {
  if (!appPlatform.isAndroid) {
    Text("Connection by number is available on Android only.")
    return
  }

  val currentUser = chatModel.currentUser.value
  val clipboard = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  val installationUuid = remember { UITPDirectoryPlatform.installationUuid() }
  val serverUrl = UITPDirectoryPlatform.serverUrl()

  var addressLoading by remember(rhId, currentUser?.userId) { mutableStateOf(false) }
  var addressError by remember(rhId, currentUser?.userId) { mutableStateOf<String?>(null) }
  var addressAttempt by remember(rhId, currentUser?.userId) { mutableStateOf(0) }
  val ownAddress = chatModel.userAddress.value?.connLinkContact?.simplexChatUri(short = true)

  var ownNumber by remember(rhId, currentUser?.userId) { mutableStateOf<String?>(null) }
  var registrationLoading by remember { mutableStateOf(false) }
  var registrationError by remember { mutableStateOf<String?>(null) }
  var registrationAttempt by remember { mutableStateOf(0) }

  var lookupNumber by rememberSaveable { mutableStateOf("") }
  var lookupLoading by remember { mutableStateOf(false) }
  var lookupError by remember { mutableStateOf<String?>(null) }

  // Reuse the core helper: create only when the address is confirmed missing.
  LaunchedEffect(rhId, currentUser?.userId, addressAttempt) {
    addressError = null
    when {
      currentUser == null -> addressError = "No active SimpleX profile."
      chatModel.userAddress.value != null -> Unit
      else -> {
        addressLoading = true
        withBGApi {
          try {
            val address = chatModel.controller.apiEnsureUserAddress(rhId)
            if (address != null) {
              chatModel.userAddress.value = address
            } else if (chatModel.userAddress.value == null) {
              addressError = "Could not create the SimpleX address. Check connection and retry."
            }
          } catch (e: Exception) {
            addressError = "Could not create the SimpleX address. Check connection and retry."
          } finally {
            addressLoading = false
          }
        }
      }
    }
  }

  // Once the address exists, publish it automatically using the registered
  // device UUID. Laravel keeps the same number for repeated registrations.
  LaunchedEffect(rhId, currentUser?.userId, ownAddress, installationUuid, serverUrl, registrationAttempt) {
    ownNumber = null
    registrationError = null
    if (currentUser == null || ownAddress == null || !validUITPServerUrl(serverUrl)) return@LaunchedEffect

    registrationLoading = true
    try {
      val response = uitpDirectoryRegister(serverUrl, installationUuid, ownAddress)
      ownNumber = response.number
      registrationError = response.error
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      registrationError = "Unable to reach Directory API: " + (e.message ?: "network error")
    } finally {
      registrationLoading = false
    }
  }

  val validLookup = Regex("^[1-9][0-9]{9}$").matches(lookupNumber)

  ColumnWithScrollBar {
    Column(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        "Connect by number",
        style = MaterialTheme.typography.h5
      )

      Text("My UITP number")

      if (addressLoading || registrationLoading) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          CircularProgressIndicator(
            Modifier.size(18.dp),
            strokeWidth = 2.dp
          )
          Text("Preparing your number...")
        }
      }

      ownNumber?.let { number ->
        Text(
          number,
          style = MaterialTheme.typography.h5
        )

        OutlinedButton(
          onClick = {
            clipboard.setText(AnnotatedString(number))
          }
        ) {
          Text("Copy my number")
        }
      }

      if (addressError != null) {
        Text(
          "Could not prepare your number.",
          color = MaterialTheme.colors.error
        )

        OutlinedButton(
          enabled = !addressLoading,
          onClick = { addressAttempt += 1 }
        ) {
          Text("Retry")
        }
      }

      if (registrationError != null) {
        Text(
          "Could not retrieve your number. Check your Messenger access and connection.",
          color = MaterialTheme.colors.error
        )

        OutlinedButton(
          enabled = !registrationLoading && ownAddress != null,
          onClick = { registrationAttempt += 1 }
        ) {
          Text("Retry")
        }
      }

      if (!validUITPServerUrl(serverUrl)) {
        Text(
          "Service unavailable.",
          color = MaterialTheme.colors.error
        )
      }

      Divider()

      OutlinedTextField(
        value = lookupNumber,
        onValueChange = {
          lookupNumber = it.filter { c ->
            c in '0'..'9'
          }.take(10)

          lookupError = null
        },
        label = { Text("Enter UITP number") },
        singleLine = true,
        enabled = !lookupLoading,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = validLookup &&
          !lookupLoading &&
          validUITPServerUrl(serverUrl),
        onClick = {
          val requestedNumber = lookupNumber
          val requestedUuid = installationUuid
          val requestedServer = serverUrl

          lookupLoading = true
          lookupError = null

          scope.launch {
            try {
              val response = uitpDirectoryResolve(
                requestedServer,
                requestedUuid,
                requestedNumber
              )

              if (lookupNumber == requestedNumber &&
                  installationUuid == requestedUuid &&
                  serverUrl == requestedServer) {

                val address = response.contactAddress

                if (response.number == requestedNumber &&
                    address != null) {
                  withBGApi {
                    planAndConnect(
                      rhId,
                      address,
                      close = close
                    )
                  }
                } else {
                  lookupError = "Could not connect to this number. Check the number and your Messenger access."
                }
              }
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              lookupError = "Connection failed. Please try again."
            } finally {
              lookupLoading = false
            }
          }
        }
      ) {
        if (lookupLoading) {
          CircularProgressIndicator(
            Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colors.onPrimary
          )
          Spacer(Modifier.width(8.dp))
        }

        Text(
          if (lookupLoading) "Connecting..." else "Connect"
        )
      }

      lookupError?.let { error ->
        Text(
          error,
          color = MaterialTheme.colors.error
        )
      }
    }
  }
}
