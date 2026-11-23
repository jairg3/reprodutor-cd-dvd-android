// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2025-2026 DiscMount contributors

package dev.discmount.app

import android.content.Context
import dev.discmount.core.DiscFileSystem
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Materializa a estrutura VIDEO_TS numa pasta privada para que o LibVLC possa abrir o DVD
 * com dvdnav, em vez de receber apenas um VOB isolado. O cache é deliberadamente privado
 * ao aplicativo e pode ser recriado quando o disco muda.
 */
object DvdCache {
    data class Prepared(val root: File, val copiedBytes: Long, val reused: Boolean)

    fun prepare(context: Context, fs: DiscFileSystem, onProgress: ((copied: Long, total: Long, file: String) -> Unit)? = null): Prepared {
        val videoDir = fs.list("/").firstOrNull {
            it.isDirectory && it.name.equals("VIDEO_TS", ignoreCase = true)
        } ?: throw IOException("VIDEO_TS não encontrada")

        val files = fs.list(videoDir.path)
            .filter { !it.isDirectory }
            .sortedBy { it.name.lowercase() }
        if (files.none { it.name.equals("VIDEO_TS.IFO", ignoreCase = true) }) {
            throw IOException("VIDEO_TS.IFO não encontrada")
        }

        val label = fs.label.ifBlank { "dvd" }
        val signature = buildString {
            append(label)
            append('-')
            append(files.size)
            append('-')
            append(files.sumOf { it.size })
        }.map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")

        val root = File(context.cacheDir, "dvd-nav/$signature")
        val targetVideo = File(root, "VIDEO_TS")
        val marker = File(root, ".ready")
        if (marker.isFile && File(targetVideo, "VIDEO_TS.IFO").isFile) {
            return Prepared(root, 0, true)
        }

        if (!root.exists() && !root.mkdirs()) throw IOException("Não foi possível criar o cache do DVD")
        if (!targetVideo.exists() && !targetVideo.mkdirs()) throw IOException("Não foi possível criar VIDEO_TS")

        var copied = 0L
        val total = files.sumOf { it.size }
        var lastProgress = 0L
        for (entry in files) {
            val output = File(targetVideo, entry.name)
            fs.open(entry.path).use { input ->
                FileOutputStream(output).use { out ->
                    val buffer = ByteArray(1024 * 1024)
                    var position = 0L
                    while (position < input.size) {
                        val wanted = minOf(buffer.size.toLong(), input.size - position).toInt()
                        val read = input.read(position, buffer, 0, wanted)
                        if (read <= 0) throw IOException("Falha lendo ${entry.name} em $position")
                        out.write(buffer, 0, read)
                        position += read
                        copied += read
                        if (copied - lastProgress >= 32L * 1024 * 1024 || copied == total) {
                            lastProgress = copied
                            onProgress?.invoke(copied, total, entry.name)
                        }
                    }
                }
            }
        }
        FileOutputStream(marker).use { it.write("ready\n".toByteArray()) }
        return Prepared(root, copied, false)
    }
}
