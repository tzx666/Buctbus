package buct.tzx.realbus

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import buct.tzx.buctbus.BuctBus
import buct.tzx.buctbus.annotation.Subscribe
import buct.tzx.realbus.event.BaseEvent
import buct.tzx.realbus.event.BaseEvent1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button = findViewById<Button>(R.id.register)
        var button1 = findViewById<Button>(R.id.subscribe)
        button.setOnClickListener {
            BuctBus.getInstance().register(this)
        }
        button1.setOnClickListener {
            var eventTest = EventTest();
            eventTest.postEvent()
            eventTest.unregister()
        }
    }

    @Subscribe
    fun handleEvnet(event: BaseEvent) {
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }

    @Subscribe
    fun handleEvnet(event: BaseEvent1) {
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        BuctBus.getInstance().unregister(this)
    }
}