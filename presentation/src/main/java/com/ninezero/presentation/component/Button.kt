package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .bounceClick()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.primary,
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
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
    contentDescription: String? = null
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
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
    commentsCount: Int,
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
            text = commentsCount.toString(),
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
                        commentsCount = 10,
                        onClick = {}
                    )
                    CommentButton(
                        commentsCount = 11,
                        onClick = {}
                    )
                }
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
                        isFollowing = false,
                        onClick = {}
                    )
                    SNSFollowingButton(
                        isFollowing = true,
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
                SNSIconButton(
                    onClick = { },
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "close"
                )
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