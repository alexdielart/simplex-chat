package chat.simplex.common.views.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.common.model.*
import chat.simplex.common.platform.*
import chat.simplex.common.views.helpers.*
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// UITP Connect first-run welcome screen, based on the supplied 390 x 844 design.
// Deliberately scoped to Android onboarding; other screens retain the upstream UI.
private val welcomeBackground = Color(0xFF0B0B12)
private val welcomeSurface = Color(0xFF15151F)
private val welcomeText = Color(0xFFF2F2F5)
private val welcomeSecondary = Color(0xFFA0A0B0)
private val welcomeMuted = Color(0xFF7A7A88)
private val welcomePurple = Color(0xFF8B5CFF)
private val welcomePink = Color(0xFFE5479B)
private val welcomeLink = Color(0xFFC79BFF)

@Composable
fun UITPWelcomeScreen(
  user: User?,
  onboardingStage: SharedPreference<OnboardingStage>
) {
  BoxWithConstraints(
    Modifier.fillMaxSize().background(welcomeBackground).systemBarsPadding()
  ) {
    val compact = maxHeight < 730.dp
    val heroHeight = when {
      maxHeight < 610.dp -> 180.dp
      maxHeight < 730.dp -> 245.dp
      else -> 330.dp
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
          Modifier.size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(welcomePurple, welcomePink))),
          contentAlignment = Alignment.Center
        ) {
          UITPChatsSymbol(Modifier.size(21.dp), monochrome = true)
        }
        Text("UITP Connect", color = welcomeText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(Modifier.height(if (compact) 12.dp else 18.dp))

      val heroShape = RoundedCornerShape(24.dp)
      Box(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
          .height(heroHeight)
          .clip(heroShape)
          .background(welcomeSurface)
          .border(1.dp, Color.White.copy(alpha = 0.06f), heroShape),
        contentAlignment = Alignment.Center
      ) {
        Canvas(Modifier.fillMaxSize()) {
          drawRect(
            Brush.radialGradient(
              colors = listOf(welcomePurple.copy(alpha = 0.28f), Color.Transparent),
              center = Offset(size.width * 0.5f, size.height * 0.45f),
              radius = size.minDimension * 0.69f
            )
          )
        }
        UITPChatsSymbol(Modifier.size(84.dp), monochrome = false)
      }

      Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp)
          .padding(top = if (compact) 20.dp else 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          stringResource(MR.strings.uitp_welcome_title).replace("\\n", "\n"),
          color = welcomeText,
          fontSize = if (compact) 26.sp else 28.sp,
          lineHeight = if (compact) 32.sp else 34.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
          stringResource(MR.strings.uitp_welcome_description),
          color = welcomeSecondary,
          fontSize = 16.sp,
          lineHeight = 22.sp,
          textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
          stringResource(MR.strings.uitp_welcome_network),
          color = welcomeMuted,
          fontSize = 14.sp,
          lineHeight = 19.sp,
          textAlign = TextAlign.Center
        )
      }

      Spacer(Modifier.weight(1f))

      Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          Modifier.fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(welcomePurple, welcomePink)))
            .clickable(role = Role.Button) {
              onboardingStage.set(OnboardingStage.Step2_CreateProfile)
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            stringResource(MR.strings.get_started),
            color = welcomeText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
      Spacer(Modifier.weight(1f))
    }
  }
}

// Vector chats icon authored for this screen. No external font or image is required.
@Composable
private fun UITPChatsSymbol(modifier: Modifier, monochrome: Boolean) {
  Canvas(modifier) {
    val w = size.width
    val h = size.height
    val fill = if (monochrome) SolidColor(welcomeText)
      else Brush.linearGradient(listOf(welcomePurple, welcomePink), start = Offset.Zero, end = Offset(w, h))

    val rear = Path().apply {
      moveTo(w * 0.20f, h * 0.10f)
      lineTo(w * 0.66f, h * 0.10f)
      cubicTo(w * 0.79f, h * 0.10f, w * 0.86f, h * 0.18f, w * 0.86f, h * 0.30f)
      lineTo(w * 0.86f, h * 0.54f)
      cubicTo(w * 0.86f, h * 0.67f, w * 0.78f, h * 0.73f, w * 0.66f, h * 0.73f)
      lineTo(w * 0.33f, h * 0.73f)
      lineTo(w * 0.13f, h * 0.88f)
      lineTo(w * 0.17f, h * 0.68f)
      cubicTo(w * 0.10f, h * 0.64f, w * 0.07f, h * 0.57f, w * 0.07f, h * 0.47f)
      lineTo(w * 0.07f, h * 0.25f)
      cubicTo(w * 0.07f, h * 0.16f, w * 0.12f, h * 0.10f, w * 0.20f, h * 0.10f)
      close()
    }
    drawPath(rear, fill, alpha = if (monochrome) 1f else 0.75f)

    val front = Path().apply {
      moveTo(w * 0.44f, h * 0.35f)
      lineTo(w * 0.79f, h * 0.35f)
      cubicTo(w * 0.90f, h * 0.35f, w * 0.96f, h * 0.42f, w * 0.96f, h * 0.52f)
      lineTo(w * 0.96f, h * 0.70f)
      cubicTo(w * 0.96f, h * 0.80f, w * 0.90f, h * 0.85f, w * 0.80f, h * 0.85f)
      lineTo(w * 0.73f, h * 0.85f)
      lineTo(w * 0.89f, h * 0.98f)
      lineTo(w * 0.62f, h * 0.85f)
      lineTo(w * 0.44f, h * 0.85f)
      cubicTo(w * 0.34f, h * 0.85f, w * 0.29f, h * 0.79f, w * 0.29f, h * 0.70f)
      lineTo(w * 0.29f, h * 0.51f)
      cubicTo(w * 0.29f, h * 0.41f, w * 0.34f, h * 0.35f, w * 0.44f, h * 0.35f)
      close()
    }
    drawPath(front, fill)
  }
}
