package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.theme.LocalTheme
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.theme.snsDarkDialogButtonBackground
import com.ninezero.presentation.theme.snsDarkDialogButtonText
import com.ninezero.presentation.theme.snsDialogButtonBackground
import com.ninezero.presentation.theme.snsDialogButtonText
import com.ninezero.presentation.theme.snsSmallButtonDarkBackground
import com.ninezero.presentation.theme.snsSmallButtonDarkText
import com.ninezero.presentation.theme.snsSmallButtonLightBackground
import com.ninezero.presentation.theme.snsSmallButtonLightText
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.snsFollowDefault
import com.ninezero.presentation.theme.snsKakao
import com.ninezero.presentation.theme.snsNaver
import com.ninezero.presentation.theme.snsSaveButtonDarkBackground
import com.ninezero.presentation.theme.snsSaveButtonDarkText
import com.ninezero.presentation.theme.snsSaveButtonLightBackground
import com.ninezero.presentation.theme.snsSaveButtonLightText

enum class ButtonState { Pressed, Idle }

@Composable
fun SNSButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SNSFilledButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SNSOutlinedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SNSOutlinedToggleButton(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
            contentColor = if (isFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = if (isFollowing) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ) else null,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .bounceClick(),
        ) {
        Text(
            text = stringResource(id = if (isFollowing) R.string.following else R.string.follow),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SNSSmallOutlinedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier
            .height(36.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SNSSmallButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    val backgroundColor = if (isDarkTheme) {
        snsSmallButtonDarkBackground
    } else {
        snsSmallButtonLightBackground
    }

    val textColor = if (isDarkTheme) {
        snsSmallButtonDarkText
    } else {
        snsSmallButtonLightText
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            disabledContentColor = textColor.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = modifier
            .height(24.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CancelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    val backgroundColor = if (isDarkTheme) {
        snsDarkDialogButtonBackground
    } else {
        snsDialogButtonBackground
    }

    val textColor = if (isDarkTheme) {
        snsDarkDialogButtonText
    } else {
        snsDialogButtonText
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ConfirmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DefaultButton(
    text: String,
    onClick: () -> Unit,
    @DrawableRes icon: Int? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DefaultAccentButton(
    text: String,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    @DrawableRes icon: Int? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
fun AdditionalButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        contentColor = if (enabled)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .let { if (enabled) it.bounceClick() else it }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = null,
                enabled = false,
                colors = SwitchDefaults.colors(
                    disabledCheckedThumbColor = if (enabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledCheckedTrackColor = if (enabled)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun SNSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes drawableId: Int? = null,
    imageVector: ImageVector? = null,
    contentDescription: String? = null,
    hasBadge: Boolean = false,
    badgeColor: Color = MaterialTheme.colorScheme.error,
) {
    Box(
        modifier = modifier
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Box {
                when {
                    drawableId != null -> Icon(
                        painter = painterResource(id = drawableId),
                        contentDescription = contentDescription
                    )
                    imageVector != null -> Icon(
                        imageVector = imageVector,
                        contentDescription = contentDescription
                    )
                }

                if (hasBadge) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(badgeColor, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun SNSIconToggleButton(
    onClick: () -> Unit,
    icon: Painter,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    iconSize: Int = 20,
    buttonSize: Int = 32,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            },
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Composable
fun SNSTextButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SNSTextButton(
    @StringRes textResId: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    SNSTextButton(
        text = stringResource(id = textResId),
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    Row(
        modifier = modifier
            .background(
                color = if (isDarkTheme) snsSmallButtonDarkBackground else snsSmallButtonLightBackground,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .bounceClick()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
            ),
            contentDescription = "like",
            modifier = Modifier.size(24.dp),
            tint = if (isDarkTheme) snsSmallButtonDarkText else snsSmallButtonLightText
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = if (isDarkTheme) snsSmallButtonDarkText else snsSmallButtonLightText
        )
    }
}

@Composable
fun CommentButton(
    commentCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    Row(
        modifier = modifier
            .background(
                color = if (isDarkTheme) snsSmallButtonDarkBackground else snsSmallButtonLightBackground,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .bounceClick()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_comment),
            contentDescription = "comments",
            modifier = Modifier.size(24.dp),
            tint = if (isDarkTheme) snsSmallButtonDarkText else snsSmallButtonLightText
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = commentCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = if (isDarkTheme) snsSmallButtonDarkText else snsSmallButtonLightText
        )
    }
}

@Composable
fun SNSFollowingButton(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
            contentColor = if (isFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = if (isFollowing) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ) else null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .height(28.dp)
            .bounceClick(),
    ) {
        Text(
            text = stringResource(id = if (isFollowing) R.string.following else R.string.follow),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SaveButton(
    isSaved: Boolean,
    text: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    Row(
        modifier = modifier
            .background(
                color = if (isDarkTheme) snsSaveButtonDarkBackground else snsSaveButtonLightBackground,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .bounceClick()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = if (isSaved) R.drawable.ic_save_filled else R.drawable.ic_save),
            contentDescription = "save",
            modifier = Modifier.size(24.dp),
            tint = if (isDarkTheme) snsSaveButtonDarkText else snsSaveButtonLightText
        )

        if (text != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = if (isDarkTheme) snsSaveButtonDarkText else snsSaveButtonLightText
            )
        }
    }
}

@Composable
fun SNSDefaultFollowingButton(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isFollowing -> MaterialTheme.colorScheme.surface
                isDarkTheme -> snsFollowDefault
                else -> snsFollowDefault
            },
            contentColor = when {
                isFollowing -> MaterialTheme.colorScheme.onSurface
                isDarkTheme -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.scrim
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = if (isFollowing) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface
        ) else null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .height(32.dp)
            .bounceClick(),
    ) {
        Text(
            text = stringResource(id = if (isFollowing) R.string.following else R.string.follow),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SNSSocialButton(
    onClick: () -> Unit,
    @DrawableRes drawableId: Int,
    backgroundColor: Color,
    contentColor: Color = Color.Unspecified,
    hasBorder: Boolean = false,
    borderColor: Color = Color.Transparent,
    iconSize: Dp = 36.dp,
    iconPadding: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .bounceClick()
            .size(48.dp)
            .then(
                if (hasBorder) Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = CircleShape
                ) else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(iconPadding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun GoogleLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color.White
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

    SNSSocialButton(
        onClick = onClick,
        drawableId = R.drawable.ic_google,
        backgroundColor = backgroundColor,
        hasBorder = true,
        borderColor = borderColor,
        iconPadding = 14.dp,
        modifier = modifier
    )
}

@Composable
fun NaverLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SNSSocialButton(
        onClick = onClick,
        drawableId = R.drawable.ic_naver,
        backgroundColor = snsNaver,
        contentColor = Color.White,
        iconSize = 16.dp,
        iconPadding = 14.dp,
        modifier = modifier
    )
}

@Composable
fun KakaoLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SNSSocialButton(
        onClick = onClick,
        drawableId = R.drawable.ic_kakao,
        backgroundColor = snsKakao,
        contentColor = Color.Black,
        iconSize = 20.dp,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PostButtonsPreview() {
    SNSTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LikeButton(
                        isLiked = false,
                        likesCount = 10,
                        onClick = {}
                    )
                    LikeButton(
                        isLiked = true,
                        likesCount = 11,
                        onClick = {}
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommentButton(
                        commentCount = 10,
                        onClick = {}
                    )
                    CommentButton(
                        commentCount = 11,
                        onClick = {}
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SaveButton(
                        isSaved = false,
                        onClick = {}
                    )
                    SaveButton(
                        isSaved = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SocialButtonsPreview() {
    SNSTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GoogleLoginButton(onClick = {})
                NaverLoginButton(onClick = {})
                KakaoLoginButton(onClick = {})
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSButtonPreview() {
    SNSTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SNSSmallButton(
                    text = "Enabled Button",
                    onClick = {}
                )
                SNSSmallButton(
                    text = "Enabled Button",
                    onClick = {},
                    enabled = false
                )
                SNSButton(
                    text = "Enabled Button",
                    onClick = {}
                )
                SNSButton(
                    text = "Disabled Button",
                    onClick = {},
                    enabled = false
                )
                SNSFilledButton(
                    text = "Enabled Button",
                    onClick = {}
                )
                SNSFilledButton(
                    text = "Disabled Button",
                    onClick = {},
                    enabled = false
                )
                SNSOutlinedButton(
                    text = "Enabled Button",
                    onClick = {}
                )
                SNSOutlinedButton(
                    text = "Disabled Button",
                    onClick = {},
                    enabled = false
                )
                SNSOutlinedToggleButton(
                    isFollowing = false,
                    onClick = {},
                )
                SNSOutlinedToggleButton(
                    isFollowing = true,
                    onClick = {},
                )
                CancelButton(
                    text = "Cancel",
                    onClick = {}
                )
                ConfirmButton(
                    text = "Confirm",
                    onClick = {}
                )
                SNSSmallOutlinedButton(
                    text = "Retry",
                    onClick = {}
                )
                Row {
                    SNSFollowingButton(
                        isFollowing = true,
                        onClick = {}
                    )
                    SNSFollowingButton(
                        isFollowing = false,
                        onClick = {}
                    )
                    SNSDefaultFollowingButton(
                        isFollowing = true,
                        onClick = {}
                    )
                    SNSDefaultFollowingButton(
                        isFollowing = false,
                        onClick = {}
                    )
                }
                DefaultButton(
                    text = "Button Text",
                    onClick = {}
                )
                DefaultAccentButton(
                    text = "Button Text",
                    onClick = {}
                )
                AdditionalButton(
                    text = "Button Text",
                    isEnabled = true,
                    onClick = {}
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSButtonPreview2() {
    SNSTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row {
                    SNSIconButton(
                        onClick = {},
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "close"
                    )
                    SNSIconButton(
                        onClick = {},
                        drawableId = R.drawable.ic_notification,
                        hasBadge = true,
                        contentDescription = "close"
                    )
                    SNSIconToggleButton(
                        onClick = {},
                        icon = painterResource(id = R.drawable.ic_multiple),
                        isActive = false
                    )
                    SNSIconToggleButton(
                        onClick = {},
                        icon = painterResource(id = R.drawable.ic_multiple),
                        isActive = true
                    )
                }
                Row {
                    SNSTextButton(
                        text = "클릭",
                        onClick = {},
                        enabled = true
                    )
                    SNSTextButton(
                        text = "클릭",
                        onClick = {},
                        enabled = false
                    )
                }
            }
        }
    }
}