package chat.simplex.common.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AccessStatus(val wireValue: String) {
  ACTIVE("active"),
  PENDING("pending"),
  DISABLED("disabled"),
}

data class AccessCheckResponse(
  val status: AccessStatus,
  val nextCheckAfterSeconds: Long,
)

object AccessClient {
  private const val TAG = "SimpleXAccess"
  private const val ACCESS_CHECK_URL = "https://access.uitp.date/v1/access/check"
  private const val DEFAULT_CHECK_INTERVAL_SECONDS = 300L

  private val client = OkHttpClient.Builder()
    .callTimeout(10, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun checkAccess(installationId: String): AccessCheckResponse? = withContext(Dispatchers.IO) {
    val json = """{"installation_id":"$installationId"}"""
    val body = json.toRequestBody(jsonMediaType)

    val request = Request.Builder()
      .url(ACCESS_CHECK_URL)
      .post(body)
      .build()

    try {
      client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
          Log.w(TAG, "Access check failed: HTTP ${response.code}")
          null
        } else {
          val responseJson = JSONObject(responseBody)

          val status = when (responseJson.getString("status").lowercase()) {
            "active" -> AccessStatus.ACTIVE
            "pending" -> AccessStatus.PENDING
            "disabled", "inactive" -> AccessStatus.DISABLED
            else -> throw IllegalArgumentException("Unknown access status")
          }

          val nextCheckAfter = responseJson
            .optLong("next_check_after", DEFAULT_CHECK_INTERVAL_SECONDS)
            .coerceAtLeast(30L)

          Log.w(
            TAG,
            "Access check response: status=$status, next_check_after=$nextCheckAfter"
          )

          AccessCheckResponse(status, nextCheckAfter)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Access check failed: ${e.message}")
      null
    }
  }
}
