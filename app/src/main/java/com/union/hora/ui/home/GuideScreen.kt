package com.union.hora.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.union.hora.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/** 引导页背景色：暖米色 */
private val GuideBackground = Color(0xFFF9F5EE)

/** 指示条激活态颜色：近黑色 */
private val IndicatorActiveColor = Color(0xFF171717)

/** 指示条非激活态颜色：浅灰 */
private val IndicatorInactiveColor = Color(0xFFD9D2C5)

/** "Enter app" 按钮颜色：绿色 */
private val EnterAppButtonColor = Color(0xFF00C687)

/** 标题文字颜色 */
private val TitleTextColor = Color(0xFF171717)

/** 描述文字颜色 */
private val DescriptionTextColor = Color(0xFF8A857C)

/** 分类标签颜色 */
private val CategoryTextColor = Color(0xFF171717)

/** 圆形图片散开动效的彩色波点颜色 */
private val ScatterDotColors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
    Color(0xFF4D96FF), Color(0xFFFF6BCB), Color(0xFFA66CFF),
    Color(0xFFFF9F43), Color(0xFF48DBFB), Color(0xFF1DD1A1),
    Color(0xFFF368E0), Color(0xFFFFC312), Color(0xFF54A0FF)
)

/**
 * 单个波点的散开参数（每次翻页重新生成，实现随机路径与随机半径）。
 *
 * @param startAngle       随机起始角度（弧度），决定波点散开的方向。
 * @param radiusMultiplier 随机半径倍数（范围 1.5~3.5），决定波点最终散开距离。
 * @param angularDrift     随机角偏移（弧度），随散开进度叠加在角度上形成曲线路径。
 * @param color            打乱后的颜色，保证每次翻页颜色顺序随机。
 */
private data class DotSpec(
    val startAngle: Float,
    val radiusMultiplier: Float,
    val angularDrift: Float,
    val color: Color
)

// ===================== 背景光晕刷新动效 =====================

/**
 * 单个引导页的光晕颜色配置。
 *
 * @param primary   主色（主光晕）。
 * @param secondary 副色（副光晕，0.3 透明度的主题绿）。
 */
private data class HaloColors(val primary: Color, val secondary: Color)

/**
 * 各页面光晕颜色配置（需求2）。
 * 副色统一为 0.3 透明度的主题绿 #00C687（0.3 × 255 ≈ 77 = 0x4D）。
 */
private val pageHaloColors = listOf(
    HaloColors(Color(0xFFFFD84A), Color(0x4D00C687)),
    HaloColors(Color(0xFF2BB3A3), Color(0x4D00C687)),
    HaloColors(Color(0xFFC93A87), Color(0x4D00C687)),
    HaloColors(Color(0xFFE04090), Color(0x4D00C687)),
    HaloColors(Color(0xFF7C4DFF), Color(0x4D00C687))
)

/**
 * 单个光晕内的子波瓣参数。多个子波瓣叠加形成非规则圆形光晕（需求3：避免标准几何圆形）。
 *
 * @param offsetX       子波瓣中心相对光晕中心的 x 偏移（半径比例）。
 * @param offsetY       子波瓣中心相对光晕中心的 y 偏移（半径比例）。
 * @param radiusFactor  子波瓣半径相对光晕主半径的比例。
 */
private data class BlobSpec(
    val offsetX: Float,
    val offsetY: Float,
    val radiusFactor: Float
)

/**
 * 单个光晕规格。
 *
 * @param cx     中心 x 坐标（像素）。
 * @param cy     中心 y 坐标（像素）。
 * @param radius 主半径（像素）。
 * @param color  光晕颜色。
 * @param blobs  子波瓣列表（叠加形成非规则形状）。
 */
private data class HaloSpec(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val color: Color,
    val blobs: List<BlobSpec>
)

/**
 * 一对光晕规格（每次翻页刷新生成 2 处光晕）。
 */
private data class HaloPairSpec(
    val halo1: HaloSpec,
    val halo2: HaloSpec
)

/**
 * 生成一对随机光晕规格（需求1）。
 *
 * - 两处光晕 x、y 随机生成；
 * - 两个光晕中心距离 >= 屏幕宽度（需求1：至少等于屏幕宽度的距离）；
 * - 每个光晕由 3~4 个随机子波瓣组成，形成非规则形状（需求3）；
 * - 半径随机但限定在 baseR × 0.8~1.3 范围内（需求3：半径随机在一定范围值）。
 */
private fun generateHaloPair(
    pageIndex: Int,
    width: Float,
    height: Float,
    random: Random
): HaloPairSpec {
    val colors = pageHaloColors[pageIndex.coerceIn(pageHaloColors.indices)]

    val x1 = random.nextFloat() * width
    val y1 = random.nextFloat() * height

    // 第二个光晕中心：随机生成，且与第一个的距离 >= 屏幕宽度（拒绝采样，最多 30 次）
    var x2 = random.nextFloat() * width
    var y2 = random.nextFloat() * height
    var attempts = 0
    while (hypot(x2 - x1, y2 - y1) < width && attempts < 30) {
        x2 = random.nextFloat() * width
        y2 = random.nextFloat() * height
        attempts++
    }

    val baseR = width * 0.45f
    val r1 = baseR * (0.8f + random.nextFloat() * 0.5f)
    val r2 = baseR * (0.8f + random.nextFloat() * 0.5f)

    return HaloPairSpec(
        halo1 = HaloSpec(x1, y1, r1, colors.primary, generateBlobs(random)),
        halo2 = HaloSpec(x2, y2, r2, colors.secondary, generateBlobs(random))
    )
}

/** 生成 3~4 个随机子波瓣，用于构造非规则光晕形状。 */
private fun generateBlobs(random: Random): List<BlobSpec> {
    val count = 3 + random.nextInt(2) // 3 或 4
    return List(count) {
        BlobSpec(
            offsetX = (random.nextFloat() - 0.5f) * 0.6f,
            offsetY = (random.nextFloat() - 0.5f) * 0.6f,
            radiusFactor = 0.55f + random.nextFloat() * 0.5f
        )
    }
}

/**
 * 绘制一对光晕（需求1：两处光晕 + 之间线性渐变过渡 + 向外过渡至背景）。
 *
 * 实现要点：
 * 1. 用粗描边 + 线性渐变连接两个光晕中心，形成两色之间的平滑过渡（需求1：线性渐变）；
 * 2. 每个光晕由多个子波瓣（径向渐变）叠加构成非规则形状，向外渐变至透明（即过渡至背景色）；
 * 3. 整体透明度由 [alpha] 控制，用于翻页时的交叉淡入淡出。
 */
private fun DrawScope.drawHaloPair(spec: HaloPairSpec, alpha: Float) {
    if (alpha <= 0.01f) return

    val c1 = Offset(spec.halo1.cx, spec.halo1.cy)
    val c2 = Offset(spec.halo2.cx, spec.halo2.cy)
    val connectionAlpha = alpha * 0.15f
    if (connectionAlpha <= 0.005f) return

    val baseWidth = (spec.halo1.radius + spec.halo2.radius) * 0.35f
    val halfWidth = baseWidth / 2f
    val lineVector = c2 - c1
    val lineLength = lineVector.getDistance()
    val centerPoint = (c1 + c2) / 2f

    // 两端基础色
    val baseColor1 = spec.halo1.color.copy(alpha = connectionAlpha)
    val baseColor2 = spec.halo2.color.copy(alpha = connectionAlpha)

//    // 1. 底层垂直蒙版：上下边缘线性透明模糊（处理椭圆上下边柔化）
//    val verticalMaskBrush = Brush.linearGradient(
//        colors = listOf(
//            baseColor1.copy(alpha = 0f),
//            baseColor1.copy(alpha = 0.3f),
//            baseColor1.copy(alpha = 0f)
//        ),
//        start = Offset(0f, -halfWidth),
//        end = Offset(0f, halfWidth),
//        tileMode = TileMode.Clamp
//    )
//
//    // 2. 上层水平主渐变：左端色→右端色 + 左右头尾透明模糊（处理椭圆两头柔化+色彩渐变）
//    val horizontalMainBrush = Brush.linearGradient(
//        colors = listOf(
//            baseColor1.copy(alpha = 0f),  // 最左端完全透明，头尾模糊
//            baseColor1.copy(alpha = 1f),                    // 左段完整光晕1颜色
//            baseColor2.copy(alpha = 1f),                    // 中线过渡到光晕2颜色
//            baseColor2.copy(alpha = 0f)    // 最右端完全透明，头尾模糊
//        ),
//        start = Offset(-lineLength / 2, 0f),
//        end = Offset(lineLength / 2, 0f),
//        tileMode = TileMode.Clamp
//    )
//
//    // 胶囊椭圆路径（完整圆角长条，整体为椭圆形态）
//    val capsulePath = Path().apply {
//        addRoundRect(
//            roundRect = RoundRect(
//                rect = Rect(
//                    left = -lineLength / 2,
//                    top = -halfWidth,
//                    right = lineLength / 2,
//                    bottom = halfWidth
//                ),
//                radiusX = halfWidth,
//                radiusY = halfWidth
//            )
//        )
//    }
//
//    // 变换：旋转到两点连线角度 + 平移到两圆心中点
//    val angle = Math.toDegrees(atan2(lineVector.y.toDouble(), lineVector.x.toDouble())).toFloat()
//    translate(centerPoint.x, centerPoint.y) {
//        rotate(angle, pivot = Offset.Zero) {
//            // 先画底层上下边缘蒙版
//            drawPath(path = capsulePath, brush = verticalMaskBrush)
//            // 再叠加上层水平色彩+头尾模糊层
//            drawPath(path = capsulePath, brush = horizontalMainBrush)
//        }
//    }

    // 绘制两个光晕本体
    drawIrregularHalo(spec.halo1, alpha)
    drawIrregularHalo(spec.halo2, alpha)
}

/** 绘制单个非规则光晕：多个径向渐变子波瓣叠加，向外渐变至透明（过渡至背景）。 */
private fun DrawScope.drawIrregularHalo(halo: HaloSpec, alpha: Float) {
    val center = Offset(halo.cx, halo.cy)
    for (blob in halo.blobs) {
        val blobCenter = Offset(
            center.x + blob.offsetX * halo.radius,
            center.y + blob.offsetY * halo.radius
        )
        val blobRadius = halo.radius * blob.radiusFactor
        val brush = Brush.radialGradient(
            colors = listOf(
                halo.color.copy(alpha = alpha * 0.55f),
                halo.color.copy(alpha = 0f)
            ),
            center = blobCenter,
            radius = blobRadius
        )
        drawCircle(brush = brush, center = blobCenter, radius = blobRadius)
    }
}

/**
 * 引导页单页文案数据。
 *
 * @param category    顶部分类标签（如 "CHAT"、"EVENTS"），字母间距较大。
 * @param title       标题文案。
 * @param description 描述文案。
 * @param imageRes    对应图片资源。
 */
private data class GuidePage(
    val category: String,
    val title: String,
    val description: String,
    val imageRes: Int
)

/**
 * 引导页数据：5 张引导图及对应文案。
 * 文案内容提取自 "Start to dev" 目录内的设计截图（通过 OCR 识别）。
 */
private val guidePages = listOf(
    GuidePage(
        category = "CHAT",
        title = "Chat & connect",
        description = "Chat with friends, groups, and new people effortlessly.",
        imageRes = R.drawable.guide_1
    ),
    GuidePage(
        category = "EVENTS",
        title = "Discover live events",
        description = "Find gigs, campus events, and rooftop nights nearby, then RSVP in one tap.",
        imageRes = R.drawable.guide_2
    ),
    GuidePage(
        category = "COMMUNITY",
        title = "Find your people",
        description = "Meet classmates and new crews who share your vibe and interests.",
        imageRes = R.drawable.guide_3
    ),
    GuidePage(
        category = "SHARE CONTENT",
        title = "Share your moments",
        description = "Post photos, videos, and stories with the people who actually want to see them.",
        imageRes = R.drawable.guide_4
    ),
    GuidePage(
        category = "AND MORE",
        title = "Do even more",
        description = "Pay securely, play mini games, and unlock more ways to connect in ZymiX.",
        imageRes = R.drawable.guide_5
    )
)

/**
 * 引导页。
 *
 * 布局结构（需求5精确计算）：
 * - 垂直可用空间 = 屏幕高度 - 状态栏高度 - 导航栏高度 - 屏幕宽度
 * - HorizontalPager 外层 Box 为正方形（边长 = 屏幕宽度）
 * - Box 距顶部 = 垂直可用空间 × 3/4，距底部 = 垂直可用空间 × 1/4
 *
 * 交互增强：
 * - 需求2：通过 nestedScroll 增加水平滑动阻力，防止轻微滑动触发翻页
 * - 需求4：点击左右两侧图片可触发翻页（左→上一页，右→下一页）
 *
 * @param onEnterApp 点击 "Enter app"（最后一张图）后的回调，通常进入主应用。
 * @param onSkip     点击 "skip" 的回调，通常返回上一页。
 */
@Composable
fun GuideScreen(
    onEnterApp: () -> Unit,
    onSkip: () -> Unit
) {
    val pageCount = guidePages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    // 是否已滑动到最后一张：用于控制 "Enter app" 按钮的显示
    val isLastPage by remember {
        derivedStateOf { pagerState.currentPage == pageCount - 1 }
    }

    // 需求2：滑动灵敏度优化 —— 通过 nestedScroll 拦截水平滚动，消耗 35% 的滚动距离。
    // 效果：pager 实际只接收到 65% 的手指拖动量，翻页所需的手指滑动距离从约 50% 页宽提升至约 77% 页宽，
    // 有效防止轻微滑动意外触发页面切换。
    val swipeResistance = 0.2f
    val pagerNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 仅在用户拖拽（Drag）时施加阻力，fling（惯性滑动）时不拦截，
                // 避免快速滑动时 snap 动画被消耗导致中间图片无法居中
                if (available.x != 0f && source == NestedScrollSource.UserInput) {
                    return Offset(available.x * swipeResistance, 0f)
                }
                return Offset.Zero
            }
        }
    }

    // 需求4-2：散开动画单向播放，到达终态后不再收缩回来。
    // 需求4-3：使用 Animatable + FastOutSlowInEasing 实现非线性缓动散开。
    val scatterAnim = remember { Animatable(1f) }
    // 标记是否已初始化，避免首次进入页面时误触发动画
    var scatterInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) {
        if (!scatterInitialized) {
            scatterInitialized = true
            return@LaunchedEffect
        }
        // 翻页完成后：从 0 散开到 1，单向不反转
        scatterAnim.snapTo(0f)
        scatterAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    // 需求4-1：每次翻页打乱 ScatterDotColors 顺序，获得随机颜色。
    // 需求4-3：同时为每个波点生成随机起始角度、随机半径倍数、随机角偏移，
    // 从而实现非线性散开 + 随机路径 + 随机半径（半径范围限定在 baseRadius × 1.5~3.5）。
    val dotSpecs = remember(pagerState.currentPage) {
        val random = Random(System.nanoTime())
        ScatterDotColors.shuffled(random).map { color ->
            DotSpec(
                startAngle = random.nextFloat() * 2f * PI.toFloat(),
                radiusMultiplier = 1.5f + random.nextFloat() * 2.0f, // 1.5 ~ 3.5
                angularDrift = (random.nextFloat() - 0.5f) * 1.0f,    // -0.5 ~ 0.5 弧度
                color = color
            )
        }
    }

    // 需求5：精确计算外层容器布局参数
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GuideBackground)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val statusBarHeight = WindowInsets.statusBars
            .asPaddingValues()
            .calculateTopPadding()
        val navBarHeight = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

        // 垂直可用空间 = 屏幕高度 - 状态栏高度 - 导航栏高度 - 屏幕宽度
        val verticalAvailable = (screenHeight - statusBarHeight - navBarHeight - screenWidth)
            .coerceAtLeast(0.dp)
        // Box 距顶部 5/9，距底部 4/9
        val topSpace = verticalAvailable * 0.5f
        val bottomSpace = verticalAvailable * 0.5f

        // 需求3：contentPadding 动态值，根据屏幕宽度计算（0.065 × 屏幕宽度）
        val pagerContentPadding = screenWidth * 0.18f

        // 需求1：pageSpacing 动态值，根据屏幕宽度按比例计算
        // 计算公式：pageSpacing = screenWidth × (-0.35f)
        // 参考基准：400dp 屏幕下 -140dp = 400 × (-0.35)，负值使页面重叠形成堆叠效果
        // 不同屏幕尺寸下保持一致的视觉比例
        val pagerPageSpacing = screenWidth * -0.35f

        // ===================== 背景光晕刷新动效（需求1/2/3/4）=====================
        // 翻页时刷新 2 处非规则光晕，两光晕之间线性渐变过渡，向外渐变至背景色。
        // 使用 Animatable 驱动交叉淡入淡出，绘制仅在 draw 阶段读取状态，避免重组。
        val density = LocalDensity.current
        val screenWidthPx = with(density) { screenWidth.toPx() }
        val screenHeightPx = with(density) { screenHeight.toPx() }

        var currentHalos by remember { mutableStateOf<HaloPairSpec?>(null) }
        var previousHalos by remember { mutableStateOf<HaloPairSpec?>(null) }
        val haloTransition = remember { Animatable(1f) }
        var haloInitialized by remember { mutableStateOf(false) }

        LaunchedEffect(pagerState.currentPage) {
            if (!haloInitialized) {
                // 首次进入：生成初始光晕，无需过渡动画
                haloInitialized = true
                currentHalos = generateHaloPair(
                    pagerState.currentPage, screenWidthPx, screenHeightPx,
                    Random(System.nanoTime())
                )
                haloTransition.snapTo(1f)
                return@LaunchedEffect
            }
            // 翻页刷新：保留旧光晕用于淡出，生成新光晕并交叉淡入
            previousHalos = currentHalos
            currentHalos = generateHaloPair(
                pagerState.currentPage, screenWidthPx, screenHeightPx,
                Random(System.nanoTime())
            )
            haloTransition.snapTo(0f)
            haloTransition.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
            // 过渡完成后释放旧光晕引用，避免内存占用（需求4：避免内存泄漏）
            previousHalos = null
        }

        // 背景光晕层：位于内容之下，铺满全屏绘制
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val t = haloTransition.value
                    // 旧光晕淡出
                    previousHalos?.let { drawHaloPair(it, 1f - t) }
                    // 新光晕淡入
                    currentHalos?.let { drawHaloPair(it, t) }
                }
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 状态栏占位
            Spacer(Modifier.height(statusBarHeight))

            // ---------- 顶部区域（高度 = topSpace）----------
            // 包含：指示条 + 圆形图片 + 左对齐文案
            Column(
                modifier = Modifier
                    .height(topSpace)
                    .fillMaxWidth()
            ) {
                // 滑动进度指示条（左对齐）
                GuideProgressIndicator(
                    pagerState = pagerState,
                    pageCount = pageCount,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 24.dp,
                        end = 24.dp
                    )
                )

                // 需求1：圆形图片位于上方（左对齐），文案位于下方（左对齐）
                // 参考 final_top.png 设计稿：圆形图片 → 间距 → 索引文案+标题
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                ) {
                    // 圆形图片：显示当前页对应的引导图缩略图（左对齐，位于指示条下侧）
                    // 需求3：翻页时圆形周边散开彩色波点动效
                    // drawBehind 放在 clip(CircleShape) 之前，使波点不被圆形裁剪
                    Image(
                        painter = painterResource(
                            guidePages[pagerState.currentPage].imageRes
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                // 需求4-2：散开进度由 scatterAnim 驱动（单向 0→1），不再随滑动偏移返回而收缩
                                val progress = scatterAnim.value
                                // 动画结束后（progress=1）波点已完全透明且半径为 0，无需绘制
                                if (progress >= 0.99f) return@drawBehind

                                val center = Offset(size.width / 2f, size.height / 2f)
                                val baseRadius = size.minDimension / 2f
                                // 需求4-3：非线性缓动，散开先快后慢
                                val easedProgress = FastOutSlowInEasing.transform(progress)

                                // 波点大小：4dp → 0dp，随散开进度缩小
                                val dotDiameterPx = lerp(4.dp.toPx(), 0.dp.toPx(), easedProgress)
                                val dotRadius = dotDiameterPx / 2f
                                // 波点透明度：完全不透明 → 完全透明
                                val dotAlpha = (1f - easedProgress).coerceIn(0f, 1f)

                                for (spec in dotSpecs) {
                                    // 需求4-3：随机半径，范围 = baseRadius × (1.5 ~ 3.5) × progress
                                    val radius = baseRadius + baseRadius * spec.radiusMultiplier * easedProgress
                                    // 需求4-3：随机曲线路径，角度随进度叠加 angularDrift
                                    val angle = spec.startAngle + spec.angularDrift * easedProgress
                                    val x = center.x + radius * cos(angle)
                                    val y = center.y + radius * sin(angle)
                                    drawCircle(
                                        color = spec.color.copy(alpha = dotAlpha),
                                        radius = dotRadius,
                                        center = Offset(x, y)
                                    )
                                }
                            }
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // 间距后放置左对齐文案（位于圆形图片下方）
                    Spacer(Modifier.height(24.dp))

                    // 左对齐文案区域
                    GuideTextContent(
                        pagerState = pagerState,
                        pages = guidePages,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ---------- 中间区域（正方形 Box，边长 = 屏幕宽度）----------
            // 需求5：HorizontalPager 外部 Box 设为与屏幕宽度等高的正方形
            Box(
                modifier = Modifier.size(screenWidth, screenWidth),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(pagerNestedScrollConnection),
                    // 两侧留白，让相邻图片以半透明窄椭圆形态露出，形成堆叠效果
                    contentPadding = PaddingValues(horizontal = pagerContentPadding),
                    pageSpacing = pagerPageSpacing,
                    // 预加载左右1张卡片，实现堆叠效果必备
                    beyondViewportPageCount = 1
                ) { page ->
                    StackedGuideImage(
                        imageRes = guidePages[page].imageRes,
                        pagerState = pagerState,
                        page = page,
                        screenWidth = screenWidth,
                        // 需求4：点击左右两侧图片触发翻页
                        onClick = {
                            if (page < pagerState.currentPage) {
                                // 点击左侧图片 → 切换到上一页
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage - 1
                                    )
                                }
                            } else if (page > pagerState.currentPage) {
                                // 点击右侧图片 → 切换到下一页
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        pagerState.currentPage + 1
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // ---------- 底部区域（高度 = bottomSpace）----------
            Box(
                modifier = Modifier
                    .height(bottomSpace)
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
            ) {

                // "Enter app" 按钮：浮动在图片区域底部，左右居中
                GuideEnterAppButton(
                    visible = isLastPage,
                    onEnterApp = onEnterApp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        // 固定触摸最小高度44dp，宽度自适应padding
                        .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSkip
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "skip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DescriptionTextColor
                    )
                }
            }

            // 导航栏占位
            Spacer(Modifier.height(navBarHeight))
        }
    }
}

/**
 * 浮动的 "Enter app" 按钮。
 * 独立为单独的 Composable 以避免外层 ColumnScope 对 AnimatedVisibility 的重载遮蔽。
 * 通过 [Modifier.align] 在父 Box 中左右居中、贴底显示。
 */
@Composable
private fun GuideEnterAppButton(
    visible: Boolean,
    onEnterApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.65f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.65f),
        modifier = modifier.padding(top = 16.dp, bottom = 24.dp)
    ) {
        Button(
            onClick = onEnterApp,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = EnterAppButtonColor,
                contentColor = Color.Black
            ),
            modifier = Modifier.size(width = 125.dp, height = 44.dp)
        ) {
            Text(
                text = "Enter app",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 指示条与图片之间的文案区域。
 *
 * 需求1：文案采用左对齐排列方式。
 *
 * 使用 [AnimatedContent] 在页面切换时实现文案的淡入淡出 + 左右滑动过渡。
 * 滑动方向根据前后页面索引判断：向前翻（索引增大）新文案从右滑入，
 * 向后翻（索引减小）新文案从左滑入，与 [HorizontalPager] 的滑动方向保持一致。
 * 文案内容由 [PagerState.currentPage] 驱动，滑动 settles 后切换。
 */
@Composable
private fun GuideTextContent(
    pagerState: PagerState,
    pages: List<GuidePage>,
    modifier: Modifier = Modifier
) {
    val currentIndex = pagerState.currentPage
    val currentPage = pages[currentIndex]

    // 需求1：使用 Pair(index, page) 作为状态，索引变化时也触发动画
    AnimatedContent(
        targetState = currentIndex to currentPage,
        transitionSpec = {
            // 根据前后页面索引判断翻页方向
            val movingForward = initialState.first < targetState.first
            if (movingForward) {
                // 向前翻：新文案从右侧滑入，旧文案向左滑出
                (slideInHorizontally(animationSpec = tween(300)) { it / 4 } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeOut(tween(300)))
            } else {
                // 向后翻：新文案从左侧滑入，旧文案向右滑出
                (slideInHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { it / 4 } + fadeOut(tween(300)))
            }
        },
        label = "guideText"
    ) { (index, page) ->
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start
        ) {
            // 索引文案："01 - CHAT" 格式，根据 index 添加 01 至 05 前缀
            Text(
                text = String.format("%02d - %s", index + 1, page.category),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CategoryTextColor,
                letterSpacing = 2.sp
            )
            // 标题
            Text(
                text = page.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TitleTextColor,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp)
            )
            // 描述文案
            Text(
                text = page.description,
                fontSize = 14.sp,
                color = DescriptionTextColor,
                textAlign = TextAlign.Start,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * 顶部滑动进度指示条。
 *
 * 参照设计截图样式：
 * - 5 段药丸形指示条，整体左对齐，不填满屏宽
 * - 非激活段：宽度 14dp，颜色 [IndicatorInactiveColor]
 * - 激活段：宽度 30dp（向右扩展覆盖 gap），颜色 [IndicatorActiveColor]
 * - 段间距 8dp
 *
 * 段宽根据连续滑动位置 [PagerState.currentPageOffsetFraction] 平滑过渡，
 * 颜色按距离阈值切换，避免过渡过程中出现"半激活"中间态。
 */
@Composable
private fun GuideProgressIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    // 连续的滑动位置（包含小数偏移），用于平滑驱动段宽
    val position = pagerState.currentPage + pagerState.currentPageOffsetFraction

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { i ->
            // 当前段与滑动位置的连续距离：0=完全居中，1=相邻段
            val distance = abs(position - i).coerceIn(0f, 1f)
            // 段宽：当前段 30dp，相邻段 14dp，平滑过渡
            val segmentWidth = lerp(30f, 14f, distance)
            // 颜色按阈值切换：距离 < 0.5 视为激活态
            val isCurrent = distance < 0.5f
            val color = if (isCurrent) IndicatorActiveColor else IndicatorInactiveColor

            Box(
                modifier = Modifier
                    .width(segmentWidth.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/**
 * 堆叠图片中的单张图片项。
 *
 * 动效说明：
 * - 宽高比：中心 aspectRatio(0.75)，两侧变窄至 aspectRatio(0.2)，滑动时平滑过渡
 * - 透明度：中心为 1.0，两侧半透明 0.4
 * - 圆角：中心为小圆角长方形（8%），两侧为窄椭圆长方形（50%）
 *
 * 需求3：当图片滑动至中心位置时，添加动态边框装饰。
 * 边框透明度根据滑动系数（progress）线性过渡：
 * - progress=0（居中）：边框完全显示（alpha=1）
 * - progress=1（远离中心）：边框完全透明（alpha=0）
 *
 * 需求4：非居中图片支持点击翻页（通过 [onClick] 回调）。
 */
@SuppressLint("FrequentlyChangingValue")
@Composable
private fun StackedGuideImage(
    imageRes: Int,
    pagerState: PagerState,
    page: Int,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 1. 计算带正负的偏移：负数=左侧卡片，正数=右侧卡片，0=当前居中卡片
    val offsetRaw = (page - pagerState.currentPage) - pagerState.currentPageOffsetFraction
    val offsetAbs = abs(offsetRaw)
    // 限制动画区间 0~1，超过1不再变化
    val progress = offsetAbs.coerceIn(0f, 1f)

    // 需求2：transX 动态计算值，根据屏幕宽度按比例计算
    // 计算公式：transX = offsetRaw × screenWidthPx × 0.1f
    // 参考基准：400dp@3x 屏幕下 120px = 40dp = 400dp × 0.1，转换为像素后保持一致视觉比例
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val transXCoefficient = screenWidthPx * 0.1f

    // ---------- 陌陌专属动效参数 ----------
    // 宽度：居中85%，侧边缩小至65%（陌陌侧边不会缩得特别小）
    val cardWidth = lerp(1f, 0.2f, progress)
    val cardHeight = lerp(0.9f, 0.68f, progress)
    // 圆角：居中12%，侧边轻微变大
    val cornerRadiusPercent = lerp(10, 50, progress)
    // Z层级：居中卡片层级最高，浮在顶层；侧边层级低，压底部
    val zOrder = lerp(20f, 0f, progress)
    // 横向偏移幅度：动态计算，根据屏幕宽度按比例缩放
    val transX = offsetRaw * transXCoefficient
    // 缩放：轻微缩小，保持卡片辨识度
    val scale = lerp(1f, 1f, progress)
    // 透明度：侧边轻微变淡，不会完全透明
    val alphaValue = lerp(1f, 0.6f, progress)
    // 阴影：居中阴影厚重，侧边微弱，强化立体堆叠
    val shadow = lerp(40f, 20f, progress)

    // 需求3：动态边框透明度 —— 居中时完全显示(1f)，远离中心时完全透明(0f)，线性过渡
    val borderAlpha = 1f - progress
    val cardShape = RoundedCornerShape(percent = cornerRadiusPercent)

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(zOrder), // 控制上下堆叠顺序，核心！
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(cardWidth)
                .fillMaxHeight(cardHeight) // 高度随宽度变化，保持长方形比例
                .graphicsLayer {
                    translationX = transX
                    scaleX = scale
                    scaleY = scale
                    alpha = alphaValue
                    shadowElevation = shadow
                }
                .clip(cardShape)
                .border(
                    width = 3.dp,
                    color = Color.Red.copy(alpha = borderAlpha),
                    shape = cardShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = "Guide image ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Float 线性插值。
 */
private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

/**
 * Int 线性插值。
 */
private fun lerp(start: Int, stop: Int, fraction: Float): Int =
    (start + (stop - start) * fraction).toInt()

@Preview(showBackground = true)
@Composable
private fun GuideScreenPreview() {
    GuideScreen(
        onEnterApp = {},
        onSkip = {}
    )
}
