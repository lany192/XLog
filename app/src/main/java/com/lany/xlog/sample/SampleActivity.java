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
        XLog.initTag(TAG);
        XLog.setSettings(true, 3, 2);
        XLog.d("hello1");
        XLog.e("hello2");
        XLog.w("hello3");
        XLog.v("hello4");
        XLog.wtf("hello5");
        XLog.json(json);
    }
}
