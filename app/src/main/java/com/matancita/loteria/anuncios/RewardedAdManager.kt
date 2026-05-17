package com.matancita.loteria.anuncios

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG_REWARDED = "RewardedAdManager"

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    // CRITICAL: This ID is currently identical to the Interstitial Ad Unit ID.
    // Rewarded and Interstitial ads MUST use separate Ad Unit IDs in AdMob.
    // Please create a dedicated Rewarded Ad Unit in AdMob and update this value.
    // Until then, Rewarded ads will likely receive NO_FILL and not show.
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-9861862421891852/4708344888"

    private var pendingReward: ((Int) -> Unit)? = null
    private var pendingDismiss: (() -> Unit)? = null
    private var rewardEarned = false
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isAdLoaded = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isAdLoadedFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = _isAdLoaded

    fun loadAd(context: Context, adUnitId: String = REWARDED_AD_UNIT_ID) {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG_REWARDED, "Rewarded ad failed to load: ${adError.message}")
                rewardedAd = null
                _isAdLoaded.value = false
                isLoading = false
                managerScope.launch {
                    delay(10000)
                    loadAd(context, adUnitId)
                }
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG_REWARDED, "Rewarded ad loaded.")
                rewardedAd = ad
                _isAdLoaded.value = true
                isLoading = false
                setCallbacks(context, adUnitId)
            }
        })
    }

    private fun setCallbacks(context: Context, adUnitId: String) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG_REWARDED, "Rewarded ad is now visible.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_REWARDED, "Rewarded ad dismissed. rewardEarned=$rewardEarned")
                rewardedAd = null
                _isAdLoaded.value = false
                if (!rewardEarned) {
                    pendingDismiss?.invoke()
                }
                cleanupCallbacks()
                managerScope.launch {
                    delay(2000)
                    loadAd(context, adUnitId)
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG_REWARDED, "Rewarded ad failed to show: ${adError.message}")
                rewardedAd = null
                _isAdLoaded.value = false
                pendingDismiss?.invoke()
                cleanupCallbacks()
                managerScope.launch {
                    delay(2000)
                    loadAd(context, adUnitId)
                }
            }
        }
    }

    private fun cleanupCallbacks() {
        pendingReward = null
        pendingDismiss = null
        rewardEarned = false
    }

    fun showAd(activity: Activity, onReward: (Int) -> Unit, onDismiss: () -> Unit = {}) {
        val ad = rewardedAd
        if (ad != null) {
            rewardEarned = false
            pendingReward = onReward
            pendingDismiss = onDismiss
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d(TAG_REWARDED, "User earned reward: ${rewardItem.amount}")
                rewardEarned = true
                pendingReward?.invoke(rewardItem.amount)
                pendingReward = null // prevent double invocation
            })
        } else {
            Log.d(TAG_REWARDED, "Rewarded ad not ready.")
            onDismiss()
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
}
