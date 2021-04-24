package buct.tzx.second;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import buct.tzx.base.ModuleEvent;
import buct.tzx.buctbus.BuctBus;
import buct.tzx.buctbus.annotation.Subscribe;

public class Main1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Button button = findViewById(R.id.button);
        BuctBus.getInstance().register(this);
        button.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClassName("buct.tzx.realbus","buct.tzx.realbus.MainActivity");
            startActivity(intent);
        });
    }

    @Subscribe(isSticky = true)
    public void handleEvnet(ModuleEvent event){
        Toast.makeText(this,"from module"+event.getStr(),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BuctBus.getInstance().unregister(this);
    }
}