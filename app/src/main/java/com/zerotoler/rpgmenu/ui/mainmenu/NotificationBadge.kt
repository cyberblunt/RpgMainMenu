package com.zerotoler.rpgmenu.ui.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotoler.rpgmenu.ui.theme.RedBadge
import com.zerotoler.rpgmenu.ui.theme.TextPrimary

@Composable
fun NotificationBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    dotSize: Dp = 7.dp,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            count != null && count > 0 -> {
                val label = if (count > 99) "99+" else count.toString()
                Text(
                    text = label,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-6).dp)
                        .clip(CircleShape)
                        .background(RedBadge)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
            count == null -> {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-4).dp)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(RedBadge),
                )
            }
        }
    }
}
