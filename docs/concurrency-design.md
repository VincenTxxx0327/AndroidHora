# 聊天页整体技术方案

## 1. 架构总览

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (Compose)               │
│  ChatsScreen → ChatsTopBar / SwipeToRevealChatItem   │
│       ↓ viewModel.currentPager (StateFlow)           │
├─────────────────────────────────────────────────────┤
│                 ViewModel Layer                      │
│  ChatsViewModel                                      │
│  ├─ PageResult (chats + isLoading + isForceRefresh) │
│  ├─ actionQueue (ConcurrentHashMap)                 │
│  ├─ 上传循环 (500ms) → processActionQueue           │
│  ├─ 定时同步 (10s)  → fetchFromTim                   │
│  └─ ActionStore (MMKV 持久化)                        │
├─────────────────────────────────────────────────────┤
│                  Model Layer                         │
│  ChatModel                                           │
│  ├─ localChats (synchronized)                        │
│  ├─ 本地操作: pinLocally / unpinLocally / deleteLocally│
│  └─ TIM操作:  pinChatOnTim / fetchChatsFromTim ...    │
├─────────────────────────────────────────────────────┤
│                  Persist Layer                       │
│  ActionStore (MMKV) — 进程级操作持久化               │
└─────────────────────────────────────────────────────┘
```

### 涉及文件

| 文件 | 职责 |
|------|------|
| `ChatModel.kt` | 数据模型 `Chat` + 本地/TIM 数据操作 |
| `PageResult.kt` | 页面状态聚合 + `RefreshStrategy` 枚举 + `Action` sealed class |
| `ActionStore.kt` | MMKV 持久化待上传操作队列 |
| `ChatsViewModel.kt` | 核心业务逻辑：乐观更新、操作队列、上传循环、定时同步 |
| `ChatsScreen.kt` | Compose UI：顶栏、列表、滑动菜单、头像、未读角标 |
| `HoraApp.kt` | Application 初始化 MMKV |
| `AndroidManifest.xml` | 注册 `android:name=".HoraApp"` |

---

## 2. 功能点统计

| 功能 | 描述 | UI表现 | 涉及模块 |
|------|------|--------|----------|
| 首次加载 | 进入页面加载本地数据 | 全屏 CircularProgressIndicator | ViewModel / ChatModel |
| 手动刷新 | 点击刷新按钮，先上传队列再从TIM同步 | 顶栏 Refresh 图标转圈 | ViewModel / ChatModel |
| 静默刷新 | 后台无感知刷新本地数据 | 无任何指示 | ViewModel / ChatModel |
| 定时同步 | 每10秒从TIM获取最新数据 | 无任何指示 | ViewModel / ChatModel |
| 左滑菜单 | 向左滑动露出"置顶/取消置顶"+"删除"按钮 | 80dp×2 操作区 | ChatsScreen |
| 滑动唯一性 | 同一时刻仅一个item展开 | 其他item自动收起 | ChatsScreen |
| 置顶/取消置顶 | 本地立即生效，异步上传TIM | 置顶标签 + 背景变灰 | ViewModel / ChatModel |
| 删除会话 | 本地立即移除，异步上传TIM | item从列表消失 | ViewModel / ChatModel |
| 操作队列去重 | Pin+Unpin抵消；同操作多次取最新时间戳 | 用户无感知 | ViewModel |
| MMKV持久化 | 进程被杀后恢复未上传操作 | App重启后继续上传 | ActionStore |
| 操作pending指示 | 上传中头像右下角显示绿色小转圈 | 16dp CircularProgressIndicator | ChatsScreen / ViewModel |
| 未读角标 | 显示未读数，>99显示"99+" | 红色圆形角标 | ChatsScreen |
| 时间戳格式化 | 刚刚/X分钟前/HH:mm/周几/d/M/yyyy年 | 右上角时间文本 | ChatModel |
| Toast提示 | 操作成功/同步状态提示 | Toast | ViewModel / ChatsScreen |
| 空列表状态 | 无聊天会话时显示提示 | 居中文字"暂无聊天会话" | ChatsScreen |

---

## 3. 核心数据结构

### 3.1 Chat

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 唯一标识 |
| name | String | 聊天名称 |
| avatar | String | 头像URL |
| lastMessage | String | 最后一条消息 |
| timestamp | Long | 最后消息时间戳 |
| unreadCount | Int | 未读数 |
| isPinned | Boolean | 是否置顶 |
| time | String(计算属性) | 由 timestamp 格式化 |

### 3.2 PageResult

| 字段 | 类型 | 说明 |
|------|------|------|
| chats | List\<Chat\> | 当前列表数据 |
| isLoading | Boolean | 全屏loading标志 |
| isForceRefreshing | Boolean | 顶栏转圈标志 |
| strategy | RefreshStrategy | 当前刷新策略 |
| showLoading | Boolean(计算属性) | isLoading && chats为空 |
| showEmpty | Boolean(计算属性) | chats为空 && !isLoading |

| 方法 | 用途 | 是否清除loading |
|------|------|----------------|
| `withLoading(strategy)` | 刷新开始 | 按策略设置 |
| `initData(chats)` | 刷新完成 | 是 |
| `updateChats(chats)` | 局部操作/定时同步 | 否 |
| `updateData(chats)` | SILENT刷新完成 | 是 |

### 3.3 Action (sealed class)

| 子类 | chatId | timestamp | 说明 |
|------|--------|-----------|------|
| Pin | String | Long | 置顶操作 |
| Unpin | String | Long | 取消置顶 |
| Delete | String | Long | 删除会话 |

### 3.4 RefreshStrategy

| 策略 | 场景 | isLoading | isForceRefreshing | 防重复 |
|------|------|-----------|-------------------|--------|
| INITIAL | 首次加载 | true | false | 是 |
| MANUAL | 手动刷新 | false | true | 是 |
| SILENT | 静默刷新 | false | false | 否 |

---

## 4. 功能流程

### 4.1 首次加载流程

```
ChatsScreen 创建
  → viewModel() 创建 ChatsViewModel
    → init {}
      → restorePendingActions()     ← 从MMKV恢复未上传操作
      → refresh(INITIAL)            ← 设置 isLoading=true
      → startUploadLoop()           ← 启动500ms上传循环
      → startPeriodicSync()         ← 启动10s定时同步

refresh(INITIAL)
  → withLoading(INITIAL) → isLoading=true
  → launch { getChatsFromLocal() }  ← delay(400) + synchronized(lock)
  → initData(chats) → isLoading=false
  → UI: showLoading=true → 显示全屏转圈 → 数据到达后显示列表
```

### 4.2 置顶/取消置顶流程

```
用户左滑 → 点击"置顶"按钮
  → onSwipeActive(null) 收起菜单
  → viewModel.pinChat(chat)
    → 判断 chat.isPinned
    ├─ 未置顶: doPin()
    │   → chatModel.pinLocally(chatId)      ← synchronized(lock) 修改 localChats
    │   → _currentPager.update { updateChats(updatedChats) }  ← 保留loading状态
    │   → enqueueAction(Action.Pin(chatId, now))  ← synchronized(actionQueue) + ActionStore.save
    │   → markPending(chatId)              ← pendingActions += chatId
    └─ 已置顶: unpinChat() → 同理但 Action.Unpin

上传循环 (500ms检查)
  → actionQueue.isNotEmpty()
  → processActionQueue()
    → processMutex.tryLock()               ← 防止与MANUAL刷新并发
    → synchronized(actionQueue) { 取快照 }
    → forEach action:
        → executeAction(action)            ← chatModel.pinChatOnTim (delay 1s, 80%成功率)
        → 成功: synchronized { actionQueue.remove + ActionStore.remove } → clearPending → toast "已置顶"
        → 失败: 保留在队列中，500ms后重试
    → unlock()
```

### 4.3 删除会话流程

```
用户左滑 → 点击"删除"按钮
  → viewModel.deleteChat(chat)
    → chatModel.deleteLocally(chatId)       ← synchronized(lock) 从 localChats 移除
    → _currentPager.update { updateChats(updatedChats) }
    → enqueueAction(Action.Delete(chatId, now))
    → markPending(chatId)

上传循环处理 Delete:
  → executeAction → chatModel.deleteChatOnTim (delay 1s, 80%成功率)
  → 成功: remove + ActionStore.remove + clearPending + toast "已删除会话"
  → 失败: 保留重试（但本地已删除，用户看不到）
```

### 4.4 操作队列去重流程

```
enqueueAction(newAction):
  synchronized(actionQueue) {
    existing = actionQueue[chatId]

    ┌──────────────┬──────────────┬─────────────────────────┐
    │ existing     │ newAction    │ 结果                     │
    ├──────────────┼──────────────┼─────────────────────────┤
    │ null         │ 任意          │ 入队 + MMKV save         │
    │ Delete       │ 任意          │ 忽略（Delete优先级最高）   │
    │ Pin/Unpin    │ Delete       │ 覆盖为Delete + MMKV save │
    │ Pin          │ Unpin        │ 互相抵消，移出队列 + MMKV remove │
    │ Unpin        │ Pin          │ 互相抵消，移出队列 + MMKV remove │
    │ Pin          │ Pin          │ 更新为最新时间戳 + MMKV save │
    │ Unpin        │ Unpin        │ 更新为最新时间戳 + MMKV save │
    └──────────────┴──────────────┴─────────────────────────┘
  }
```

### 4.5 手动刷新流程

```
用户点击刷新按钮
  → viewModel.refresh(MANUAL)
    → 检查 isForceRefreshing，防重复
    → withLoading(MANUAL) → isForceRefreshing=true → 顶栏转圈
    → launch(Dispatchers.IO) {
        → processActionQueue()                 ← tryLock，失败则跳过
        → fetchFromTim(clearLoading=true)      ← 从TIM获取 + initData 清除状态
      }

定时同步 (10s)
  → fetchFromTim(clearLoading=false)           ← updateChats 保留状态
  → 不影响顶栏转圈
```

### 4.6 定时同步流程

```
startPeriodicSync() 启动后:
  while(true) {
    delay(10s)
    fetchFromTim(clearLoading=false)
      → chatModel.fetchChatsFromTim()          ← delay(1.5s) + synchronized(lock) 随机更新部分chat
      → _currentPager.update { updateChats }   ← 仅替换chats，保留loading/forceRefreshing
  }
```

### 4.7 MMKV 持久化流程

```
操作入队时:
  enqueueAction → ActionStore.save(action)    ← MMKV.encode("action_$chatId", json)

上传成功时:
  processActionQueue → synchronized { ActionStore.remove(chatId) }  ← MMKV.removeValueForKey

进程重启时:
  ChatsViewModel.init → restorePendingActions()
    → ActionStore.restoreAll()                ← MMKV.allKeys() → 解码每个action
    → actionQueue[chatId] = action             ← 恢复到队列
    → markPending(chatId)                      ← 恢复pending指示
```

### 4.8 滑动唯一性流程

```
ChatsScreen 维护 activeSwipeChatId: String?

用户开始拖拽 itemA:
  → onDragStart → onSwipeActive(itemA.id) → activeSwipeChatId = itemA.id
  → itemB 的 LaunchedEffect(isActiveSwipe) 检测到 isActiveSwipe=false
  → itemB.swipeState.animateTo(0f) → 自动收起

用户拖拽结束:
  → onDragEnd → 判断展开/收起
  → 如果收起: onSwipeActive(null) → activeSwipeChatId = null
  → 如果展开: activeSwipeChatId 保持为 itemA.id

用户点击操作按钮:
  → onSwipeActive(null) → activeSwipeChatId = null → 收起菜单
```

---

## 5. 锁机制总览

### 5.1 锁清单

| 锁 | 类型 | 作用域 | 保护资源 | 持锁时间 |
|----|------|--------|----------|----------|
| `processMutex` | `kotlinx.coroutines.sync.Mutex` | `processActionQueue()` | 防止上传循环与MANUAL刷新并发执行 | 整个上传过程(含网络请求) |
| `synchronized(actionQueue)` | JVM内置锁 | `enqueueAction()` / `processActionQueue()` | `actionQueue` 读写 + `ActionStore` 调用 | 短暂(仅内存操作) |
| `synchronized(lock)` | JVM内置锁 | `ChatModel` 所有方法 | `localChats` 读写 | 短暂(仅内存操作) |
| `StateFlow.update{}` | CAS原子操作 | 所有 `_currentPager` / `_pendingActions` 写入 | StateFlow 值更新 | 极短(CAS循环) |
| MMKV内部锁 | MMKV C++层 | `ActionStore` 所有方法 | MMKV 文件读写 | 短暂 |

### 5.2 锁获取顺序（单向，无死锁）

```
processMutex.tryLock()                    ← 第1层（协程锁）
    ↓
synchronized(actionQueue)                 ← 第2层（JVM锁）
    ↓
ActionStore.save/remove() → MMKV内部锁    ← 第3层
```

**规则**：绝不反向获取（如先 `synchronized` 再 `tryLock`），避免死锁。

### 5.3 tryLock 设计

`processActionQueue()` 使用 `tryLock()` 而非 `withLock()`：

| 调用方 | tryLock失败 | 后果 | 是否可接受 |
|--------|------------|------|-----------|
| 上传循环 | 跳过本次 | 500ms后重试，操作不丢失 | 是 |
| MANUAL刷新 | 跳过队列处理 | 直接fetchFromTim，上传循环后续处理 | 是 |

---

## 6. 设计细节

### 6.1 乐观更新

用户操作（置顶/删除）先在本地立即生效，再异步上传TIM。保证即时反馈，不阻塞UI。

```
用户点击 → pinLocally() (本地立即生效) → enqueueAction() (异步上传)
                                              ↓
                                        成功: clearPending
                                        失败: 保留重试
```

### 6.2 PageResult 状态聚合

将 `chats`、`isLoading`、`isForceRefreshing` 整合到一个 `PageResult`，避免多个 StateFlow 之间的状态不一致。

**关键区分**：
- `updateChats()` — 仅替换数据，保留 loading 状态（局部操作/定时同步用）
- `initData()` — 替换数据 + 清除所有 loading 状态（刷新完成用）

### 6.3 fetchFromTim(clearLoading) 参数

防止定时同步清除 MANUAL 刷新状态：

| 调用方 | clearLoading | 效果 |
|--------|-------------|------|
| 定时同步 | false | `updateChats()` 保留 isForceRefreshing |
| MANUAL刷新 | true | `initData()` 清除 isForceRefreshing |

### 6.4 ActionStore.remove 在 synchronized 块内

防止 `processActionQueue` 的 remove 与 `enqueueAction` 的 save 竞态：

```
// 修复前（有bug）:
synchronized { actionQueue.remove(A) }   ← 释放锁
                                          ← 此时 enqueueAction 可能写入新A到MMKV
ActionStore.remove(A)                    ← 把新A也删了！

// 修复后:
synchronized {
    actionQueue.remove(A)
    ActionStore.remove(A)                ← 在锁内，不会被打断
}
```

### 6.5 ChatModel.synchronized(lock)

`localChats` 是普通 `mutableListOf`，被多线程访问：
- `pinLocally` 等（Main线程）
- `getChatsFromLocal` / `fetchChatsFromTim`（IO线程）

用 `synchronized(lock)` 保护所有访问，避免 `ConcurrentModificationException`。

### 6.6 MMKV by lazy 初始化

`ActionStore` 是 `object`，`mmkv` 字段用 `by lazy` 延迟初始化，确保首次访问时 `HoraApp.onCreate()` 中的 `MMKV.initialize()` 已执行。

### 6.7 时间戳格式化

`Chat.time` 是计算属性，每次访问实时计算显示文本：

| 时间差 | 显示 |
|--------|------|
| < 1分钟 | "刚刚" |
| < 1小时 | "X分钟前" |
| 今天 | "HH:mm" |
| 7天内 | "周几" |
| 7天外 | "d/M/yyyy年" |

### 6.8 日志体系

| 标签 | 场景 | 示例 |
|------|------|------|
| `[enqueue]` | 操作入队/抵消/覆盖 | `[enqueue] chatId=1001 新操作=Pin 入队，当前队列大小=1` |
| `[upload-loop]` | 上传循环检测到队列 | `[upload-loop] 检测到队列非空，大小=3，触发上传` |
| `[process]` | processActionQueue | `[process] tryLock 成功，待处理操作数=3` |
| `[process]` | 单个操作上传 | `[process] 开始上传 chatId=1001 action=Pin` |
| `[process]` | 上传成功 | `[process] chatId=1001 上传成功，剩余队列大小=0` |
| `[process]` | 上传失败 | `[process] chatId=1001 上传失败！500ms后重试` |
| `[process]` | tryLock竞争 | `[process] tryLock 失败，跳过本次` |
| `[periodic-sync]` | 定时同步 | `[periodic-sync] 10秒定时同步触发，clearLoading=false` |
| `[sync]` | fetchFromTim | `[sync] fetchFromTim 完成，使用 updateChats 保留 loading 状态` |
| `[refresh]` | 刷新策略 | `[refresh] MANUAL 开始：先处理队列，再 fetchFromTim(clearLoading=true)` |

---

## 7. 优点

| 优点 | 说明 |
|------|------|
| **乐观更新** | 用户操作即时反馈，不阻塞UI |
| **操作队列去重** | Pin+Unpin抵消，同操作取最新，减少无意义网络请求 |
| **MMKV持久化** | 进程被杀后操作不丢失，重启自动恢复继续上传 |
| **PageResult状态聚合** | 单一数据源，避免多StateFlow状态不一致 |
| **updateChats vs initData** | 局部操作不清除刷新状态，避免顶栏转圈闪烁 |
| **tryLock非阻塞** | 上传循环与MANUAL刷新互不阻塞，失败方自动跳过 |
| **synchronized保护** | actionQueue和localChats线程安全，无ConcurrentModificationException |
| **StateFlow.update CAS** | 跨线程更新_currentPager不丢失 |
| **滑动唯一性** | activeSwipeChatId确保同一时刻仅一个item展开 |
| **时间戳计算属性** | 实时格式化，数据层只存timestamp，展示层自动适配 |
| **详细日志** | 结构化标签，便于排查上传失败和竞态问题 |

---

## 8. 缺点

| 缺点 | 影响 | 改进方向 |
|------|------|----------|
| **删除失败无回滚** | 本地已删除但TIM未同步，item永久丢失，用户无感知 | 删除前保存snapshot，失败时restoreLocally恢复 |
| **10秒定时同步过于频繁** | 无操作时仍每10秒网络请求，浪费电量 | 改为WebSocket长连接推送，或30-60秒间隔 |
| **processActionQueue持锁含网络请求** | tryLock成功后整个forEach(含delay 1s/操作)期间持锁，MANUAL刷新必然tryLock失败 | 将网络请求移到锁外，或改为更细粒度的per-action锁 |
| **Toast互相覆盖** | 快速连续操作时toast被覆盖 | 改用Channel队列化toast |
| **fetchChatsFromTim基于localChats模拟** | 真实场景服务端可能恢复已删除的chat，与pending Delete冲突 | 同步时需过滤pending操作涉及的chatId |
| **上传成功率80%模拟** | 测试环境失败率过高，实际场景应更高 | 模拟参数可配置 |
| **50条数据全内存** | 数据量大时内存压力 | 改为分页加载 + Room数据库 |
| **无网络状态检测** | 离线时上传循环持续重试 | 检测网络状态，离线暂停上传 |

---

## 9. 线程安全总览

### 9.1 共享资源与保护方式

| 资源 | 访问者 | 保护方式 | 风险 |
|------|--------|---------|------|
| `actionQueue` | enqueueAction(Main), processActionQueue(IO) | `synchronized(actionQueue)` | 无 |
| `localChats` | pinLocally(Main), getChatsFromLocal/fetchChatsFromTim(IO) | `synchronized(lock)` | 无 |
| `_currentPager` | 所有协程 | `update{}` CAS | 无 |
| `_pendingActions` | 所有协程 | `update{}` CAS | 无 |
| `processActionQueue` | 上传循环 + MANUAL刷新 | `processMutex.tryLock()` | 无 |
| MMKV | enqueueAction + processActionQueue | 在 `synchronized(actionQueue)` 内 | 无 |

### 9.2 协程调度

| 协程 | Dispatcher | 生命周期 |
|------|-----------|----------|
| 上传循环 | `Dispatchers.IO` | ViewModelScope |
| 定时同步 | `Dispatchers.IO` | ViewModelScope |
| INITIAL/MANUAL刷新 | `Dispatchers.IO` (MANUAL) / 默认 (INITIAL) | ViewModelScope |
| 局部操作(pin/delete) | Main (同步调用) | 即时 |

---

## 10. 测试用例统计

### 10.1 单元测试 (PageResultTest)

| 用例 | 验证内容 | 关键断言 |
|------|----------|----------|
| `withLoading INITIAL sets isLoading true` | INITIAL策略设置isLoading | isLoading=true, isForceRefreshing=false |
| `withLoading MANUAL sets isForceRefreshing true` | MANUAL策略设置isForceRefreshing | isForceRefreshing=true, isLoading=false |
| `withLoading SILENT clears both loading flags` | SILENT策略清除所有flag | isLoading=false, isForceRefreshing=false |
| `initData clears all loading states` | initData清除状态 | isLoading=false, isForceRefreshing=false, chats非空 |
| `updateChats preserves loading states` | **核心修复**：updateChats不清除forceRefreshing | isForceRefreshing=true(保留), chats已替换 |
| `updateData clears loading states` | updateData清除状态 | isLoading=false, isForceRefreshing=false |
| `showLoading only true when isLoading and chats empty` | showLoading计算属性 | 仅isLoading且chats为空时为true |
| `showEmpty only true when chats empty and not loading` | showEmpty计算属性 | 仅chats为空且非loading时为true |

### 10.2 Instrumented测试 (ChatsViewModelRaceConditionTest)

| 用例 | 验证内容 | 关键断言 |
|------|----------|----------|
| `concurrentProcessActionQueue_completesWithoutDeadlock` | 5操作入队 + MANUAL刷新并发，无死锁 | pendingActions为空, isForceRefreshing=false, MMKV无残留 |
| `periodicSync_doesNotClearForceRefreshing_duringManualRefresh` | 定时同步不清除MANUAL刷新状态 | MANUAL进行中isForceRefreshing=true, 完成后=false |
| `pinThenUnpin_cancelsActionInQueueAndMmkv` | Pin+Unpin抵消后队列和MMKV均无残留 | MMKV为空, pendingActions为空, isPinned不变 |
| `mmkv_persistsAndRestoresActions` | 操作入队写入MMKV，上传成功后移除 | 入队后MMKV有记录, 上传后无残留 |

---

## 11. 文件依赖关系

```
AndroidManifest.xml
  └─ android:name=".HoraApp"
       └─ MMKV.initialize(this)

ChatsScreen.kt
  ├─ import ChatsViewModel
  ├─ import PageResult / RefreshStrategy
  └─ import Chat

ChatsViewModel.kt
  ├─ import ChatModel
  ├─ import PageResult / RefreshStrategy / Action
  └─ import ActionStore

ChatModel.kt
  └─ data class Chat (含 time 计算属性)

PageResult.kt
  ├─ enum RefreshStrategy
  ├─ data class PageResult
  └─ sealed class Action

ActionStore.kt
  ├─ MMKV (by lazy)
  └─ Gson
```
