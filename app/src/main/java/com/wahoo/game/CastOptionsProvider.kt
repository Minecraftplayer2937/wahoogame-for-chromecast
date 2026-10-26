package com.wahoo.game

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/**
 * DashCast (84912283) is a community-run Chromecast receiver that simply opens a web page.
 * Using its ID means you don't have to register (or pay for) a receiver of your own.
 */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId("CC1AD845")
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
