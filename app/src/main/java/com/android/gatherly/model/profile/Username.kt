package com.android.gatherly.model.profile

object Username {
    fun normalize(str: String): String{
        return str.trim().lowercase()
    }

    private val allowedPattern = Regex("^[a-z0-9._-]{3,20}$")

    fun isValid(str: String): Boolean{
        return allowedPattern.matches(normalize(str))
    }
}