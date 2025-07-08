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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// ID de Unidad de Anuncio de Prueba para Banners Adaptables: ca-app-pub-3940256099942544/9214589741
const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9861862421891852/2370788758"
const val TAG = "AdmobAdaptiveBanner"

@Composable
fun AdmobAdaptiveBanner(
    modifier: Modifier = Modifier,
    adUnitId: String // Pasa tu ID de unidad real aquí
) {
    val context = LocalContext.current
    val isInEditMode = LocalInspectionMode.current

    val adView = remember { AdView(context) }

    LaunchedEffect(adUnitId, adView) {
        if (isInEditMode) return@LaunchedEffect

        try {
            val activity = context as Activity
            val windowManager = activity.windowManager

            val adWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                val bounds = windowMetrics.bounds
                val adWidthPixels = bounds.width() - insets.left - insets.right
                (adWidthPixels / context.resources.displayMetrics.density).toInt()
            } else {
                @Suppress("DEPRECATION")
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                (displayMetrics.widthPixels / displayMetrics.density).toInt()
            }

            adView.adUnitId = adUnitId
            // Se establece el tamaño del anuncio ANTES de que se cargue
            adView.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    context,
                    adWidth
                )
            )
            // Se carga el anuncio
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el banner adaptable", e)
        }
    }

    // El Box ahora envuelve la altura del AndroidView
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (isInEditMode) {
            // Placeholder para el Preview con una altura fija representativa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // Altura típica de un banner para el preview
                    .background(Color.Gray.copy(alpha = 0.5f))
            ) {
                Text(text = "Ad Banner Preview", modifier = Modifier.align(Alignment.Center))
            }
        } else {
            // --- SOLUCIÓN ---
            // El AndroidView solo debe llenar el ancho.
            // Su altura será determinada por el AdView que contiene.
            // El Box padre se adaptará a esa altura.
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
