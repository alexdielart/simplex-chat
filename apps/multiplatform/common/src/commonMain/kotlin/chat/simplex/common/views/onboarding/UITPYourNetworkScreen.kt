package chat.simplex.common.views.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.stringResource

/**
 * UITP Connect / step 04, Your Network. Visual-only composable.
 * Callbacks MUST be wired to the original SimpleX onboarding handlers.
 * This component never changes onboardingStage and never selects servers.
 */
private val networkBg = Color(0xFF0B0B12)
private val networkPanel = Color(0xFF15151F)
private val networkText = Color(0xFFF2F2F5)
private val networkMuted = Color(0xFFA0A0B0)
private val networkPurple = Color(0xFF8B5CFF)
private val networkPink = Color(0xFFE5479B)
private val networkHighlight = Color(0xFFC79BFF)
private val networkGradient = Brush.linearGradient(listOf(networkPurple, networkPink))

@Composable
fun UITPYourNetworkScreen(
  onConfigureServers: () -> Unit,
  onConfigureNotifications: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  // UITP_CTA_MIDPOINT_V1
  UITPCenteredActionLayout(
    modifier = modifier.fillMaxSize().background(networkBg).systemBarsPadding(),
    action = {
      Box(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
          .height(52.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(networkGradient)
          .clickable(role = Role.Button, onClick = onContinue),
        contentAlignment = Alignment.Center
      ) {
        Text(
          stringResource(MR.strings.uitp_network_continue),
          color = networkText,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  ) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

      Spacer(Modifier.height(20.dp))
      BoxWithConstraints(Modifier.fillMaxWidth()) {
        // 340dp is the height in the 390 x 844 reference; shrink on small phones.
        val artHeight = if (maxHeight < 540.dp) 242.dp else 320.dp
        Box(
          Modifier.fillMaxWidth().height(artHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(networkPanel)
            .background(
              Brush.radialGradient(
                0.0f to networkPurple.copy(alpha = 0.28f),
                0.66f to Color.Transparent,
                center = Offset.Unspecified,
                radius = 650f
              )
            )
            .border(1.dp, Color.White.copy(alpha = .06f), RoundedCornerShape(24.dp)),
          contentAlignment = Alignment.Center
        ) {
          NetworkServerArtwork()
        }
      }
      Spacer(Modifier.height(30.dp))
      Text(
        text = stringResource(MR.strings.uitp_network_title),
        color = networkText,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(12.dp))
      Text(
        text = stringResource(MR.strings.uitp_network_subtitle),
        color = networkMuted,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(22.dp))
      NetworkSettingRow(
        title = stringResource(MR.strings.uitp_network_configure_notifications),
        iconIsServer = false,
        onClick = onConfigureNotifications
      )
    }
  }
}

@Composable
private fun NetworkSettingRow(title: String, iconIsServer: Boolean, onClick: () -> Unit) {
  val shape = RoundedCornerShape(12.dp)
  Row(
    Modifier.fillMaxWidth().height(52.dp)
      .clip(shape)
      .background(Color.White.copy(alpha = .035f))
      .border(1.dp, Color.White.copy(alpha = .06f), shape)
      .clickable(role = Role.Button, onClick = onClick)
      .padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Canvas(Modifier.size(20.dp)) {
      val w = size.width
      val h = size.height
      val sw = 1.7.dp.toPx()
      if (iconIsServer) {
        for (j in 0..1) {
          drawRoundRect(
            color = networkHighlight,
            topLeft = Offset(w * .13f, h * (.13f + j * .36f)),
            size = Size(w * .74f, h * .29f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
            style = Stroke(width = sw)
          )
          drawCircle(networkHighlight, radius = w * .045f, center = Offset(w * .25f, h * (.275f + j * .36f)))
        }
      } else {
        val p = androidx.compose.ui.graphics.Path().apply {
          moveTo(w * .56f, h * .04f)
          lineTo(w * .21f, h * .55f)
          lineTo(w * .49f, h * .55f)
          lineTo(w * .39f, h * .96f)
          lineTo(w * .80f, h * .40f)
          lineTo(w * .54f, h * .40f)
          close()
        }
        drawPath(path = p, color = networkHighlight, style = Stroke(width = sw, cap = StrokeCap.Round))
      }
    }
    Spacer(Modifier.width(12.dp))
    Text(title, color = networkText, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    Canvas(Modifier.size(16.dp)) {
      val sw = 1.6.dp.toPx()
      drawLine(Color(0xFF7A7A88), Offset(size.width * .38f, size.height * .22f), Offset(size.width * .67f, size.height * .50f), sw, cap = StrokeCap.Round)
      drawLine(Color(0xFF7A7A88), Offset(size.width * .67f, size.height * .50f), Offset(size.width * .38f, size.height * .78f), sw, cap = StrokeCap.Round)
    }
  }
}

/** Three stacked server trays, recreated as Compose vector drawing. */
@Composable
private fun NetworkServerArtwork() {
  Canvas(Modifier.size(100.dp)) {
    val w = size.width
    val h = size.height
    val sw = 3.dp.toPx()
    for (i in 0..2) {
      val y = h * (.13f + i * .29f)
      drawRoundRect(
        brush = networkGradient,
        topLeft = Offset(w * .12f, y),
        size = Size(w * .76f, h * .22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
        style = Stroke(width = sw)
      )
      drawCircle(
        brush = networkGradient,
        radius = w * .045f,
        center = Offset(w * .24f, y + h * .11f)
      )
      drawLine(
        brush = networkGradient,
        start = Offset(w * .52f, y + h * .11f),
        end = Offset(w * .76f, y + h * .11f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
      )
    }
  }
}
