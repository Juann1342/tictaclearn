package com.chifuz.tictaclearn.domain.model

data class GameMode(
    val id: String,
    val displayName: String,
    val boardSize: Int,
    val winningLength: Int
) {
    companion object {
        val CLASSIC = GameMode("classic_3x3", "Classic (3x3)", 3, 3)
        val GOMOKU = GameMode("gomoku_9x9", "Gomoku (9x9)", 9, 5)

        // 🚨 NUEVO MODO
        val PARTY = GameMode("gomoku_party", "Party PvP (9x9)", 9, 5)

        val ALL_MODES = listOf(CLASSIC, GOMOKU, PARTY)

        fun fromId(id: String): GameMode? {
            // Manejo especial para IDs compuestos del modo Party
            if (id.startsWith("gomoku_party")) return PARTY
            return ALL_MODES.find { it.id == id }
        }
    }
}