package com.mrv.wallet.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SyncerState(
    @PrimaryKey
    val key: String,
    val value: String
)
