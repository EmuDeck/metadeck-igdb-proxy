package com.emudeck.igdb_proxy.routes.metadata

import com.emudeck.igdb_proxy.API_URL
import com.emudeck.igdb_proxy.auth
import com.emudeck.igdb_proxy.client
import com.emudeck.igdb_proxy.routes.metadata.models.GameResponse
import com.emudeck.igdb_proxy.routes.metadata.models.MetadataData
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours

@Serializable
data class SearchBody(val title: String)

@Serializable
data class GetBody(val id: Int)

fun Route.api() {
	val searchCache = Cache.Builder<String, List<GameResponse>>()
		.expireAfterWrite(8.hours)
		.build()
	val getCache = Cache.Builder<Int, GameResponse>()
		.expireAfterWrite(24.hours)
		.build()
	post<SearchBody>("/search") {
		val (title) = it
		val cached = searchCache.get(title)
		if (cached != null)
		{
			call.respond(cached)
		} else
		{
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody("search \"$title\"; fields *, involved_companies.*, involved_companies.company.*, game_modes.*, multiplayer_modes.*, platforms.*; limit 20;")
			}.body<List<GameResponse>>()
			val metadata = response.sortedBy { x -> x.id }
			searchCache.put(title, metadata)
			getCache.put(metadata.first().id, metadata.first())
			call.respond(metadata)
		}
	}
	post<GetBody>("/get") {
		val (id) = it
		val cached = getCache.get(id)
		if (cached != null)
		{
			call.respond(cached)
		} else
		{
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody("where id = $id; fields *, involved_companies.*, involved_companies.company.*, game_modes.*, multiplayer_modes.*, platforms.*; limit 20;")
			}.body<List<GameResponse>>()
			val metadata = response.first()
			println(metadata)
			getCache.put(id, metadata)
			call.respond(metadata)
		}
	}
}