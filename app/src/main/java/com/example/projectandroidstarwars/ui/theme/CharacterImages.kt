package com.example.projectandroidstarwars.ui

import androidx.annotation.DrawableRes
import com.example.projectandroidstarwars.R

@DrawableRes
fun characterImageResource(characterId: Int): Int = when (characterId) {
    1 -> R.drawable.luke
    2 -> R.drawable.c3po
    3 -> R.drawable.r2d2
    4 -> R.drawable.vader
    5 -> R.drawable.leia
    10 -> R.drawable.obi_wan
    13 -> R.drawable.chewbacca
    14 -> R.drawable.han_solo
    20 -> R.drawable.yoda
    21 -> R.drawable.palpatine
    22 -> R.drawable.boba_fett
    25 -> R.drawable.lando
    else -> R.mipmap.ic_launcher
}
