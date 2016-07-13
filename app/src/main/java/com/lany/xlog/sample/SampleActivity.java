package com.lany.xlog.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lany.xlog.XLog;

public class SampleActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private String json = "{\"desc\":\"OK\",\"status\":1000,\"data\":{\"wendu\":\"22\",\"ganmao\":\"风较大，较易发生感冒，注意防护。\",\"forecast\":[{\"fengxiang\":\"北风\",\"fengli\":\"5-6级\",\"high\":\"高温 24℃\",\"type\":\"晴\",\"low\":\"低温 11℃\",\"date\":\"3日星期六\"},{\"fengxiang\":\"北风\",\"fengli\":\"4-5级\",\"high\":\"高温 19℃\",\"type\":\"晴\",\"low\":\"低温 8℃\",\"date\":\"4日星期日\"},{\"fengxiang\":\"无持续风向\",\"fengli\":\"微风\",\"high\":\"高温 21℃\",\"type\":\"晴\",\"low\":\"低温 9℃\",\"date\":\"5日星期一\"},{\"fengxiang\":\"无持续风向\",\"fengli\":\"微风\",\"high\":\"高温 21℃\",\"type\":\"多云\",\"low\":\"低温 10℃\",\"date\":\"6日星期二\"},{\"fengxiang\":\"无持续风向\",\"fengli\":\"微风\",\"high\":\"高温 24℃\",\"type\":\"晴\",\"low\":\"低温 12℃\",\"date\":\"7日星期三\"},{\"fengxiang\":\"无持续风向\",\"fengli\":\"微风\",\"high\":\"高温 23℃\",\"type\":\"晴\",\"low\":\"低温 11℃\",\"date\":\"8日星期四\"}],\"yesterday\":{\"fl\":\"微风\",\"fx\":\"无持续风向\",\"high\":\"高温 23℃\",\"type\":\"晴\",\"low\":\"低温 12℃\",\"date\":\"2日星期五\"},\"aqi\":\"59\",\"city\":\"北京\"}}";
    private String XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!--  Copyright w3school.com.cn --><note><to>George</to><from>John</from><heading>Reminder</heading><body>Don't forget the meeting!</body></note>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        XLog.init(true);
        //XLog.init(TAG);
        //XLog.setSettings(true, 3, 2);
        XLog.d("hello1");
        XLog.e("hello2");
        XLog.w("hello3");
        XLog.v("hello4");
        XLog.json(json);
        XLog.xml("Hello",XML);
    }
}
