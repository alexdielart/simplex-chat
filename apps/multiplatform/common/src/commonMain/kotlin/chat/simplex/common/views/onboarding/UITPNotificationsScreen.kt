package chat.simplex.common.views.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.common.model.NotificationsMode
import chat.simplex.common.ui.theme.*
import chat.simplex.common.views.helpers.*
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// UITP Connect onboarding screen 06. Scoped to Android so Desktop keeps its own layout.
private val notificationBackground = Color(0xFF0B0B12)
private val notificationText = Color(0xFFF2F2F5)
private val notificationSecondary = Color(0xFFA0A0B0)
private val notificationPurple = Color(0xFF8B5CFF)
private val notificationPink = Color(0xFFE5479B)
private val notificationLink = Color(0xFFC79BFF)

@Composable
fun UITPNotificationsScreen(currentMode: MutableState<NotificationsMode>, onDone: () -> Unit) {
  CompositionLocalProvider(LocalAppBarHandler provides rememberAppBarHandler()) {
    ModalView({}, showClose = false) {
      // UITP_CTA_MIDPOINT_V1
      UITPCenteredActionLayout(
        modifier = Modifier.fillMaxSize().background(notificationBackground),
        action = {
          Box(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp)
              .height(52.dp).clip(RoundedCornerShape(12.dp))
              .background(Brush.linearGradient(listOf(notificationPurple, notificationPink)))
              .clickable(role = Role.Button, onClick = onDone),
            contentAlignment = Alignment.Center
          ) {
            Text(
              stringResource(MR.strings.uitp_notifications_done),
              color = notificationText, fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = MaterialTheme.typography.body1.fontFamily
            )
          }
        }
      ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {

          // Leave space for the standard onboarding back/navigation area.
          Spacer(Modifier.height(112.dp))
          Text(
            stringResource(MR.strings.onboarding_notifications_mode_title),
            fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold,
            color = notificationText, fontFamily = MaterialTheme.typography.body1.fontFamily
          )
          Spacer(Modifier.height(12.dp))
          Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
              .clickable(role = Role.Button) {
                ModalManager.fullscreen.showModalCloseable { NotificationBatteryUsageInfo() }
              },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              painterResource(MR.images.ic_info), contentDescription = null,
              modifier = Modifier.size(18.dp), tint = notificationPurple
            )
            Text(
              stringResource(MR.strings.onboarding_notifications_mode_subtitle),
              color = notificationLink, fontSize = 15.sp, lineHeight = 20.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = MaterialTheme.typography.body1.fontFamily
            )
          }
          Spacer(Modifier.height(40.dp))
          Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            UITPNotificationOption(
              selected = currentMode.value == NotificationsMode.SERVICE,
              title = stringResource(MR.strings.onboarding_notifications_mode_service),
              description = annotatedStringResource(MR.strings.onboarding_notifications_mode_service_desc_short),
              icon = painterResource(MR.images.ic_bolt)
            ) { currentMode.value = NotificationsMode.SERVICE }
            UITPNotificationOption(
              selected = currentMode.value == NotificationsMode.PERIODIC,
              title = stringResource(MR.strings.onboarding_notifications_mode_periodic),
              description = annotatedStringResource(MR.strings.onboarding_notifications_mode_periodic_desc_short),
              icon = painterResource(MR.images.ic_timer)
            ) { currentMode.value = NotificationsMode.PERIODIC }
            UITPNotificationOption(
              selected = currentMode.value == NotificationsMode.OFF,
              title = stringResource(MR.strings.onboarding_notifications_mode_off),
              description = annotatedStringResource(MR.strings.onboarding_notifications_mode_off_desc_short),
              icon = painterResource(MR.images.ic_bolt_off)
            ) { currentMode.value = NotificationsMode.OFF }
          }
                }
      }
    }
  }
}

@Composable
private fun UITPNotificationOption(
  selected: Boolean,
  title: String,
  description: AnnotatedString,
  icon: Painter,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(16.dp)
  Row(
    Modifier.fillMaxWidth().clip(shape)
      .background(if (selected) notificationPurple.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.035f))
      .border(1.dp, if (selected) notificationPurple else Color.White.copy(alpha = 0.08f), shape)
      .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    val iconShape = RoundedCornerShape(10.dp)
    Box(
      Modifier.size(40.dp).clip(iconShape).then(
        if (selected) Modifier.background(Brush.linearGradient(listOf(notificationPurple, notificationPink)))
        else Modifier.background(Color.White.copy(alpha = 0.06f))
      ),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
        tint = if (selected) notificationText else notificationSecondary)
    }
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        title, color = notificationText, fontSize = 16.sp, lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = MaterialTheme.typography.body1.fontFamily
      )
      Text(
        description, color = notificationSecondary, fontSize = 13.sp, lineHeight = 18.sp,
        fontFamily = MaterialTheme.typography.body1.fontFamily
      )
    }
    Box(
      Modifier.size(22.dp).border(2.dp,
        if (selected) notificationPurple else Color.White.copy(alpha = 0.18f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      if (selected) {
        Box(Modifier.size(10.dp).background(notificationPurple, CircleShape))
      }
    }
  }
}
