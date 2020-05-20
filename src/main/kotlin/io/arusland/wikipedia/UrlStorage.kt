package io.arusland.wikipedia

import java.io.File

class UrlStorage(private val file: File) {
    private val lines = mutableSetOf<String>()
    private val lineSeparator = System.getProperty("line.separator")

    fun add(url: String) {
        lines.add(url)
        file.appendText(lineSeparator + url)
    }

    fun contains(url: String): Boolean {
        return lines.contains(url)
    }

    fun load(): UrlStorage {
        lines.clear()

        if (file.exists()) {
            lines.addAll(file.readLines().filter { it.isNotBlank() })
        }

        return this
    }

    fun save(): UrlStorage {
        file.delete()
        file.bufferedWriter().use { bw ->
            lines.forEach { line ->
                bw.write(line)
                bw.newLine()
            }
        }

        return this
    }
}
