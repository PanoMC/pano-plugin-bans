package com.panomc.plugins.bans.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.api.config.PluginConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.InvalidFields
import com.panomc.platform.error.PageNotFound
import com.panomc.platform.model.*
import com.panomc.plugins.bans.BansPlugin
import com.panomc.plugins.bans.config.BansConfig
import com.panomc.plugins.bans.dao.BansDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.optionalParam
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.intSchema
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetBansAPI(
    private val plugin: BansPlugin,
    private val bansDao: BansDao
) : Api() {
    override val paths = listOf(Path("/bans", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "The banned players, paged, with the display settings of the bans page.",
        tag = "bans",
        response = objectSchema()
            .requiredProperty("items", arraySchema().items(objectSchema()))
            .requiredProperty(
                "page",
                objectSchema()
                    .requiredProperty("number", intSchema())
                    .requiredProperty("size", intSchema())
                    .requiredProperty("totalItems", intSchema())
                    .requiredProperty("totalPages", intSchema())
            )
            .requiredProperty("config", objectSchema()),
        errors = listOf(InvalidFields::class, PageNotFound::class)
    )

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    private val configManager by lazy {
        plugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<BansConfig>
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        Paging.params(
            ValidationHandlerBuilder.create(schemaRepository)
                .queryParameter(optionalParam("search", io.vertx.json.schema.common.dsl.Schemas.stringSchema()))
        ).build()

    override suspend fun handle(context: RoutingContext): Result {
        val config = configManager.config
        val page = Paging.request(context, config.paginationSize.coerceIn(1, Paging.MAX_SIZE))
        val search = context.request().getParam("search")

        val total = bansDao.getBannedPlayersCount(config.showHistory, search)

        Paging.requireInRange(page, total)

        val bans = bansDao.getBannedPlayers(page.number, page.size, config.showHistory, search)

        return Successful(Paging.response(bans, total, page, mapOf("config" to config)))
    }
}
