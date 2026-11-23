// SPDX-License-Identifier: GPL-3.0-or-later
package dev.discmount.core

import dev.discmount.core.scsi.BotTransport
import dev.discmount.core.scsi.DataDirection
import dev.discmount.core.scsi.UsbTransport

fun main() {
    val cdb = byteArrayOf(0x12, 0, 0, 0, 36, 0)
    for (atapi in listOf(false, true)) {
        val usb = ScriptedBot()
        val bot = BotTransport(usb, padCommandsTo12Bytes = atapi)
        usb.input.add(byteArrayOf())
        usb.input.add(usb.status(tag = 1))

        val result = bot.execute(cdb, DataDirection.NONE, null, 0, 0)
        check(result.goodStatus && result.transferred == 0)
        check(usb.command[14].toInt() == if (atapi) 12 else 6)
        check(usb.command.copyOfRange(15, 21).contentEquals(cdb))
        check(usb.command.copyOfRange(21, 27).all { it == 0.toByte() })
        check(usb.input.isEmpty())

        val data = ByteArray(40)
        usb.input.add(usb.status(tag = 2, failed = true))
        val earlyStatus = bot.execute(cdb, DataDirection.IN, data, 4, 36)
        check(!earlyStatus.goodStatus && earlyStatus.transferred == 0)
        check(usb.input.isEmpty())

        // A short payload of 13 bytes belongs to data if its status tag doesn't match.
        usb.input.add(usb.status(tag = 99))
        usb.input.add(usb.status(tag = 3))
        val shortData = bot.execute(cdb, DataDirection.IN, data, 4, 36)
        check(shortData.goodStatus && shortData.transferred == 13)
        check(usb.input.isEmpty())
    }
    println("BOT: ATAPI/transparent command length, zero-length packet and early CSW passed")
}

private class ScriptedBot : UsbTransport {
    var command = ByteArray(0)
    val input = ArrayDeque<ByteArray>()

    override fun bulkOut(src: ByteArray, off: Int, len: Int, timeoutMs: Int): Int {
        command = src.copyOfRange(off, off + len)
        return len
    }

    override fun bulkIn(dst: ByteArray, off: Int, len: Int, timeoutMs: Int): Int {
        val data = input.removeFirst()
        check(data.size <= len)
        data.copyInto(dst, off)
        return data.size
    }

    override fun clearHalt(inEndpoint: Boolean) = error("unexpected endpoint stall")
    override fun massStorageReset() = error("unexpected BOT reset")

    fun status(tag: Int, failed: Boolean = false) =
        byteArrayOf(0x55, 0x53, 0x42, 0x53, tag.toByte(), 0, 0, 0, 0, 0, 0, 0, if (failed) 1 else 0)
}
