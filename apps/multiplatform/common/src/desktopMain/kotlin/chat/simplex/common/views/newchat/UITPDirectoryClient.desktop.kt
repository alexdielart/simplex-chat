package chat.simplex.common.views.newchat

/**
 * UITP number PoC is Android-only for now. Keep the common screen compilable
 * on desktop without introducing networking dependencies to the desktop app.
 */
internal actual object UITPDirectoryPlatform {
  actual fun installationUuid(): String = ""
  actual fun setInstallationUuid(uuid: String) = Unit
  actual fun serverUrl(): String = UITP_DIRECTORY_DEFAULT_URL
  actual fun setServerUrl(url: String) = Unit

  actual suspend fun postJson(
    baseUrl: String,
    route: String,
    jsonBody: String
  ): UITPDirectoryHttpResponse =
    UITPDirectoryHttpResponse(501, """{"detail":"UITP Directory PoC is only available on Android."}""")
}
