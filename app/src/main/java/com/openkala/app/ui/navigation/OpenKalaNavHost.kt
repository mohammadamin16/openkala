package com.openkala.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.imageLoader
import coil.request.ImageRequest
import com.openkala.app.domain.model.ProductPreview
import com.openkala.app.ui.detail.ProductDetailScreenRoute
import com.openkala.app.ui.home.HomeScreenRoute

private const val HOME_ROUTE = "home"
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

@Composable
fun OpenKalaNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = Modifier.fillMaxSize()
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
                }
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
