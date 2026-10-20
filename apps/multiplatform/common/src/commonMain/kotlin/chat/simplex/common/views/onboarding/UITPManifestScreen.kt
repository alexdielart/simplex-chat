package chat.simplex.common.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.stringResource

// UITP Connect manifesto from the supplied 390 x 844 onboarding design.
// This screen is opened only by the link on UITPWelcomeScreen.
private val manifestBackground = Color(0xFF0B0B12)
private val manifestText = Color(0xFFF2F2F5)
private val manifestSecondary = Color(0xFFA0A0B0)
private val manifestPurple = Color(0xFF8B5CFF)
private val manifestPink = Color(0xFFE5479B)

@Composable
fun UITPManifestScreen(onBack: () -> Unit, onGetStarted: () -> Unit) {
  val backLabel = stringResource(MR.strings.uitp_manifest_back)

  Column(
    Modifier.fillMaxSize().background(manifestBackground).systemBarsPadding()
  ) {
    // Back arrow: existing modal is dismissed; no onboarding data is changed.
    Row(
      Modifier.fillMaxWidth().padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        Modifier.size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color.White.copy(alpha = 0.06f))
          .clickable(role = Role.Button, onClick = onBack)
          .semantics { contentDescription = backLabel },
        contentAlignment = Alignment.Center
      ) {
        Canvas(Modifier.size(20.dp)) {
          val lineColor = manifestText
          val stroke = 2.2.dp.toPx()
          drawLine(lineColor, Offset(size.width * 0.65f, size.height * 0.16f), Offset(size.width * 0.32f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
          drawLine(lineColor, Offset(size.width * 0.32f, size.height * 0.50f), Offset(size.width * 0.65f, size.height * 0.84f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
      }
    }

    // Text is scrollable independently from the fixed bottom CTA.
    // UITP_CTA_MIDPOINT_V1
    UITPCenteredActionLayout(
      modifier = Modifier.fillMaxWidth().weight(1f),
      action = {
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
          Box(
            Modifier.fillMaxWidth().height(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Brush.linearGradient(listOf(manifestPurple, manifestPink)))
              .clickable(role = Role.Button, onClick = onGetStarted),
            contentAlignment = Alignment.Center
          ) {
            Text(
              stringResource(MR.strings.get_started),
              color = manifestText,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    ) {
      Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)) {

        Text(
          stringResource(MR.strings.uitp_manifest_title),
          color = manifestText,
          fontSize = 34.sp,
          lineHeight = 40.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))
        ManifestParagraph(stringResource(MR.strings.uitp_manifest_p1), manifestSecondary)
        Spacer(Modifier.height(16.dp))
        ManifestParagraph(stringResource(MR.strings.uitp_manifest_p2), manifestSecondary)
        Spacer(Modifier.height(16.dp))
        ManifestParagraph(stringResource(MR.strings.uitp_manifest_p3), manifestText)
        Spacer(Modifier.height(16.dp))
        ManifestParagraph(stringResource(MR.strings.uitp_manifest_p4), manifestSecondary)
            }
    }
  }
}

@Composable
private fun ManifestParagraph(value: String, color: Color) {
  Text(value, color = color, fontSize = 16.sp, lineHeight = 23.sp)
}
