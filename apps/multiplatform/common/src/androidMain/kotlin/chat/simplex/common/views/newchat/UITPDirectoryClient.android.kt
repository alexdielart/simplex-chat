package chat.simplex.common.views.newchat

import chat.simplex.common.model.ChatController.appPrefs
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual object UITPDirectoryPlatform {
  // The same UUID used by the existing Android access check.
  // It is initialized once on SimplexApp startup and never replaced by Directory.
  actual fun installationUuid(): String =
    checkNotNull(appPrefs.installationId.get()) { "Installation UUID is not initialized." }

  // Always use production, even if an earlier PoC APK saved a staging URL.
  actual fun serverUrl(): String = UITP_DIRECTORY_DEFAULT_URL

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
