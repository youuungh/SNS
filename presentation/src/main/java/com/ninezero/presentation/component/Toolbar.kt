package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun PostToolbar(
    modifier: Modifier = Modifier,
    richTextState: RichTextState
) {
    SNSSurface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        elevation = 8.dp
    ) {
        Row(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.bold),
                    contentDescription = "bold",
                    tint = if (richTextState.currentSpanStyle.fontWeight == FontWeight.Bold) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }

            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    richTextState.toggleSpanStyle(
                        SpanStyle(fontStyle = FontStyle.Italic)
                    )
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.italic),
                    contentDescription = "italic",
                    tint = if (richTextState.currentSpanStyle.fontStyle == FontStyle.Italic) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }

            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    richTextState.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline)
                    )
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.underline),
                    contentDescription = "underline",
                    tint = if (richTextState.currentSpanStyle.textDecoration?.contains(
                            TextDecoration.Underline
                        ) == true
                    ) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }

            IconButton(
                onClick = {
                    richTextState.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough)
                    )
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.strikethrough),
                    contentDescription = "strikethrough",
                    tint = if (richTextState.currentSpanStyle.textDecoration?.contains(
                            TextDecoration.LineThrough
                        ) == true
                    ) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = false)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PostToolbarPreview() {
    SNSTheme {
        PostToolbar(
            modifier = Modifier.fillMaxWidth(),
            richTextState = RichTextState()
        )
    }
}