package com.union.hora.ui.home

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.union.hora.ui.PostViewModel

@Composable
fun GoodsScreen() {
    val viewModel: PostViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        SportsTopBar(
            onSearchClick = { /* 进入搜索页逻辑 */ },
            onMenuClick = { },
            onNotificationClick = { }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "加载中...")
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = error?.toString() ?: "加载失败")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        HeroSection()
                    }

                    item {
                        CategorySection()
                    }

                    item {
                        ProductGridSection()
                    }

                    item {
                        NewsletterSection()
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "加载更多...")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SportsTopBar(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "菜单",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    SubcomposeAsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAem544JWQXUG62ogvuf0xAp01Nncl_wGgDiqXpUSjzFpefHRLhHorPDRxcnpB4xjcsyljsymEcYipi7PIvCp1IW6koy2gbqfAVKby4vyhU3gwPwkmCOQqqoiimt3KrQ52qe7VzZ0gIHQbvPx611WmuRlASekuvxiMBnMwfLxihol_eusQMLjc2ebTi8mmyJlfg09v9lTFinw06xgXHLiKYyQJN7cfEvdwX6YIADLxEBD-Jud9gThcNtCd2t3g6jOO7cxEhAf-VvQU",
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "KINETIC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF95AAFF),
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color(0xFF95AAFF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "通知",
                        tint = Color(0xFF95AAFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAnsezrSon-06lqgre5I1vdrhUeBIOUPq_rTjpBo1xzJ2w8XVBdm5LB4DE7RTPcsNsm1M-QSJmh9Z-UIwEsVUTIOvvK_X8NPmg917zMhW8c8r9IdfRu9dC9gUwcqGgbm44-4eKDxMg6E6-BMCuexwzYZ1HhSQP2nTXHPMM_KM3R2R1lvBi2Md_gmwuVfZopChhwDmU_CuOYt---GYx6U0cj7W6LjfyL3Q2IzDcYTZ8IHEpjbE2KQPcITdHCZGj3bOHIjVlrTUWw_cM",
                contentDescription = "运动英雄区域",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF5F5F5),
                                Color(0xFFF5F5F5).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "NEW SEASON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC3F400),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "LIMITLESS SPEED.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        lineHeight = 30.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFC3F400))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "SHOP ELITE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GEAR CATEGORIES",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Text(
                text = "Scroll to filter",
                fontSize = 10.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(getCategories()) { category ->
                CategoryCard(category = category)
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (category.isSelected) Color(0xFF95AAFF) else Color(0xFF262626)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (category.isSelected) Color.White else Color(0xFFC3F400),
                modifier = Modifier.size(32.dp)
            )

            Column {
                Text(
                    text = category.subtitle,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (category.isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (category.isSelected) Color.White else Color.White
                )
            }
        }
    }
}

@Composable
fun ProductGridSection() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ELITE GEAR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFC3F400), CircleShape)
                )
            }

            Text(
                text = "View All",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF95AAFF),
                letterSpacing = 1.sp
            )
        }

        // Use a Column with Rows to avoid nesting LazyVerticalGrid inside LazyColumn
        val products = getProducts()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.chunked(2).forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowProducts.forEach { product ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(product = product)
                        }
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (product.isFeatured) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFC3F400))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TOP RATED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "评分",
                        tint = Color(0xFFC3F400),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = product.rating.toString(),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "(${product.reviews})",
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF95AAFF)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "购物车",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ADD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsletterSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "JOIN THE SQUAD.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Get early access to drops, professional training programs, and exclusive elite-tier discounts.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF262626))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "ENTER EMAIL",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF95AAFF))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { }
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JOIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

data class Category(
    val name: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSelected: Boolean = false
)

data class Product(
    val name: String,
    val price: Int,
    val rating: Double,
    val reviews: Int,
    val imageUrl: String,
    val isFeatured: Boolean = false
)

fun getCategories(): List<Category> {
    return listOf(
        Category("FOOTWEAR", "Performance", Icons.Default.ShoppingCart, false),
        Category("APPAREL", "Precision", Icons.Default.ShoppingCart, true),
        Category("EQUIPMENT", "Technical", Icons.Default.ShoppingCart, false),
        Category("RECOVERY", "Bio-Flow", Icons.Default.ShoppingCart, false)
    )
}

fun getProducts(): List<Product> {
    return listOf(
        Product(
            "VELOCITY X-1",
            219,
            5.0,
            128,
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDfv7FWwRYI4HV23gAz7xnNsIf6GS4zSCcKkD3Tp-JQfcjHvDzQMGLfu3_vYyRGIHIJUyxp3hnsQHt4xuSCrvhlXC5Swb22u_3YEdHoO_1_kdLRz_Q0Yzsj70P6dkFmjaH0JBX4PjmCQj_Zl8C8BegLB0VEId-eZFZCOHejzyTOsu3u7JjHRGMPfTrT5-Yk_hXJvYKs48TmLahTun99iSNQZLthdHz4ywVNk6_0jC9vCw9ZFLoWtmk0prAUoLncgVRPZtkiJWLss2c",
            true
        ),
        Product(
            "PRO-GRIP HEX 15KG",
            85,
            4.8,
            96,
            "https://lh3.googleusercontent.com/aida-public/AB6AXuC2_zLKgE_TxryDWN2iyljAp185pLTwtrbYoFzi_wc8kItn-KXSbyzpZKrb6Dw6yOaqjyKJxOEPiicOHrbaYF0cB0zQCq5Ee_tsZFOLR9P-OFxOed7_FzpitgKEZkcZQ9x8hXtyfs6EdJ3YxP7i-eo6e-kR3W-IgX5oJ3XCpWtXKOLpVI7BempeA-cUZT8VIfB30kfoCBnrwe5PleSRtCeDSFukxHFlTco-eq3PgKqEyAyQKE0OKFGyIdV1g7P1__QcI_-nN2y7jTI"
        ),
        Product(
            "TITAN DUFFEL 40L",
            110,
            4.9,
            84,
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCjlxzaiprFCWSb63VMVa_69SqYyIVElEmoj_IwvX2JMLdQAinxmQY_TZF8iW53-s2mvIj5FffUHR2lekQM8hl8RmMe_1fxZlTEh6lbvM74NOX5fDDuvO52wZFbIQliiwUgnXlnuLbh56V206ZYhSgyi5GrGrTTVOxcQ3Iy_PnJrKzceZks5jDo886jrjqUas8KpiWq_C3iLrGQkXPERn3bSeMkfpr1DnkSxAykaqWP9fwB71_LEyCVfMrxivsZrmdRpzYS5FBe-Y"
        ),
        Product(
            "AERO-DRY COMPRESSION",
            55,
            4.7,
            72,
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCRhwmYZeUSNR5p5HAE5gAFWNnalQhePeDbrAPw3hacI9gBz8FpDA7Aqtc5vAEub3g2WUej04YFVCfguhMaQH0r72MbvBUoAIkvWlpzKW1-BBLqa2LTAGBwGj5-59dzgidxBzFPoCk_Vm04V3xwFf6h0-JuWYk6lTBFzBk77mxHdHjD7jCGznnSCHLxRiLpGCP0A6Ucm9Xnweon_lkdV6NgaP_6ceeQTcuq74lO44utFlZs2KNCNdRg7y0l7XOKGB_pefBdBZZhlnA"
        ),
        Product(
            "FLEX-RESIST KIT",
            35,
            4.5,
            68,
            "https://lh3.googleusercontent.com/aida-public/AB6AXuAU3QlZIRZhHnEkKCEkZcbmliTo9MlxnCBS__CbivjHDqlyDg5lZSRgbhzhErfdgcFyQcJgAIXFUny_FLwDXppuHYiK5LhXWuycoszF0oYMTLNcpUexvlU-ql4UmdCKbtoqiR__tfclyb5V6lbK_U6hgWE7j7Lgsyncq1zRUWL2zlSu5IPRbMejkFXTwuFQ7yCQtPTAaiy2fG6K3qb1nu_mNLGbWMfH74gLjbl4e0eHAeYZ0TjOJzcr7jeYGM6i9rg987-afgL4Bc"
        )
    )
}

@Preview
@Composable
fun GoodsScreenPreview() {
    GoodsScreen()
}