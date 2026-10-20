package chat.simplex.common.views.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// UITP Connect, design reference o8 (empty chat list).
// Visual-only Android component. Actual SimpleX actions are passed from ChatListView.
private val introBackground = Color(0xFF0B0B12)
private val introSurface = Color(0xFF15151F)
private val introText = Color(0xFFF2F2F5)
private val introPurple = Color(0xFF8B5CFF)
private val introPink = Color(0xFFE5479B)

@Composable
internal fun UITPScreen8Content(
  onShareLink: () -> Unit,
  onConnectViaLinkOrQr: () -> Unit
) {
  Column(
    Modifier.fillMaxSize()
      .background(introBackground)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.Top
  ) {
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(MR.strings.talk_to_someone),
      color = introText,
      fontSize = 34.sp,
      lineHeight = 40.sp,
      fontWeight = FontWeight.SemiBold,
      maxLines = 2
    )
    Spacer(Modifier.height(24.dp))
    UITPScreen8Card(
      label = stringResource(MR.strings.let_someone_connect_to_you),
      icon = painterResource(MR.images.ic_add_link),
      glow = introPurple.copy(alpha = 0.28f),
      onClick = onShareLink
    )
    Spacer(Modifier.height(14.dp))
    UITPScreen8Card(
      label = stringResource(MR.strings.connect_via_link_or_qr_code),
      icon = painterResource(MR.images.ic_qr_code_scanner),
      glow = introPink.copy(alpha = 0.22f),
      onClick = onConnectViaLinkOrQr
    )
    Spacer(Modifier.height(24.dp))
  }
}

@Composable
private fun UITPScreen8Card(
  label: String,
  icon: Painter,
  glow: Color,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(24.dp)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(introSurface)
      .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
      .clickable(role = Role.Button, onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .background(
          Brush.radialGradient(
            colors = listOf(glow, Color.Transparent),
            center = Offset.Unspecified,
            radius = 260f
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier
          .size(64.dp)
          .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
          .drawWithCache {
            val gradient = Brush.linearGradient(listOf(introPurple, introPink))
            onDrawWithContent {
              drawContent()
              drawRect(brush = gradient, blendMode = BlendMode.SrcIn)
            }
          }
      )
    }
    Text(
      text = label,
      color = introText,
      fontSize = 17.sp,
      lineHeight = 23.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Start,
      modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 18.dp)
    )
  }
}
