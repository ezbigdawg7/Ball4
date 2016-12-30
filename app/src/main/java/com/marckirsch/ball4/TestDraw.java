package com.marckirsch.ball4;

import android.app.Activity;
import android.os.Bundle;


public class TestDraw extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_test_draw);
        setContentView(new SingleTouchEventView(this, null));
        setContentView(new SingleTouchEventView(this,null));


    }

}
