/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package com.github.shadowsocks.utils

import java.io.File
import java.io.IOException

object TcpFastOpen {
    private const val PATH = "/proc/sys/net/ipv4/tcp_fastopen"

    /**
     * Is kernel version >= 3.7.1.
     */
    val supported by lazy {
        val match = """^(\d+)\.(\d+)\.(\d+)""".toRegex().find(System.getProperty("os.version") ?: "")
        if (match == null) false else when (match.groupValues[1].toInt()) {
            in Int.MIN_VALUE..2 -> false
            3 -> when (match.groupValues[2].toInt()) {
                in Int.MIN_VALUE..6 -> false
                7 -> match.groupValues[3].toInt() >= 1
                else -> true
            }
            else -> true
        }
    }

    val sendEnabled: Boolean get() {
        val file = File(PATH)
        // File.readText doesn't work since this special file will return length 0
        return file.canRead() && file.bufferedReader().use { it.readText() }.trim().toInt() and 1 > 0
    }

    fun enable(): String? {
        return try {
            ProcessBuilder("su", "-c", "echo 3 > $PATH").redirectErrorStream(true).start()
                    .inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.localizedMessage
        }
    }
    fun enableAsync() = thread("TcpFastOpen") { enable() }.join(1000)
}