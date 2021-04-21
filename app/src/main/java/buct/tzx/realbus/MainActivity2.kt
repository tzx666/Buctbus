package buct.tzx.realbus

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import buct.tzx.buctbus.BuctBus
import buct.tzx.buctbus.annotation.Subscribe
import buct.tzx.realbus.event.ActivityEvent
import buct.tzx.realbus.event.StickyEvent

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var button = findViewById<Button>(R.id.btn)
        button.setOnClickListener {
            BuctBus.getInstance().post(ActivityEvent("666"))
        }
        BuctBus.getInstance().register(this)
    }

    @Subscribe(isSticky = true)
    fun handleEvent(event:StickyEvent) {
        Toast.makeText(this, "粘性事件处理"+event.str, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        BuctBus.getInstance().unregister(this)
    }
}