package jp.bizen.android.nfc.felica.entity.felicalite

@Suppress("MemberVisibilityCanBePrivate")
class PollingResult(
    result: ByteArray,
) {
    val isSuccess = result[1].toInt() == 0x01
    val iDm = result.copyOfRange(2, 10)
    val pMm = result.copyOfRange(10, 18)
}
