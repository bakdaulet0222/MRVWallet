package com.mrv.wallet.modules.transactionInfo.resendbitcoin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrv.wallet.core.App
import com.mrv.wallet.core.adapters.BitcoinBaseAdapter
import com.mrv.wallet.core.factories.FeeRateProviderFactory
import com.mrv.wallet.entities.transactionrecords.bitcoin.BitcoinOutgoingTransactionRecord
import com.mrv.wallet.modules.transactionInfo.options.SpeedUpCancelType
import com.mrv.wallet.modules.transactions.TransactionSource
import com.mrv.wallet.modules.xrate.XRateService

object ResendBitcoinModule {

    class Factory(
        private val optionType: SpeedUpCancelType,
        private val transactionRecord: BitcoinOutgoingTransactionRecord,
        private val source: TransactionSource
    ) : ViewModelProvider.Factory {

        private val adapter by lazy {
            App.transactionAdapterManager.getAdapter(source) as BitcoinBaseAdapter
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val replacementInfo = when (optionType) {
                SpeedUpCancelType.SpeedUp -> adapter.speedUpTransactionInfo(transactionRecord.transactionHash)
                SpeedUpCancelType.Cancel -> adapter.cancelTransactionInfo(transactionRecord.transactionHash)
            }

            return ResendBitcoinViewModel(
                type = optionType,
                transactionRecord = transactionRecord,
                replacementInfo = replacementInfo,
                adapter = adapter,
                xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency),
                feeRateProvider =  FeeRateProviderFactory.provider(adapter.wallet.token.blockchainType)!!,
                contactsRepo = App.contactsRepository
            ) as T
        }
    }

}
