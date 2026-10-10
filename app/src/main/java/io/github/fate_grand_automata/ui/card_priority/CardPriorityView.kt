package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.drag_sort.DragSortItemStyle
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>,
    useServantPriority: Pref<Boolean>,
    useMightyChain: Pref<Boolean>,
    npCardTypes: Pref<List<CardTypeEnum>>,
    mightyChainTurns: List<List<Set<Int>>>,
    mightyChainOrders: List<SnapshotStateList<CardTypeEnum>>
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
                    selectedNpSlots = mightyChainTurns
                        .getOrNull(pagerState.currentPage)
                        .orEmpty()
                        .flatten()
                        .distinct()
                        .sorted(),
                    order = mightyChainOrders.getOrNull(pagerState.currentPage)
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
                        useServantPriority = servantPriority,
                        useMightyChain = mightyChain
                    )
                }
            }
        }
    }
}

@Composable
private fun MightyChainSettings(
    npCardTypes: Pref<List<CardTypeEnum>>,
    selectedNpSlots: List<Int>,
    order: SnapshotStateList<CardTypeEnum>?
) {
    var types by npCardTypes.remember()
    val entries = linkedMapOf(
        CardTypeEnum.Buster to stringResource(R.string.p_chain_type_buster),
        CardTypeEnum.Arts to stringResource(R.string.p_chain_type_arts),
        CardTypeEnum.Quick to stringResource(R.string.p_chain_type_quick),
        CardTypeEnum.Unknown to stringResource(R.string.p_mighty_chain_np_type_unknown),
    )

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        if (selectedNpSlots.isNotEmpty()) {
            Text(
                text = stringResource(R.string.p_mighty_chain_np_types),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    selectedNpSlots.forEach { index ->
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

                        ListItem(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialog.show() },
                            headlineContent = {
                                Text(stringResource(R.string.p_mighty_chain_servant_np, index + 1))
                            },
                            supportingContent = { Text(entries[selected].orEmpty()) },
                            colors = FGAListItemColors()
                        )
                    }
                }
            }
        }

        if (order != null) {
            MightyChainOrder(
                order = order,
                selectedNpSlots = selectedNpSlots,
                npTypes = types
            )
        }
    }
}

@Composable
private fun MightyChainOrder(
    order: SnapshotStateList<CardTypeEnum>,
    selectedNpSlots: List<Int>,
    npTypes: List<CardTypeEnum>
) {
    val labels = mapOf(
        CardTypeEnum.Buster to stringResource(R.string.p_chain_type_buster),
        CardTypeEnum.Arts to stringResource(R.string.p_chain_type_arts),
        CardTypeEnum.Quick to stringResource(R.string.p_chain_type_quick)
    )
    val npLabel = stringResource(R.string.p_mighty_chain_np)

    DragSort(
        items = order,
        key = { it.name },
        style = { cardType ->
            val isNp = selectedNpSlots.any { slot ->
                npTypes.getOrElse(slot) { CardTypeEnum.Unknown } == cardType
            }
            DragSortItemStyle(
                backgroundColor = colorResource(
                    when (cardType) {
                        CardTypeEnum.Buster -> R.color.colorBuster
                        CardTypeEnum.Arts -> R.color.colorArts
                        CardTypeEnum.Quick -> R.color.colorQuick
                        CardTypeEnum.Unknown -> R.color.colorPrimaryDark
                    }
                ),
                contentColor = Color.White,
                text = if (isNp) npLabel else labels[cardType].orEmpty()
            )
        },
        modifier = Modifier.fillMaxWidth()
    )

    Text(
        stringResource(R.string.card_priority_hint_move_item),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
