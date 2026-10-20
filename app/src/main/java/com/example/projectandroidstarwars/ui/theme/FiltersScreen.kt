package com.example.projectandroidstarwars.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.viewmodel.FiltersViewModel

@Composable
fun FiltersScreen(
    viewModel: FiltersViewModel,
    onDone: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки поиска",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = viewModel.name,
            onValueChange = viewModel::updateName,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Имя на английском")
            },
            singleLine = true,
            enabled = !viewModel.saving
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = viewModel.onlyWithPhoto,
                onCheckedChange = viewModel::updateOnlyWithPhoto,
                enabled = !viewModel.saving
            )

            Text(
                text = "Только персонажи с фотографией",
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Пол",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            GenderFilter.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = viewModel.gender == option,
                            enabled = !viewModel.saving,
                            role = Role.RadioButton,
                            onClick = {
                                viewModel.updateGender(option)
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = viewModel.gender == option,
                        onClick = null,
                        enabled = !viewModel.saving
                    )

                    Text(
                        text = option.title,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.maxMassText,
            onValueChange = viewModel::updateMaxMass,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Максимальная масса, кг")
            },
            placeholder = {
                Text("Например, 80")
            },
            supportingText = {
                Text("Пустое поле — без ограничения массы.")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            singleLine = true,
            enabled = !viewModel.saving
        )

        Text(
            text = "Условия применяются одновременно. Фильтры по фотографии, " +
                    "полу и массе работают среди загруженных персонажей. " +
                    "При ограничении массы персонажи с неизвестной массой " +
                    "не показываются.",
            style = MaterialTheme.typography.bodySmall
        )

        viewModel.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                viewModel.save(onDone)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.saving
        ) {
            Text(
                if (viewModel.saving) {
                    "Сохранение…"
                } else {
                    "Готово"
                }
            )
        }

        OutlinedButton(
            onClick = viewModel::resetDraft,
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.saving
        ) {
            Text("Сбросить")
        }

        Text(
            text = "После сброса нажми «Готово», чтобы сохранить изменения.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}