package chat.simplex.app

import chat.simplex.common.views.newchat.UITPDirectoryHttpResponse
import chat.simplex.common.views.newchat.parseUITPDirectoryResponse
import chat.simplex.common.views.newchat.validUITPServerUrl
import chat.simplex.common.views.newchat.validUITPUuid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UITPDirectoryClientTest {
  @Test fun validUuidAndHttpsServer() {
    assertTrue(validUITPUuid("fb6fa892-9638-4e78-ac2b-8d0bdb08f712"))
    assertFalse(validUITPUuid("not-a-uuid"))
    assertTrue(validUITPServerUrl("https://uitp.io"))
    assertTrue(validUITPServerUrl("https://staging.example.com:8443"))
    assertFalse(validUITPServerUrl("http://uitp.io"))
    assertFalse(validUITPServerUrl("https://uitp.io/path"))
  }

  @Test fun validRegistrationOrLookupResponse() {
    val result = parseUITPDirectoryResponse(UITPDirectoryHttpResponse(
      200,
      """{"number":"5831049276","contact_address":"https://simplex.chat/contact#/?v=2&smp=abc"}"""
    ))
    assertEquals("5831049276", result.number)
    assertEquals("https://simplex.chat/contact#/?v=2&smp=abc", result.contactAddress)
    assertEquals(null, result.error)
  }

  @Test fun rejectsMalformedResponse() {
    assertEquals(
      "Directory API returned an invalid response.",
      parseUITPDirectoryResponse(UITPDirectoryHttpResponse(200, """{"status":"ok"}""")).error
    )
  }

  @Test fun returnsLaravelError() {
    assertEquals(
      "A contact address is already registered for this device.",
      parseUITPDirectoryResponse(UITPDirectoryHttpResponse(
        409, """{"detail":"A contact address is already registered for this device."}"""
      )).error
    )
    assertEquals(
      "Directory server returned HTTP 404",
      parseUITPDirectoryResponse(UITPDirectoryHttpResponse(404, "<html>not found</html>")).error
    )
  }
}
