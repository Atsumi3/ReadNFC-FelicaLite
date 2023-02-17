package jp.bizen.android.nfc.felica.extension

import java.util.*

val ByteArray?.toStr: String
    get() = this?.joinToString("") {
        it.toStr
    } ?: ""

val Byte.toStr: String
    get() {
        var str = Integer.toHexString(toInt())
        str = if (str.length == 1) "0$str" else str
        str = str.replace("ff", "")
        if (str == "") str = "FF"
        return "${str.uppercase(Locale.getDefault())} "
    }
