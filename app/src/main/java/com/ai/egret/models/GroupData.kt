package com.ai.egret.models

import com.google.firebase.Timestamp

/**
 * Represents a group created by a user. Includes Firestore document ID and creation timestamp.
 */
data class GroupData(
    val id: String = "",
    val name: String = "",
    val leaderUid: String = "",
    val leaderName: String = "",
    val commodity: String = "",
    val quantity: Int = 0,
    val location: String = "",
    val status: String = "open",
    val createdAt: Timestamp? = null
)


