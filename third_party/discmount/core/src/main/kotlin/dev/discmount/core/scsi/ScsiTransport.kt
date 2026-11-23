// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2025-2026 DiscMount contributors

package dev.discmount.core.scsi

import java.io.IOException

enum class DataDirection { NONE, IN, OUT }

/** Resultado de un comando SCSI: [goodStatus] false = CHECK CONDITION (usar REQUEST SENSE). */
class ScsiResult(val goodStatus: Boolean, val transferred: Int)

/** Ejecuta CDBs SCSI/MMC. Es el punto de enganche para fases posteriores (autenticación CSS, AACS). */
interface ScsiTransport {
    fun execute(cdb: ByteArray, direction: DataDirection, buf: ByteArray?, off: Int, len: Int, timeoutMs: Int = 10_000): ScsiResult
}

class UsbProtocolException(msg: String) : IOException(msg)

/** Protocolo USB Mass Storage Bulk-Only Transport (BOT). */
class BotTransport(
    private val usb: UsbTransport,
    private val lun: Int = 0,
    private val padCommandsTo12Bytes: Boolean = false,
) : ScsiTransport {
    private var nextTag = 1

    @Synchronized
    override fun execute(cdb: ByteArray, direction: DataDirection, buf: ByteArray?, off: Int, len: Int, timeoutMs: Int): ScsiResult {
        require(cdb.size in 1..16)
        require(direction == DataDirection.NONE || (buf != null && len > 0))
        val tag = nextTag++

        val cbw = ByteArray(31)
        putLE32(cbw, 0, 0x43425355)              // "USBC"
        putLE32(cbw, 4, tag)
        putLE32(cbw, 8, if (direction == DataDirection.NONE) 0 else len)
        cbw[12] = if (direction == DataDirection.IN) 0x80.toByte() else 0
        cbw[13] = lun.toByte()
        // SFF-8020i/ATAPI uses 12-byte packets for shorter SCSI commands.
        cbw[14] = (if (padCommandsTo12Bytes) maxOf(cdb.size, 12) else cdb.size).toByte()
        System.arraycopy(cdb, 0, cbw, 15, cdb.size)

        if (usb.bulkOut(cbw, 0, cbw.size, timeoutMs) != cbw.size) {
            recover()
            throw protocolError(cdb, "falha ao enviar o comando")
        }

        var transferred = 0
        var dataStall = false
        when (direction) {
            DataDirection.IN -> {
                val n = usb.bulkIn(buf!!, off, len, timeoutMs)
                if (n < 0) { usb.clearHalt(true); dataStall = true } else transferred = n
            }
            DataDirection.OUT -> {
                val n = usb.bulkOut(buf!!, off, len, timeoutMs)
                if (n < 0) { usb.clearHalt(false); dataStall = true } else transferred = n
            }
            DataDirection.NONE -> {}
        }

        val csw = ByteArray(CSW_SIZE)
        // Some bridges omit the empty data packet and send CSW during data-in.
        if (direction == DataDirection.IN && transferred == CSW_SIZE && len > CSW_SIZE &&
            getLE32(buf!!, off) == CSW_SIGNATURE && getLE32(buf, off + 4) == tag
        ) {
            buf.copyInto(csw, 0, off, off + CSW_SIZE)
            transferred = 0
        } else {
            var got = usb.bulkIn(csw, 0, CSW_SIZE, timeoutMs)
            if (got == 0) got = usb.bulkIn(csw, 0, CSW_SIZE, timeoutMs)
            if (got < 0) {
                usb.clearHalt(true)
                got = usb.bulkIn(csw, 0, CSW_SIZE, timeoutMs)
            }
            if (got != CSW_SIZE) {
                recover()
                throw protocolError(cdb, if (got < 0) "sem resposta de status USB (CSW)" else "status USB incompleto (CSW: $got/$CSW_SIZE bytes)")
            }
        }
        if (getLE32(csw, 0) != CSW_SIGNATURE || getLE32(csw, 4) != tag) {
            recover()
            throw protocolError(cdb, "status USB inválido (assinatura ou tag do CSW)")
        }
        return when (csw[12].toInt()) {
            0 -> ScsiResult(!dataStall || direction == DataDirection.NONE, transferred)
            1 -> ScsiResult(false, transferred)
            else -> { recover(); throw protocolError(cdb, "erro de fase BOT") }
        }
    }

    private fun protocolError(cdb: ByteArray, reason: String) =
        UsbProtocolException("SCSI 0x%02X: %s".format(cdb[0], reason))

    private fun recover() {
        try {
            usb.massStorageReset()
            usb.clearHalt(true)
            usb.clearHalt(false)
        } catch (_: Exception) { /* mejor esfuerzo */ }
    }

    private fun putLE32(b: ByteArray, o: Int, v: Int) {
        b[o] = v.toByte(); b[o + 1] = (v shr 8).toByte(); b[o + 2] = (v shr 16).toByte(); b[o + 3] = (v shr 24).toByte()
    }

    private fun getLE32(b: ByteArray, o: Int) =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)

    private companion object {
        const val CSW_SIZE = 13
        const val CSW_SIGNATURE = 0x53425355
    }
}
