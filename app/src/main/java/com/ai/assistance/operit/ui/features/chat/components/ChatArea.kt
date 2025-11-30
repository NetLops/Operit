package com.ai.assistance.operit.ui.features.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.model.AiReference
import com.ai.assistance.operit.data.model.ChatMessage
import com.ai.assistance.operit.ui.features.chat.components.style.bubble.BubbleStyleChatMessage
import com.ai.assistance.operit.ui.features.chat.components.style.cursor.CursorStyleChatMessage

/** 清理消息中的XML标签，保留纯文本内容和换行格式 */
private fun cleanXmlTags(content: String): String {
    return content.replace(Regex("<[^>]*>"), "") // 移除所有XML/HTML标签
            .replace(Regex("[ \t]+"), " ") // 将多个空格和制表符替换为单个空格，但保留换行符
            .replace(Regex("\n+"), "\n") // 将多个连续换行符替换为单个换行符
            .trim() // 移除首尾空白
}

enum class ChatStyle {
    CURSOR,
    BUBBLE
}

@Composable
fun ChatArea(
        chatHistory: List<ChatMessage>,
        scrollState: ScrollState,
        aiReferences: List<AiReference> = emptyList(),
        isLoading: Boolean,
        userMessageColor: Color,
        aiMessageColor: Color,
        userTextColor: Color,
        aiTextColor: Color,
        systemMessageColor: Color,
        systemTextColor: Color,
        thinkingBackgroundColor: Color,
        thinkingTextColor: Color,
        hasBackgroundImage: Boolean = false,
        modifier: Modifier = Modifier,
        onSelectMessageToEdit: ((Int, ChatMessage, String) -> Unit)? = null,
        onCopyMessage: ((ChatMessage) -> Unit)? = null,
        onDeleteMessage: ((Int) -> Unit)? = null,
        onDeleteMessagesFrom: ((Int) -> Unit)? = null,
        onSpeakMessage: ((String) -> Unit)? = null, // 添加朗读回调参数
        onAutoReadMessage: ((String) -> Unit)? = null, // 添加自动朗读回调参数
        onReplyToMessage: ((ChatMessage) -> Unit)? = null, // 添加回复回调参数
        onCreateBranch: ((Long) -> Unit)? = null, // 添加创建分支回调参数
        messagesPerPage: Int = 10, // 每页显示的消息数量
        topPadding: Dp = 0.dp,
        chatStyle: ChatStyle = ChatStyle.CURSOR, // 新增参数，默认为CURSOR风格
        isMultiSelectMode: Boolean = false, // 是否处于多选模式
        selectedMessageIndices: Set<Int> = emptySet(), // 已选中的消息索引集合
        onToggleMultiSelectMode: ((Int?) -> Unit)? = null, // 切换多选模式的回调，可传入要初始选中的消息索引
        onToggleMessageSelection: ((Int) -> Unit)? = null, // 切换消息选中状态的回调
        horizontalPadding: Dp = 16.dp // 水平内边距，可自定义
) {
    // 记住当前深度状态，但当chatHistory发生变化时重置为1
    var currentDepth = remember(chatHistory) { mutableStateOf(1) }

    Column(modifier = modifier) {
        // 移除References display

        // Plan display removed

        // 改用普通Column替代LazyColumn，避免复杂的回收逻辑带来的性能问题
        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .verticalScroll(scrollState) // 使用从外部传入的scrollState
                                .background(Color.Transparent)
                                .padding(top = topPadding),
        ) {
            val lastMessage = chatHistory.lastOrNull()
            // 判断是否应该显示加载指示器，并且在 Bubble 模式下隐藏最后一条空消息
            val showLoadingIndicator =
                    isLoading &&
                            (lastMessage?.sender == "user" ||
                                    (lastMessage?.sender == "ai" && lastMessage.content.isBlank()))
            val shouldHideLastAiMessage =
                    showLoadingIndicator &&
                            chatStyle == ChatStyle.BUBBLE &&
                            lastMessage?.sender == "ai"

            val messagesCount = chatHistory.size
            val maxVisibleIndex = messagesCount - 1
            val minVisibleIndex =
                    maxOf(0, maxVisibleIndex - currentDepth.value * messagesPerPage + 1)
            val hasMoreMessages = minVisibleIndex > 0

            // "加载更多"文本 - 改为灰色文本而非按钮
            if (hasMoreMessages) {
                Text(
                        text = stringResource(id = R.string.load_more_history),
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { currentDepth.value += 1 }
                                        .padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 根据当前深度筛选显示的消息
            chatHistory.subList(minVisibleIndex, messagesCount).forEachIndexed {
                    relativeIndex,
                    message ->
                val actualIndex = minVisibleIndex + relativeIndex
                val isLastAiMessage = actualIndex == messagesCount - 1 && message.sender == "ai"
                val shouldHide = shouldHideLastAiMessage && isLastAiMessage

                // 使用key组合函数为每个消息项设置单独的重组作用域
                key(message.timestamp) {
                    MessageItem(
                            index = actualIndex,
                            message = message,
                            userMessageColor = userMessageColor,
                            aiMessageColor = aiMessageColor,
                            userTextColor = userTextColor,
                            aiTextColor = aiTextColor,
                            systemMessageColor = systemMessageColor,
                            systemTextColor = systemTextColor,
                            thinkingBackgroundColor = thinkingBackgroundColor,
                            thinkingTextColor = thinkingTextColor,
                            onSelectMessageToEdit = onSelectMessageToEdit,
                            onCopyMessage = onCopyMessage,
                            onDeleteMessage = onDeleteMessage,
                            onDeleteMessagesFrom = onDeleteMessagesFrom,
                            onSpeakMessage = onSpeakMessage, // 传递朗读回调
                            onReplyToMessage = onReplyToMessage, // 传递回复回调
                            onCreateBranch = onCreateBranch, // 传递创建分支回调
                            chatStyle = chatStyle, // 传递风格
                            isHidden = shouldHide, // 新增参数控制隐藏
                            isMultiSelectMode = isMultiSelectMode, // 传递多选模式状态
                            isSelected = selectedMessageIndices.contains(actualIndex), // 传递选中状态
                            onToggleSelection = {
                                onToggleMessageSelection?.invoke(actualIndex)
                            }, // 传递选中切换回调
                            onToggleMultiSelectMode = onToggleMultiSelectMode, // 传递多选模式切换回调
                            messageIndex = actualIndex // 传递消息索引
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 当AI正在响应但尚未输出任何文本时，显示加载指示器
            if (showLoadingIndicator) {
                when (chatStyle) {
                    ChatStyle.BUBBLE -> {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = 0.dp)
                                                .offset(y = (-24).dp)
                        ) {
                            // 加载指示器放在左侧，与标签对齐
                            Box(modifier = Modifier.padding(start = 16.dp)) {
                                LoadingDotsIndicator(aiTextColor)
                            }
                        }
                    }
                    ChatStyle.CURSOR -> {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)) {
                            // 加载指示器放在左侧，与标签对齐
                            Box(modifier = Modifier.padding(start = 16.dp)) {
                                LoadingDotsIndicator(aiTextColor)
                            }
                        }
                    }
                }
            }

            // 添加额外的空白区域，防止消息被输入框遮挡
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** 单个消息项组件 将消息渲染逻辑提取到单独的组件，减少重组范围 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageItem(
        index: Int,
        message: ChatMessage,
        userMessageColor: Color,
        aiMessageColor: Color,
        userTextColor: Color,
        aiTextColor: Color,
        systemMessageColor: Color,
        systemTextColor: Color,
        thinkingBackgroundColor: Color,
        thinkingTextColor: Color,
        onSelectMessageToEdit: ((Int, ChatMessage, String) -> Unit)?,
        onCopyMessage: ((ChatMessage) -> Unit)?,
        onDeleteMessage: ((Int) -> Unit)?,
        onDeleteMessagesFrom: ((Int) -> Unit)?,
        onSpeakMessage: ((String) -> Unit)? = null, // 添加朗读回调
        onReplyToMessage: ((ChatMessage) -> Unit)? = null, // 添加回复回调
        onCreateBranch: ((Long) -> Unit)? = null, // 添加创建分支回调
        chatStyle: ChatStyle, // 新增参数
        isHidden: Boolean = false, // 新增参数控制隐藏
        isMultiSelectMode: Boolean = false, // 是否处于多选模式
        isSelected: Boolean = false, // 是否被选中
        onToggleSelection: (() -> Unit)? = null, // 切换选中状态的回调
        onToggleMultiSelectMode: ((Int?) -> Unit)? = null, // 切换多选模式的回调，可传入要初始选中的消息索引
        messageIndex: Int // 消息索引，用于进入多选时自动选中
) {
    val context = LocalContext.current
    var showContextMenu by remember { mutableStateOf(false) }

    // 只有用户和AI的消息才能被操作
    val isActionable = message.sender == "user" || message.sender == "ai"

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + expandVertically(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier =
                Modifier.alpha(if (isHidden) 0f else 1f)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                        } else Modifier
                    )
                    .combinedClickable(
                        onClick = {
                            if (isMultiSelectMode && isActionable) {
                                onToggleSelection?.invoke()
                            }
                        },
                        onLongClick = {
                            if (!isMultiSelectMode && isActionable) {
                                showContextMenu = true
                            }
                        },
                    ),
        ) {
            when (chatStyle) {
                ChatStyle.CURSOR -> {
                    CursorStyleChatMessage(
                        message = message,
                        userMessageColor = userMessageColor,
                        aiMessageColor = aiMessageColor,
                        userTextColor = userTextColor,
                        aiTextColor = aiTextColor,
                        systemMessageColor = systemMessageColor,
                        systemTextColor = systemTextColor,
                        thinkingBackgroundColor = thinkingBackgroundColor,
                        thinkingTextColor = thinkingTextColor,
                        supportToolMarkup = true,
                        initialThinkingExpanded = true,
                        onDeleteMessage = onDeleteMessage,
                        index = index,
                        onSpeakMessage = onSpeakMessage
                    )
                }
                ChatStyle.BUBBLE -> {
                    if (message.sender == "user") {
                        BubbleUserMessageComposable(
                            message = message,
                            backgroundColor = userMessageColor,
                            textColor = userTextColor,
                            onSpeakMessage = onSpeakMessage
                        )
                    } else if (message.sender == "ai") {
                        BubbleAiMessageComposable(
                            message = message,
                            backgroundColor = aiMessageColor,
                            textColor = aiTextColor,
                            thinkingBackgroundColor = thinkingBackgroundColor,
                            thinkingTextColor = thinkingTextColor,
                            onSpeakMessage = onSpeakMessage,
                            onCreateBranch = onCreateBranch
                        )
                    } else {
                        // System message fallback
                        CursorStyleChatMessage(
                            message = message,
                            userMessageColor = userMessageColor,
                            aiMessageColor = aiMessageColor,
                            userTextColor = userTextColor,
                            aiTextColor = aiTextColor,
                            systemMessageColor = systemMessageColor,
                            systemTextColor = systemTextColor,
                            thinkingBackgroundColor = thinkingBackgroundColor,
                            thinkingTextColor = thinkingTextColor,
                            supportToolMarkup = true,
                            initialThinkingExpanded = true,
                            onDeleteMessage = onDeleteMessage,
                            index = index,
                            onSpeakMessage = onSpeakMessage
                        )
                    }
                }
            }

            // Context Menu
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                modifier =
                    Modifier.background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
            ) {
                // Reply
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.reply_message)) },
                    onClick = {
                        onReplyToMessage?.invoke(message)
                        showContextMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Reply, contentDescription = stringResource(R.string.reply_message))
                    }
                )

                // Copy
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.copy_message)) },
                    onClick = {
                        val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as
                                        ClipboardManager
                        val cleanContent = cleanXmlTags(message.content)
                        val clipData = ClipData.newPlainText("聊天消息", cleanContent)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(
                                        context,
                                        context.getString(R.string.message_copied),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                        onCopyMessage?.invoke(message)
                        showContextMenu = false
                    },
                    leadingIcon = {
                        leadingIcon = {
                            Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription =
                                            stringResource(id = R.string.edit_and_resend),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.height(40.dp)
                )
            } else if (message.sender == "ai") {
                // 修改记忆选项
                DropdownMenuItem(
                        text = {
                            Text(
                                    stringResource(id = R.string.modify_memory),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onSelectMessageToEdit?.invoke(index, message, "ai")
                            showContextMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription =
                                            stringResource(id = R.string.modify_memory),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.height(40.dp)
                )
            }

            // 删除
            DropdownMenuItem(
                    text = {
                        Text(
                                stringResource(id = R.string.delete),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        onDeleteMessage?.invoke(index)
                        showContextMenu = false
                    },
                    leadingIcon = {
                        Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.height(40.dp)
            )

            // 回复选项
            if (message.sender == "ai") {
                DropdownMenuItem(
                        text = {
                            Text(
                                    stringResource(R.string.reply_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onReplyToMessage?.invoke(message)
                            showContextMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                    imageVector = Icons.Default.Reply,
                                    contentDescription = stringResource(R.string.reply_message),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.height(40.dp)
                )
            }

            // 创建分支
            DropdownMenuItem(
                    text = {
                        Text(
                                stringResource(id = R.string.create_branch),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onCreateBranch?.invoke(message.timestamp)
                        showContextMenu = false
                    },
                    leadingIcon = {
                        Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = stringResource(id = R.string.create_branch),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.height(40.dp)
            )

            // 多选
            DropdownMenuItem(
                    text = {
                        Text(
                                stringResource(id = R.string.multi_select),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onToggleMultiSelectMode?.invoke(messageIndex) // 传入消息索引
                        showContextMenu = false
                    },
                    leadingIcon = {
                        Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(id = R.string.multi_select),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
private fun LoadingDotsIndicator(textColor: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        val jumpHeight = -5f
        val animationDelay = 160

        (0..2).forEach { index ->
            val offsetY by
                    infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = jumpHeight,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation =
                                                    keyframes {
                                                        durationMillis = 600
                                                        0f at 0
                                                        jumpHeight * 0.4f at 100
                                                        jumpHeight * 0.8f at 200
                                                        jumpHeight at 300
                                                        jumpHeight * 0.8f at 400
                                                        jumpHeight * 0.4f at 500
                                                        0f at 600
                                                    },
                                            repeatMode = RepeatMode.Restart,
                                            initialStartOffset =
                                                    StartOffset(index * animationDelay),
                                    ),
                            label = "",
                    )

            Box(
                    modifier =
                            Modifier.size(8.dp)
                                    .offset(y = offsetY.dp)
                                    .background(
                                            color =
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.8f
                                                    ),
                                            shape = CircleShape,
                                    ),
            )
        }
    }
}
