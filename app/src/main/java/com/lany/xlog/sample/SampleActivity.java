package com.lany.xlog.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.lany.xlog.XLog;

public class SampleActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private String json = "{\"weatherinfo\":{\"city\":\"北京\",\"cityid\":\"101010100\",\"temp\":\"22\",\"WD\":\"北风\",\"WS\":\"2级\",\"SD\":\"19%\",\"WSE\":\"2\",\"time\":\"16:05\",\"isRadar\":\"1\",\"Radar\":\"JC_RADAR_AZ9010_JB\"}}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        XLog.init(TAG)
                .methodCount(3)                 // default 2
                .showThreadInfo()               // default shown
                .methodOffset(2);                // default 0

        XLog.d("hello");
        XLog.e("hello");
        XLog.w("hello");
        XLog.v("hello");
        XLog.wtf("hello");
        XLog.json(json);
    }
}
