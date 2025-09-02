package com.mrv.wallet.modules.coin.indicators

import com.mrv.wallet.R
import com.mrv.wallet.core.providers.Translator

class NotIntegerException : Exception() {
    override fun getLocalizedMessage(): String {
        return Translator.getString(R.string.Error_NotInteger)
    }
}

class OutOfRangeException(val lower: Int, val upper: Int) : Exception() {
    override fun getLocalizedMessage(): String {
        return Translator.getString(R.string.Error_OutOfRange, lower, upper)
    }
}