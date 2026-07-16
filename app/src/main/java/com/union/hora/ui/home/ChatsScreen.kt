package com.union.hora.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.union.hora.model.Chat
import com.union.hora.model.PageResult
import com.union.hora.model.RefreshStrategy
import com.union.hora.ui.ChatsViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel = viewModel(),
    onSearchClick: () -> Unit = {}
) {
    val pager by viewModel.currentPager.collectAsState()
    val toast by viewModel.toast.collectAsState()
    val pendingActions by viewModel.pendingActions.collectAsState()

    ChatsScreenContent(
        pager = pager,
        toast = toast,
        pendingActions = pendingActions,
        onSearchClick = onSearchClick,
        onRefresh = { viewModel.refresh(RefreshStrategy.MANUAL) },
        onPinChat = { viewModel.pinChat(it) },
        onDeleteChat = { viewModel.deleteChat(it) },
        onConsumeToast = { viewModel.consumeToast() }
    )
}

@Composable
fun ChatsScreenContent(
    pager: PageResult,
    toast: String?,
    pendingActions: Set<String>,
    onSearchClick: () -> Unit,
    onRefresh: () -> Unit,
    onPinChat: (Chat) -> Unit,
    onDeleteChat: (Chat) -> Unit,
    onConsumeToast: () -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var activeSwipeChatId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        toast?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            onConsumeToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED))
    ) {
        ChatsTopBar(
            onSearchClick = onSearchClick,
            onAddClick = { },
            onRefreshClick = onRefresh,
            isForceRefreshing = pager.showForceRefreshing
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (pager.showLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (pager.showEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无聊天会话", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    items(pager.chats, key = { it.id }) { chat ->
                        SwipeToRevealChatItem(
                            chat = chat,
                            isPending = pendingActions.contains(chat.id),
                            isActiveSwipe = activeSwipeChatId == chat.id,
                            onSwipeActive = { id -> activeSwipeChatId = id },
                            onPin = { onPinChat(chat) },
                            onDelete = { onDeleteChat(chat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatsTopBar(
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isForceRefreshing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onSearchClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color(0xFF181818),
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "聊天",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF181818)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRefreshClick, modifier = Modifier.size(40.dp)) {
                    if (isForceRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF181818)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = Color(0xFF181818),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(onClick = onAddClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "发起聊天",
                        tint = Color(0xFF181818),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeToRevealChatItem(
    chat: Chat,
    isPending: Boolean,
    isActiveSwipe: Boolean,
    onSwipeActive: (String?) -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val actionWidth = 80.dp
    val revealWidthPx = with(density) { (actionWidth * 2).toPx() }
    val swipeState = remember(chat.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isActiveSwipe) {
        if (!isActiveSwipe && swipeState.value != 0f) {
            swipeState.animateTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .background(Color(0xFFC8C7CC))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        onSwipeActive(null)
                        onPin()
                        scope.launch { swipeState.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = if (chat.isPinned) "取消置顶" else "置顶",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (chat.isPinned) "取消置顶" else "置顶",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .background(Color(0xFFFF3B30))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        onSwipeActive(null)
                        onDelete()
                        scope.launch { swipeState.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "删除", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(swipeState.value.roundToInt(), 0) }
                .pointerInput(chat.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            onSwipeActive(chat.id)
                        },
                        onDragEnd = {
                            val target =
                                if (swipeState.value < -revealWidthPx / 2) -revealWidthPx else 0f
                            if (target == 0f) onSwipeActive(null)
                            scope.launch { swipeState.animateTo(target) }
                        },
                        onDragCancel = {
                            val target =
                                if (swipeState.value < -revealWidthPx / 2) -revealWidthPx else 0f
                            if (target == 0f) onSwipeActive(null)
                            scope.launch { swipeState.animateTo(target) }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            swipeState.snapTo(
                                (swipeState.value + dragAmount).coerceIn(-revealWidthPx, 0f)
                            )
                        }
                    }
                }
        ) {
            ChatItemContent(chat = chat, isPending = isPending)
        }
    }
}

@Composable
fun ChatItemContent(chat: Chat, isPending: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(if (chat.isPinned) Color(0xFFF2F2F2) else Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            SubcomposeAsyncImage(
                model = chat.avatar,
                contentDescription = chat.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.name.take(1),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
            if (isPending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF07C160)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF181818),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (chat.isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFC8C7CC))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = "置顶", fontSize = 9.sp, color = Color(0xFF666666))
                    }
                }
            }
            Text(
                text = chat.lastMessage,
                fontSize = 13.sp,
                color = Color(0xFF999999),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = chat.time,
                fontSize = 11.sp,
                color = Color(0xFFB2B2B2)
            )
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(if (chat.unreadCount > 99) 20.dp else 18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(start = 72.dp)
            .background(Color(0xFFE0E0E0))
    )
}

@Preview(showBackground = true)
@Composable
fun ChatsScreenPreview() {
    ChatsScreenContent(
        pager = PageResult(
            chats = listOf(
                Chat("1", "张三", "https://randomuser.me/api/portraits/men/1.jpg", "最近怎么样？", System.currentTimeMillis(), 3, true),
                Chat("2", "李四", "https://randomuser.me/api/portraits/women/2.jpg", "文档已收到", System.currentTimeMillis() - 3600000, 0, false),
                Chat("3", "技术交流群", "https://randomuser.me/api/portraits/men/3.jpg", "架构师: 下周开会", System.currentTimeMillis() - 86400000, 15, false)
            )
        ),
        toast = null,
        pendingActions = emptySet(),
        onSearchClick = {},
        onRefresh = {},
        onPinChat = {},
        onDeleteChat = {},
        onConsumeToast = {}
    )
}
