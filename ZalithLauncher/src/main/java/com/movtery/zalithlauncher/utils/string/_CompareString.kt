/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.utils.string

import org.apache.maven.artifact.versioning.ComparableVersion

fun naturalCompare(a: String, b: String): Int {
    var i = 0
    var j = 0

    while (i < a.length && j < b.length) {
        val aIsDigit = a[i].isDigit()
        val bIsDigit = b[j].isDigit()

        when {
            aIsDigit && bIsDigit -> {
                val startI = i
                val startJ = j

                while (i < a.length && a[i] == '0') i++
                while (j < b.length && b[j] == '0') j++

                val numStartI = i
                val numStartJ = j

                while (i < a.length && a[i].isDigit()) i++
                while (j < b.length && b[j].isDigit()) j++

                val numLenI = i - numStartI
                val numLenJ = j - numStartJ

                if (numLenI != numLenJ) {
                    return numLenI.compareTo(numLenJ)
                }

                for (k in 0 until numLenI) {
                    val digitI = a[numStartI + k]
                    val digitJ = b[numStartJ + k]
                    if (digitI != digitJ) {
                        return digitI.compareTo(digitJ)
                    }
                }

                val leadingZerosI = numStartI - startI
                val leadingZerosJ = numStartJ - startJ
                if (leadingZerosI != leadingZerosJ) {
                    return leadingZerosI.compareTo(leadingZerosJ)
                }
            }

            !aIsDigit && !bIsDigit -> {
                val cmp = a[i].lowercaseChar().compareTo(b[j].lowercaseChar())
                if (cmp != 0) return cmp

                if (a[i] != b[j]) {
                    return a[i].compareTo(b[j])
                }
                i++
                j++
            }

            else -> {
                return if (aIsDigit) -1 else 1
            }
        }
    }

    return a.length.compareTo(b.length)
}

/**
 * 与另一个版本比较
 */
fun String.compareVersion(otherVer: String): Int {
    return ComparableVersion(this).compareTo(ComparableVersion(otherVer))
}

/**
 * 是否等于另一个版本（版本语义一致即可）
 */
fun String.isVersionEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) == ComparableVersion(otherVer)
}

/**
 * 是否大于等于另一个版本
 */
fun String.isBiggerOrEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) >= ComparableVersion(otherVer)
}

/**
 * 是否大于另一个版本
 */
fun String.isBiggerTo(otherVer: String): Boolean {
    return ComparableVersion(this) > ComparableVersion(otherVer)
}

/**
 * 是否小于等于另一个版本
 */
fun String.isLowerOrEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) <= ComparableVersion(otherVer)
}

/**
 * 是否小于另一个版本
 */
fun String.isLowerTo(otherVer: String): Boolean {
    return ComparableVersion(this) < ComparableVersion(otherVer)
}

/**
 * 是否等于另一个版本
 */
fun String.isEqualTo(otherVer: String): Boolean {
    return ComparableVersion(this) == ComparableVersion(otherVer)
}

/**
 * 是否在某个版本区间之内（闭区间）
 */
fun String.isBetween(min: String, max: String): Boolean {
    val ver = ComparableVersion(this)
    return ver >= ComparableVersion(min) && ver <= ComparableVersion(max)
}

/**
 * 是否是早于某个主版本的大版本变更（比如用于判定是否是 1.13 以前）
 */
fun String.isBeforeMajorVersion(major: Int): Boolean {
    val majorVersion = this.split('.').firstOrNull()?.toIntOrNull() ?: return false
    return majorVersion < major
}