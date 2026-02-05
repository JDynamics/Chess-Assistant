package com.chessassistant.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.chessassistant.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google AdMob ads for the application.
 *
 * Features:
 * - Banner ads (via composable)
 * - Interstitial ads (full-screen between actions)
 * - Respects user preference to disable ads
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AdManager"

        // Ad Unit IDs from BuildConfig (test IDs in debug, real IDs in release)
        val BANNER_AD_UNIT_ID: String = BuildConfig.ADMOB_BANNER_ID
        val INTERSTITIAL_AD_UNIT_ID: String = BuildConfig.ADMOB_INTERSTITIAL_ID
    }

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    /**
     * Initialize the Mobile Ads SDK.
     * Call this once in Application.onCreate() or MainActivity.
     */
    fun initialize() {
        if (isInitialized) return

        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapter, status) in statusMap) {
                Log.d(TAG, "Adapter: $adapter, Status: ${status.initializationState}")
            }
            isInitialized = true
            Log.d(TAG, "AdMob SDK initialized")

            // Pre-load an interstitial ad
            loadInterstitialAd()
        }
    }

    /**
     * Set ad-free mode (e.g., if user purchased premium).
     */
    fun setAdFree(adFree: Boolean) {
        _isAdFree.value = adFree
    }

    /**
     * Load an interstitial ad for later display.
     */
    fun loadInterstitialAd() {
        if (_isAdFree.value) return

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    _isInterstitialReady.value = true

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            _isInterstitialReady.value = false
                            // Reload for next time
                            loadInterstitialAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Interstitial failed to show: ${error.message}")
                            interstitialAd = null
                            _isInterstitialReady.value = false
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad shown")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${error.message}")
                    interstitialAd = null
                    _isInterstitialReady.value = false
                }
            }
        )
    }

    /**
     * Show an interstitial ad if one is loaded.
     *
     * @param activity The activity to show the ad in
     * @param onAdDismissed Callback when ad is dismissed (or if no ad available)
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (_isAdFree.value) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialReady.value = false
                    loadInterstitialAd() // Reload for next time
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Failed to show interstitial: ${error.message}")
                    interstitialAd = null
                    _isInterstitialReady.value = false
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial not ready, skipping")
            onAdDismissed()
            loadInterstitialAd() // Try to load one for next time
        }
    }

    /**
     * Create an ad request for banner ads.
     */
    fun createAdRequest(): AdRequest = AdRequest.Builder().build()
}
