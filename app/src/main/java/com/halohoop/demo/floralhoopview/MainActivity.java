package com.halohoop.demo.floralhoopview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.halohoop.floralhoopview.widgets.FloralHoopView;

public class MainActivity extends AppCompatActivity {

    private FloralHoopView mFhv;
    private int mCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFhv = (FloralHoopView) findViewById(R.id.fhv);
    }

    public void addBitmap(View view) {
        int count = ((++mCount) % 8) + 1;
        int mipmap = getResources().getIdentifier("avatar" + count, "mipmap", "com.halohoop.demo.floralhoopview");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mipmap);
        mFhv.addBitmap(bitmap);
        bitmap.recycle();
    }

    public void clearAll(View view) {
        mFhv.clearAll();
    }
}
