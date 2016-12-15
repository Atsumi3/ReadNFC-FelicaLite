package info.nukoneko.android.readnfcf.ui;

import android.databinding.ObservableField;

public final class MainActivityViewModel {
    private ObservableField<String> displayText = new ObservableField<>();

    public ObservableField<String> getDisplayText() {
        return displayText;
    }

    void setDisplayText(String displayText) {
        this.displayText.set(displayText);
    }
}
