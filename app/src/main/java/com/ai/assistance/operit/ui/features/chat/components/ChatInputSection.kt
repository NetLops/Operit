package com.ai.assistance.operit.ui.features.chat.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.model.AttachmentInfo
import com.ai.assistance.operit.data.model.ChatMessage
import com.ai.assistance.operit.data.model.InputProcessingState
import com.ai.assistance.operit.ui.common.animations.SimpleAnimatedVisibility
import com.ai.assistance.operit.ui.features.chat.viewmodel.ChatViewModel
import com.ai.assistance.operit.ui.floating.FloatingMode
import com.ai.assistance.operit.util.ChatUtils

@Composable
fun ChatInputSection(
        actualViewModel: ChatViewModel,
        userMessage: TextFieldValue,
        onUserMessageChange: (TextFieldValue) -> Unit,
        onSendMessage: () -> Unit,
        onCancelMessage: () -> Unit,
        isLoading: Boolean,
        inputState: InputProcessingState = InputProcessingState.Idle,
        allowTextInputWhileProcessing: Boolean = false,
        onAttachmentRequest: (String) -> Unit = {},
        attachments: List<AttachmentInfo> = emptyList(),
        onRemoveAttachment: (String) -> Unit = {},
        onInsertAttachment: (AttachmentInfo) -> Unit = {},
        onAttachScreenContent: () -> Unit = {},
        onAttachNotifications: () -> Unit = {},
        onAttachLocation: () -> Unit = {},
        onAttachMemory: () -> Unit = {},
        onTakePhoto: (Uri) -> Unit,
        hasBackgroundImage: Boolean = false,
        chatInputTransparent: Boolean = false,
        modifier: Modifier = Modifier,
        externalAttachmentPanelState: Boolean? = null,
        onAttachmentPanelStateChange: ((Boolean) -> Unit)? = null,
        showInputProcessingStatus: Boolean = true,
        enableTools: Boolean = true,
        replyToMessage: ChatMessage? = null,
        onClearReply: (() -> Unit)? = null,
        isWorkspaceOpen: Boolean = false
) {
        val showTokenLimitDialog = remember { mutableStateOf(false) }
        val showFullscreenInput = remember { mutableStateOf(false) }
        val context = LocalContext.current

        if (showTokenLimitDialog.value) {
                AlertDialog(
                        onDismissRequest = { showTokenLimitDialog.value = false },
                        title = { Text(context.getString(R.string.token_limit_warning)) },
                        text = { Text(context.getString(R.string.token_limit_warning_message)) },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                showTokenLimitDialog.value = false
                                                onSendMessage()
                                        }
                                ) { Text(context.getString(R.string.continue_send)) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showTokenLimitDialog.value = false }) {
                                        Text(context.getString(R.string.cancel))
                                }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }

        val scope = rememberCoroutineScope()
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography

        val isProcessing = isLoading

        // Token limit calculation
        val currentWindowSize by actualViewModel.currentWindowSize.collectAsState()
        val maxWindowSizeInK by actualViewModel.maxWindowSizeInK.collectAsState()
        val maxTokens = (maxWindowSizeInK * 1024).toInt()
        val userMessageTokens =
                remember(userMessage.text) { ChatUtils.estimateTokenCount(userMessage.text) }

        val isOverTokenLimit =
                if (maxTokens > 0) {
                        (userMessageTokens + currentWindowSize) > maxTokens
                } else {
                        false
                }

        val canSendMessage = userMessage.text.isNotBlank() || attachments.isNotEmpty()
        val sendButtonEnabled =
                when {
                        isProcessing -> true // Cancel button
                        canSendMessage -> true // Send button is always enabled if there's content
                        else -> true // Mic button
                }

        val voicePermissionLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                        if (isGranted) {
                                actualViewModel.launchFloatingModeIn(
                                        FloatingMode.FULLSCREEN,
                                        colorScheme,
                                        typography
                                )
                        } else {
                                actualViewModel.showToast(
                                        context.getString(
                                                R.string.microphone_permission_denied_toast
                                        )
                                )
                        }
                }

        // Attachment panel state
        val (showAttachmentPanel, setShowAttachmentPanel) =
                androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(
                                externalAttachmentPanelState ?: false
                        )
                }

        androidx.compose.runtime.LaunchedEffect(externalAttachmentPanelState) {
                externalAttachmentPanelState?.let { setShowAttachmentPanel(it) }
        }

        androidx.compose.runtime.LaunchedEffect(showAttachmentPanel) {
                onAttachmentPanelStateChange?.invoke(showAttachmentPanel)
        }

        // Main Container
        Column(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {

                // Reply Preview
                replyToMessage?.let { message ->
                        Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                border =
                                        border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(
                                                        alpha = 0.2f
                                                ),
                                                RoundedCornerShape(12.dp)
                                        )
                        ) {
                                Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Reply,
                                                contentDescription =
                                                        context.getString(R.string.reply_message),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        val previewText =
                                                message.content
                                                        .replace(Regex("<[^>]*>"), "")
                                                        .trim()
                                                        .let {
                                                                if (it.length > 50)
                                                                        it.take(50) + "..."
                                                                else it
                                                        }
                                        Text(
                                                text =
                                                        "${stringResource(R.string.reply_message)}: $previewText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                                onClick = { onClearReply?.invoke() },
                                                modifier = Modifier.size(24.dp)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription =
                                                                context.getString(
                                                                        R.string.cancel_reply
                                                                ),
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                )
                                        }
                                }
                        }
                }

                // Processing Indicator
                SimpleAnimatedVisibility(
                        visible =
                                showInputProcessingStatus &&
                                        inputState !is InputProcessingState.Idle &&
                                        inputState !is InputProcessingState.Completed
                ) {
                        val (progressColor, message) =
                                when (inputState) {
                                        is InputProcessingState.Connecting ->
                                                MaterialTheme.colorScheme.tertiary to
                                                        inputState.message
                                        is InputProcessingState.ExecutingTool ->
                                                MaterialTheme.colorScheme.secondary to
                                                        context.getString(
                                                                R.string.executing_tool,
                                                                inputState.toolName
                                                        )
                                        is InputProcessingState.Processing ->
                                                MaterialTheme.colorScheme.primary to
                                                        inputState.message
                                        is InputProcessingState.ProcessingToolResult ->
                                                MaterialTheme.colorScheme.tertiary.copy(
                                                        alpha = 0.8f
                                                ) to
                                                        context.getString(
                                                                R.string.processing_tool_result,
                                                                inputState.toolName
                                                        )
                                        is InputProcessingState.Summarizing ->
                                                MaterialTheme.colorScheme.tertiary to
                                                        inputState.message
                                        is InputProcessingState.Receiving ->
                                                MaterialTheme.colorScheme.secondary to
                                                        inputState.message
                                        else -> MaterialTheme.colorScheme.primary to ""
                                }

                        Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                        ) {
                                Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = progressColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = message,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                                }
                        }
                }

                // Attachment Chips
                if (attachments.isNotEmpty()) {
                        LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(attachments) { attachment ->
                                        AttachmentChip(
                                                attachmentInfo = attachment,
                                                onRemove = {
                                                        onRemoveAttachment(attachment.filePath)
                                                },
                                                onInsert = { onInsertAttachment(attachment) }
                                        )
                                }
                        }
                }

                // Floating Command Bar
                Surface(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(28.dp),
                                                spotColor =
                                                        MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.2f
                                                        )
                                        ),
                        shape = RoundedCornerShape(28.dp),
                        color =
                                MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.9f
                                ), // Glass-like effect
                        border =
                                border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        RoundedCornerShape(28.dp)
                                )
                ) {
                        Row(
                                modifier =
                                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                                .heightIn(min = 56.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Attachment Button
                                IconButton(
                                        onClick = { setShowAttachmentPanel(!showAttachmentPanel) },
                                        modifier =
                                                Modifier.size(40.dp)
                                                        .background(
                                                                color =
                                                                        if (showAttachmentPanel)
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else Color.Transparent,
                                                                shape = CircleShape
                                                        )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription =
                                                        context.getString(R.string.add_attachment),
                                                tint =
                                                        if (showAttachmentPanel)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                        )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Input Field
                                Box(
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.CenterStart
                                ) {
                                        if (userMessage.text.isEmpty()) {
                                                Text(
                                                        text =
                                                                if (isWorkspaceOpen)
                                                                        context.getString(
                                                                                R.string
                                                                                        .input_question_with_workspace
                                                                        )
                                                                else
                                                                        context.getString(
                                                                                R.string
                                                                                        .input_question_hint
                                                                        ),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant.copy(
                                                                        alpha = 0.6f
                                                                )
                                                )
                                        }

                                        androidx.compose.foundation.text.BasicTextField(
                                                value = userMessage,
                                                onValueChange = onUserMessageChange,
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .heightIn(max = 120.dp),
                                                textStyle =
                                                        MaterialTheme.typography.bodyLarge.copy(
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        ),
                                                cursorBrush =
                                                        androidx.compose.ui.graphics.SolidColor(
                                                                MaterialTheme.colorScheme.primary
                                                        ),
                                                maxLines = 5,
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                imeAction = ImeAction.Default
                                                        ),
                                                enabled =
                                                        !isProcessing ||
                                                                allowTextInputWhileProcessing
                                        )
                                }

                                // Fullscreen Toggle (only if multiline)
                                if (userMessage.text.contains("\n")) {
                                        IconButton(
                                                onClick = { showFullscreenInput.value = true },
                                                modifier = Modifier.size(32.dp)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Fullscreen,
                                                        contentDescription = "Fullscreen",
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Send/Mic Button
                                Box(
                                        modifier =
                                                Modifier.size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                when {
                                                                        isProcessing ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .errorContainer
                                                                        canSendMessage ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                        else ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surfaceVariant
                                                                }
                                                        )
                                                        .clickable(
                                                                enabled = sendButtonEnabled,
                                                                onClick = {
                                                                        when {
                                                                                isProcessing ->
                                                                                        onCancelMessage()
                                                                                canSendMessage -> {
                                                                                        if (isOverTokenLimit
                                                                                        ) {
                                                                                                showTokenLimitDialog
                                                                                                        .value =
                                                                                                        true
                                                                                        } else {
                                                                                                onSendMessage()
                                                                                                setShowAttachmentPanel(
                                                                                                        false
                                                                                                )
                                                                                        }
                                                                                }
                                                                                else -> {
                                                                                        actualViewModel
                                                                                                .onFloatingButtonClick(
                                                                                                        FloatingMode
                                                                                                                .FULLSCREEN,
                                                                                                        voicePermissionLauncher,
                                                                                                        colorScheme,
                                                                                                        typography
                                                                                                )
                                                                                }
                                                                        }
                                                                }
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        AnimatedContent(
                                                targetState =
                                                        when {
                                                                isProcessing -> Icons.Default.Close
                                                                canSendMessage -> Icons.Default.Send
                                                                else -> Icons.Default.Mic
                                                        },
                                                transitionSpec = {
                                                        scaleIn() togetherWith scaleOut()
                                                },
                                                label = "SendButtonIcon"
                                        ) { targetIcon ->
                                                Icon(
                                                        imageVector = targetIcon,
                                                        contentDescription = null,
                                                        tint =
                                                                when (targetIcon) {
                                                                        Icons.Default.Close ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onErrorContainer
                                                                        Icons.Default.Send ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimary
                                                                        else ->
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant
                                                                },
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }
                                }
                        }
                }

                // Token Limit Warning
                if (isOverTokenLimit && canSendMessage) {
                        Text(
                                text =
                                        context.getString(
                                                R.string.token_limit_exceeded_message,
                                                userMessageTokens + currentWindowSize,
                                                maxTokens
                                        ),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier =
                                        Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp),
                                textAlign = TextAlign.Start
                        )
                }

                // Attachment Panel
                AttachmentSelectorPanel(
                        visible = showAttachmentPanel,
                        onAttachImage = { onAttachmentRequest(it) },
                        onAttachFile = { onAttachmentRequest(it) },
                        onAttachScreenContent = onAttachScreenContent,
                        onAttachNotifications = onAttachNotifications,
                        onAttachLocation = onAttachLocation,
                        onAttachMemory = onAttachMemory,
                        onTakePhoto = onTakePhoto,
                        userQuery = userMessage.text,
                        onDismiss = { setShowAttachmentPanel(false) }
                )

                if (showFullscreenInput.value) {
                        FullscreenInputDialog(
                                value = userMessage,
                                onValueChange = onUserMessageChange,
                                onDismiss = { showFullscreenInput.value = false },
                                onConfirm = { showFullscreenInput.value = false }
                        )
                }
        }
}

@Composable
fun AttachmentChip(attachmentInfo: AttachmentInfo, onRemove: () -> Unit, onInsert: () -> Unit) {
        val context = LocalContext.current
        val isImage = attachmentInfo.mimeType.startsWith("image/")
        val icon: ImageVector = if (isImage) Icons.Default.Image else Icons.Default.Description

        Surface(
                modifier =
                        Modifier.height(26.dp)
                                .border(
                                        width = 1.dp,
                                        color =
                                                MaterialTheme.colorScheme.outline.copy(
                                                        alpha = 0.5f
                                                ),
                                        shape = RoundedCornerShape(13.dp)
                                ),
                shape = RoundedCornerShape(13.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                                text = attachmentInfo.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        IconButton(onClick = onInsert, modifier = Modifier.size(14.dp)) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription =
                                                context.getString(R.string.insert_attachment),
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        IconButton(onClick = onRemove, modifier = Modifier.size(14.dp)) {
                                Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription =
                                                context.getString(R.string.remove_attachment),
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}
