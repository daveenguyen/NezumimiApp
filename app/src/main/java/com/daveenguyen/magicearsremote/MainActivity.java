package com.daveenguyen.magicearsremote;

import com.daveenguyen.magicears.EarCode;

import android.graphics.Color;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String DV_TAG = "dvMain";
    private final String[] color_table = {
            "#F9F1FF",
            "#00BFFF",
            "#1F4DFE",
            "#5740FF",
            "#0000FE",
            "#FFD8FF",
            "#D19DFF",
            "#A988FF",
            "#B261D5",
            "#FFC1FF",
            "#FF8BFF",
            "#FF7AFF",
            "#FF73E0",
            "#FF009B",
            "#FF4062",
            "#FFAD50",
            "#FF560A",
            "#FF7701",
            "#FFC600",
            "#FF7340",
            "#FF5900",
            "#FF0000",
            "#00E0FF",
            "#72FED1",
            "#00FE97",
            "#00F700",
            "#00ED87",
            "#FFEBF3",
            "#FFE4EF",
            "#000000"
    };
    private ConsumerIrManager mIrManager;
    private EditText mEditTextCode;
    private SwitchCompat mSwitchCalcCrc;
    private SwitchCompat mSwitchRandColor;
    private ImageView mImageLeft;
    private ImageView mImageRight;
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
                if (mCanTransmitEarCode) {
                    String hexCode;
                    EarCode earCode;

                    if (mRandColor) {
                        Random rand = new Random();
                        int leftColor = rand.nextInt(0x1E);
                        int rightColor = rand.nextInt(0x1E);
                        hexCode = String.format("9B 26 0E %02X 0E %02X D1 42 F7 20 D0 32 F0", leftColor, rightColor + 0x80);
                        mEditTextCode.setText(hexCode);
                        mImageLeft.setColorFilter(Color.parseColor(color_table[leftColor]));
                        mImageRight.setColorFilter(Color.parseColor(color_table[rightColor]));

                        earCode = new EarCode(hexCode, true, mIsUsingNewApi);
                    } else {
                        hexCode = mEditTextCode.getText().toString().trim();
                        if (hexCode.isEmpty()) {
                            return;
                        }
                        earCode = new EarCode(hexCode, mCalcCrc, mIsUsingNewApi);

                        mImageLeft.setColorFilter(Color.WHITE);
                        mImageRight.setColorFilter(Color.WHITE);
                    }

                    mIrManager.transmit(earCode.getCarrierFrequency(), earCode.getPattern());
                }
            }
        });

        mEditTextCode = (EditText) findViewById(R.id.editTextCode);
        mSwitchCalcCrc = (SwitchCompat) findViewById(R.id.swCalcCrc);
        mSwitchRandColor = (SwitchCompat) findViewById(R.id.swRandColor);
        mImageLeft = (ImageView) findViewById(R.id.imageLeft);
        mImageRight = (ImageView) findViewById(R.id.imageRight);

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
