package jp.bizen.android.nfc.felica.entity.felicalite

@Suppress("MemberVisibilityCanBePrivate")
class PollingResult(
    result: ByteArray,
) {
    val responseCode = result[1]
    val iDm = result.copyOfRange(2, 10)
    val pMm = result.copyOfRange(10, 18)
}
