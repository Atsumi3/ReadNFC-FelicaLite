package info.nukoneko.android.readnfcf.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import info.nukoneko.android.readnfcf.R;
import info.nukoneko.android.readnfcf.databinding.ActivityMainBinding;
import info.nukoneko.android.readnfcf.utils.NFCUtils;

public final class MainActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new MainActivityViewModel();
        binding.setViewModel(viewModel);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent =
                PendingIntent.getActivity(
                        this, 0,
                        new Intent(this, this.getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
                );
    }

    private void resolveIntent(Intent intent){
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
            final String result = NFCUtils.readNFC(getIntent());
            if (viewModel != null) {
                viewModel.setDisplayText(result);
            }
        } else {
            finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected  void onPause(){
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}
