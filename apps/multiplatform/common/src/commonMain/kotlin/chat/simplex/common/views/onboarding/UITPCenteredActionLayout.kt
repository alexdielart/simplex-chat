package chat.simplex.common.views.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Places the CTA in the middle of the spare vertical area below content.
 * Equal gaps above and below it. On a short screen, content and CTA scroll
 * together so the action remains reachable, including when the IME is open.
 * Use only for the UITP onboarding screens, not global application dialogs.
 */
@Composable
internal fun UITPCenteredActionLayout(
  modifier: Modifier = Modifier,
  actionHeight: Dp = 52.dp,
  minimumGap: Dp = 16.dp,
  action: @Composable () -> Unit,
  content: @Composable ColumnScope.() -> Unit
) {
  BoxWithConstraints(modifier) {
    var contentHeightPx by remember { mutableIntStateOf(0) }
    val measuredContent = with(LocalDensity.current) { contentHeightPx.toDp() }
    val gap = ((maxHeight - measuredContent - actionHeight) / 2f).coerceAtLeast(minimumGap)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
      Column(Modifier.fillMaxWidth().onSizeChanged { contentHeightPx = it.height }) {
        content()
      }
      Spacer(Modifier.height(gap))
      action()
      Spacer(Modifier.height(gap))
    }
  }
}
