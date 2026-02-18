package com.openkala.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.imageLoader
import coil.request.ImageRequest
import com.openkala.app.domain.model.ProductPreview
import com.openkala.app.ui.categories.CategoriesScreenRoute
import com.openkala.app.ui.detail.ProductDetailScreenRoute
import com.openkala.app.ui.home.HomeScreenRoute
import com.openkala.app.ui.home.PixelPerfectHomeStyle
import com.openkala.app.ui.home.bottomNavTextSize
import com.openkala.app.ui.search.SearchEntryScreenRoute
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens

private const val HOME_ROUTE = "home"
private const val CATEGORIES_ROUTE = "categories"
private const val SEARCH_ENTRY_ROUTE = "search-entry"
private const val PRODUCT_ROUTE_PATTERN =
    "product/{productId}?title={title}&imageUrl={imageUrl}&price={price}&discount={discount}"

object OpenKalaDestinations {
    const val ProductRoute = PRODUCT_ROUTE_PATTERN

    fun productRoute(preview: ProductPreview): String {
        val title = Uri.encode(preview.title)
        val imageUrl = Uri.encode(preview.imageUrl)
        val price = preview.price ?: -1L
        val discount = preview.discountPercent ?: -1
        return "product/${preview.productId}?title=$title&imageUrl=$imageUrl&price=$price&discount=$discount"
    }
}

private data class BottomTabItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val BottomTabs = listOf(
    BottomTabItem(
        route = HOME_ROUTE,
        title = "خانه",
        icon = Icons.Outlined.Home
    ),
    BottomTabItem(
        route = CATEGORIES_ROUTE,
        title = "دسته‌بندی",
        icon = Icons.Outlined.Category
    )
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OpenKalaNavHost() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    var isHomeTopTabWebMode by remember { mutableStateOf(false) }
    val showBottomBar = when (currentRoute) {
        HOME_ROUTE -> !isHomeTopTabWebMode
        CATEGORIES_ROUTE -> true
        else -> false
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != HOME_ROUTE) {
            isHomeTopTabWebMode = false
        }
    }

    SharedTransitionLayout {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = OpenKalaColorTokens.AppBackground,
            bottomBar = {
                if (showBottomBar) {
                    BottomTabBar(
                        currentRoute = currentRoute,
                        onTabClick = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = HOME_ROUTE,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                composable(route = HOME_ROUTE) {
                    HomeScreenRoute(
                        onProductClick = { product ->
                            if (product.imageUrl.isNotBlank()) {
                                context.imageLoader.enqueue(
                                    ImageRequest.Builder(context)
                                        .data(product.imageUrl)
                                        .build()
                                )
                            }

                            navController.navigate(
                                OpenKalaDestinations.productRoute(
                                    preview = ProductPreview(
                                        productId = product.id,
                                        title = product.title,
                                        imageUrl = product.imageUrl,
                                        price = product.price,
                                        discountPercent = product.discountPercent
                                    )
                                )
                            )
                        },
                        onSearchClick = {
                            navController.navigate(SEARCH_ENTRY_ROUTE)
                        },
                        onWebModeChanged = { isWebMode ->
                            isHomeTopTabWebMode = isWebMode
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }

                composable(route = CATEGORIES_ROUTE) {
                    CategoriesScreenRoute(
                        onSearchClick = { navController.navigate(SEARCH_ENTRY_ROUTE) },
                        onBackClick = {
                            navController.navigate(HOME_ROUTE) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }

                composable(
                    route = SEARCH_ENTRY_ROUTE,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 8 },
                            animationSpec = tween(durationMillis = 260)
                        ) + fadeIn(animationSpec = tween(durationMillis = 260))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 8 },
                            animationSpec = tween(durationMillis = 260)
                        ) + fadeOut(animationSpec = tween(durationMillis = 260))
                    }
                ) {
                    SearchEntryScreenRoute(
                        onBack = { navController.popBackStack() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }

                composable(
                    route = PRODUCT_ROUTE_PATTERN,
                    arguments = listOf(
                        navArgument("productId") { type = NavType.LongType },
                        navArgument("title") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument("imageUrl") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument("price") {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument("discount") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    ),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 5 },
                            animationSpec = tween(durationMillis = 220)
                        ) + fadeIn(animationSpec = tween(durationMillis = 220))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 5 },
                            animationSpec = tween(durationMillis = 220)
                        ) + fadeOut(animationSpec = tween(durationMillis = 220))
                    }
                ) {
                    ProductDetailScreenRoute(
                        onClose = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabBar(
    currentRoute: String?,
    onTabClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .navigationBarsPadding()
            .padding(top = PixelPerfectHomeStyle.bottomNavTopPadding, bottom = PixelPerfectHomeStyle.bottomNavBottomPadding),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            Column(
                modifier = Modifier.clickable { onTabClick(tab.route) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = if (selected) OpenKalaColorTokens.TextPrimary else OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(PixelPerfectHomeStyle.bottomNavIconSize)
                )
                Text(
                    text = tab.title,
                    style = OpenKalaTypographyTokens.Subtitle,
                    color = if (selected) OpenKalaColorTokens.TextPrimary else OpenKalaColorTokens.TextMedium,
                    fontSize = PixelPerfectHomeStyle.bottomNavTextSize
                )
            }
        }
    }
}
