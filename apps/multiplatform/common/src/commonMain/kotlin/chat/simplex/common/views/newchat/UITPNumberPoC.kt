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

/**
 * Temporary local-only number-to-address resolver for the Android proof of concept.
 * No UUID, database, directory API or permanent settings are involved.
 * Delete this screen when real Directory API lookup is available.
 */
@Composable
fun UITPNumberPoC(rhId: Long?, close: () -> Unit) {
  var setupNumber by rememberSaveable { mutableStateOf("1000000001") }
  var setupAddress by rememberSaveable { mutableStateOf("") }
  var lookupNumber by rememberSaveable { mutableStateOf("") }
  var mappedNumber by remember { mutableStateOf<String?>(null) }
  var mappedAddress by remember { mutableStateOf<String?>(null) }

  val currentUser = chatModel.currentUser.value
  val clipboard = LocalClipboardManager.current
  var addressLoading by remember(rhId, currentUser?.userId) { mutableStateOf(false) }
  var addressError by remember(rhId, currentUser?.userId) { mutableStateOf<String?>(null) }
  var addressAttempt by remember(rhId, currentUser?.userId) { mutableStateOf(0) }
  val ownAddress = chatModel.userAddress.value?.connLinkContact?.simplexChatUri(short = true)

  // ChatController loads an existing address into chatModel.userAddress with the active profile.
  // Create one only when there is no address; never replace an existing address.
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

  val canSave = setupNumber.length == 10 &&
    setupNumber.all { it in '0'..'9' } && setupAddress.trim().isNotEmpty()
  val canConnect = mappedAddress != null && lookupNumber == mappedNumber

  ColumnWithScrollBar {
    Column(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text("UITP numbers - PoC", style = MaterialTheme.typography.h5)
      Text("Local test only. Paste the permanent SimpleX contact address from the other phone. The mapping exists only while this screen stays open.")

      Divider()
      Text("My SimpleX address (loaded or created automatically)")
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
          label = { Text("My permanent contact address") },
          maxLines = 4,
          modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { clipboard.setText(AnnotatedString(ownAddress)) }) {
          Text("Copy my address")
        }
      }
      if (addressError != null) {
        Text(addressError!!, color = MaterialTheme.colors.error)
        Button(onClick = { addressAttempt += 1 }) { Text("Retry") }
      }
      Divider()

      OutlinedTextField(
        value = setupNumber,
        onValueChange = { setupNumber = it.filter { c -> c in '0'..'9' }.take(10) },
        label = { Text("Test 10-digit number") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = setupAddress,
        onValueChange = { setupAddress = it },
        label = { Text("SimpleX contact address from other phone") },
        modifier = Modifier.fillMaxWidth()
      )
      Button(
        enabled = canSave,
        onClick = {
          mappedNumber = setupNumber
          mappedAddress = setupAddress.trim()
          lookupNumber = ""
        }
      ) { Text("Save test mapping") }
      Text(if (mappedNumber != null) "Temporary mapping saved for $mappedNumber" else "No test mapping saved")

      Divider()
      OutlinedTextField(
        value = lookupNumber,
        onValueChange = { lookupNumber = it.filter { c -> c in '0'..'9' }.take(10) },
        label = { Text("Enter number to connect") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      Button(
        enabled = canConnect,
        onClick = {
          mappedAddress?.let { address ->
            withBGApi {
              planAndConnect(rhId, address, close = close)
            }
          }
        }
      ) { Text("Connect by number") }
      Text("This checks the existing SimpleX contact flow. It does not assign or resolve real UITP numbers.")
    }
  }
}
