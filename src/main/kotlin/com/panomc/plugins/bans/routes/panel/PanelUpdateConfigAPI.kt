package com.panomc.plugins.bans.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.api.config.PluginConfigManager
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.model.*
import com.panomc.plugins.bans.BansPlugin
import com.panomc.plugins.bans.config.AvatarSize
import com.panomc.plugins.bans.config.BansConfig
import com.panomc.plugins.bans.config.ViewLayout
import com.panomc.plugins.bans.permission.ManageBansPermission
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelUpdateConfigAPI(
    private val plugin: BansPlugin
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/bans/config", RouteType.POST))

    private val configManager by lazy {
        plugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<BansConfig>
    }
    
    private val authProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .body(
                json(
                    objectSchema()
                        .optionalProperty("avatarSize", stringSchema())
                        .optionalProperty("showAvatars", booleanSchema())
                        .optionalProperty("showReason", booleanSchema())
                        .optionalProperty("showBannedBy", booleanSchema())
                        .optionalProperty("showDuration", booleanSchema())
                        .optionalProperty("showExpiry", booleanSchema())
                        .optionalProperty("showHistory", booleanSchema())
                        .optionalProperty("showTotalBans", booleanSchema())
                        .optionalProperty("viewLayout", stringSchema())
                        .optionalProperty("paginationSize", intSchema())
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageBansPermission(), context)

        val body = context.body().asJsonObject()
        
        val currentConfig = configManager.config
        
        body.getString("avatarSize")?.let { currentConfig.avatarSize = AvatarSize.valueOf(it) }
        body.getBoolean("showAvatars")?.let { currentConfig.showAvatars = it }
        body.getBoolean("showReason")?.let { currentConfig.showReason = it }
        body.getBoolean("showBannedBy")?.let { currentConfig.showBannedBy = it }
        body.getBoolean("showDuration")?.let { currentConfig.showDuration = it }
        body.getBoolean("showExpiry")?.let { currentConfig.showExpiry = it }
        body.getBoolean("showHistory")?.let { currentConfig.showHistory = it }
        body.getBoolean("showTotalBans")?.let { currentConfig.showTotalBans = it }
        body.getString("viewLayout")?.let { currentConfig.viewLayout = ViewLayout.valueOf(it) }
        body.getInteger("paginationSize")?.let { currentConfig.paginationSize = it }

        configManager.saveConfig(JsonObject.mapFrom(currentConfig))

        return Successful(mapOf("config" to currentConfig))
    }
}
