// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2025-2026 DiscMount contributors

package dev.discmount.app

import dev.discmount.core.DiscFile
import dev.discmount.core.scsi.ScsiDrive

/**
 * Expõe o disco óptico como uma imagem seekable para o VLC. As leituras são
 * traduzidas em READ(10) SCSI sob demanda, sem copiar todos os VOBs para o TV.
 */
class DiscImageFile(private val drive: ScsiDrive) : DiscFile {
    private val blockSize = drive.blockSize
    override val size: Long = drive.blockCount * blockSize

    override fun read(pos: Long, dst: ByteArray, off: Int, len: Int): Int {
        if (pos >= size || len <= 0) return -1
        val wanted = minOf(len.toLong(), size - pos).toInt()
        var done = 0
        while (done < wanted) {
            val absolute = pos + done
            val lba = absolute / blockSize
            val inside = (absolute % blockSize).toInt()
            val blocks = minOf(
                drive.maxBlocksPerRead,
                ((inside + (wanted - done) + blockSize - 1) / blockSize),
            )
            val tmp = ByteArray(blocks * blockSize)
            drive.readBlocks(lba, blocks, tmp, 0)
            val count = minOf(wanted - done, tmp.size - inside)
            System.arraycopy(tmp, inside, dst, off + done, count)
            done += count
        }
        return done
    }
}
