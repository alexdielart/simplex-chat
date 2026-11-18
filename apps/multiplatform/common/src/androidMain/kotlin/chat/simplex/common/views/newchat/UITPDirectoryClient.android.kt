package chat.simplex.common.views.newchat

import chat.simplex.common.platform.androidAppContext
import chat.simplex.common.model.ChatController.appPrefs
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual object UITPDirectoryPlatform {
  private const val PREFS_NAME = "uitp_directory_poc"
  private const val KEY_SERVER = "server_url"

  private val prefs get() = androidAppContext.getSharedPreferences(PREFS_NAME, 0)

  // The same UUID used by the existing Android access check.
  // It is initialized once on SimplexApp startup and never replaced by Directory.
  actual fun installationUuid(): String =
    checkNotNull(appPrefs.installationId.get()) { "Installation UUID is not initialized." }

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
