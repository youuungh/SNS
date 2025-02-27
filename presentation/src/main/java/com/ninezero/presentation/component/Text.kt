package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.domain.model.Comment
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
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    focusRequester: FocusRequester
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(1.dp)
                .background(
                    color = if (isFocused) Color.Transparent else backgroundColor,
                    shape = RoundedCornerShape(11.dp)
                )
                .clip(RoundedCornerShape(11.dp))
                .clickable(onClick = onFocus)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onValueChange(newValue.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    onFocus()
                                    textFieldValue = TextFieldValue(
                                        text = textFieldValue.text,
                                        selection = TextRange(textFieldValue.text.length)
                                    )
                                }
                            },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = value.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SNSCommentInputField(
    @StringRes hint: Int,
    replyToComment: Comment? = null,
    onSend: (String) -> Unit,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = replyToComment != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            replyToComment?.let { comment ->
                SNSSurface(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SNSProfileImage(
                                modifier = Modifier.size(24.dp),
                                imageUrl = comment.profileImageUrl
                            )

                            Text(
                                text = stringResource(
                                    id = R.string.replying_to_user,
                                    comment.userName
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        SNSIconButton(
                            onClick = onCancelReply,
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        SNSSurface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 8.dp
        ) {
            val isDarkTheme = LocalTheme.current
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusRequester = remember { FocusRequester() }

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

            LaunchedEffect(replyToComment) {
                text = ""
                replyToComment?.let {
                    focusRequester.requestFocus()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (replyToComment != null) {
                        Text(
                            text = "@${replyToComment.userName} ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 44.sp,
                                baselineShift = BaselineShift.None
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 44.sp,
                                baselineShift = BaselineShift.None
                            ),
                            cursorBrush = SolidColor(cursorColor),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (text.isEmpty() && replyToComment == null) {
                                        Text(
                                            text = stringResource(id = hint),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 44.sp,
                                                baselineShift = BaselineShift.None
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = "send",
                    modifier = Modifier
                        .size(24.dp)
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
    }
}

@Composable
fun SNSMessageInputField(
    @StringRes hint: Int,
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
                                text = stringResource(id = hint),
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

@Composable
fun SNSMediumText(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
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
private fun SNSMediumTextPreview() {
    SNSTheme {
        Surface {
            SNSMediumText(text = "Text")
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchTextFieldPreview() {
    SNSTheme {
        Surface {
            var text by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                SearchTextField(
                    value = text,
                    onValueChange = { text = it },
                    onFocus = {},
                    focusRequester = remember { FocusRequester() },
                    placeholder = "검색"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchTextField(
                    value = "검색어",
                    onValueChange = {},
                    onFocus = {},
                    focusRequester = remember { FocusRequester() },
                    placeholder = "검색"
                )
            }
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
private fun SNSCommentInputFieldPreview() {
    SNSTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            val sampleComment = Comment(
                id = 1L,
                userId = 1L,
                text = "Sample comment",
                userName = "SampleUser",
                profileImageUrl = null,
                parentId = null,
                parentUserName = null,
                depth = 0,
                replyCount = 0
            )

            SNSCommentInputField(
                hint = R.string.label_input_comment,
                replyToComment = sampleComment,
                onSend = {},
                onCancelReply = {}
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSMessageInputFieldPreview() {
    SNSTheme {
        SNSMessageInputField(
            hint = R.string.label_input_comment,
            onSend = {}
        )
    }
}