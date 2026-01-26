package com.panomc.plugins.bans.event

import com.panomc.platform.api.annotation.EventListener
import com.panomc.platform.api.event.SetupEventListener
import com.panomc.plugins.bans.BansPlugin
import org.pf4j.PluginState

@EventListener
class SetupEventHandler(private val plugin: BansPlugin): SetupEventListener {
    private val logger by lazy {
        plugin.logger
    }

    override suspend fun onSetupFinished() {
        if (plugin.pluginState == PluginState.STARTED) {
            logger.info("Setup finished! Initializing plugin...")

            plugin.startPlugin()
        }
    }
}
