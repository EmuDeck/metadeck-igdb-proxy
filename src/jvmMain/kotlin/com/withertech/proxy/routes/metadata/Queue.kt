package com.withertech.proxy.routes.metadata

import com.withertech.proxy.API_URL
import com.withertech.proxy.auth
import com.withertech.proxy.client
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

var queue: Flow<QueueItem>? = null

suspend fun serveQueue() = coroutineScope {
	if (queue != null) {
		queue!!.collect { (body, context, callback) ->
			val response = client.post {
				url(API_URL)
				header("Client-ID", auth.clientId)
				setBody(body)
			}
			context.callback(response)
			delay(0.25.seconds)
		}
	}
	else {
		delay(1.seconds)
	}
}

data class QueueItem(
	val body: String,
	val context: PipelineContext<Unit, ApplicationCall>,
	val callback: suspend PipelineContext<Unit, ApplicationCall>.(HttpResponse) -> Unit,
)