package chat.simplex.common.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
  small = RoundedCornerShape(UITPDimens.FieldRadius),
  medium = RoundedCornerShape(UITPDimens.SectionRadius),
  large = RoundedCornerShape(UITPDimens.CardRadius)
)
