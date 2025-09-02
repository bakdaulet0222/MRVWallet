package com.mrv.wallet.entities

import androidx.room.Entity

@Entity(primaryKeys = ["url"])
data class MoneroNodeRecord(
    val url: String,
    val username: String?,
    val password: String?
)
