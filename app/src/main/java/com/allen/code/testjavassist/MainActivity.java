package com.allen.code.testjavassist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.allen.code.testjavassist.event.OkBus;
import com.app.annotation.javassist.Bus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.oktext).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        OkBus.getInstance().onEvent(1);
    }

    @Bus(1)
    public void busEvent() {
        Toast.makeText(this, "event", Toast.LENGTH_SHORT).show();
    }
}
