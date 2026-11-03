package com.example.projectandroidstarwars.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projectandroidstarwars.domain.model.GenderFilter
import com.example.projectandroidstarwars.viewmodel.FiltersUiState

@Composable
fun FiltersScreen(
    state: FiltersUiState,
    onNameChange: (String) -> Unit,
    onOnlyWithPhotoChange: (Boolean) -> Unit,
    onGenderChange: (GenderFilter) -> Unit,
    onMaxMassChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
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
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Имя на английском")
            },
            singleLine = true,
            enabled = !state.saving
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.onlyWithPhoto,
                onCheckedChange = onOnlyWithPhotoChange,
                enabled = !state.saving
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
                            selected = state.gender == option,
                            enabled = !state.saving,
                            role = Role.RadioButton,
                            onClick = {
                                onGenderChange(option)
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.gender == option,
                        onClick = null,
                        enabled = !state.saving
                    )

                    Text(
                        text = option.title,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.maxMassText,
            onValueChange = onMaxMassChange,
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
            enabled = !state.saving
        )

        Text(
            text = "Условия применяются одновременно. Фильтры по фотографии, " +
                    "полу и массе работают среди загруженных персонажей. " +
                    "При ограничении массы персонажи с неизвестной массой " +
                    "не показываются.",
            style = MaterialTheme.typography.bodySmall
        )

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.saving
        ) {
            Text(state.saveButtonText)
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.saving
        ) {
            Text("Сбросить")
        }

        Text(
            text = "После сброса нажми «Готово», чтобы сохранить изменения.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}