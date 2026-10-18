package chat.simplex.common.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** UITP Connect design system. Source: UITP Connect Onboarding (offline).html. */
object UITPColors {
  val Background = Color(0xFF0B0B12)
  val Surface = Color(0xFF15151F)
  val Input = Color(0xFF1A1A24)
  val Text = Color(0xFFF2F2F5)
  val Secondary = Color(0xFFA0A0B0)
  val Muted = Color(0xFF7A7A88)
  val Purple = Color(0xFF8B5CFF)
  val Pink = Color(0xFFE5479B)
  val Link = Color(0xFFC79BFF)
  val SentMessage = Color(0xFF2E2346)
  val SentQuote = Color(0xFF473566)
  val ReceivedMessage = Color(0xFF20202B)
  val ReceivedQuote = Color(0xFF30303F)
  val Error = Color(0xFFFF6D86)

  val PrimaryGradient: Brush get() = Brush.linearGradient(listOf(Purple, Pink))
}

object UITPDimens {
  val ScreenPadding = 24.dp
  val CardRadius = 24.dp
  val SectionRadius = 16.dp
  val FieldRadius = 12.dp
  val ActionHeight = 52.dp
  val SectionGap = 12.dp
  val IconRadius = 10.dp
}
