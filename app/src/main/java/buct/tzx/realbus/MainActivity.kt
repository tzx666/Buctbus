package buct.tzx.realbus

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import buct.tzx.buctbus.BuctBus
import buct.tzx.buctbus.ThreadMode
import buct.tzx.buctbus.annotation.Subscribe
import buct.tzx.realbus.event.BaseEvent
import buct.tzx.realbus.event.BaseEvent1

class MainActivity : AppCompatActivity(), View.OnClickListener {
    // 事件注册
    private lateinit var register:Button
    // 事件投放测试
    private lateinit var subscribe:Button
    // 主线程事件投放测试
    private lateinit var subscribemain:Button
    // 异步线程投放测试
    private lateinit var subscribeasync:Button
    // 跨activity线程投放测试
    private lateinit var subscribeotheractivity:Button
    // 粘性事件投放测试
    private lateinit var subscribesticky:Button
    // 打印所有订阅事件
    private lateinit var subscriblist:Button
    // 跨模块事件投放测试
    private lateinit var subscribeothermodule:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        register = findViewById<Button>(R.id.register)
        subscribe = findViewById<Button>(R.id.subscribe)
        subscribemain = findViewById(R.id.subscribemain)
        subscribeasync = findViewById(R.id.subscribeasync)
        subscribeotheractivity = findViewById(R.id.subscribeotheractivity)
        subscribesticky = findViewById(R.id.subscribesticky)
        subscriblist = findViewById(R.id.subscriblist)
        subscribeothermodule = findViewById(R.id.subscribeothermodule)
    }

    @Subscribe
    fun handleEvnet(event: BaseEvent) {
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }

    @Subscribe
    fun handleEvnet(event: BaseEvent1) {
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleMainEvent(event: BaseEvent){

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleBackgroundEvent(event: BaseEvent){

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleasyncEvent(event: BaseEvent){

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handlestickyEvent(event: BaseEvent){

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleactivityEvent(event: BaseEvent){

    }
    fun handlelistEvent(event: BaseEvent){

    }
    fun handlemoduleEvent(event: BaseEvent){

    }
    override fun onDestroy() {
        super.onDestroy()
        BuctBus.getInstance().unregister(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.register ->  BuctBus.getInstance().register(this)
            R.id.subscribe -> {
                val eventTest = EventTest();
                eventTest.postEvent()
                eventTest.unregister()
            }
        }
    }
}