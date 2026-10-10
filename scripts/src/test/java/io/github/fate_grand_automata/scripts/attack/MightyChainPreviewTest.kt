package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import kotlin.test.Test

class MightyChainPreviewTest {
    private fun card(
        commandCard: CommandCard.Face,
        slot: FieldSlot,
        type: CardTypeEnum,
    ) = ParsedCard(
        card = commandCard,
        servant = when (slot) {
            FieldSlot.A -> TeamSlot.A
            FieldSlot.B -> TeamSlot.B
            FieldSlot.C -> TeamSlot.C
        },
        fieldSlot = slot,
        type = type,
    )

    @Test
    fun `required slot selects mighty brave before mixed mighty`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Quick),
            card(CommandCard.Face.E, FieldSlot.C, CardTypeEnum.Quick),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            requiredFieldSlot = FieldSlot.A,
        )!!.take(3).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `manual NP color completes mighty chain`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.C, CardTypeEnum.Arts),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.E, FieldSlot.C, CardTypeEnum.Buster),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Quick),
        )!!.take(2).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B)
    }
}
