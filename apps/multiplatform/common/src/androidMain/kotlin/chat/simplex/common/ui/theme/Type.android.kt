package chat.simplex.common.ui.theme

import androidx.compose.ui.text.font.*
import chat.simplex.res.MR

// Kept as "Inter" for Kotlin API compatibility. Font resources are Golos Text.
actual val Inter: FontFamily = FontFamily(
  Font(MR.fonts.GolosText.regular.fontResourceId, FontWeight.Normal),
  Font(MR.fonts.GolosText.regular.fontResourceId, FontWeight.Medium),
  Font(MR.fonts.GolosText.regular.fontResourceId, FontWeight.SemiBold),
  Font(MR.fonts.GolosText.regular.fontResourceId, FontWeight.Bold)
)

actual val EmojiFont: FontFamily = FontFamily.Default
