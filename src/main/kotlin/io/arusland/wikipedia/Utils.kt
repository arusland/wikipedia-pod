package io.arusland.wikipedia

import java.net.URL

fun URL.toFullHost(): String {
    return "$protocol://$host"
}
