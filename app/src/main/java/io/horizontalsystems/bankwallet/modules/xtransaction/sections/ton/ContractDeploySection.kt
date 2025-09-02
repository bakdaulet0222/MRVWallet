package com.mrv.wallet.modules.xtransaction.sections.ton

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrv.wallet.R
import com.mrv.wallet.modules.xtransaction.cells.HeaderCell
import com.mrv.wallet.ui.compose.components.cell.SectionUniversalLawrence

@Composable
fun ContractDeploySection(
    interfaces: List<String>,
) {
    SectionUniversalLawrence {
        HeaderCell(
            title = stringResource(R.string.Transactions_ContractDeploy),
            value = interfaces.joinToString(),
            painter = null
        )
    }
}