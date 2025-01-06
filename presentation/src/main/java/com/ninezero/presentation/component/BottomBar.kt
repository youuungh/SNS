package com.ninezero.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ninezero.presentation.main.MainRoute

private val BottomNavHeight = 56.dp

@Composable
fun SNSBottomBar(
    modifier: Modifier = Modifier,
    currentRoute: String,
    imageUrl: String? = null,
    navigateToRoute: (String) -> Unit,
    color: Color = MaterialTheme.colorScheme.surface,
    iconColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val navItems = remember { MainRoute.BottomNavItem.bottomNavItems() }
    val currentNavItem = remember(currentRoute) {
        navItems.find { it.route == currentRoute } ?: MainRoute.BottomNavItem.Feed
    }

    SNSSurface(
        modifier = modifier,
        color = color,
        contentColor = iconColor,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .height(BottomNavHeight)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val selected = item == currentNavItem
                val tint by animateColorAsState(
                    if (selected) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    label = "tint"
                )

                val borderWidth by animateDpAsState(
                    if (selected && item.isProfile) 2.dp else 1.dp,
                    label = "border_width"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        item.isProfile -> {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = borderWidth,
                                        color = tint,
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { navigateToRoute(item.route) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = imageUrl ?: item.defaultIconRes,
                                    contentDescription = item.contentDescription,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        else -> {
                            Icon(
                                painter = painterResource(
                                    if (selected) item.selectedIconRes
                                    else item.defaultIconRes
                                ),
                                contentDescription = item.contentDescription,
                                tint = tint,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { navigateToRoute(item.route) }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}