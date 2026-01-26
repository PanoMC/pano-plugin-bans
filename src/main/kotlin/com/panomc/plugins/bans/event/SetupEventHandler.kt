package com.panomc.plugins.bans.event

import com.panomc.platform.api.event.SetupEventListener
import com.panomc.plugins.bans.BansPlugin
import org.pf4j.PluginState
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SetupEventHandler(private val plugin: BansPlugin) : SetupEventListener {
    @EventListener
    override suspend fun onSetupFinished() {
        if (plugin.pluginState == PluginState.STARTED) {
            plugin.startPlugin()
        }
    }
}
