/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.chat

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * An axochat user
 *
 * @param name of user
 * @param uuid of user
 */
data class User(
    @SerializedName("name")
    val name: String,

    @SerializedName("uuid")
    val uuid: UUID
)