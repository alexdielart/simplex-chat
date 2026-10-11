package chat.simplex.common.platform

import chat.simplex.common.model.ChatController.appPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AccessManager {
  private const val TAG = "SimpleXAccess"
  private const val STARTUP_ATTEMPTS = 10
  private const val STARTUP_RETRY_DELAY_MS = 5_000L
  private const val DEFAULT_CHECK_INTERVAL_SECONDS = 300L

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var monitoringJob: Job? = null

  fun start() {
    monitoringJob?.cancel()

    monitoringJob = scope.launch {
      val installationId = appPrefs.installationId.get()

      if (installationId == null) {
        Log.e(TAG, "Installation ID is missing")
        return@launch
      }

      val startupResponse = checkAtStartup(installationId)

      if (startupResponse != null) {
        saveConfirmedStatus(startupResponse.status)

        if (startupResponse.status == AccessStatus.ACTIVE) {
          runPeriodicChecks(
            installationId,
            startupResponse.nextCheckAfterSeconds
          )
        } else {
          Log.w(TAG, "Periodic access checks stopped: status=${startupResponse.status}")
        }
      } else {
        val lastKnownStatus = getLastKnownStatus()

        Log.w(
          TAG,
          "Startup access check timed out; keeping last known status=$lastKnownStatus"
        )

        if (lastKnownStatus == null || lastKnownStatus == AccessStatus.ACTIVE) {
          runPeriodicChecks(
            installationId,
            DEFAULT_CHECK_INTERVAL_SECONDS
          )
        }
      }
    }
  }

  private suspend fun checkAtStartup(
    installationId: String
  ): AccessCheckResponse? {
    repeat(STARTUP_ATTEMPTS) { attempt ->
      Log.w(TAG, "Startup access check attempt ${attempt + 1}/$STARTUP_ATTEMPTS")

      val response = AccessClient.checkAccess(installationId)

      if (response != null) {
        return response
      }

      if (attempt < STARTUP_ATTEMPTS - 1) {
        delay(STARTUP_RETRY_DELAY_MS)
      }
    }

    return null
  }

  private suspend fun runPeriodicChecks(
    installationId: String,
    initialIntervalSeconds: Long
  ) {
    var intervalSeconds = initialIntervalSeconds

    while (currentCoroutineContext().isActive) {
      delay(intervalSeconds * 1_000)

      val response = AccessClient.checkAccess(installationId)

      if (response == null) {
        Log.w(TAG, "Periodic access check failed; keeping last known status")
        continue
      }

      saveConfirmedStatus(response.status)

      if (response.status != AccessStatus.ACTIVE) {
        Log.w(TAG, "Periodic access checks stopped: status=${response.status}")
        return
      }

      intervalSeconds = response.nextCheckAfterSeconds
    }
  }

  private fun saveConfirmedStatus(status: AccessStatus) {
    appPrefs.accessLastKnownStatus.set(status.wireValue)
    Log.w(TAG, "Confirmed access status saved: $status")
  }

  private fun getLastKnownStatus(): AccessStatus? =
    when (appPrefs.accessLastKnownStatus.get()) {
      AccessStatus.ACTIVE.wireValue -> AccessStatus.ACTIVE
      AccessStatus.PENDING.wireValue -> AccessStatus.PENDING
      AccessStatus.DISABLED.wireValue -> AccessStatus.DISABLED
      else -> null
    }
}