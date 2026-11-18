package chat.simplex.common.views.newchat

import chat.simplex.common.platform.androidAppContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual object UITPDirectoryPlatform {
  private const val PREFS_NAME = "uitp_directory_poc"
  private const val KEY_UUID = "installation_uuid"
  private const val KEY_SERVER = "server_url"

  private val prefs get() = androidAppContext.getSharedPreferences(PREFS_NAME, 0)

  actual fun installationUuid(): String = synchronized(this) {
    val existing = prefs.getString(KEY_UUID, null)
    if (existing != null && validUITPUuid(existing)) {
      existing
    } else {
      UUID.randomUUID().toString().also {
        prefs.edit().putString(KEY_UUID, it).apply()
      }
    }
  }

  actual fun setInstallationUuid(uuid: String) {
    require(validUITPUuid(uuid))
    prefs.edit().putString(KEY_UUID, uuid.lowercase()).apply()
  }

  actual fun serverUrl(): String =
    prefs.getString(KEY_SERVER, UITP_DIRECTORY_DEFAULT_URL) ?: UITP_DIRECTORY_DEFAULT_URL

  actual fun setServerUrl(url: String) {
    require(validUITPServerUrl(url))
    prefs.edit().putString(KEY_SERVER, url).apply()
  }

  actual suspend fun postJson(
    baseUrl: String,
    route: String,
    jsonBody: String
  ): UITPDirectoryHttpResponse = withContext(Dispatchers.IO) {
    require(validUITPServerUrl(baseUrl)) { "Directory server must use HTTPS." }

    val connection = URL(baseUrl + route).openConnection() as HttpURLConnection
    try {
      connection.requestMethod = "POST"
      connection.instanceFollowRedirects = false
      connection.connectTimeout = 10000
      connection.readTimeout = 10000
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
      connection.setRequestProperty("Accept", "application/json")

      connection.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }

      val status = connection.responseCode
      val stream = if (status in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
      UITPDirectoryHttpResponse(status, body)
    } finally {
      connection.disconnect()
    }
  }
}
