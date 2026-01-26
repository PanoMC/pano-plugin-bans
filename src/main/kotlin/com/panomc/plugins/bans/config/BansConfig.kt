package com.panomc.plugins.bans.config

import com.panomc.platform.api.config.PluginConfig

class BansConfig(
    var avatarSize: AvatarSize = AvatarSize.PX_64,
    var showAvatars: Boolean = true,
    var showReason: Boolean = true,
    var showBannedBy: Boolean = true,
    var showDuration: Boolean = true,
    var showExpiry: Boolean = true,
    var showHistory: Boolean = false,
    var showTotalBans: Boolean = true,
    var viewLayout: ViewLayout = ViewLayout.LIST,
    var paginationSize: Int = 20,
    version: Int = 1
) : PluginConfig(version)

enum class ViewLayout {
    LIST,
    GRID
}

enum class AvatarSize(val size: Int) {
    PX_16(16),
    PX_32(32),
    PX_64(64)
}
