package chat.simplex.common.views.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.stringResource

/**
 * UITP Connect onboarding / profile skin, from the supplied design (390 x 844).
 *
 * INTENTIONALLY CONTROLLED: creation/import/back callbacks must be supplied
 * by the EXISTING SimpleX profile-creation flow in WelcomeView.kt. This
 * composable does not create an account, invent an UITP ID, or alter storage.
 *
 * No simulated keyboard: Android displays the real IME on input focus.
 */
private val profileBackground = Color(0xFF0B0B12)
private val profilePanel = Color(0xFF15151F)
private val profileText = Color(0xFFF2F2F5)
private val profileSecondary = Color(0xFFA0A0B0)
private val profileMuted = Color(0xFF7A7A88)
private val profilePurple = Color(0xFF8B5CFF)
private val profilePink = Color(0xFFE5479B)
private val profileLink = Color(0xFFC79BFF)

@Composable
fun UITPProfileScreen(
  displayName: String,
  onDisplayNameChange: (String) -> Unit,
  onCreateProfile: () -> Unit,
  onTransfer: () -> Unit,
  onBack: () -> Unit,
  canSubmit: Boolean,
  creating: Boolean = false,
  modifier: Modifier = Modifier
) {
  val backLabel = stringResource(MR.strings.uitp_profile_back)
  val transferLabel = stringResource(MR.strings.uitp_profile_transfer)
  val createLabel = stringResource(MR.strings.uitp_profile_create)
  val inputLabel = stringResource(MR.strings.uitp_profile_display_name)
  val canCreate = canSubmit && !creating
  val scroll = rememberScrollState()

  Column(
    modifier.fillMaxSize()
      .background(profileBackground)
      .systemBarsPadding()
      .imePadding()
  ) {
    Row(
      Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
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
          val strokeWidth = 2.2.dp.toPx()
          drawLine(profileText, Offset(size.width * .65f, size.height * .16f), Offset(size.width * .32f, size.height * .50f), strokeWidth, cap = StrokeCap.Round)
          drawLine(profileText, Offset(size.width * .32f, size.height * .50f), Offset(size.width * .65f, size.height * .84f), strokeWidth, cap = StrokeCap.Round)
        }
      }
      Row(
        Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
          .clickable(role = Role.Button, onClick = onTransfer)
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Canvas(Modifier.size(18.dp)) {
          val s = 1.8.dp.toPx()
          drawLine(profileLink, Offset(size.width * .5f, size.height * .1f), Offset(size.width * .5f, size.height * .65f), s, cap = StrokeCap.Round)
          drawLine(profileLink, Offset(size.width * .23f, size.height * .40f), Offset(size.width * .5f, size.height * .7f), s, cap = StrokeCap.Round)
          drawLine(profileLink, Offset(size.width * .77f, size.height * .40f), Offset(size.width * .5f, size.height * .7f), s, cap = StrokeCap.Round)
          drawLine(profileLink, Offset(size.width * .15f, size.height * .87f), Offset(size.width * .85f, size.height * .87f), s, cap = StrokeCap.Round)
        }
        Text(transferLabel, color = profileLink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
      }
    }

    Column(
      Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(Modifier.height(8.dp))
      Box(
        Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
          .background(profilePanel)
          .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(Modifier.fillMaxSize()) {
          drawRect(
            brush = Brush.radialGradient(
              listOf(profilePurple.copy(alpha = .30f), Color.Transparent),
              center = Offset(size.width * .5f, size.height * .4f),
              radius = size.minDimension * .75f
            )
          )
        }
        Canvas(Modifier.size(48.dp)) {
          val stroke = 2.2.dp.toPx()
          drawCircle(profileLink, radius = size.minDimension * .18f,
            center = Offset(size.width * .5f, size.height * .29f), style = Stroke(stroke))
          drawArc(profileLink, startAngle = 198f, sweepAngle = 144f, useCenter = false,
            topLeft = Offset(size.width * .16f, size.height * .51f),
            size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .48f),
            style = Stroke(stroke, cap = StrokeCap.Round))
        }
      }
      Spacer(Modifier.height(16.dp))
      Text(
        stringResource(MR.strings.uitp_profile_title),
        color = profileText, fontSize = 28.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(10.dp))
      Text(
        stringResource(MR.strings.uitp_profile_local),
        color = profileSecondary, fontSize = 16.sp, lineHeight = 22.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 28.dp)
      )
      Spacer(Modifier.height(10.dp))
      Text(
        stringResource(MR.strings.uitp_profile_privacy).replace("\\n", "\n"),
        color = profileMuted, fontSize = 14.sp, lineHeight = 19.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 28.dp)
      )

      Spacer(Modifier.height(28.dp))
      Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Text(inputLabel, color = profileSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        val fieldShape = RoundedCornerShape(12.dp)
        BasicTextField(
          value = displayName,
          onValueChange = onDisplayNameChange,
          singleLine = true,
          enabled = !creating,
          textStyle = TextStyle(color = profileText, fontSize = 16.sp, fontFamily = chat.simplex.common.ui.theme.Inter),
          cursorBrush = Brush.linearGradient(listOf(profilePurple, profilePink)),
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { if (canCreate) onCreateProfile() }),
          modifier = Modifier.fillMaxWidth().height(48.dp)
            .clip(fieldShape)
            .background(profilePurple.copy(alpha = .08f))
            .border(1.dp, profilePurple, fieldShape)
            .padding(horizontal = 14.dp),
          decorationBox = { innerTextField ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
              if (displayName.isEmpty()) {
                Text(
                  stringResource(MR.strings.uitp_profile_hint),
                  color = profileMuted, fontSize = 16.sp
                )
              }
              innerTextField()
            }
          }
        )
      }
      Spacer(Modifier.height(16.dp))
      Box(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
          .height(52.dp).clip(RoundedCornerShape(12.dp))
          .background(if (canCreate) Brush.linearGradient(listOf(profilePurple, profilePink))
              else Brush.linearGradient(listOf(profilePurple.copy(alpha = .35f), profilePink.copy(alpha = .35f))))
          .clickable(enabled = canCreate, role = Role.Button, onClick = onCreateProfile),
        contentAlignment = Alignment.Center
      ) {
        if (creating) {
          CircularProgressIndicator(color = profileText, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
          Text(createLabel, color = profileText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
