package com.emudeck.igdb_proxy.routes.metadata.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameResponse(
	@SerialName("id") val id: Int,
	@SerialName("name") val name: String? = null,
	@SerialName("summary") val summary: String? = null,
	@SerialName("first_release_date") val firstReleaseDate: Int? = null,
	@SerialName("game_modes") val gameModes: List<GameMode>? = null,
	@SerialName("multiplayer_modes") val multiplayerModes: List<MultiplayerMode>? = null,
	@SerialName("platforms") val platforms: List<Platform>? = null,
	@SerialName("involved_companies") val involvedCompanies: List<InvolvedCompany>? = null
)
{
	suspend fun toMetaDeck(): MetadataData
	{
		val gameDevs = mutableListOf<Developer>()
		val gamePubs = mutableListOf<Publisher>()
		val gameCats = mutableListOf<StoreCategory>()

		if (gameModes != null)
		{
			for (gameMode in gameModes)
			{
				if (gameMode.slug != null)
				{
					when (gameMode.slug)
					{
						"single-player" -> {
							gameCats.add(StoreCategory.SinglePlayer)
						}
						"multiplayer" -> {
							gameCats.add(StoreCategory.MultiPlayer)
						}
					}
				}
			}
		}

		if (multiplayerModes != null)
		{
			for (multiplayerMode in multiplayerModes)
            {
                if (multiplayerMode.onlineCoop == true)
                {
					gameCats.add(StoreCategory.OnlineCoOp)
                }
	            if (multiplayerMode.offlineCoop == true)
	            {
					gameCats.add(StoreCategory.LocalCoOp)
	            }
	            if (multiplayerMode.splitScreen == true || multiplayerMode.splitScreenOnline == true)
	            {
					gameCats.add(StoreCategory.SplitScreen)
	            }
	            if (multiplayerMode.onlineCoop == true || multiplayerMode.splitScreenOnline == true)
	            {
					gameCats.add(StoreCategory.OnlineMultiPlayer)
	            }
	            if (multiplayerMode.offlineCoop == true || multiplayerMode.lanCoop == true || multiplayerMode.splitScreen == true)
	            {
					gameCats.add(StoreCategory.LocalMultiPlayer)
	            }
            }
		}

		if (involvedCompanies != null)
		{
			for (involvedCompany in involvedCompanies)
            {
				if (involvedCompany.company?.name != null &&
					involvedCompany.company.url != null)
				{
					if (involvedCompany.developer == true)
					{
						gameDevs.add(
							Developer(
							involvedCompany.company.name,
							involvedCompany.company.url
						)
						)
					}
					if (involvedCompany.publisher == true)
					{
						gamePubs.add(
							Publisher(
							involvedCompany.company.name,
							involvedCompany.company.url
						)
						)
					}
				}
            }
		}

		val compatCategoryResult = name?.let { VerifiedDB.search(it) }
		var compatCategory = SteamDeckCompatCategory.UNKNOWN
		if (compatCategoryResult != null)
		{
			if (compatCategoryResult.boots == YesNo.YES && compatCategoryResult.playable == YesNo.YES)
			{
				compatCategory = SteamDeckCompatCategory.VERIFIED
			}
			else if (compatCategoryResult.boots == YesNo.YES && compatCategoryResult.playable == YesNo.NO)
			{
				compatCategory = SteamDeckCompatCategory.PLAYABLE
			}
			else if (compatCategoryResult.boots == YesNo.YES && compatCategoryResult.playable == YesNo.PARTIAL)
			{
				compatCategory = SteamDeckCompatCategory.PLAYABLE
			}
			else
			{
				compatCategory = SteamDeckCompatCategory.UNSUPPORTED
			}
		}

		return MetadataData(
			title = name ?: "No Title",
			id = id,
			description = summary ?: "No Description",
			developers = gameDevs,
			publishers = gamePubs,
			releaseDate = firstReleaseDate,
			compatCategory = compatCategory,
			compatNotes = compatCategoryResult?.notes,
			storeCategories = gameCats,
        )
	}
}

@Serializable
data class GameMode(
	@SerialName("id") val id: Int,
    @SerialName("slug") val slug: String? = null
)

@Serializable
data class MultiplayerMode(
	@SerialName("id") val id: Int,
    @SerialName("onlinecoop") val onlineCoop: Boolean? = null,
    @SerialName("offlinecoop") val offlineCoop: Boolean? = null,
    @SerialName("splitscreen") val splitScreen: Boolean? = null,
    @SerialName("splitscreenonline") val splitScreenOnline: Boolean? = null,
    @SerialName("lancoop") val lanCoop: Boolean? = null
)

@Serializable
data class Platform(
	@SerialName("id") val id: Int,
	@SerialName("category") val category: PlatformCategory? = null
)

@Serializable
data class InvolvedCompany(
	@SerialName("id") val id: Int,
    @SerialName("developer") val developer: Boolean? = null,
    @SerialName("publisher") val publisher: Boolean? = null,
    @SerialName("company") val company: Company? = null
)

@Serializable
data class Company(
	@SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null
)

@Serializable(with = PlatformCategory.Serializer::class)
enum class PlatformCategory(val value: Int)
{
	Console(1),
	Arcade(2),
	Platform(3),
	OperatingSystem(4),
	PortableConsole(5),
	Computer(6);
	object Serializer : EnumAsIntSerializer<PlatformCategory>(
		"PlatformCategory",
		{ it.value },
		{ v -> entries.first { it.value == v } }
	)
}
