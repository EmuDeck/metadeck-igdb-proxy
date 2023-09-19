package com.emudeck.igdb_proxy

import com.emudeck.igdb_proxy.routes.docs
import com.emudeck.igdb_proxy.routes.metadata.api
import com.emudeck.igdb_proxy.routes.metadata.serveQueue
import com.emudeck.igdb_proxy.routes.root
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

const val AUTH_URL = "https://id.twitch.tv/oauth2/token"
const val API_URL = "https://api.igdb.com/v4/games"

@Serializable
data class AuthData(val clientId: String, val clientSecret: String)

@Serializable
data class AuthResponse(
	@SerialName("access_token") val accessToken: String,
	@SerialName("token_type") val tokenType: String,
	@SerialName("expires_in") val expiresIn: Int
)
val json = Json {
	prettyPrint = true
	isLenient = true
	ignoreUnknownKeys = true
}
val auth: AuthData = readAuthData()


fun main() = runBlocking {
	val job = launch { serveQueue() }
	embeddedServer(Netty, port = 8083, host = "127.0.0.1", module = Application::module).start(wait = true)
	job.cancel()
}


fun readAuthData(): AuthData
{
	if (!FileSystem.SYSTEM.exists("./settings.json".toPath()))
	{
		FileSystem.SYSTEM.write("./settings.json".toPath()) {
			writeUtf8(json.encodeToString(AuthData("", "")))
		}
	}
	return FileSystem.SYSTEM.read("./settings.json".toPath()) {
		json.decodeFromString<AuthData>(readUtf8())
	}
}

val bearerTokenStorage = mutableListOf<BearerTokens>()

val client = HttpClient(Apache5) {
    install(ClientContentNegotiation) {
        json(json)
    }
	install(Auth) {
		bearer {
			refreshTokens {
				logger.info("Refreshing tokens...")
				val response = client.submitForm(url = AUTH_URL, formParameters =
				Parameters.build {
                    append("client_id", auth.clientId)
					append("client_secret", auth.clientSecret)
					append("grant_type", "client_credentials")
				}, encodeInQuery = true)
				{
					method = HttpMethod.Post
				}.body<AuthResponse>()
				bearerTokenStorage.add(BearerTokens(response.accessToken, ""))
				bearerTokenStorage.last()
			}
		}
	}
	engine {

	}
}

lateinit var logger: Logger


fun Application.module()
{
	logger = log
	plugins()
	routing()
}

fun Application.plugins()
{
	install(CallLogging) {

	}

	install(ContentNegotiation) {
		json(Json {
			prettyPrint = true
			isLenient = true
		})
    }

	install(CORS) {
		anyHost()
		allowHeader(HttpHeaders.ContentType)
	}
}

private fun Application.routing()
{
	routing {
		root()
		api()
		docs()
	}
}

