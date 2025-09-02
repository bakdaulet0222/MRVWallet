package com.mrv.wallet.core.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrv.wallet.core.storage.migrations.Migration_31_32
import com.mrv.wallet.core.storage.migrations.Migration_32_33
import com.mrv.wallet.core.storage.migrations.Migration_33_34
import com.mrv.wallet.core.storage.migrations.Migration_34_35
import com.mrv.wallet.core.storage.migrations.Migration_35_36
import com.mrv.wallet.core.storage.migrations.Migration_36_37
import com.mrv.wallet.core.storage.migrations.Migration_37_38
import com.mrv.wallet.core.storage.migrations.Migration_38_39
import com.mrv.wallet.core.storage.migrations.Migration_39_40
import com.mrv.wallet.core.storage.migrations.Migration_40_41
import com.mrv.wallet.core.storage.migrations.Migration_41_42
import com.mrv.wallet.core.storage.migrations.Migration_42_43
import com.mrv.wallet.core.storage.migrations.Migration_43_44
import com.mrv.wallet.core.storage.migrations.Migration_44_45
import com.mrv.wallet.core.storage.migrations.Migration_45_46
import com.mrv.wallet.core.storage.migrations.Migration_46_47
import com.mrv.wallet.core.storage.migrations.Migration_47_48
import com.mrv.wallet.core.storage.migrations.Migration_48_49
import com.mrv.wallet.core.storage.migrations.Migration_49_50
import com.mrv.wallet.core.storage.migrations.Migration_50_51
import com.mrv.wallet.core.storage.migrations.Migration_51_52
import com.mrv.wallet.core.storage.migrations.Migration_52_53
import com.mrv.wallet.core.storage.migrations.Migration_53_54
import com.mrv.wallet.core.storage.migrations.Migration_54_55
import com.mrv.wallet.core.storage.migrations.Migration_55_56
import com.mrv.wallet.core.storage.migrations.Migration_56_57
import com.mrv.wallet.core.storage.migrations.Migration_57_58
import com.mrv.wallet.core.storage.migrations.Migration_58_59
import com.mrv.wallet.core.storage.migrations.Migration_59_60
import com.mrv.wallet.core.storage.migrations.Migration_60_61
import com.mrv.wallet.core.storage.migrations.Migration_61_62
import com.mrv.wallet.core.storage.migrations.Migration_62_63
import com.mrv.wallet.core.storage.migrations.Migration_63_64
import com.mrv.wallet.core.storage.migrations.Migration_64_65
import com.mrv.wallet.core.storage.migrations.Migration_65_66
import com.mrv.wallet.entities.ActiveAccount
import com.mrv.wallet.entities.BlockchainSettingRecord
import com.mrv.wallet.entities.EnabledWallet
import com.mrv.wallet.entities.EnabledWalletCache
import com.mrv.wallet.entities.EvmAddressLabel
import com.mrv.wallet.entities.EvmMethodLabel
import com.mrv.wallet.entities.EvmSyncSourceRecord
import com.mrv.wallet.entities.LogEntry
import com.mrv.wallet.entities.MoneroNodeRecord
import com.mrv.wallet.entities.RecentAddress
import com.mrv.wallet.entities.RestoreSettingRecord
import com.mrv.wallet.entities.SpamAddress
import com.mrv.wallet.entities.SpamScanState
import com.mrv.wallet.entities.StatRecord
import com.mrv.wallet.entities.SyncerState
import com.mrv.wallet.entities.TokenAutoEnabledBlockchain
import com.mrv.wallet.entities.nft.NftAssetBriefMetadataRecord
import com.mrv.wallet.entities.nft.NftAssetRecord
import com.mrv.wallet.entities.nft.NftCollectionRecord
import com.mrv.wallet.entities.nft.NftMetadataSyncRecord
import com.mrv.wallet.modules.chart.ChartIndicatorSetting
import com.mrv.wallet.modules.chart.ChartIndicatorSettingsDao
import com.mrv.wallet.modules.pin.core.Pin
import com.mrv.wallet.modules.pin.core.PinDao
import com.mrv.wallet.modules.profeatures.storage.ProFeaturesDao
import com.mrv.wallet.modules.profeatures.storage.ProFeaturesSessionKey
import com.mrv.wallet.modules.walletconnect.storage.WCSessionDao
import com.mrv.wallet.modules.walletconnect.storage.WalletConnectV2Session

@Database(version = 66, exportSchema = false, entities = [
    EnabledWallet::class,
    EnabledWalletCache::class,
    AccountRecord::class,
    BlockchainSettingRecord::class,
    EvmSyncSourceRecord::class,
    LogEntry::class,
    FavoriteCoin::class,
    WalletConnectV2Session::class,
    RestoreSettingRecord::class,
    ActiveAccount::class,
    NftCollectionRecord::class,
    NftAssetRecord::class,
    NftMetadataSyncRecord::class,
    NftAssetBriefMetadataRecord::class,
    ProFeaturesSessionKey::class,
    EvmAddressLabel::class,
    EvmMethodLabel::class,
    SyncerState::class,
    TokenAutoEnabledBlockchain::class,
    ChartIndicatorSetting::class,
    Pin::class,
    StatRecord::class,
    SpamAddress::class,
    SpamScanState::class,
    RecentAddress::class,
    MoneroNodeRecord::class
])

@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chartIndicatorSettingsDao(): ChartIndicatorSettingsDao
    abstract fun walletsDao(): EnabledWalletsDao
    abstract fun enabledWalletsCacheDao(): EnabledWalletsCacheDao
    abstract fun accountsDao(): AccountsDao
    abstract fun blockchainSettingDao(): BlockchainSettingDao
    abstract fun evmSyncSourceDao(): EvmSyncSourceDao
    abstract fun restoreSettingDao(): RestoreSettingDao
    abstract fun logsDao(): LogsDao
    abstract fun marketFavoritesDao(): MarketFavoritesDao
    abstract fun wcSessionDao(): WCSessionDao
    abstract fun nftDao(): NftDao
    abstract fun proFeaturesDao(): ProFeaturesDao
    abstract fun evmAddressLabelDao(): EvmAddressLabelDao
    abstract fun evmMethodLabelDao(): EvmMethodLabelDao
    abstract fun syncerStateDao(): SyncerStateDao
    abstract fun tokenAutoEnabledBlockchainDao(): TokenAutoEnabledBlockchainDao
    abstract fun pinDao(): PinDao
    abstract fun statsDao(): StatsDao
    abstract fun spamAddressDao(): SpamAddressDao
    abstract fun recentAddressDao(): RecentAddressDao
    abstract fun moneroNodeDao(): MoneroNodeDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "dbBankWallet")
//                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addMigrations(
                            Migration_31_32,
                            Migration_32_33,
                            Migration_33_34,
                            Migration_34_35,
                            Migration_35_36,
                            Migration_36_37,
                            Migration_37_38,
                            Migration_38_39,
                            Migration_39_40,
                            Migration_40_41,
                            Migration_41_42,
                            Migration_42_43,
                            Migration_43_44,
                            Migration_44_45,
                            Migration_45_46,
                            Migration_46_47,
                            Migration_47_48,
                            Migration_48_49,
                            Migration_49_50,
                            Migration_50_51,
                            Migration_51_52,
                            Migration_52_53,
                            Migration_53_54,
                            Migration_54_55,
                            Migration_55_56,
                            Migration_56_57,
                            Migration_57_58,
                            Migration_58_59,
                            Migration_59_60,
                            Migration_60_61,
                            Migration_61_62,
                            Migration_62_63,
                            Migration_63_64,
                            Migration_64_65,
                            Migration_65_66,
                    )
                    .build()
        }

    }
}
