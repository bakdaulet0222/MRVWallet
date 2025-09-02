package com.mrv.wallet.modules.multiswap.action

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mrv.wallet.R
import com.mrv.wallet.core.slideFromBottomForResult
import com.mrv.wallet.entities.CoinValue
import com.mrv.wallet.modules.eip20revoke.Eip20RevokeConfirmFragment
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

class ActionRevoke(
    private val token: Token,
    private val spenderAddress: String,
    override val inProgress: Boolean,
    private val allowance: BigDecimal
) : ISwapProviderAction {

    @Composable
    override fun getTitle() = stringResource(R.string.Swap_Revoke)

    @Composable
    override fun getTitleInProgress() = stringResource(R.string.Swap_Revoking)

    @Composable
    override fun getDescription() =
        stringResource(R.string.Approve_RevokeAndApproveInfo, CoinValue(token, allowance).getFormattedFull())

    override fun execute(navController: NavController, onActionCompleted: () -> Unit) {
        navController.slideFromBottomForResult<Eip20RevokeConfirmFragment.Result>(
            R.id.eip20RevokeConfirmFragment,
            Eip20RevokeConfirmFragment.Input(token, spenderAddress, allowance)
        ) {
            onActionCompleted.invoke()
        }
    }
}
