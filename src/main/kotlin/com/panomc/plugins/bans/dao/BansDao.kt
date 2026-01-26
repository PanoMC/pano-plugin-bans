package com.panomc.plugins.bans.dao

import com.panomc.platform.db.DBEntity
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.User
import com.panomc.plugins.bans.BansPlugin
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import org.springframework.stereotype.Component

@Component
class BansDao(private val bansPlugin: BansPlugin) {
    private val databaseManager by lazy {
        bansPlugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    suspend fun getBannedPlayers(page: Int, pageSize: Int, showHistory: Boolean): List<Map<String, Any>> {
        val offset = (page - 1) * pageSize
        val prefix = databaseManager.getTablePrefix()
        
        val query = if (showHistory) {
            "SELECT u.username, u.banned, u.lastActivityTime, h.reason as banMessage, h.bannedUntil, h.createdAt as banDate FROM `${prefix}ban_history` h LEFT JOIN `${prefix}user` u ON u.id = h.userId ORDER BY h.createdAt DESC LIMIT ? OFFSET ?"
        } else {
            "SELECT u.*, (SELECT createdAt FROM `${prefix}ban_history` WHERE userId = u.id ORDER BY id DESC LIMIT 1) as banDate FROM `${prefix}user` u WHERE u.banned = 1 ORDER BY u.id DESC LIMIT ? OFFSET ?"
        }

        val sqlClient = databaseManager.getSqlClient()
        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(pageSize, offset))
            .coAwait()

        return rows.map { it.toJson().map }
    }

    suspend fun getBannedPlayersCount(showHistory: Boolean): Long {
        val prefix = databaseManager.getTablePrefix()
        
        val query = if (showHistory) {
            "SELECT COUNT(*) FROM `${prefix}ban_history`"
        } else {
            "SELECT COUNT(*) FROM `${prefix}user` u WHERE u.banned = 1"
        }

        val sqlClient = databaseManager.getSqlClient()
        val rows: RowSet<Row> = sqlClient
            .query(query)
            .execute()
            .coAwait()

        return rows.iterator().next().getLong(0)
    }

    fun Row.toEntity(): User = DBEntity.gson.fromJson(this.toJson().toString(), User::class.java)

    fun RowSet<Row>.toEntities() = this.map { it.toEntity() }
}
