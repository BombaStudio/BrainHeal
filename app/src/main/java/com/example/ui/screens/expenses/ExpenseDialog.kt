package com.example.ui.screens.expenses

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ExpenseEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.AmberImportant

private data class ExpenseCategoryOption(val id: String, @StringRes val labelRes: Int)

private val expenseCategoryOptions = listOf(
    ExpenseCategoryOption("food", R.string.category_food),
    ExpenseCategoryOption("transport", R.string.category_transport),
    ExpenseCategoryOption("shopping", R.string.category_shopping),
    ExpenseCategoryOption("bills", R.string.category_bills),
    ExpenseCategoryOption("entertainment", R.string.category_entertainment),
    ExpenseCategoryOption("health", R.string.category_health),
    ExpenseCategoryOption("tech", R.string.category_tech),
    ExpenseCategoryOption("general", R.string.category_general)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseDialog(
    expenseToEdit: ExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amountStr by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(expenseToEdit?.type ?: TransactionType.EXPENSE) }
    var category by remember {
        mutableStateOf(
            expenseToEdit?.category?.lowercase()?.let { cat ->
                when (cat) {
                    "food", "yemek" -> "food"
                    "transport", "ulaşım", "ulasim" -> "transport"
                    "shopping", "alışveriş", "alisveris" -> "shopping"
                    "bills", "faturalar", "fatura" -> "bills"
                    "entertainment", "eğlence", "eglence" -> "entertainment"
                    "health", "sağlık", "saglik" -> "health"
                    "tech", "teknoloji" -> "tech"
                    "general", "genel" -> "general"
                    else -> cat
                }
            } ?: "food"
        )
    }
    var note by remember { mutableStateOf(expenseToEdit?.note ?: "") }
    var isImpulseWishlist by remember { mutableStateOf(expenseToEdit?.isImpulseWishlist ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("expense_dialog"),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (expenseToEdit == null) stringResource(R.string.expenses_dialog_add_title) else stringResource(R.string.expenses_dialog_edit_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector (Gider / Gelir)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = {
                                type = TransactionType.EXPENSE
                            },
                            label = { Text("💸 ${stringResource(TransactionType.EXPENSE.stringRes)}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = {
                                type = TransactionType.INCOME
                                isImpulseWishlist = false
                            },
                            label = { Text("💰 ${stringResource(TransactionType.INCOME.stringRes)}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.expenses_dialog_title_label)) },
                        placeholder = { Text(stringResource(R.string.expenses_dialog_title_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(stringResource(R.string.expenses_dialog_amount_label)) },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )
                }

                // Category Chips
                item {
                    Text(
                        text = stringResource(R.string.expenses_dialog_category_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        expenseCategoryOptions.forEach { opt ->
                            FilterChip(
                                selected = category == opt.id,
                                onClick = { category = opt.id },
                                label = { Text(stringResource(opt.labelRes), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // ADHD Impulse Buying Wishlist Protection (Only for expenses)
                if (type == TransactionType.EXPENSE) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isImpulseWishlist,
                                        onCheckedChange = { isImpulseWishlist = it },
                                        colors = CheckboxDefaults.colors(checkedColor = AmberImportant),
                                        modifier = Modifier.testTag("impulse_checkbox")
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(
                                            text = stringResource(R.string.expenses_dialog_impulse_toggle),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AmberImportant
                                        )
                                        Text(
                                            text = stringResource(R.string.expenses_dialog_impulse_info),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.expenses_dialog_note_label)) },
                        placeholder = { Text(stringResource(R.string.expenses_dialog_note_hint)) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val amountVal = amountStr.replace(",", ".").toDoubleOrNull()
            Button(
                onClick = {
                    if (title.isNotBlank() && amountVal != null && amountVal > 0) {
                        val expense = ExpenseEntity(
                            id = expenseToEdit?.id ?: 0L,
                            title = title.trim(),
                            amount = amountVal,
                            type = type,
                            category = category,
                            note = note.trim(),
                            isImpulseWishlist = isImpulseWishlist,
                            coolingHours = 48,
                            impulseAddedAt = expenseToEdit?.impulseAddedAt ?: System.currentTimeMillis(),
                            date = expenseToEdit?.date ?: System.currentTimeMillis()
                        )
                        onSave(expense)
                    }
                },
                enabled = title.isNotBlank() && amountStr.replace(",", ".").toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_expense_btn")
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

