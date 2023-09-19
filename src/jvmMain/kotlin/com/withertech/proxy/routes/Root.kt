package com.withertech.proxy.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun HTML.index()
{
	head {
		title("Hey!")
	}
	body {
		div {
			id = "root"
		}
		script(src = "/metadeck/api/static/metadeck-igdb-proxy.js") {}
	}
}

fun Route.root()
{
	get("/") {
		call.respondHtml(HttpStatusCode.OK, HTML::index)
	}
	staticResources("/static", null)
}