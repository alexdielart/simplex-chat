package chat.simplex.common.views.onboarding

import SectionBottomSpacer
import SectionDividerSpaced
import SectionItemView
import SectionTextFooter
import SectionView
import TextIconSpaced
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.common.BuildConfigCommon
import chat.simplex.common.model.*
import chat.simplex.common.model.ChatController.appPrefs
import chat.simplex.common.platform.*
import chat.simplex.common.ui.theme.*
import chat.simplex.common.views.helpers.*
import chat.simplex.common.views.newchat.darkStops
import chat.simplex.common.views.newchat.gradientPoints
import chat.simplex.common.views.newchat.lightStops
import chat.simplex.common.views.usersettings.networkAndServers.*
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// UITP_COMMITMENTS_SCREEN_V2_BEGIN
private const val UITP_LEGAL_URL = "https://uitp.io/legal"
private val uitpCommitmentsBackground = Color(0xFF0B0B12)
private val uitpCommitmentsSurface = Color(0xFF15151F)
private val uitpCommitmentsText = Color(0xFFF2F2F5)
private val uitpCommitmentsMuted = Color(0xFF7A7A88)
private val uitpCommitmentsPurple = Color(0xFF8B5CFF)
private val uitpCommitmentsPink = Color(0xFFE5479B)
private val uitpCommitmentsLink = Color(0xFFC79BFF)

@Composable
fun OnboardingConditionsView(chatModel: ChatModel) {
  val uriHandler = LocalUriHandler.current
  LaunchedEffect(Unit) {
    prepareChatBeforeFinishingOnboarding()
  }

  val serverOperators = remember { derivedStateOf { chatModel.conditions.value.serverOperators } }
  val selectedOperatorIds = remember {
    mutableStateOf(OnboardingSharedState.selectedOperatorIds.ifEmpty {
      serverOperators.value.filter { it.enabled }.map { it.operatorId }.toSet()
    })
  }

  if (appPlatform.isDesktop) {
    OnboardingConditionsDesktop(selectedOperatorIds)
  } else {
    CompositionLocalProvider(LocalAppBarHandler provides rememberAppBarHandler()) {
      ModalView({}, showClose = false, showAppBar = false, cardScreen = true) {
        BoxWithConstraints(
          Modifier.fillMaxSize().background(uitpCommitmentsBackground).systemBarsPadding()
        ) {
          val compact = maxHeight < 750.dp
          val heroHeight = when {
            maxHeight < 620.dp -> 130.dp
            maxHeight < 750.dp -> 170.dp
            else -> 200.dp
          }

          // UITP_CTA_MIDPOINT_V1
          UITPCenteredActionLayout(
            modifier = Modifier.fillMaxSize(),
            action = {
              Box(
                Modifier.fillMaxWidth()
                  .padding(horizontal = 24.dp)
                  .height(52.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (selectedOperatorIds.value.isNotEmpty()) {
                      Brush.linearGradient(listOf(uitpCommitmentsPurple, uitpCommitmentsPink))
                    } else {
                      Brush.linearGradient(listOf(uitpCommitmentsMuted, uitpCommitmentsMuted))
                    }
                  )
                  .clickable(enabled = selectedOperatorIds.value.isNotEmpty(), role = Role.Button) {
                    acceptConditions(selectedOperatorIds.value)
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  stringResource(MR.strings.onboarding_conditions_accept),
                  color = uitpCommitmentsText,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {

              Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
              UITPCommitmentsHero(Modifier.fillMaxWidth().height(heroHeight))
              Spacer(Modifier.height(if (compact) 18.dp else 24.dp))
              Text(
                stringResource(MR.strings.onboarding_network_commitments),
                color = uitpCommitmentsText,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(Modifier.height(18.dp))
              UITPCommitmentsList(stringResource(MR.strings.onboarding_conditions_private_chats_not_accessible))
              UITPCommitmentsList(stringResource(MR.strings.onboarding_conditions_by_using_you_agree))
              Spacer(Modifier.height(18.dp))
              Text(
                stringResource(MR.strings.onboarding_conditions_privacy_policy_and_conditions_of_use),
                color = uitpCommitmentsLink,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                  uriHandler.openUriCatching(UITP_LEGAL_URL)
                }
              )
                        }
          }
        }
      }
    }
  }
}

@Composable
private fun UITPCommitmentsHero(modifier: Modifier = Modifier) {
  val shape = RoundedCornerShape(24.dp)
  Box(
    modifier.clip(shape)
      .background(uitpCommitmentsSurface)
      .drawBehind {
        drawRect(
          Brush.radialGradient(
            colors = listOf(uitpCommitmentsPurple.copy(alpha = 0.28f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.68f
          )
        )
      }
      .border(1.dp, Color.White.copy(alpha = 0.06f), shape),
    contentAlignment = Alignment.Center
  ) {
    Canvas(Modifier.size(76.dp)) {
      val w = size.width
      val h = size.height
      val shield = Path().apply {
        moveTo(w * 0.50f, h * 0.06f)
        lineTo(w * 0.84f, h * 0.20f)
        lineTo(w * 0.84f, h * 0.49f)
        cubicTo(w * 0.84f, h * 0.71f, w * 0.70f, h * 0.87f, w * 0.50f, h * 0.96f)
        cubicTo(w * 0.30f, h * 0.87f, w * 0.16f, h * 0.71f, w * 0.16f, h * 0.49f)
        lineTo(w * 0.16f, h * 0.20f)
        close()
      }
      val gradient = Brush.linearGradient(
        listOf(uitpCommitmentsPurple, uitpCommitmentsPink),
        start = Offset.Zero,
        end = Offset(w, h)
      )
      drawPath(shield, brush = gradient, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
      drawLine(
        brush = gradient,
        start = Offset(w * 0.34f, h * 0.51f),
        end = Offset(w * 0.46f, h * 0.63f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
      )
      drawLine(
        brush = gradient,
        start = Offset(w * 0.46f, h * 0.63f),
        end = Offset(w * 0.68f, h * 0.39f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
      )
    }
  }
}

@Composable
private fun UITPCommitmentsList(rawText: String) {
  val lines = remember(rawText) {
    rawText.replace("\\n", "\n").lines()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
  }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      lines.firstOrNull().orEmpty().trimEnd(':').uppercase(),
      color = uitpCommitmentsMuted,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      letterSpacing = 0.8.sp,
      fontWeight = FontWeight.SemiBold
    )
    lines.drop(1).forEach { bullet ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
      ) {
        Canvas(Modifier.size(16.dp)) {
          drawLine(
            color = uitpCommitmentsLink,
            start = Offset(size.width * 0.18f, size.height * 0.53f),
            end = Offset(size.width * 0.39f, size.height * 0.76f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
          drawLine(
            color = uitpCommitmentsLink,
            start = Offset(size.width * 0.39f, size.height * 0.76f),
            end = Offset(size.width * 0.82f, size.height * 0.25f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
        }
        Text(
          bullet.removePrefix("-").trimStart(),
          color = uitpCommitmentsText,
          fontSize = 15.sp,
          lineHeight = 20.sp,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}
// UITP_COMMITMENTS_SCREEN_V2_END

@Composable
private fun OnboardingConditionsDesktop(selectedOperatorIds: MutableState<Set<Long>>) {
  val uriHandler = LocalUriHandler.current
  CompositionLocalProvider(LocalAppBarHandler provides rememberAppBarHandler()) {
    ModalView({}, showClose = false, cardScreen = true) {
      ColumnWithScrollBar(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxHeight().padding(horizontal = DEFAULT_PADDING).align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
          Box(Modifier.align(Alignment.CenterHorizontally)) {
            AppBarTitle(stringResource(MR.strings.onboarding_network_commitments), bottomPadding = DEFAULT_PADDING, withPadding = false, overrideTitleColor = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center, lineHeight = 42.sp)
          }
          Column(Modifier.width(450.dp), horizontalAlignment = Alignment.Start) {
            ReadableText(MR.strings.onboarding_conditions_private_chats_not_accessible, TextAlign.Start, padding = PaddingValues(), style = MaterialTheme.typography.body1)
            Spacer(Modifier.height(DEFAULT_PADDING))
            ReadableText(MR.strings.onboarding_conditions_by_using_you_agree, TextAlign.Start, padding = PaddingValues(), style = MaterialTheme.typography.body1)
            Spacer(Modifier.height(DEFAULT_PADDING))
            Text(
              stringResource(MR.strings.onboarding_conditions_privacy_policy_and_conditions_of_use),
              style = MaterialTheme.typography.body1,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colors.primary,
              modifier = Modifier
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                ) {
                  uriHandler.openUriCatching(UITP_LEGAL_URL)
                }
            )
          }
        }
        Spacer(Modifier.fillMaxHeight().weight(1f))
        Column(Modifier.widthIn(max = 1000.dp).align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
          AcceptConditionsButton(enabled = selectedOperatorIds.value.isNotEmpty(), selectedOperatorIds)
          TextButtonBelowOnboardingButton("", null)
        }
      }
    }
  }
}

@Composable
fun ModalData.ChooseServerOperators(
  serverOperators: State<List<ServerOperator>>,
  selectedOperatorIds: MutableState<Set<Long>>,
  close: (() -> Unit)
) {
  LaunchedEffect(Unit) {
    prepareChatBeforeFinishingOnboarding()
  }
  CompositionLocalProvider(LocalAppBarHandler provides rememberAppBarHandler()) {
    ModalView(close, enableClose = selectedOperatorIds.value.isNotEmpty(), cardScreen = true) {
      ColumnWithScrollBar(
        Modifier
          .themedBackground(bgLayerSize = LocalAppBarHandler.current?.backgroundGraphicsLayerSize, bgLayer = LocalAppBarHandler.current?.backgroundGraphicsLayer),
        maxIntrinsicSize = true
      ) {
        Box(Modifier.align(Alignment.CenterHorizontally)) {
          AppBarTitle(stringResource(MR.strings.onboarding_choose_server_operators), bottomPadding = DEFAULT_PADDING)
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = DEFAULT_PADDING), horizontalAlignment = Alignment.CenterHorizontally) {
          OnboardingInformationButton(
            stringResource(MR.strings.how_it_helps_privacy),
            onClick = { ModalManager.fullscreen.showModal { ChooseServerOperatorsInfoView() } }
          )
        }

        Spacer(Modifier.weight(1f))
        Column((
            if (appPlatform.isDesktop) Modifier.width(600.dp).align(Alignment.CenterHorizontally) else Modifier)
          .fillMaxWidth()
          .padding(horizontal = DEFAULT_ONBOARDING_HORIZONTAL_PADDING),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          serverOperators.value.forEachIndexed { index, srvOperator ->
            OperatorCheckView(srvOperator, selectedOperatorIds)
            if (index != serverOperators.value.lastIndex) {
              Spacer(Modifier.height(DEFAULT_PADDING))
            }
          }
          Spacer(Modifier.height(DEFAULT_PADDING_HALF))

          SectionTextFooter(annotatedStringResource(MR.strings.onboarding_network_operators_simplex_flux_agreement), textAlign = TextAlign.Center)
          SectionTextFooter(annotatedStringResource(MR.strings.onboarding_network_operators_configure_via_settings), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))

        Column(Modifier.widthIn(max = if (appPlatform.isAndroid) 450.dp else 1000.dp).padding(bottom = DEFAULT_PADDING * 2).align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
          val enabled = selectedOperatorIds.value.isNotEmpty()
          SetOperatorsButton(enabled, close)
        }
      }
    }
  }
}

@Composable
private fun OperatorCheckView(serverOperator: ServerOperator, selectedOperatorIds: MutableState<Set<Long>>) {
  val checked = selectedOperatorIds.value.contains(serverOperator.operatorId)
  TextButton({
    if (checked) {
      selectedOperatorIds.value -= serverOperator.operatorId
    } else {
      selectedOperatorIds.value += serverOperator.operatorId
    }
  },
    border = BorderStroke(1.dp, color = if (checked) MaterialTheme.colors.primary else MaterialTheme.colors.secondary.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(18.dp)
  ) {
    Row(Modifier.padding(DEFAULT_PADDING_HALF), verticalAlignment = Alignment.CenterVertically) {
      Image(painterResource(serverOperator.largeLogo), null, Modifier.height(48.dp))
      Spacer(Modifier.width(DEFAULT_PADDING_HALF).weight(1f))
      CircleCheckbox(checked)
    }
  }
}

@Composable
private fun CircleCheckbox(checked: Boolean) {
  if (checked) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        painterResource(MR.images.ic_circle_filled),
        null,
        Modifier.size(26.dp),
        tint = MaterialTheme.colors.primary
      )
      Icon(
        painterResource(MR.images.ic_check_filled),
        null,
        Modifier.size(20.dp), tint = MaterialTheme.colors.background
      )
    }
  } else {
    Icon(
      painterResource(MR.images.ic_circle),
      null,
      Modifier.size(26.dp),
      tint = MaterialTheme.colors.secondary.copy(alpha = 0.5f)
    )
  }
}

@Composable
private fun SetOperatorsButton(enabled: Boolean, close: () -> Unit) {
  OnboardingActionButton(
    modifier = if (appPlatform.isAndroid) Modifier.padding(horizontal = DEFAULT_ONBOARDING_HORIZONTAL_PADDING).fillMaxWidth() else Modifier.widthIn(min = 300.dp),
    labelId = MR.strings.ok,
    onboarding = null,
    enabled = enabled,
    onclick = {
      close()
    }
  )
}

private fun acceptConditions(selectedOperatorIds: Set<Long>) {
  withBGApi {
    val conditionsId = chatModel.conditions.value.currentConditions.conditionsId
    val r = chatController.acceptConditions(chatModel.remoteHostId(), conditionsId = conditionsId, operatorIds = selectedOperatorIds.toList())
    if (r != null) {
      chatModel.conditions.value = r
      val enabledOps = enabledOperators(r.serverOperators, selectedOperatorIds)
      if (enabledOps != null) {
        val r2 = chatController.setServerOperators(rh = chatModel.remoteHostId(), operators = enabledOps)
        if (r2 != null) {
          chatModel.conditions.value = r2
          completeOnboarding()
        }
      } else {
        completeOnboarding()
      }
    }
  }
}

@Composable
private fun AcceptConditionsButton(
  enabled: Boolean,
  selectedOperatorIds: State<Set<Long>>
) {
  OnboardingActionButton(
    modifier = if (appPlatform.isAndroid) Modifier.padding(horizontal = DEFAULT_ONBOARDING_HORIZONTAL_PADDING).fillMaxWidth() else Modifier.widthIn(min = 300.dp),
    labelId = MR.strings.onboarding_conditions_accept,
    onboarding = null,
    enabled = enabled,
    onclick = { acceptConditions(selectedOperatorIds.value) }
  )
}

// UITP_NETWORK_AUTOPROVISION_V1
private suspend fun completeOnboarding() {
  if (appPlatform.isAndroid && !applyUITPNetworkDefaults()) {
    AlertManager.shared.showAlertMsg(
      "UITP network configuration",
      "The configured UITP servers could not be saved or validated. Registration has not finished. Check the server addresses and connectivity."
    )
    return
  }
  appPrefs.onboardingStage.set(OnboardingStage.OnboardingComplete)
}

private fun enabledOperators(operators: List<ServerOperator>, selectedOperatorIds: Set<Long>): List<ServerOperator>? {
  val ops = ArrayList(operators)
  if (ops.isNotEmpty()) {
    for (i in ops.indices) {
      val op = ops[i]
      ops[i] = op.copy(enabled = selectedOperatorIds.contains(op.operatorId))
    }
    val haveSMPStorage = ops.any { it.enabled && it.smpRoles.storage }
    val haveSMPProxy = ops.any { it.enabled && it.smpRoles.proxy }
    val haveXFTPStorage = ops.any { it.enabled && it.xftpRoles.storage }
    val haveXFTPProxy = ops.any { it.enabled && it.xftpRoles.proxy }
    val firstEnabledIndex = ops.indexOfFirst { it.enabled }
    if (haveSMPStorage && haveSMPProxy && haveXFTPStorage && haveXFTPProxy) {
      return ops
    } else if (firstEnabledIndex != -1) {
      var op = ops[firstEnabledIndex]
      if (!haveSMPStorage) op = op.copy(smpRoles = op.smpRoles.copy(storage = true))
      if (!haveSMPProxy) op = op.copy(smpRoles = op.smpRoles.copy(proxy = true))
      if (!haveXFTPStorage) op = op.copy(xftpRoles = op.xftpRoles.copy(storage = true))
      if (!haveXFTPProxy) op = op.copy(xftpRoles = op.xftpRoles.copy(proxy = true))
      ops[firstEnabledIndex] = op
      return ops
    } else { // Shouldn't happen - view doesn't let to proceed if no operators are enabled
      return null
    }
  } else {
    return null
  }
}

@Composable
private fun ChooseServerOperatorsInfoView() {
  ColumnWithScrollBar {
    AppBarTitle(stringResource(MR.strings.onboarding_network_operators))

    Column(
      Modifier.padding(horizontal = DEFAULT_PADDING)
    ) {
      ReadableText(stringResource(MR.strings.onboarding_network_operators_app_will_use_different_operators))
      ReadableText(stringResource(MR.strings.onboarding_network_operators_cant_see_who_talks_to_whom))
      ReadableText(stringResource(MR.strings.onboarding_network_operators_app_will_use_for_routing))
    }

    SectionDividerSpaced()

    SectionView(title = stringResource(MR.strings.onboarding_network_about_operators)) {
      chatModel.conditions.value.serverOperators.forEach { op ->
        ServerOperatorRow(op)
      }
    }
    SectionBottomSpacer()
  }
}

@Composable
private fun ServerOperatorRow(
  operator: ServerOperator
) {
  SectionItemView(
    {
      ModalManager.fullscreen.showModalCloseable { close ->
        OperatorInfoView(operator)
      }
    }
  ) {
    Image(
      painterResource(operator.logo),
      operator.tradeName,
      modifier = Modifier.size(24.dp)
    )
    TextIconSpaced()
    Text(operator.tradeName)
  }
}
