/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.util.MovingObjectPosition.MovingObjectType

val MovingObjectType.isMiss
    get() = this == MovingObjectType.MISS

val MovingObjectType.isBlock
    get() = this == MovingObjectType.BLOCK

val MovingObjectType.isEntity
    get() = this == MovingObjectType.ENTITY