package com.daveenguyen.nezumimi;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.daveenguyen.mahoucode.MahouCode;

import android.graphics.Color;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {
    private static final String DV_TAG = "dvMain";
    private final int[] color_table = {
            Color.parseColor("#F9F1FF"),
            Color.parseColor("#00BFFF"),
            Color.parseColor("#1F4DFE"),
            Color.parseColor("#5740FF"),
            Color.parseColor("#0000FE"),
            Color.parseColor("#FFD8FF"),
            Color.parseColor("#D19DFF"),
            Color.parseColor("#A988FF"),
            Color.parseColor("#B261D5"),
            Color.parseColor("#FFC1FF"),
            Color.parseColor("#FF8BFF"),
            Color.parseColor("#FF7AFF"),
            Color.parseColor("#FF73E0"),
            Color.parseColor("#FF009B"),
            Color.parseColor("#FF4062"),
            Color.parseColor("#FFAD50"),
            Color.parseColor("#FF560A"),
            Color.parseColor("#FF7701"),
            Color.parseColor("#FFC600"),
            Color.parseColor("#FF7340"),
            Color.parseColor("#FF5900"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#00E0FF"),
            Color.parseColor("#72FED1"),
            Color.parseColor("#00FE97"),
            Color.parseColor("#00F700"),
            Color.parseColor("#00ED87"),
            Color.parseColor("#FFEBF3"),
            Color.parseColor("#FFE4EF"),
            Color.parseColor("#000000")
    };
    private ConsumerIrManager mIrManager;
    private EditText mEditTextCode;
    private SwitchCompat mSwitchRandColor;
    private ImageView mImageLeft;
    private ImageView mImageRight;
    private boolean mCanTransmitCode = false;
    private boolean mIsUsingNewApi = true;
    private boolean mRandColor;
    private boolean mLeftClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mEditTextCode = (EditText) findViewById(R.id.editTextCode);
        mSwitchRandColor = (SwitchCompat) findViewById(R.id.swRandColor);
        mImageLeft = (ImageView) findViewById(R.id.imageLeft);
        mImageRight = (ImageView) findViewById(R.id.imageRight);


        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCanTransmitCode) {
                    String hexCode;
                    MahouCode mCode;

                    if (mRandColor) {
                        Random rand = new Random();
                        int leftColor = rand.nextInt(0x1E);
                        int rightColor = rand.nextInt(0x1E);
                        hexCode = MahouCode.parse9x(String.format("26 0E %02X 0E %02X D1 42 F7 20 D0 32 F0", leftColor, rightColor + 0x80));
                        mEditTextCode.setText(hexCode);

                        mImageLeft.setColorFilter(color_table[leftColor]);
                        mImageRight.setColorFilter(color_table[rightColor]);
                    } else {
                        hexCode = mEditTextCode.getText().toString().trim();
                        if (hexCode.isEmpty()) {
                            return;
                        }

                        mImageLeft.setColorFilter(getResources().getColor(R.color.defaultEars));
                        mImageRight.setColorFilter(getResources().getColor(R.color.defaultEars));
                    }

                    mCode = new MahouCode(hexCode);
                    helperTransmit(mCode);
                }
            }
        });

        mImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEarClick(true, view);
            }
        });

        mImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEarClick(false, view);
            }
        });

        mRandColor = mSwitchRandColor.isChecked();

        mSwitchRandColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRandColor = mSwitchRandColor.isChecked();
                mEditTextCode.setEnabled(!mRandColor);
            }
        });

        mIrManager = (ConsumerIrManager) this.getSystemService(CONSUMER_IR_SERVICE);

        checkInfrared();
    }

    private void checkInfrared() {
        if (mIrManager.hasIrEmitter()) {

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                // On KitKat, check if we're using 4.4.3+ API
                int lastIdx = Build.VERSION.RELEASE.lastIndexOf(".");
                int VERSION_MR = Integer.valueOf(Build.VERSION.RELEASE.substring(lastIdx + 1));
                mIsUsingNewApi = (VERSION_MR >= 3);
            } else {
                mIsUsingNewApi = true;
            }

            ConsumerIrManager.CarrierFrequencyRange[] freqs = mIrManager.getCarrierFrequencies();
            if (freqs != null) {
                for (ConsumerIrManager.CarrierFrequencyRange freq : freqs) {
                    int carrierFreq = MahouCode.getCarrierFrequency();
                    mCanTransmitCode = ((carrierFreq >= freq.getMinFrequency()) && (carrierFreq <= freq.getMaxFrequency()));
                }
            }
        }
    }

    private void helperTransmit(MahouCode code) {
        if (mCanTransmitCode) return;

        if (mIsUsingNewApi) {
            mIrManager.transmit(code.getCarrierFrequency(), code.getPattern());
        } else {
            mIrManager.transmit(code.getCarrierFrequency(), code.getOldApiPattern());
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

    private void onEarClick(boolean isLeft, View view) {
        mLeftClicked = isLeft;

        // Pass AppCompatActivity which implements ColorCallback, along with the title of the dialog
        new ColorChooserDialog.Builder(this, R.string.color_chooser_title)
                .allowUserColorInput(false)
                .customColors(color_table, null)
                .doneButton(R.string.md_done_label)  // changes label of the done button
                .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                .backButton(R.string.md_back_label)  // changes label of the back button
                .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                .show();
    }

    @Override
    public void onColorSelection(ColorChooserDialog colorChooserDialog, int chosenColor) {
        for (int i = 0; i < color_table.length ; i++) {
            if (color_table[i] == chosenColor) {
                mEditTextCode.setText("" + i);
                if (mLeftClicked) {
                    mImageLeft.setColorFilter(color_table[i]);
                } else {
                    mImageRight.setColorFilter(color_table[i]);
                }
            }
        }
    }
}
