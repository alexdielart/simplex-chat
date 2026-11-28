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
import kotlinx.coroutines.delay
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.compose.painterResource
import chat.simplex.res.MR

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
    Text(stringResource(MR.strings.uitp_number_android_only))
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
  var lookupError by remember { mutableStateOf<StringResource?>(null) }

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

  var copyAttempt by remember(ownNumber) { mutableStateOf(0) }

  LaunchedEffect(ownNumber, copyAttempt) {
    if (copyAttempt != 0) {
      delay(2000)
      copyAttempt = 0
    }
  }

  val validLookup = Regex("^[1-9][0-9]{9}$").matches(lookupNumber)

  ColumnWithScrollBar {
    Column(
      Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        stringResource(MR.strings.uitp_connect_by_number),
        style = MaterialTheme.typography.h5
      )

      Text(stringResource(MR.strings.uitp_my_number))

      if (addressLoading || registrationLoading) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          CircularProgressIndicator(
            Modifier.size(18.dp),
            strokeWidth = 2.dp
          )
          Text(stringResource(MR.strings.uitp_preparing_number))
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
            copyAttempt += 1
          }
        ) {
          if (copyAttempt != 0) {
            Icon(
              painterResource(MR.images.ic_check),
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(MR.strings.uitp_number_copied))
          } else {
            Text(stringResource(MR.strings.uitp_copy_number))
          }
        }
      }

      if (addressError != null) {
        Text(
          stringResource(MR.strings.uitp_prepare_number_failed),
          color = MaterialTheme.colors.error
        )

        OutlinedButton(
          enabled = !addressLoading,
          onClick = { addressAttempt += 1 }
        ) {
          Text(stringResource(MR.strings.uitp_number_retry))
        }
      }

      if (registrationError != null) {
        Text(
          stringResource(MR.strings.uitp_retrieve_number_failed),
          color = MaterialTheme.colors.error
        )

        OutlinedButton(
          enabled = !registrationLoading && ownAddress != null,
          onClick = { registrationAttempt += 1 }
        ) {
          Text(stringResource(MR.strings.uitp_number_retry))
        }
      }

      if (!validUITPServerUrl(serverUrl)) {
        Text(
          stringResource(MR.strings.uitp_number_unavailable),
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
        label = { Text(stringResource(MR.strings.uitp_enter_number)) },
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
                  lookupError = MR.strings.uitp_lookup_failed
                }
              }
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              lookupError = MR.strings.uitp_connection_failed
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
          if (lookupLoading) stringResource(MR.strings.uitp_number_connecting) else stringResource(MR.strings.uitp_number_connect)
        )
      }

      lookupError?.let { error ->
        Text(
          stringResource(error),
          color = MaterialTheme.colors.error
        )
      }
    }
  }
}
