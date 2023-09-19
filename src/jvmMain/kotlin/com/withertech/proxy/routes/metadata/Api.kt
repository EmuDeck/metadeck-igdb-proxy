package com.withertech.proxy.routes.metadata

import com.withertech.proxy.API_URL
import com.withertech.proxy.auth
import com.withertech.proxy.client
import com.withertech.proxy.routes.metadata.models.GameResponse
import com.withertech.proxy.routes.metadata.models.MetadataData
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours

@Serializable
data class SearchBody(val title: String)

@Serializable
data class GetBody(val id: Int)

fun Route.api() {
	val searchCache = Cache.Builder<String, List<MetadataData>>()
		.expireAfterWrite(1.hours)
		.build()
	val getCache = Cache.Builder<Int, MetadataData>()
		.expireAfterWrite(1.hours)
		.build()
	post<SearchBody>("/search") {
		val (title) = it
		if (searchCache.get(title) == null) {
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody("search \"$title\"; fields *, involved_companies.*, involved_companies.company.*, game_modes.*, multiplayer_modes.*, platforms.*; limit 20;")
			}.body<List<GameResponse>>()

			val metadata = response.map { element -> element.toMetaDeck() }
			searchCache.put(title, metadata)
			call.respond(metadata)
		}
		else
		{
			call.respond(searchCache.get(title)!!)
		}
	}
	post<GetBody>("/get") {
		val (id) = it
		if (getCache.get(id) == null)
		{
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody("where id = $id; fields *, involved_companies.*, involved_companies.company.*, game_modes.*, multiplayer_modes.*, platforms.*; limit 20;")
			}.body<List<GameResponse>>()
			val metadata = response.first().toMetaDeck()
			println(metadata)
			getCache.put(id, metadata)
			call.respond(metadata)
		}
		else
		{
			call.respond(getCache.get(id)!!)
		}
	}
}