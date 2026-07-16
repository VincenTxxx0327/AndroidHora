package com.union.hora.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.union.hora.ui.home.PostsScreen
import com.union.hora.ui.home.SportsScreen
import com.dylanc.longan.pxToDp
import com.dylanc.longan.screenWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.filled.Face
import com.union.hora.ui.home.ChatsScreen
import com.union.hora.ui.home.GuideScreen
import com.union.hora.ui.widget.SimpleCenteredText

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

// Simple sealed class to represent bottom navigation destinations
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Posts : Screen("posts", "帖子", Icons.Default.Home)
    object Goods : Screen("goods", "商品", Icons.Default.ShoppingCart)
    object Sports : Screen("sports", "运动", Icons.Default.Favorite)
    object Chats : Screen("chats", "聊天", Icons.Default.Face)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

// 引导页路由（无底部导航栏的独立全屏页面）
private const val GUIDE_ROUTE = "guide"

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // 引导页为全屏页面，隐藏底部导航栏
    val showBottomBar = currentRoute != GUIDE_ROUTE

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        // 需求4：不在此处对 NavHost 整体应用 innerPadding，
        // 避免从 GuideScreen 返回时 bottomBar 出现导致 innerPadding 变化、
        // GuideScreen 内容瞬间被往上顶。改为在各非 guide 页面内部单独应用。
        NavHost(
            navController = navController,
            startDestination = Screen.Posts.route
        ) {
            composable(Screen.Posts.route) {
                Box(Modifier.padding(innerPadding)) { PostsScreen() }
            }
            composable(Screen.Goods.route) {
                Box(Modifier.padding(innerPadding)) { GoodsScreen() }
            }
            composable(Screen.Sports.route) {
                Box(Modifier.padding(innerPadding)) { SportsScreen() }
            }
            composable(Screen.Chats.route) {
                // 点击搜索图标导航至引导页
                Box(Modifier.padding(innerPadding)) {
                    ChatsScreen(
                        onSearchClick = {
                            navController.navigate(GUIDE_ROUTE) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable(Screen.Profile.route) {
                Box(Modifier.padding(innerPadding)) { ProfileScreen() }
            }
            // 引导页：全屏页面，不应用 innerPadding，避免返回时布局跳变
            composable(GUIDE_ROUTE) {
                GuideScreen(
                    onEnterApp = { navController.popBackStack() },
                    onSkip = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        Screen.Posts,
        Screen.Goods,
        Screen.Sports,
        Screen.Chats,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 自定义底部导航栏
    Column {
        // 底部导航栏容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 背景底部线条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // 导航项布局
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavItem(
                        item = item,
                        itemSize = items.size,
                        selected = currentRoute == item.route
                    ) {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavItem(item: Screen, itemSize: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(top = 8.dp, bottom = 8.dp)
            .defaultMinSize(minWidth = (screenWidth / itemSize).pxToDp().dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = item.title,
            fontSize = 12.sp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}



@Composable
fun GoodsScreen() {

}

@Composable
fun ProfileScreen() {
    SimpleCenteredText(text = "我的 页面")
}
