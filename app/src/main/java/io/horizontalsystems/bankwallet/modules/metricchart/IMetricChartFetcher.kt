package com.mrv.wallet.modules.metricchart

import com.mrv.wallet.ui.compose.TranslatableString

interface IMetricChartFetcher {
    val title: Int
    val description: TranslatableString
    val poweredBy: TranslatableString
}