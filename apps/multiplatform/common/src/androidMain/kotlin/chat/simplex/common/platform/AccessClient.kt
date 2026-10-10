package chat.simplex.common.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object AccessClient {
  private const val TAG = "SimpleXAccess"
  private const val ACCESS_CHECK_URL = "http://85.215.166.59/v1/access/check"

  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun checkAccess(installationId: String) = withContext(Dispatchers.IO) {
    val json = """{"installation_id":"$installationId"}"""
    val body = json.toRequestBody(jsonMediaType)

    val request = Request.Builder()
      .url(ACCESS_CHECK_URL)
      .post(body)
      .build()

    try {
      client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string().orEmpty()

        Log.w(
          TAG,
          "Access check response: HTTP ${response.code}, body=$responseBody"
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Access check failed: ${e.message}")
    }
  }
}