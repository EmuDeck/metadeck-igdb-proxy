package com.withertech.proxy.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.io.File
import java.io.FileNotFoundException

fun Route.docs()
{
	swaggerUI(path = "docs", prefix = "/metadeck/api")
}

fun Route.swaggerUI(
	path: String,
	swaggerFile: String = "openapi/documentation.yaml",
	prefix: String = "",
	block: SwaggerConfig.() -> Unit = {}
) {
	val resource = application.environment.classLoader.getResourceAsStream(swaggerFile)
		?.bufferedReader()

	if (resource != null) {
		swaggerUI(path, swaggerFile.takeLastWhile { it != '/' }, prefix, resource.readText(), block)
		return
	}

	swaggerUI(path, File(swaggerFile), prefix, block)
}

fun Route.swaggerUI(path: String, apiFile: File, prefix: String, block: SwaggerConfig.() -> Unit = {}) {
	if (!apiFile.exists()) {
		throw FileNotFoundException("Swagger file not found: ${apiFile.absolutePath}")
	}

	val content = apiFile.readText()
	swaggerUI(path, apiFile.name, prefix, content, block)
}

fun Route.swaggerUI(
	path: String,
	apiUrl: String,
	prefix: String,
	api: String,
	block: SwaggerConfig.() -> Unit = {}
) {
	val config = SwaggerConfig().apply(block)

	route(path) {
		get(apiUrl) {
			call.respondText(api, ContentType.fromFilePath(apiUrl).firstOrNull())
		}
		get {
			val fullPath = call.request.path()
			call.respondHtml {
				head {
					title { +"Swagger UI" }
					link(
						href = "${config.packageLocation}@${config.version}/swagger-ui.css",
						rel = "stylesheet"
					)
//					config.customStyle?.let {
//						link(href = it, rel = "stylesheet")
//					}
				}
				body {
					div { id = "swagger-ui" }
					script(src = "${config.packageLocation}@${config.version}/swagger-ui-bundle.js") {
						attributes["crossorigin"] = "anonymous"
					}

					val src = "${config.packageLocation}@${config.version}/swagger-ui-standalone-preset.js"
					script(src = src) {
						attributes["crossorigin"] = "anonymous"
					}

					script {
						unsafe {
							+"""
window.onload = function() {
    window.ui = SwaggerUIBundle({
        url: '$prefix$fullPath/$apiUrl',
        dom_id: '#swagger-ui',
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: 'StandaloneLayout'
    });
}
                            """.trimIndent()
						}
					}
				}
			}
		}
	}
}