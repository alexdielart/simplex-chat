package chat.simplex.common.views.newchat

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
class UITPOwnNumberState {
  var ownNumber by mutableStateOf<String?>(null)
  var addressLoading by mutableStateOf(false)
  var registrationLoading by mutableStateOf(false)
  var addressError by mutableStateOf<String?>(null)
  var registrationError by mutableStateOf<String?>(null)
  var addressAttempt by mutableStateOf(0)
  var registrationAttempt by mutableStateOf(0)
}

@Composable
fun rememberUITPOwnNumber(rhId: Long?): UITPOwnNumberState {
  val currentUser = chatModel.currentUser.value
  val installationUuid = remember { UITPDirectoryPlatform.installationUuid() }
  val serverUrl = UITPDirectoryPlatform.serverUrl()
  val ownAddress = chatModel.userAddress.value?.connLinkContact?.simplexChatUri(short = true)

  val state = remember(rhId, currentUser?.userId, installationUuid, serverUrl) {
    UITPOwnNumberState()
  }

  LaunchedEffect(state, state.addressAttempt) {
    state.addressError = null

    if (currentUser != null && chatModel.userAddress.value == null) {
      state.addressLoading = true

      withBGApi {
        try {
          val address = chatModel.controller.apiEnsureUserAddress(rhId)

          if (chatModel.currentUser.value?.userId == currentUser.userId &&
              chatModel.currentRemoteHost.value?.remoteHostId == rhId) {
            if (address != null) {
              chatModel.userAddress.value = address
            } else if (chatModel.userAddress.value == null) {
              state.addressError = "Address unavailable"
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          state.addressError = "Address unavailable"
        } finally {
          state.addressLoading = false
        }
      }
    }
  }

  LaunchedEffect(state, ownAddress, state.registrationAttempt) {
    state.ownNumber = null
    state.registrationError = null

    if (currentUser == null || ownAddress == null ||
        !validUITPServerUrl(serverUrl)) return@LaunchedEffect

    state.registrationLoading = true

    try {
      val response = uitpDirectoryRegister(
        serverUrl, installationUuid, ownAddress
      )

      state.ownNumber = response.number
      state.registrationError = response.error
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      state.registrationError = "Directory unavailable"
    } finally {
      state.registrationLoading = false
    }
  }

  return state
}

@Composable
fun UITPOwnNumberRow(state: UITPOwnNumberState) {
  val clipboard = LocalClipboardManager.current
  val number = state.ownNumber

  var copyAttempt by remember(number) { mutableStateOf(0) }

  LaunchedEffect(number, copyAttempt) {
    if (copyAttempt != 0) {
      delay(2000)
      copyAttempt = 0
    }
  }

  Row(
    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        stringResource(MR.strings.uitp_my_number),
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.secondary
      )

      if (number != null) {
        Text(
          number,
          style = MaterialTheme.typography.h6,
          color = MaterialTheme.colors.primary
        )
      } else if (state.addressLoading || state.registrationLoading) {
        Text(
          stringResource(MR.strings.uitp_preparing_number),
          style = MaterialTheme.typography.caption
        )
      } else {
        Text(
          if (state.addressError != null || state.registrationError != null)
            stringResource(MR.strings.uitp_number_unavailable)
          else "—",
          style = MaterialTheme.typography.body2
        )
      }
    }

    if (number != null) {
      IconButton(onClick = {
        clipboard.setText(AnnotatedString(number))
        copyAttempt += 1
      }) {
        Icon(
          painterResource(
            if (copyAttempt != 0) MR.images.ic_check
            else MR.images.ic_content_copy
          ),
          contentDescription = stringResource(
            if (copyAttempt != 0) MR.strings.uitp_number_copied
            else MR.strings.uitp_copy_number
          ),
          tint = MaterialTheme.colors.primary
        )
      }
    } else if (!state.addressLoading && !state.registrationLoading) {
      TextButton(onClick = {
        if (chatModel.userAddress.value == null) {
          state.addressAttempt += 1
        } else {
          state.registrationAttempt += 1
        }
      }) {
        Text(stringResource(MR.strings.uitp_number_retry))
      }
    }
  }

  Divider()
}

@Composable
fun UITPNumberPoC(
  rhId: Long?,
  close: () -> Unit,
  sharedNumberState: UITPOwnNumberState? = null
) {
  if (!appPlatform.isAndroid) {
    Text(stringResource(MR.strings.uitp_number_android_only))
    return
  }

  val clipboard = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val installationUuid = remember { UITPDirectoryPlatform.installationUuid() }
  val serverUrl = UITPDirectoryPlatform.serverUrl()

  val numberState = sharedNumberState ?: rememberUITPOwnNumber(rhId)

  val ownAddress = chatModel.userAddress.value?.connLinkContact?.simplexChatUri(short = true)
  val ownNumber = numberState.ownNumber
  val addressLoading = numberState.addressLoading
  val registrationLoading = numberState.registrationLoading
  val addressError = numberState.addressError
  val registrationError = numberState.registrationError

  var lookupNumber by rememberSaveable { mutableStateOf("") }
  var lookupLoading by remember { mutableStateOf(false) }
  var lookupError by remember { mutableStateOf<StringResource?>(null) }

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
          onClick = { numberState.addressAttempt += 1 }
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
          onClick = { numberState.registrationAttempt += 1 }
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
