package chat.simplex.common.views.newchat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
    Text("UITP Directory PoC is available on Android only.")
    return
  }

  val currentUser = chatModel.currentUser.value
  val clipboard = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  var installationUuid by remember { mutableStateOf(UITPDirectoryPlatform.installationUuid()) }
  var uuidInput by rememberSaveable { mutableStateOf(installationUuid) }
  var serverUrl by remember { mutableStateOf(UITPDirectoryPlatform.serverUrl()) }
  var serverInput by rememberSaveable { mutableStateOf(serverUrl) }
  var configError by remember { mutableStateOf<String?>(null) }

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
  var resolvedNumber by remember { mutableStateOf<String?>(null) }
  var resolvedAddress by remember { mutableStateOf<String?>(null) }

  // Preserve the existing PoC behavior: create an address only when missing.
  LaunchedEffect(rhId, currentUser?.userId, addressAttempt) {
    addressError = null
    when {
      currentUser == null -> addressError = "No active SimpleX profile."
      chatModel.userAddress.value != null -> Unit
      else -> {
        addressLoading = true
        withBGApi {
          try {
            val createdAddress = chatModel.controller.apiCreateUserAddress(rhId)
            if (createdAddress != null) {
              val hasShortLink = createdAddress.connShortLink != null
              chatModel.userAddress.value = UserContactLinkRec(
                createdAddress,
                shortLinkDataSet = hasShortLink,
                shortLinkLargeDataSet = hasShortLink,
                addressSettings = AddressSettings(
                  businessAddress = false,
                  autoAccept = null,
                  autoReply = null
                )
              )
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
  val canConnect = resolvedAddress != null && resolvedNumber == lookupNumber && !lookupLoading

  ColumnWithScrollBar {
    Column(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text("UITP Directory - Android PoC", style = MaterialTheme.typography.h5)
      Text("The UUID must be registered under a Messenger device in your UITP account. The server must have the Directory API deployed.")

      OutlinedTextField(
        value = serverInput,
        onValueChange = { serverInput = it },
        label = { Text("Directory server HTTPS URL") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Button(onClick = {
        val normalized = serverInput.trim().trimEnd('/')
        if (validUITPServerUrl(normalized)) {
          UITPDirectoryPlatform.setServerUrl(normalized)
          serverUrl = normalized
          registrationAttempt += 1
          resolvedNumber = null
          resolvedAddress = null
          lookupError = null
          configError = null
        } else {
          configError = "Use an HTTPS server URL, for example https://uitp.io"
        }
      }) { Text("Save server") }

      OutlinedTextField(
        value = uuidInput,
        onValueChange = { uuidInput = it.trim().take(36) },
        label = { Text("Device UUID (same as in UITP account)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
          val normalized = uuidInput.trim().lowercase()
          if (validUITPUuid(normalized)) {
            UITPDirectoryPlatform.setInstallationUuid(normalized)
            installationUuid = normalized
            registrationAttempt += 1
            resolvedNumber = null
            resolvedAddress = null
            lookupError = null
            configError = null
          } else {
            configError = "Invalid UUID: use a standard 36-character UUID."
          }
        }) { Text("Use UUID") }
        Button(onClick = {
          clipboard.setText(AnnotatedString(installationUuid))
        }) { Text("Copy UUID") }
      }

      if (configError != null) {
        Text(configError!!, color = MaterialTheme.colors.error)
      }
      Divider()

      Text("My permanent SimpleX address")
      if (addressLoading && ownAddress == null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
          Text("Preparing your contact address...")
        }
      }
      if (ownAddress != null) {
        OutlinedTextField(
          value = ownAddress,
          onValueChange = {},
          readOnly = true,
          label = { Text("SimpleX contact address") },
          maxLines = 4,
          modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { clipboard.setText(AnnotatedString(ownAddress)) }) {
          Text("Copy my address")
        }
      }
      if (addressError != null) {
        Text(addressError!!, color = MaterialTheme.colors.error)
        Button(onClick = { addressAttempt += 1 }) { Text("Retry address creation") }
      }
      Divider()

      Text("My UITP number")
      if (registrationLoading) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
          Text("Registering with Directory API...")
        }
      }
      if (ownNumber != null) {
        Text(ownNumber!!, style = MaterialTheme.typography.h5)
        Button(onClick = { clipboard.setText(AnnotatedString(ownNumber!!)) }) {
          Text("Copy my number")
        }
      }
      if (registrationError != null) {
        Text(registrationError!!, color = MaterialTheme.colors.error)
        Text("Check your UUID, Messenger access, and Directory server. An existing address cannot be replaced automatically.")
      }
      Button(
        enabled = !registrationLoading && ownAddress != null,
        onClick = { registrationAttempt += 1 }
      ) { Text("Retry registration") }
      Divider()

      OutlinedTextField(
        value = lookupNumber,
        onValueChange = {
          lookupNumber = it.filter { c -> c in '0'..'9' }.take(10)
          resolvedNumber = null
          resolvedAddress = null
          lookupError = null
        },
        label = { Text("Enter UITP number to connect") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Button(
        enabled = validLookup && !lookupLoading && validUITPServerUrl(serverUrl),
        onClick = {
          val requestedNumber = lookupNumber
          val requestedUuid = installationUuid
          val requestedServer = serverUrl
          lookupLoading = true
          lookupError = null
          resolvedNumber = null
          resolvedAddress = null
          scope.launch {
            try {
              val response = uitpDirectoryResolve(requestedServer, requestedUuid, requestedNumber)
              if (lookupNumber == requestedNumber &&
                  installationUuid == requestedUuid &&
                  serverUrl == requestedServer) {
                if (response.number == requestedNumber && response.contactAddress != null) {
                  resolvedNumber = requestedNumber
                  resolvedAddress = response.contactAddress
                } else {
                  lookupError = response.error ?: "Directory lookup failed."
                }
              }
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              lookupError = "Unable to reach Directory API: " + (e.message ?: "network error")
            } finally {
              lookupLoading = false
            }
          }
        }
      ) { Text(if (lookupLoading) "Searching..." else "Find by number") }

      if (lookupError != null) {
        Text(lookupError!!, color = MaterialTheme.colors.error)
      }
      if (canConnect) {
        Text("Contact found: " + resolvedNumber)
      }
      Button(
        enabled = canConnect,
        onClick = {
          resolvedAddress?.let { address ->
            withBGApi {
              planAndConnect(rhId, address, close = close)
            }
          }
        }
      ) { Text("Connect by number") }
      Text("After lookup, the usual SimpleX encrypted connection flow is used.")
    }
  }
}
