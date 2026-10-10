package com.example.projectandroidstarwars.ui.theme

import androidx.annotation.DrawableRes
import com.example.projectandroidstarwars.R

@DrawableRes
fun planetImageResource(planetId: Int): Int? {
    return when (planetId) {
        1 -> R.drawable.planet_tatooine
        2 -> R.drawable.planet_alderan
        3 -> R.drawable.planet_yavin
        4 -> R.drawable.planet_hoth
        5 -> R.drawable.planet_dagobah
        else -> null
    }
}