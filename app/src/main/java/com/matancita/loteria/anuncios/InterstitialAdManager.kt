package com.matancita.loteria.anuncios

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


// ID de Unidad de Anuncio de Prueba para Intersticiales: ca-app-pub-3940256099942544/1033173712
const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9861862421891852/3574997556"
private const val TAG2 = "InterstitialAdManager"

object InterstitialAdManager {

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoadingAd: Boolean = false
    private var adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID // Default, puede ser sobreescrito

    fun loadAd(context: Context, adUnit: String = TEST_INTERSTITIAL_AD_UNIT_ID) {
        if (isLoadingAd || mInterstitialAd != null) {
            Log.d(TAG2, "Interstitial ad ya está cargando o cargado.")
            return
        }
        isLoadingAd = true
        adUnitId = adUnit // Actualizar el adUnitId si se pasa uno diferente

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG2, "Interstitial ad falló al cargar: ${adError.message}")
                    mInterstitialAd = null
                    isLoadingAd = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG2, "Interstitial ad cargado exitosamente.")
                    mInterstitialAd = interstitialAd
                    isLoadingAd = false
                    setFullScreenContentCallback()
                }
            }
        )
    }

    private fun setFullScreenContentCallback() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG2, "Interstitial ad fue clickeado.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG2, "Interstitial ad fue descartado (dismissed).")
                // Es importante establecer mInterstitialAd a null para que se pueda cargar uno nuevo.
                mInterstitialAd = null
                // Opcionalmente, puedes precargar el siguiente anuncio aquí si tu lógica lo requiere.
                // loadAd(context, adUnitId) // Cuidado con el contexto aquí si lo haces
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG2, "Interstitial ad falló al mostrarse: ${adError.message}")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                Log.d(TAG2, "Interstitial ad generó una impresión.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG2, "Interstitial ad se mostró en pantalla completa.")
                // El anuncio se ha mostrado. No es necesario hacer nada con mInterstitialAd aquí,
                // se manejará en onAdDismissedFullScreenContent.
            }
        }
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
            // El callback onAdDismissed se maneja ahora a través de FullScreenContentCallback
        } else {
            Log.d(TAG2,"Interstitial ad no estaba listo para mostrarse.")
            // Opcionalmente, intenta cargarlo de nuevo para la próxima vez.
            // loadAd(activity.applicationContext, adUnitId)
            onAdDismissed() // Llama al callback si el anuncio no se muestra
        }
    }

    fun isAdLoaded(): Boolean {
        return mInterstitialAd != null
    }

    // Opcional: un método para limpiar la referencia si es necesario al destruir la actividad
    // aunque el FullScreenContentCallback debería manejarlo.
    fun destroy() {
        mInterstitialAd = null
    }
}