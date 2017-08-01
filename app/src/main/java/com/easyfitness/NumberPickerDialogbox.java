package com.easyfitness;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class NumberPickerDialogbox extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button cancel, ok;
    public Button lbtn_minusOne, lbtn_minusFive, lbtn_plusOne, lbtn_plusFive;
    public ProgressBar progressBar;

    int iRestTime = 60;

    public NumberPickerDialogbox(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Pick number"); //ChronometerLabel
        setContentView(R.layout.dialog_numberpicker);
        this.setCanceledOnTouchOutside(true); // make it not modal

        ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);

        cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(this);

        lbtn_minusOne = (Button) findViewById(R.id.btn_minusOne);
        lbtn_minusOne.setOnClickListener(this);
        lbtn_minusFive = (Button) findViewById(R.id.btn_minusFive);
        lbtn_minusFive.setOnClickListener(this);
        lbtn_plusOne = (Button) findViewById(R.id.btn_plusOne);
        lbtn_plusOne.setOnClickListener(this);
        lbtn_plusFive = (Button) findViewById(R.id.btn_plusFive);
        lbtn_plusFive.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                //dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_minusOne:
                //dismiss();
                break;
            case R.id.btn_plusOne:
                //dismiss();
                break;
            case R.id.btn_minusFive:
                //dismiss();
                break;
            case R.id.btn_plusFive:
                //dismiss();
                break;
            default:
                break;
        }
    }

  /*
  public OnDismissListener onDismissChrono = new OnDismissListener()
  {
	  @Override
	  public void onDismiss(DialogInterface dialog) {
		  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	  }
  };*/


}