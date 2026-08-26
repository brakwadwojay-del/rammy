package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExpenseCategory(
    val title: String,
    val emoji: String,
    val colorValue: Long
) {
    FOOD("Food", "🍛", 0xFFF97316),
    TRANSPORT("Transport", "🚕", 0xFF0284C7),
    SHOPPING("Shopping", "🛍", 0xFFEC4899),
    DRINKS("Drinks", "☕", 0xFF8B5CF6),
    BILLS("Bills", "💡", 0xFFEAB308),
    OTHER("Other", "✦", 0xFF6B7280);

    val color: Color get() = Color(colorValue)
    val colorHex: Long get() = colorValue

    companion object {
        fun fromString(value: String?): ExpenseCategory {
            if (value.isNullOrBlank()) return OTHER
            return entries.find { 
                it.title.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) 
            } ?: inferFromDescription(value)
        }

        fun inferFromDescription(desc: String): ExpenseCategory {
            val lower = desc.lowercase()
            return when {
                lower.contains("food") || lower.contains("lunch") || lower.contains("dinner") || 
                lower.contains("breakfast") || lower.contains("snack") || lower.contains("eat") || 
                lower.contains("meal") || lower.contains("rice") || lower.contains("waakye") || 
                lower.contains("fufu") || lower.contains("kenkey") || lower.contains("burger") || 
                lower.contains("pizza") || lower.contains("grocer") -> FOOD
                
                lower.contains("transport") || lower.contains("taxi") || lower.contains("uber") || 
                lower.contains("bolt") || lower.contains("trotro") || lower.contains("bus") || 
                lower.contains("fuel") || lower.contains("gas") || lower.contains("fare") -> TRANSPORT
                
                lower.contains("shopping") || lower.contains("clothes") || lower.contains("shoes") || 
                lower.contains("market") || lower.contains("store") || lower.contains("mall") || 
                lower.contains("buy") -> SHOPPING
                
                lower.contains("drink") || lower.contains("coffee") || lower.contains("tea") || 
                lower.contains("water") || lower.contains("juice") || lower.contains("beer") || 
                lower.contains("soda") || lower.contains("cafe") || lower.contains("bar") -> DRINKS
                
                lower.contains("bill") || lower.contains("electricity") || lower.contains("water bill") || 
                lower.contains("rent") || lower.contains("wifi") || lower.contains("internet") || 
                lower.contains("data") || lower.contains("subscription") || lower.contains("airtime") -> BILLS
                
                else -> OTHER
            }
        }
    }
}

@Entity(
    tableName = "expense_items",
    indices = [Index(value = ["date"]), Index(value = ["isDelayed"])]
)
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,          // Format: YYYY-MM-DD
    val amount: Double,        // Expense amount in GH₵
    val description: String,   // e.g. "Food", "Transport", "Lunch", "Drink"
    val category: String = "Other", // Food, Transport, Shopping, Drinks, Bills, Other
    val timeFormatted: String, // e.g. "12:30 PM"
    val timestamp: Long = System.currentTimeMillis(),
    val isDelayed: Boolean = false
) {
    fun getEffectiveCategory(): ExpenseCategory {
        return if (category.isNotBlank() && category != "Other") {
            ExpenseCategory.fromString(category)
        } else {
            ExpenseCategory.inferFromDescription(description)
        }
    }
}
