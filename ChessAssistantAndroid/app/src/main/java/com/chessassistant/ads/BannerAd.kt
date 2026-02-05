package com.chessassistant.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner ad composable that can be placed in any screen.
 *
 * Usage:
 * ```
 * BannerAd(
 *     adManager = adManager,
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun BannerAd(
    adManager: AdManager,
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER
) {
    val isAdFree by adManager.isAdFree.collectAsState()

    if (isAdFree) {
        // Don't show ads if user has ad-free version
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = AdManager.BANNER_AD_UNIT_ID
                loadAd(adManager.createAdRequest())
            }
        },
        update = { adView ->
            // Reload ad if needed
        }
    )
}

/**
 * Adaptive banner ad that adjusts to screen width.
 */
@Composable
fun AdaptiveBannerAd(
    adManager: AdManager,
    modifier: Modifier = Modifier
) {
    val isAdFree by adManager.isAdFree.collectAsState()

    if (isAdFree) {
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val displayMetrics = context.resources.displayMetrics
            val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
            val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

            AdView(context).apply {
                setAdSize(adaptiveSize)
                adUnitId = AdManager.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
