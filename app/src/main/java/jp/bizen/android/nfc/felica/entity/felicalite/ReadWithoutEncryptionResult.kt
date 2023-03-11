package jp.bizen.android.nfc.felica.entity.felicalite

import jp.bizen.android.nfc.felica.extension.toStr

@Suppress("MemberVisibilityCanBePrivate")
class ReadWithoutEncryptionResult(
    private val result: ByteArray,
    private val requestBlock: List<Block>
) {
    val responseCode = result[1]
    val idm = result.copyOfRange(2, 10)
    val statusFlag1 = result[10]
    val statusFlag2 = result[11]
    val blockNum = result.getOrNull(12)?.toInt() ?: 0
    val blockData: List<ByteArray>
        get() {
            val firstIndex = 13
            val dataSize = 16
            return (0 until blockNum).mapIndexed { index, _ ->
                val offset = dataSize * index
                val nextIndex = firstIndex + offset
                result.copyOfRange(
                    nextIndex,
                    nextIndex + dataSize
                )
            }
        }

    val readableText = blockData.mapIndexed { index, bytes ->
        "${requestBlock[index].name}\t\t${bytes.toStr}"
    }.joinToString("\n")
}
