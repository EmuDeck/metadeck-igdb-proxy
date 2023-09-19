package com.emudeck.igdb_proxy.routes.metadata.models

import com.emudeck.igdb_proxy.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object VerifiedDB
{
	var lastUpdated = 0.toDuration(DurationUnit.MILLISECONDS)
	var db: List<VerifiedDBResult> = listOf()

	suspend fun refresh()
	{
		lastUpdated = getTimeMillis().toDuration(DurationUnit.MILLISECONDS)
		db = client.get("https://opensheet.elk.sh/1fRqvAh_wW8Ho_8i966CCSBgPJ2R_SuDFIvvKsQCv05w/Database").body()
	}

	private fun measureDamerauLevenshtein(a: CharSequence, b: CharSequence): Int {
		val cost = Array(a.length + 1, { IntArray(b.length + 1) })
		for (iA in 0..a.length) {
			cost[iA][0] = iA
		}
		for (iB in 0..b.length) {
			cost[0][iB] = iB
		}
		val mapCharAToIndex = hashMapOf<Char, Int>()

		for (iA in 1..a.length) {
			var prevMatchingBIndex = 0
			for (iB in 1..b.length) {
				val doesPreviousMatch = (a[iA - 1] == b[iB - 1])

				val possibleCosts = mutableListOf<Int>()
				if (doesPreviousMatch) {
					// Perfect match cost.
					possibleCosts.add(cost[iA - 1][iB - 1])
				} else {
					// Substitution cost.
					possibleCosts.add(cost[iA - 1][iB - 1] + 1)
				}
				// Insertion cost.
				possibleCosts.add(cost[iA][iB - 1] + 1)
				// Deletion cost.
				possibleCosts.add(cost[iA - 1][iB] + 1)

				// Transposition cost.
				val bCharIndexInA = mapCharAToIndex.getOrDefault(b[iB - 1], 0)
				if (bCharIndexInA != 0 && prevMatchingBIndex != 0) {
					possibleCosts.add(cost[bCharIndexInA - 1][prevMatchingBIndex - 1]
							+ (iA - bCharIndexInA - 1) + 1 + (iB - prevMatchingBIndex - 1))
				}

				cost[iA][iB] = possibleCosts.min()!!

				if (doesPreviousMatch) prevMatchingBIndex = iB
			}
			mapCharAToIndex[a[iA - 1]] = iA
		}
		return cost[a.length][b.length]
	}

	suspend fun search(name: String): VerifiedDBResult
	{
		if (getTimeMillis().toDuration(DurationUnit.MILLISECONDS).inWholeHours - lastUpdated.inWholeHours > 0)
		{
			refresh()
		}

		var closest: VerifiedDBResult? = null
		var closestDistance: Int = Int.MAX_VALUE
		for (verifiedDBResult: VerifiedDBResult in db)
		{
			val distance = measureDamerauLevenshtein(name, verifiedDBResult.game)
			if (distance < closestDistance)
			{
				closestDistance = distance
				closest = verifiedDBResult
			}
			if (distance == 0)
				return verifiedDBResult
		}
		return closest!!
	}
}

@Serializable
data class VerifiedDBResult(
	@SerialName("Timestamp") val timestamp: String,
	@SerialName("Console") val console: String? = null,
	@SerialName("Game") val game: String,
	@SerialName("Emulator") val emulator: String? = null,
	@SerialName("Boots") val boots: YesNo,
	@SerialName("Playable") val playable: YesNo,
	@SerialName("Notes") val notes: String? = null,
)

@Serializable(with = YesNo.Serializer::class)
enum class YesNo(val value: String)
{
	NO("No"),
	YES("Yes"),
	PARTIAL("Partial");

	object Serializer : EnumAsStringSerializer<YesNo>(
		"YesNo",
		{ it.value },
		{ v -> entries.first { it.value == v } }
	)
}