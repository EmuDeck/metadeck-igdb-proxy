package com.emudeck.igdb_proxy.routes.metadata

import com.emudeck.igdb_proxy.API_URL
import com.emudeck.igdb_proxy.auth
import com.emudeck.igdb_proxy.client
import com.emudeck.igdb_proxy.logger
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

val queue: MutableSharedFlow<QueueItem> = MutableSharedFlow()

suspend fun serveQueue() {
	queue.collect { (mutex, body, callback) ->
		val time = measureTime {
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody(body)
			}
			callback(response)
		}
		val toWait = (0.25.seconds - time)
		if (toWait.isPositive())
			delay(toWait)
		mutex.unlock()
	}
}

data class QueueItem(
	val mutex: Mutex,
	val body: String,
	val callback: suspend (HttpResponse) -> Unit,
)