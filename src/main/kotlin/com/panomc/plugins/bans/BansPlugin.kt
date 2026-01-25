package com.panomc.plugins.bans

import com.panomc.platform.api.PanoPlugin

class BansPlugin : PanoPlugin() {
    override suspend fun onStart() {
        logger.info("Starting...")
    }

    override suspend fun onEnable() {
        logger.info("Enabled!")
    }

    override suspend fun onUninstall() {
        logger.info("Uninstalling...")

        // add some cleanup codes for your data used in plugin before uninstalling
    }
}
