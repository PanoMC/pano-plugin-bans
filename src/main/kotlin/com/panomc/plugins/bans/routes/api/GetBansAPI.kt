package com.panomc.plugins.bans.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.api.config.PluginConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.bans.BansPlugin
import com.panomc.plugins.bans.config.BansConfig
import com.panomc.plugins.bans.dao.BansDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.intSchema

@Endpoint
class GetBansAPI(
    private val plugin: BansPlugin,
    private val bansDao: BansDao
) : Api() {
    override val paths = listOf(Path("/api/bans", RouteType.GET))

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    private val configManager by lazy {
        plugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<BansConfig>
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .queryParameter(optionalParam("hash", intSchema()))
            .queryParameter(optionalParam("search", io.vertx.json.schema.common.dsl.Schemas.stringSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val config = configManager.config
        val pageSize = config.paginationSize
        val page = context.request().getParam("page")?.toIntOrNull() ?: 1
        val search = context.request().getParam("search")

        val bans = bansDao.getBannedPlayers(page, pageSize, config.showHistory, search)
        val total = bansDao.getBannedPlayersCount(config.showHistory, search)
        val lastPage = kotlin.math.ceil(total.toDouble() / pageSize).toInt()

        return Successful(
            mapOf(
                "bans" to bans,
                "pagination" to mapOf(
                    "current" to page,
                    "last" to lastPage,
                    "total" to total,
                    "perPage" to pageSize
                ),
                "config" to config
            )
        )
    }
}
