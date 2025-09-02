package com.mrv.wallet.core.managers

import com.mrv.wallet.core.IAccountCleaner
import com.mrv.wallet.core.adapters.BitcoinAdapter
import com.mrv.wallet.core.adapters.BitcoinCashAdapter
import com.mrv.wallet.core.adapters.DashAdapter
import com.mrv.wallet.core.adapters.ECashAdapter
import com.mrv.wallet.core.adapters.Eip20Adapter
import com.mrv.wallet.core.adapters.EvmAdapter
import com.mrv.wallet.core.adapters.SolanaAdapter
import com.mrv.wallet.core.adapters.TronAdapter
import com.mrv.wallet.core.adapters.zcash.ZcashAdapter

class AccountCleaner : IAccountCleaner {

    override fun clearAccounts(accountIds: List<String>) {
        accountIds.forEach { clearAccount(it) }
    }

    private fun clearAccount(accountId: String) {
        BitcoinAdapter.clear(accountId)
        BitcoinCashAdapter.clear(accountId)
        ECashAdapter.clear(accountId)
        DashAdapter.clear(accountId)
        EvmAdapter.clear(accountId)
        Eip20Adapter.clear(accountId)
        ZcashAdapter.clear(accountId)
        SolanaAdapter.clear(accountId)
        TronAdapter.clear(accountId)
    }

}
