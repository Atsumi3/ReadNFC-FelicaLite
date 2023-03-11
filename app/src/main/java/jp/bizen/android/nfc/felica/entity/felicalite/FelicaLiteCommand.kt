package jp.bizen.android.nfc.felica.entity.felicalite

import android.nfc.tech.NfcF
import jp.bizen.android.nfc.felica.extension.toStr
import java.io.IOException

object FelicaLiteCommand {
    // [CommandCode, SystemCode, SystemCode, RequestCode, TimeSlot]
    private fun polling(
        nfcF: NfcF,
        systemCode: ByteArray,
    ): PollingResult {
        val command = mutableListOf(
            0x00, systemCode[0], systemCode[1], 0x00, 0x01
        ).apply {
            add(0, (size + 1).toByte())
        }.toByteArray()
        return PollingResult(
            result = nfcF.transceive(command)
        )
    }

    private fun readWithoutEncryption(
        nfcF: NfcF,
        IDm: ByteArray,
        blockList: List<Block>
    ): ReadWithoutEncryptionResult {
        val commend = mutableListOf(
            0x06, // read
            IDm[0],
            IDm[1],
            IDm[2],
            IDm[3],
            IDm[4],
            IDm[5],
            IDm[6],
            IDm[7],
            0x01,
            0x0b,
            0x0,
        ).apply {
            // add BlockNum
            add(blockList.size.toByte())
            // add Block
            blockList.forEach { block ->
                add(0x80.toByte())
                add(block.blockNum.toByte())
            }
            add(0, (size + 1).toByte())
        }.toByteArray()
        return ReadWithoutEncryptionResult(
            result = nfcF.transceive(commend),
            requestBlock = blockList
        )
    }

    fun dumpData(nfcF: NfcF): String {
        return try {
            nfcF.connect()
            val result = if (nfcF.isConnected) {
                val systemCode = nfcF.systemCode
                val result = polling(
                    nfcF,
                    systemCode
                )
                val idm = result.iDm
                if (result.responseCode == 0x01.toByte()) {
                    listOf(
                        "SYSTEM_CODE ${systemCode.toStr}",
                        "----------------------------",
                        "IDm \t- ${result.iDm.toStr}",
                        "PMm \t- ${result.pMm.toStr}",
                        "----------------------------",
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.S_PAD0,
                                Block.S_PAD1,
                                Block.S_PAD2,
                                Block.S_PAD3,
                            )
                        ).readableText,
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.S_PAD4,
                                Block.S_PAD5,
                                Block.S_PAD6,
                                Block.S_PAD7,
                            )
                        ).readableText,
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.S_PAD8,
                                Block.S_PAD9,
                                Block.S_PAD10,
                                Block.S_PAD11,
                            )
                        ).readableText,
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.S_PAD12,
                                Block.S_PAD13,
                                Block.REG,
                                Block.RC,
                            )
                        ).readableText,
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.MAC,
                                Block.ID,
                                Block.D_ID,
                                Block.SER_C,
                            )
                        ).readableText,
                        readWithoutEncryption(
                            nfcF = nfcF, IDm = idm, blockList = listOf(
                                Block.SYS_C,
                                Block.CKV,
                                Block.CK,
                                Block.MC,
                            )
                        ).readableText
                    ).joinToString("\n")
                } else {
                    "Error: Read Failed"
                }
            } else {
                "Error: Disconnected"
            }
            nfcF.close()
            result
        } catch (e: IOException) {
            e.printStackTrace()
            "Error: ${e.localizedMessage}"
        }
    }
}
