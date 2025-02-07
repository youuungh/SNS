package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.LocalTheme
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.theme.snsCommentDarkBackground
import com.ninezero.presentation.theme.snsCommentLightBackground
import com.ninezero.presentation.theme.snsCursorDark
import com.ninezero.presentation.theme.snsCursorLight
import com.ninezero.presentation.theme.snsDefault
import com.ninezero.presentation.theme.snsDefaultMedium
import com.ninezero.presentation.theme.snsSmallButtonDarkBackground
import com.ninezero.presentation.theme.snsSmallButtonDarkText
import com.ninezero.presentation.theme.snsSmallButtonLightBackground
import com.ninezero.presentation.theme.snsSmallButtonLightText

@Composable
fun SNSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val isDarkTheme = LocalTheme.current
    val cursorColor = if (isDarkTheme) {
        snsCursorDark
    } else {
        snsCursorLight
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = snsDefaultMedium.copy(alpha = 0.3f),
                disabledContainerColor = snsDefaultMedium.copy(alpha = 0.3f),
                focusedBorderColor = snsDefault,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = cursorColor,
//                focusedContainerColor = snsDefaultMedium.copy(alpha = 0.3f),
//                unfocusedContainerColor = snsDefaultMedium.copy(alpha = 0.3f),
//                disabledContainerColor = snsDefaultMedium.copy(alpha = 0.3f),
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor = Color.Transparent,
//                errorIndicatorColor = Color.Transparent,
//                cursorColor = snsDefault
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CommentInputField(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = LocalTheme.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf("") }

    val backgroundColor = if (isDarkTheme) {
        snsCommentDarkBackground
    } else {
        snsCommentLightBackground
    }

    val cursorColor = if (isDarkTheme) {
        snsCursorDark
    } else {
        snsCursorLight
    }


    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 44.sp,
                baselineShift = BaselineShift.None
            ),
            cursorBrush = SolidColor(cursorColor),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "댓글을 입력하세요",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 44.sp,
                                    baselineShift = BaselineShift.None
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "send",
                        modifier = Modifier
                            .clickable(
                                enabled = text.isNotEmpty(),
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onSend(text)
                                text = ""
                            }
                    )
                }
            }
        )
    }
}

@Composable
fun SNSSmallText(
    text: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    Box(
        modifier = modifier
            .height(24.dp)
            .background(
                if (isDarkTheme) snsSmallButtonDarkBackground
                else snsSmallButtonLightBackground,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkTheme) snsSmallButtonDarkText
            else snsSmallButtonLightText
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSSmallTextPreview() {
    SNSTheme {
        Surface {
            SNSSmallText(text = "Text")
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSTextFieldPreview() {
    SNSTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var normalText by remember { mutableStateOf("") }
                var passwordText by remember { mutableStateOf("") }
                var errorText by remember { mutableStateOf("") }

                SNSTextField(
                    value = normalText,
                    onValueChange = { normalText = it },
                    label = "이메일",
                    placeholder = "이메일을 입력하세요"
                )

                SNSTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = "비밀번호",
                    placeholder = "비밀번호를 입력하세요",
                    isPassword = true
                )

                SNSTextField(
                    value = errorText,
                    onValueChange = { errorText = it },
                    label = "전화번호",
                    placeholder = "전화번호를 입력하세요",
                    keyboardType = KeyboardType.Phone,
                    isError = true,
                    errorMessage = "올바른 전화번호 형식이 아닙니다"
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CommentInputFieldPreview() {
    SNSTheme {
        CommentInputField(
            onSend = {}
        )
    }
}