package com.emirozturk.brainheal.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirozturk.brainheal.R
import com.emirozturk.brainheal.data.model.ExpenseEntity
import com.emirozturk.brainheal.data.model.TransactionType
import com.emirozturk.brainheal.ui.util.CategoryUtils
import java.util.Locale
import kotlin.math.atan2

data class ExpenseCategorySlice(
    val rawCategory: String,
    val totalAmount: Double,
    val percentage: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Color
)

private val CategoryColorPalette = listOf(
    Color(0xFFFF8A65), // Coral Orange (Food)
    Color(0xFF4FC3F7), // Light Blue (Transport)
    Color(0xFFBA68C8), // Purple Lavender (Shopping)
    Color(0xFFFFD54F), // Amber Gold (Bills)
    Color(0xFFFF6492), // Rose Pink (Entertainment)
    Color(0xFF81C784), // Soft Green (Health)
    Color(0xFF64B5F6), // Cyan Sky (Tech)
    Color(0xFF4DB6AC), // Teal (Work/Study)
    Color(0xFFFFB74D), // Orange Warm
    Color(0xFFA1887F), // Warm Brown
    Color(0xFF90A4AE)  // Blue Grey (General)
)

fun getCategoryColor(category: String, index: Int): Color {
    val norm = category.lowercase(Locale.ROOT).trim()
    return when (norm) {
        "food", "yemek" -> Color(0xFFFF8A65)
        "transport", "ulaşım", "ulasim" -> Color(0xFF4FC3F7)
        "shopping", "alışveriş", "alisveris" -> Color(0xFFBA68C8)
        "bills", "faturalar", "fatura" -> Color(0xFFFFD54F)
        "entertainment", "eğlence", "eglence" -> Color(0xFFFF6492)
        "health", "sağlık", "saglik" -> Color(0xFF81C784)
        "tech", "teknoloji" -> Color(0xFF64B5F6)
        "work", "iş", "is", "study", "ders" -> Color(0xFF4DB6AC)
        "general", "genel" -> Color(0xFF90A4AE)
        else -> CategoryColorPalette[index % CategoryColorPalette.size]
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpensePieChartCard(
    expenses: List<ExpenseEntity>,
    modifier: Modifier = Modifier
) {
    val actualExpensesOnly = remember(expenses) {
        expenses.filter { it.type == TransactionType.EXPENSE && !it.isImpulseWishlist }
    }

    val totalSpent = remember(actualExpensesOnly) {
        actualExpensesOnly.sumOf { it.amount }
    }

    val categorySlices = remember(actualExpensesOnly, totalSpent) {
        if (totalSpent <= 0.0) {
            emptyList()
        } else {
            val grouped = actualExpensesOnly.groupBy { it.category.trim() }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            var currentAngle = -90f
            grouped.mapIndexed { index, (cat, amount) ->
                val percentage = (amount / totalSpent).toFloat()
                val sweep = percentage * 360f
                val slice = ExpenseCategorySlice(
                    rawCategory = cat,
                    totalAmount = amount,
                    percentage = percentage,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    color = getCategoryColor(cat, index)
                )
                currentAngle += sweep
                slice
            }
        }
    }

    var selectedSlice by remember { mutableStateOf<ExpenseCategorySlice?>(null) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categorySlices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("expense_pie_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.expenses_chart_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (categorySlices.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${categorySlices.size} ${stringResource(R.string.expenses_dialog_category_label).trimEnd(':')}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.expenses_chart_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (categorySlices.isEmpty() || totalSpent <= 0.0) {
                // Empty state view
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.expenses_chart_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Donut Chart Graphic & Center Metric
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(190.dp)
                            .pointerInput(categorySlices) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < -90f) angle += 360f

                                    val clicked = categorySlices.firstOrNull { slice ->
                                        val start = slice.startAngle
                                        val end = slice.startAngle + slice.sweepAngle
                                        angle in start..end
                                    }
                                    selectedSlice = if (selectedSlice == clicked) null else clicked
                                }
                            }
                    ) {
                        val strokeWidth = 28.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        categorySlices.forEach { slice ->
                            val isSelected = selectedSlice == null || selectedSlice == slice
                            val sliceAlpha = if (isSelected) 1f else 0.35f
                            val activeStrokeWidth = if (selectedSlice == slice) strokeWidth * 1.18f else strokeWidth

                            drawArc(
                                color = slice.color.copy(alpha = sliceAlpha),
                                startAngle = slice.startAngle,
                                sweepAngle = slice.sweepAngle * animationProgress.value,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = activeStrokeWidth, cap = StrokeCap.Butt)
                            )
                        }
                    }

                    // Donut Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        if (selectedSlice != null) {
                            Text(
                                text = CategoryUtils.getLocalizedCategoryName(selectedSlice!!.rawCategory),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = selectedSlice!!.color,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "₺%.2f", selectedSlice!!.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${(selectedSlice!!.percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.expenses_chart_total_expense),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "₺%.0f", totalSpent),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.expenses_chart_tap_hint),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Legends Grid / FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categorySlices.forEach { slice ->
                        val isSelected = selectedSlice == slice
                        val localizedName = CategoryUtils.getLocalizedCategoryName(slice.rawCategory)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) slice.color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) slice.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSlice = if (selectedSlice == slice) null else slice
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = localizedName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${(slice.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isSelected) slice.color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
