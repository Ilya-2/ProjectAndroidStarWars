package com.example.projectandroidstarwars.ui

import androidx.annotation.DrawableRes
import com.example.projectandroidstarwars.R

@DrawableRes
fun characterImageResource(characterId: Int): Int? {
    return when (characterId) {
        1 -> R.drawable.luke
        2 -> R.drawable.c3po
        3 -> R.drawable.r2d2
        4 -> R.drawable.vader
        5 -> R.drawable.leia
        6 -> R.drawable.owen_lars
        7 -> R.drawable.beru_lars
        8 -> R.drawable.r5d4
        9 -> R.drawable.biggs
        10 -> R.drawable.obi_wan
        11 -> R.drawable.anakin
        12 -> R.drawable.tarkin
        13 -> R.drawable.chewbacca
        14 -> R.drawable.han_solo
        15 -> R.drawable.greedo
        20 -> R.drawable.yoda
        21 -> R.drawable.palpatine
        22 -> R.drawable.boba_fett
        25 -> R.drawable.lando
        else -> null
    }
}