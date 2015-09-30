package com.daveenguyen.magicearsremote;

import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String DV_TAG = "dvMain";
    private ConsumerIrManager mIrManager;
    private EditText mEditTextCode;
    private SwitchCompat mSwitchCalcCrc;
    private SwitchCompat mSwitchRandColor;
    private boolean mCanTransmitEarCode = false;
    private boolean mIsUsingNewApi = true;
    private boolean mCalcCrc;
    private boolean mRandColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, String.format("Rand: %b Crc: %b", mRandColor, mCalcCrc), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mEditTextCode = (EditText) findViewById(R.id.editTextCode);
        mSwitchCalcCrc = (SwitchCompat) findViewById(R.id.swCalcCrc);
        mSwitchRandColor = (SwitchCompat) findViewById(R.id.swRandColor);

        mCalcCrc = mSwitchCalcCrc.isChecked();
        mRandColor = mSwitchRandColor.isChecked();

        mSwitchRandColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRandColor = mSwitchRandColor.isChecked();

                mEditTextCode.setEnabled(!mRandColor);
                mSwitchCalcCrc.setEnabled(!mRandColor);
            }
        });

        mSwitchCalcCrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCalcCrc = mSwitchCalcCrc.isChecked();
            }
        });

        mIrManager = (ConsumerIrManager) this.getSystemService(CONSUMER_IR_SERVICE);

        if (mIrManager.hasIrEmitter()) {

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                // On KitKat, check if we're using 4.4.3+ API due
                int lastIdx = Build.VERSION.RELEASE.lastIndexOf(".");
                int VERSION_MR = Integer.valueOf(Build.VERSION.RELEASE.substring(lastIdx + 1));
                mIsUsingNewApi = (VERSION_MR >= 3);
            }

            ConsumerIrManager.CarrierFrequencyRange[] freqs = mIrManager.getCarrierFrequencies();
            if (freqs != null) {
                for (ConsumerIrManager.CarrierFrequencyRange freq : freqs) {
                    int earCarrierFreq = 38005; // TODO: Get from EarCode
                    mCanTransmitEarCode = ((earCarrierFreq >= freq.getMinFrequency()) && (earCarrierFreq <= freq.getMaxFrequency()));
                }
            }
        } else {
            Log.e(DV_TAG, "Cannot find IR Emitter on the device");
            fab.hide();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
