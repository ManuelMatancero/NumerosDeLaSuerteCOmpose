package com.matancita.loteria.anuncios

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
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
    val isInEditMode = LocalInspectionMode.current // Para evitar crashes en el preview

    // Recordar la AdView para que no se recree en cada recomposición innecesaria
    val adView = remember { AdView(context) }

    // LaunchedEffect para cargar el anuncio una vez cuando el composable entra en la composición
    // y cuando adUnitId cambia (aunque usualmente no cambia dinámicamente para un banner fijo)
    LaunchedEffect(adUnitId, adView) {
        if (isInEditMode) return@LaunchedEffect

        try {
            val display = (context as? Activity)?.windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density
            var adWidthPixels = adView.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()

            adView.adUnitId = adUnitId
            adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el banner adaptable")
            // Considera mostrar un placeholder o nada si hay un error
        }
    }

    Box( // Contenedor para el banner
        modifier = modifier
            .fillMaxWidth()
        // La altura se ajustará por el banner adaptable, pero puedes poner un minHeight
        // o un placeholder si lo deseas.
        // .height(AdSize.SMART_BANNER.getHeightInDp(context).dp) // Ejemplo de altura fija (no adaptable)
        ,
        contentAlignment = Alignment.Center
    ) {
        if (isInEditMode) {
            // Placeholder para el Preview en Android Studio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // Altura típica de un banner
                    .background(Color.Gray.copy(alpha = 0.5f))
            ) {
                Text(text = "Ad Banner Preview", modifier = Modifier.align(Alignment.Center))
            }
        } else {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}