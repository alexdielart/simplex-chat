package chat.simplex.common.views.newchat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
