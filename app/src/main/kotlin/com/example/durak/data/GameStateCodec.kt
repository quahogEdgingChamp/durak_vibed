package com.example.durak.data

import com.example.durak.game.AiDifficulty
import com.example.durak.game.Card
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus
import com.example.durak.game.Player
import com.example.durak.game.Rank
import com.example.durak.game.Suit
import com.example.durak.game.TableCard

/**
 * Serializes a full [GameState] to a line-based text format for SharedPreferences.
 * Kept dependency-free so it stays testable in plain JVM unit tests.
 */
object GameStateCodec {
    private const val VERSION = 1
    private const val NONE = "-"

    fun encode(state: GameState): String = buildString {
        line("v", VERSION.toString())
        line("deckMode", state.settings.deckMode.name)
        line("gameMode", state.settings.gameMode.name)
        line("playerCount", state.settings.playerCount.toString())
        line("aiDifficulty", state.settings.aiDifficulty.name)
        line("trump", encodeCard(state.trumpCard))
        line("drawPile", encodeCards(state.drawPile))
        line("discard", encodeCards(state.discardPile))
        line("table", state.table.joinToString(",") { pair ->
            encodeCard(pair.attack) + ":" + (pair.defense?.let(::encodeCard) ?: NONE)
        })
        line("attacker", state.attackerIndex.toString())
        line("defender", state.defenderIndex.toString())
        line("defenderHandStart", state.defenderHandSizeAtBoutStart.toString())
        line("takingDefender", state.takingDefenderIndex?.toString() ?: NONE)
        line("throwInActor", state.throwInActorIndex?.toString() ?: NONE)
        line("passedThrowIn", state.playersPassedThrowIn.sorted().joinToString(","))
        line("status", state.status.name)
        line("loser", state.loserIndex?.toString() ?: NONE)
        line("isDraw", state.isDraw.toString())
        line("message", state.message.replace("\n", " "))
        state.players.forEach { player ->
            line("player", "${player.id}|${player.name}|${player.isHuman}|${encodeCards(player.hand)}")
        }
    }

    fun decode(text: String): GameState? = runCatching {
        val fields = mutableMapOf<String, String>()
        val playerLines = mutableListOf<String>()
        text.lineSequence().forEach { raw ->
            if (raw.isBlank()) return@forEach
            val key = raw.substringBefore('=')
            val value = raw.substringAfter('=', "")
            if (key == "player") playerLines += value else fields[key] = value
        }
        if (fields["v"]?.toInt() != VERSION) return null

        val settings = GameSettings(
            deckMode = DeckMode.valueOf(fields.getValue("deckMode")),
            gameMode = GameMode.valueOf(fields.getValue("gameMode")),
            playerCount = fields.getValue("playerCount").toInt(),
            aiDifficulty = AiDifficulty.valueOf(fields.getValue("aiDifficulty"))
        )
        val players = playerLines.map { lineValue ->
            val parts = lineValue.split('|')
            Player(
                id = parts[0].toInt(),
                name = parts[1],
                isHuman = parts[2].toBoolean(),
                hand = decodeCards(parts[3])
            )
        }.sortedBy { it.id }
        if (players.size != settings.playerCount) return null

        val trumpCard = decodeCard(fields.getValue("trump"))
        val table = fields.getValue("table").splitToNonBlank().map { pairText ->
            val attack = decodeCard(pairText.substringBefore(':'))
            val defenseText = pairText.substringAfter(':')
            TableCard(attack, if (defenseText == NONE) null else decodeCard(defenseText))
        }
        GameState(
            settings = settings,
            players = players,
            drawPile = decodeCards(fields.getValue("drawPile")),
            trumpCard = trumpCard,
            trumpSuit = trumpCard.suit,
            table = table,
            discardPile = decodeCards(fields.getValue("discard")),
            attackerIndex = fields.getValue("attacker").toInt(),
            defenderIndex = fields.getValue("defender").toInt(),
            defenderHandSizeAtBoutStart = fields.getValue("defenderHandStart").toInt(),
            takingDefenderIndex = fields.getValue("takingDefender").toIntOrNone(),
            throwInActorIndex = fields.getValue("throwInActor").toIntOrNone(),
            playersPassedThrowIn = fields.getValue("passedThrowIn").splitToNonBlank().map { it.toInt() }.toSet(),
            status = GameStatus.valueOf(fields.getValue("status")),
            loserIndex = fields.getValue("loser").toIntOrNone(),
            isDraw = fields.getValue("isDraw").toBoolean(),
            message = fields["message"].orEmpty()
        )
    }.getOrNull()

    private fun StringBuilder.line(key: String, value: String) {
        append(key).append('=').append(value).append('\n')
    }

    private fun encodeCard(card: Card): String = card.rank.label + card.suit.name.first()

    private fun decodeCard(text: String): Card {
        val suitLetter = text.last()
        val rankLabel = text.dropLast(1)
        val suit = Suit.entries.first { it.name.first() == suitLetter }
        val rank = Rank.entries.first { it.label == rankLabel }
        return Card(suit, rank)
    }

    private fun encodeCards(cards: List<Card>): String = cards.joinToString(",") { encodeCard(it) }

    private fun decodeCards(text: String): List<Card> = text.splitToNonBlank().map { decodeCard(it) }

    private fun String.splitToNonBlank(): List<String> = split(',').filter { it.isNotBlank() }

    private fun String.toIntOrNone(): Int? = if (this == NONE) null else toInt()
}
