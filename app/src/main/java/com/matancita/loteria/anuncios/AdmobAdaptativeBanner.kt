package com.matancita.loteria.anuncios

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


// ID de Unidad de Anuncio de Prueba para Banners Adaptables: ca-app-pub-3940256099942544/9214589741
const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9861862421891852/2370788758"
const val TAG = "AdmobAdaptiveBanner"

@Composable
fun AdmobAdaptiveBanner(
    modifier: Modifier = Modifier,
    adUnitId: String // Pasa tu ID de unidad real aquí
) {
    val context = LocalContext.current

    val adView = remember { AdView(context) }

    val isAdLoaded = remember { mutableStateOf(false) }

//    LaunchedEffect(adUnitId, adView) {
//        if (isAdLoaded.value) {
//            Log.d(TAG, "Anuncio ya cargado, omitiendo recarga.")
//            return@LaunchedEffect
//        }
//        try {
//            adView.adUnitId = adUnitId
//            // Se establece el tamaño del anuncio ANTES de que se cargue
//            adView.setAdSize(
//                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
//                    context,
//                    320
//                )
//            )
//            // Se carga el anuncio
//            val adRequest = AdRequest.Builder().build()
//            adView.loadAd(adRequest)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error al cargar el banner adaptable", e)
//        }
//    }

    LaunchedEffect(adUnitId, adView) {
        if (isAdLoaded.value) {
            Log.d(TAG, "Anuncio ya cargado, omitiendo recarga.")
            return@LaunchedEffect
        }

        try {
            adView.adUnitId = adUnitId
            adView.setAdSize(
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, 320)
            )
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.setAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded.value = true // Marcar como cargado
                    Log.d(TAG, "Banner adaptable cargado.")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded.value = false // Permitir reintentos si falla
                    Log.e(TAG, "Error al cargar el banner adaptable: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar el banner adaptable", e)
        }
    }

    // El Box ahora envuelve la altura del AndroidView
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
            BannerAd(
                adView,
                Modifier.fillMaxWidth()
            )
    }
}

@Composable
fun BannerAd(adView: AdView, modifier: Modifier = Modifier) {
    // Ad load does not work in preview mode because it requires a network connection.
    if (LocalInspectionMode.current) {
        Box { Text(text = "Google Mobile Ads preview banner.", modifier.align(Alignment.Center)) }
        return
    }

    AndroidView(modifier = modifier.wrapContentSize(), factory = { adView })

    // Pause and resume the AdView when the lifecycle is paused and resumed.
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
    DisposableEffect(Unit) {
        // Destroy the AdView to prevent memory leaks when the screen is disposed.
        onDispose { adView.destroy() }
    }
}
