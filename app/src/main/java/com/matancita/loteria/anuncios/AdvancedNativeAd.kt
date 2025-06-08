package com.matancita.loteria.anuncios

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.matancita.loteria.R // ¡IMPORTANTE! Reemplaza 'your.package.name' con tu paquete real

// --- Estado para gestionar el ciclo de vida del anuncio ---
sealed class NativeAdState {
    object Loading : NativeAdState()
    data class Loaded(val nativeAd: NativeAd) : NativeAdState()
    object Failed : NativeAdState()
    object Empty : NativeAdState()
}

/**
 * Un Composable que solicita y muestra un anuncio nativo avanzado.
 *
 * @param adUnitId El ID de tu bloque de anuncios nativos. Reemplaza con tu propio ID.
 * @param modifier El modificador a aplicar a este layout.
 */
@Composable
fun AdvancedNativeAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-9861862421891852/5150067478" // ID de prueba
) {
    var nativeAdState by remember { mutableStateOf<NativeAdState>(NativeAdState.Empty) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        // MobileAds.initialize(context) {} // Asegúrate de que esto se llame en tu clase Application

        nativeAdState = NativeAdState.Loading
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                nativeAdState = NativeAdState.Loaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    println("Fallo al cargar el anuncio nativo: ${loadAdError.message}")
                    nativeAdState = NativeAdState.Failed
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            val currentState = nativeAdState
            if (currentState is NativeAdState.Loaded) {
                currentState.nativeAd.destroy()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        when (val state = nativeAdState) {
            is NativeAdState.Loading -> NativeAdPlaceholder()
            is NativeAdState.Loaded -> AdContent(nativeAd = state.nativeAd)
            is NativeAdState.Failed -> NativeAdError()
            is NativeAdState.Empty -> Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

/**
 * La UI que infla el XML y muestra el contenido del anuncio nativo.
 */
@Composable
private fun AdContent(nativeAd: NativeAd) {
    AndroidView(
        factory = { context ->
            // Inflar el layout XML del anuncio
            val adView = LayoutInflater.from(context)
                .inflate(R.layout.native_ad_layout, null) as NativeAdView
            adView
        },
        update = { adView ->
            // Encontrar todas las vistas dentro del layout inflado
            val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
            val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
            val starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)
            val bodyView = adView.findViewById<TextView>(R.id.ad_body)
            val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
            val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
            val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

            // Asignar las vistas al NativeAdView
            adView.headlineView = headlineView
            adView.bodyView = bodyView
            adView.callToActionView = callToActionView
            adView.iconView = iconView
            adView.mediaView = mediaView
            adView.advertiserView = advertiserView
            adView.starRatingView = starRatingView

            // Poblar las vistas con los datos del anuncio
            headlineView.text = nativeAd.headline
            mediaView.mediaContent = nativeAd.mediaContent
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)

            if (nativeAd.body == null) {
                bodyView.visibility = View.INVISIBLE
            } else {
                bodyView.visibility = View.VISIBLE
                bodyView.text = nativeAd.body
            }

            if (nativeAd.callToAction == null) {
                callToActionView.visibility = View.INVISIBLE
            } else {
                callToActionView.visibility = View.VISIBLE
                callToActionView.text = nativeAd.callToAction
            }

            if (nativeAd.icon == null) {
                iconView.visibility = View.GONE
            } else {
                iconView.setImageDrawable(nativeAd.icon?.drawable)
                iconView.visibility = View.VISIBLE
            }

            if (nativeAd.starRating == null) {
                starRatingView.visibility = View.INVISIBLE
            } else {
                starRatingView.rating = nativeAd.starRating!!.toFloat()
                starRatingView.visibility = View.VISIBLE
            }

            if (nativeAd.advertiser == null) {
                advertiserView.visibility = View.INVISIBLE
            } else {
                advertiserView.text = nativeAd.advertiser
                advertiserView.visibility = View.VISIBLE
            }

            // Registrar el objeto NativeAd para que el SDK maneje los clics e impresiones.
            adView.setNativeAd(nativeAd)
        }
    )
}


/**
 * Un marcador de posición que se muestra mientras se carga el anuncio.
 */
@Composable
private fun NativeAdPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF)) // Un gris claro para el fondo
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Box(Modifier.height(16.dp).fillMaxWidth(0.7f).background(Color.LightGray))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.height(12.dp).fillMaxWidth(0.4f).background(Color.LightGray))
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )
    }
}

/**
 * Un Composable que se muestra si el anuncio no se pudo cargar.
 */
@Composable
private fun NativeAdError() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "No se pudo cargar el anuncio",
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}