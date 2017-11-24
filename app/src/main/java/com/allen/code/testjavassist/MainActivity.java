package com.allen.code.testjavassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
        findViewById(R.id.ok2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.oktext:
                OkBus.getInstance().onEvent(1);
                break;


            case R.id.ok2:
                OkBus.getInstance().onEvent(2);
                break;
        }

    }

    @Bus(1)
    public void busEvent() {
        startActivity(new Intent(this, OtherActivity.class));
    }



    @Bus(2)
    public void busEvent2() {
        Toast.makeText(this, getEvent2Message(), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private String getEvent2Message() {





        return "event2";
    }


}
