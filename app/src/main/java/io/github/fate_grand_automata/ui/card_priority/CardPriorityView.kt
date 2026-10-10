package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.Preference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>,
    useServantPriority: Pref<Boolean>,
    useMightyChain: Pref<Boolean>,
    npCardTypes: Pref<List<CardTypeEnum>>,
    mightyChainTurns: List<List<Set<Int>>>
) {
    val pagerState = rememberPagerState(pageCount = {items.size},)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Heading(stringResource(R.string.card_priority))

            val servantPriority by useServantPriority.remember()
            useServantPriority.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_servant_priority),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            val mightyChain by useMightyChain.remember()
            useMightyChain.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_mighty_chain),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            CardPriorityWaveSelector(
                items = items,
                selectedWave = pagerState.currentPage,
                onSelectedWaveChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            )

            HorizontalDivider()

            if (mightyChain) {
                MightyChainSettings(
                    npCardTypes = npCardTypes,
                    turns = mightyChainTurns.getOrNull(pagerState.currentPage).orEmpty()
                )
                HorizontalDivider()
            }

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 5.dp)
                            .padding(top = 11.dp)
                    ) {
                        Text(stringResource(R.string.card_priority_high))
                        Text(stringResource(R.string.card_priority_low))
                    }

                    items.getOrNull(it)?.Render(
                        useServantPriority = servantPriority
                    )
                }
            }
        }
    }
}

@Composable
private fun MightyChainSettings(
    npCardTypes: Pref<List<CardTypeEnum>>,
    turns: List<Set<Int>>
) {
    var types by npCardTypes.remember()
    val entries = linkedMapOf(
        CardTypeEnum.Buster to stringResource(R.string.p_chain_type_buster),
        CardTypeEnum.Arts to stringResource(R.string.p_chain_type_arts),
        CardTypeEnum.Quick to stringResource(R.string.p_chain_type_quick),
        CardTypeEnum.Unknown to stringResource(R.string.p_mighty_chain_np_type_unknown),
    )

    val selectedSlots = turns.flatten().distinct().sorted()

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.p_mighty_chain_np_types),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        selectedSlots.forEach { index ->
            val selected = types.getOrElse(index) { CardTypeEnum.Unknown }
            val dialog = listDialog(
                selected = selected,
                onSelectedChange = { selectedType ->
                    types = List(3) { slot ->
                        if (slot == index) selectedType
                        else types.getOrElse(slot) { CardTypeEnum.Unknown }
                    }
                },
                entries = entries,
                title = stringResource(R.string.p_mighty_chain_servant_np_type, index + 1)
            )

            Preference(
                title = stringResource(R.string.p_mighty_chain_servant_np_type, index + 1),
                summary = entries[selected].orEmpty(),
                onClick = { dialog.show() }
            )
        }

        turns.forEachIndexed { turnIndex, npSlots ->
            MightyChainPreview(
                turn = turnIndex + 1,
                npSlots = npSlots,
                types = types
            )
        }
    }
}

@Composable
private fun MightyChainPreview(
    turn: Int,
    npSlots: Set<Int>,
    types: List<CardTypeEnum>
) {
    val cardTypes = listOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Quick)
    val labels = listOf(
        stringResource(R.string.p_chain_type_buster),
        stringResource(R.string.p_chain_type_arts),
        stringResource(R.string.p_chain_type_quick)
    )
    val colors = listOf(Color(0xFFC62828), Color(0xFF1565C0), Color(0xFF2E7D32))
    val npLabel = stringResource(R.string.p_mighty_chain_np)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = stringResource(R.string.p_mighty_chain_turn, turn),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            cardTypes.forEachIndexed { index, cardType ->
                val isNp = npSlots.any { slot ->
                    types.getOrElse(slot) { CardTypeEnum.Unknown } == cardType
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(colors[index])
                ) {
                    Text(
                        text = if (isNp) npLabel else labels[index],
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
