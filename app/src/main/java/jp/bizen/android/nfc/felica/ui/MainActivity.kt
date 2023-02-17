package jp.bizen.android.nfc.felica.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import jp.bizen.android.nfc.felica.entity.felicalite.FelicaLiteCommand

class MainActivity : Activity(), ReaderCallback {
    private val nfcAdapter: NfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }

    private lateinit var resultArea: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                text = "Please tap your FeliCa card."
                gravity = Gravity.END
                typeface = android.graphics.Typeface.SERIF
                setPadding(16, 16, 16, 16)
            }.apply {
                resultArea = this
            }
        )
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onTagDiscovered(tag: Tag) {
        val techF = NfcF.get(tag) ?: kotlin.run {
            val techList = tag.techList.joinToString("\n")
            runOnUiThread {
                resultArea.text = """
                    Error: Unsupported Tag type.
                    -----------
                    $techList
                """.trimIndent()
            }
            return
        }
        runOnUiThread {
            resultArea.text = FelicaLiteCommand.dumpData(techF)
        }
    }
}
