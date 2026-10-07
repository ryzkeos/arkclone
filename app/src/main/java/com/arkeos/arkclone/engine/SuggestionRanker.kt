package com.arkeos.arkclone.engine

import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.RealInstalledApk

object SuggestionRanker {

    private const val TOP_TIER_SCORE = 100
    private const val SECOND_TIER_SCORE = 60
    private const val HEAVY_APK_BYTES = 500L * 1024 * 1024
    private const val HEAVY_APK_PENALTY = 15

    private val top_tier_packages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.facebook.lite",
        "com.facebook.mlite",
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.thunderdog.challegram",
        "com.discord",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.twitter.android",
        "com.snapchat.android",
        "com.instagram.barcelona",
        "jp.naver.line.android",
        "com.viber.voip",
        "org.thoughtcrime.securesms",
        "com.tencent.mm",
        "com.kakao.talk"
    )

    private val second_tier_packages = setOf(
        "com.reddit.frontpage",
        "com.linkedin.android",
        "com.pinterest",
        "com.google.android.gm",
        "com.microsoft.office.outlook",
        "com.shopee.ph",
        "com.lazada.android",
        "com.roblox.client",
        "com.mobile.legends",
        "com.dts.freefireth",
        "com.dts.freefiremax",
        "com.activision.callofduty.shooter",
        "com.tencent.ig",
        "com.miHoYo.GenshinImpact",
        "com.supercell.clashofclans",
        "com.supercell.brawlstars"
    )

    fun rank(
        apps: List<RealInstalledApk>,
        instances: List<ClonedInstance>
    ): List<RealInstalledApk> {
        val cloned_packages = instances.map { it.packageName }.toSet()
        return apps
            .filter { !it.isSystemApp && it.hasLaunchIntent && it.packageName !in cloned_packages }
            .sortedWith(
                compareByDescending<RealInstalledApk> { score(it) }
                    .thenBy { it.appName.lowercase() }
            )
    }

    private fun score(app: RealInstalledApk): Int {
        var total = when (app.packageName) {
            in top_tier_packages -> TOP_TIER_SCORE
            in second_tier_packages -> SECOND_TIER_SCORE
            else -> 0
        }
        total += when (app.category) {
            AppCategory.MESSAGING -> 40
            AppCategory.SOCIAL -> 35
            AppCategory.GAMES -> 30
            else -> 5
        }
        if (app.apkSizeBytes > HEAVY_APK_BYTES) total -= HEAVY_APK_PENALTY
        return total
    }
}
