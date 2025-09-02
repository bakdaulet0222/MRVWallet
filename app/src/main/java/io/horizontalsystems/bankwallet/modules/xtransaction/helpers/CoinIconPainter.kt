package com.mrv.wallet.modules.xtransaction.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.mrv.wallet.R

@Composable
fun coinIconPainter(
    url: String?,
    alternativeUrl: String?,
    placeholder: Int?,
    fallback: Int = placeholder ?: R.drawable.coin_placeholder
) = rememberAsyncImagePainter(
    model = url,
    error = alternativeUrl?.let {
        rememberAsyncImagePainter(
            model = it,
            error = painterResource(fallback)
        )
    } ?: painterResource(fallback)
)