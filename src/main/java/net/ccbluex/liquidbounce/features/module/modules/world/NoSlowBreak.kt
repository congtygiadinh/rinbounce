/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoSlowBreak : Module("NoSlowBreak", Category.WORLD, gameDetecting = false) {
    val air by boolean("Air", true)
    val water by boolean("Water", false)
}
