package com.emudeck.igdb_proxy.routes.metadata.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetadataData(
	@SerialName("title") val title: String,
	@SerialName("id") val id: Int,
	@SerialName("description") val description: String,
	@SerialName("developers") val developers: List<Developer>?,
	@SerialName("publishers") val publishers: List<Publisher>?,
	@SerialName("release_date") val releaseDate: Int?,
	@SerialName("compat_category") val compatCategory: SteamDeckCompatCategory,
	@SerialName("compat_notes") val compatNotes: String?,
	@SerialName("store_categories") val storeCategories: List<StoreCategory>,
)

@Serializable
data class Developer(
	@SerialName("name") val name: String,
	@SerialName("url") val url: String
)

@Serializable
data class Publisher(
	@SerialName("name") val name: String,
	@SerialName("url") val url: String
)

@Serializable(with = SteamDeckCompatCategory.Serializer::class)
enum class SteamDeckCompatCategory
{
	UNKNOWN,
	UNSUPPORTED,
	PLAYABLE,
	VERIFIED;
	object Serializer : EnumAsIntSerializer<SteamDeckCompatCategory>(
		"SteamDeckCompatCategory",
		{ it.ordinal },
		{ v -> entries.first { it.ordinal == v } }
	)
}

@Serializable(with = StoreCategory.Serializer::class)
enum class StoreCategory(val value: Int)
{
	MultiPlayer(1),
	SinglePlayer(2),
	CoOp(9),
	PartialController(18),
	MMO(20),
	Achievements(22),
	SteamCloud(23),
	SplitScreen(24),
	CrossPlatformMultiPlayer(27),
	FullController(28),
	TradingCards(29),
	Workshop(30),
	VRSupport(31),
	OnlineMultiPlayer(36),
	LocalMultiPlayer(37),
	OnlineCoOp(38),
	LocalCoOp(392),
	RemotePlayTogether(44),
	HighQualitySoundtrackAudio(50);
	object Serializer : EnumAsIntSerializer<StoreCategory>(
        "StoreCategory",
        { it.value },
        { v -> entries.first { it.value == v } }
    )
}