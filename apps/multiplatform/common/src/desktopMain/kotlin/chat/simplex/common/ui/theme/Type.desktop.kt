package chat.simplex.common.ui.theme

import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.platform.Font
import chat.simplex.common.platform.desktopPlatform
import chat.simplex.res.MR

// Kept as "Inter" for Kotlin API compatibility. Font resources are Golos Text.
actual val Inter: FontFamily = FontFamily(
  Font(MR.fonts.GolosText.regular.file, FontWeight.Normal),
  Font(MR.fonts.GolosText.regular.file, FontWeight.Medium),
  Font(MR.fonts.GolosText.regular.file, FontWeight.SemiBold),
  Font(MR.fonts.GolosText.regular.file, FontWeight.Bold)
)

actual val EmojiFont: FontFamily = if (desktopPlatform.isMac()) {
  FontFamily.Default
} else {
  FontFamily(
    Font(MR.fonts.NotoColorEmoji.regular.file),
    Font(MR.fonts.NotoColorEmoji.regular.file, style = FontStyle.Italic),
    Font(MR.fonts.NotoColorEmoji.regular.file, FontWeight.Bold),
    Font(MR.fonts.NotoColorEmoji.regular.file, FontWeight.SemiBold),
    Font(MR.fonts.NotoColorEmoji.regular.file, FontWeight.Medium),
    Font(MR.fonts.NotoColorEmoji.regular.file, FontWeight.Light)
  )
}
