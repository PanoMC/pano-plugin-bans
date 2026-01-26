package com.panomc.plugins.bans.dao

import com.panomc.platform.db.DBEntity
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.User
import com.panomc.platform.util.BanUtil
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

    suspend fun getBannedPlayers(page: Int, pageSize: Int, showHistory: Boolean, search: String? = null): List<Map<String, Any>> {
        val offset = (page - 1) * pageSize
        val prefix = databaseManager.getTablePrefix()
        
        val searchCondition = if (search != null && search.isNotBlank()) {
            if (showHistory) {
                "AND (u.username LIKE ? OR h.reason LIKE ?)"
            } else {
                "AND u.username LIKE ?"
            }
        } else {
            ""
        }

        val query = if (showHistory) {
            "SELECT u.*, h.reason as banMessage, h.bannedUntil, h.createdAt as banDate FROM `${prefix}ban_history` h LEFT JOIN `${prefix}user` u ON u.id = h.userId WHERE 1=1 $searchCondition ORDER BY h.createdAt DESC LIMIT ? OFFSET ?"
        } else {
            "SELECT u.*, (SELECT createdAt FROM `${prefix}ban_history` WHERE userId = u.id ORDER BY id DESC LIMIT 1) as banDate FROM `${prefix}user` u WHERE u.banned = 1 $searchCondition ORDER BY u.id DESC LIMIT ? OFFSET ?"
        }

        val sqlClient = databaseManager.getSqlClient()
        val params = mutableListOf<Any>()
        if (search != null && search.isNotBlank()) {
            params.add("%$search%")
            if (showHistory) {
                params.add("%$search%")
            }
        }
        params.add(pageSize)
        params.add(offset)

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.from(params))
            .coAwait()

        return rows.map { row ->
            val map = row.toJson().map
            val user = row.toEntity()
            
            map["banned"] = BanUtil.isBanned(user)
            map
        }
    }

    suspend fun getBannedPlayersCount(showHistory: Boolean, search: String? = null): Long {
        val prefix = databaseManager.getTablePrefix()
        
        val searchCondition = if (search != null && search.isNotBlank()) {
            if (showHistory) {
                "LEFT JOIN `${prefix}user` u ON u.id = h.userId WHERE (u.username LIKE ? OR h.reason LIKE ?)"
            } else {
                "AND u.username LIKE ?"
            }
        } else {
            ""
        }

        val query = if (showHistory) {
            "SELECT COUNT(*) FROM `${prefix}ban_history` h $searchCondition"
        } else {
            "SELECT COUNT(*) FROM `${prefix}user` u WHERE u.banned = 1 $searchCondition"
        }

        val sqlClient = databaseManager.getSqlClient()
        val params = mutableListOf<Any>()
        if (search != null && search.isNotBlank()) {
            params.add("%$search%")
            if (showHistory) {
                params.add("%$search%")
            }
        }

        val rows: RowSet<Row> = if (params.isEmpty()) {
            sqlClient.query(query).execute().coAwait()
        } else {
            sqlClient.preparedQuery(query).execute(Tuple.from(params)).coAwait()
        }

        return rows.iterator().next().getLong(0)
    }

    fun Row.toEntity(): User = DBEntity.gson.fromJson(this.toJson().toString(), User::class.java)

    fun RowSet<Row>.toEntities() = this.map { it.toEntity() }
}
