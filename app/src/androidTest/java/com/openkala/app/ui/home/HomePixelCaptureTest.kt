package com.openkala.app.ui.home

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openkala.app.MainActivity
import java.io.File
import java.io.FileOutputStream
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePixelCaptureTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Ignore("Manual baseline capture for visual diff workflow")
    @Test
    fun captureHomeScreenAsPng() {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            composeRule.onAllNodesWithTag("home_list").fetchSemanticsNodes().isNotEmpty()
        }

        val bitmap = composeRule.onNodeWithTag("home_list").captureToImage().asAndroidBitmap()
        val file = File(composeRule.activity.cacheDir, "home-screen-candidate.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
        }

        check(file.exists()) {
            "Expected screenshot file to exist at ${file.absolutePath}"
        }
    }
}
