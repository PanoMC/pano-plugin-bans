package com.panomc.plugins.bans

import com.panomc.platform.api.PanoPlugin

import com.panomc.platform.api.config.PluginConfigManager
import com.panomc.platform.setup.SetupManager
import com.panomc.plugins.bans.config.BansConfig

class BansPlugin : PanoPlugin() {
    private val setupManager by lazy {
        applicationContext.getBean(SetupManager::class.java)
    }

    private var isInitialized = false

    override suspend fun onStart() {
        logger.info("Starting...")

        startPlugin()
    }

    internal fun startPlugin() {
        if (isInitialized) return
        isInitialized = true

        if (!setupManager.isSetupDone()) {
            logger.info("Setup is not finished, waiting for setup completion...")
            return
        }

        val configManager = PluginConfigManager(this, BansConfig::class.java)
        pluginBeanContext.beanFactory.registerSingleton(PluginConfigManager::class.java.name, configManager)

        logger.info("Started!")
    }

    override suspend fun onEnable() {
        logger.info("Enabled!")
    }

    override suspend fun onDisable() {
        isInitialized = false
    }

    override suspend fun onUninstall() {
        logger.info("Uninstalling...")

        // add some cleanup codes for your data used in plugin before uninstalling
    }
}
