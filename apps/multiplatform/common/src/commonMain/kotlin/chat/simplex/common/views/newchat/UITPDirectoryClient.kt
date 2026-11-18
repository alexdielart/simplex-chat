package chat.simplex.common.views.newchat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Temporary PoC endpoint. Configurable on the screen so a staging server can be used. */
internal const val UITP_DIRECTORY_DEFAULT_URL = "https://uitp.io"

internal data class UITPDirectoryHttpResponse(val status: Int, val body: String)

internal data class UITPDirectoryResponse(
  val number: String? = null,
  val contactAddress: String? = null,
  val error: String? = null
)

/**
 * The existing Laravel installation UUID is the only credential for this PoC.
 * A locally generated UUID must be entered into the account's Messenger device
 * list before registration; an already registered UUID can be pasted instead.
 */
internal expect object UITPDirectoryPlatform {
  fun installationUuid(): String
  fun setInstallationUuid(uuid: String)
  fun serverUrl(): String
  fun setServerUrl(url: String)
  suspend fun postJson(baseUrl: String, route: String, jsonBody: String): UITPDirectoryHttpResponse
}

internal fun validUITPUuid(text: String): Boolean =
  Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    .matches(text)

internal fun validUITPServerUrl(text: String): Boolean =
  Regex("^https://(?:[A-Za-z0-9.-]+|\\[[0-9a-fA-F:]+\\])(?::[0-9]{1,5})?$")
    .matches(text)

internal fun parseUITPDirectoryResponse(response: UITPDirectoryHttpResponse): UITPDirectoryResponse {
  val body = runCatching { Json.parseToJsonElement(response.body).jsonObject }.getOrNull()
  fun field(name: String): String? = runCatching {
    body?.get(name)?.jsonPrimitive?.contentOrNull
  }.getOrNull()

  if (response.status in 200..299) {
    val number = field("number")
    val address = field("contact_address")
    if (number != null && Regex("^[1-9][0-9]{9}$").matches(number) && !address.isNullOrBlank()) {
      return UITPDirectoryResponse(number = number, contactAddress = address)
    }
    return UITPDirectoryResponse(error = "Directory API returned an invalid response.")
  }

  return UITPDirectoryResponse(
    error = field("detail") ?: field("message") ?: "Directory server returned HTTP " + response.status
  )
}

internal suspend fun uitpDirectoryRegister(
  baseUrl: String,
  installationUuid: String,
  contactAddress: String
): UITPDirectoryResponse {
  val json = buildJsonObject {
    put("installation_id", installationUuid)
    put("contact_address", contactAddress)
  }.toString()
  return parseUITPDirectoryResponse(
    UITPDirectoryPlatform.postJson(baseUrl, "/v1/directory/register", json)
  )
}

internal suspend fun uitpDirectoryResolve(
  baseUrl: String,
  installationUuid: String,
  number: String
): UITPDirectoryResponse {
  val json = buildJsonObject {
    put("installation_id", installationUuid)
    put("number", number)
  }.toString()
  return parseUITPDirectoryResponse(
    UITPDirectoryPlatform.postJson(baseUrl, "/v1/directory/resolve", json)
  )
}
