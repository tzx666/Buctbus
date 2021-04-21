package buct.tzx.realbus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import buct.tzx.buctbus.BuctBus
import buct.tzx.buctbus.ThreadMode
import buct.tzx.buctbus.annotation.Subscribe
import buct.tzx.realbus.event.*

class MainActivity : AppCompatActivity() {
    // 事件注册
    private lateinit var register:Button
    // 事件投放测试
    private lateinit var subscribe:Button
    // 主线程事件投放测试
    private lateinit var subscribemain:Button
    // 异步线程投放测试
    private lateinit var subscribeasync:Button
    // background线程投放测试
    private lateinit var subscribeback:Button
    // 跨activity线程投放测试
    private lateinit var subscribeotheractivity:Button
    // 粘性事件投放测试
    private lateinit var subscribesticky:Button
    // 打印所有订阅事件
    private lateinit var subscriblist:Button
    // 跨模块事件投放测试
    private lateinit var subscribeothermodule:Button
    private var test = EventTest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 对于其他的activity，应该做的做法是：在oncreate 进行注册，在ondestory进行解注册
        register = findViewById<Button>(R.id.register)
        register.setOnClickListener {
            BuctBus.getInstance().register(this)
        }
        subscribe = findViewById<Button>(R.id.subscribe)
        subscribe.setOnClickListener {
            test.postEvent()
        }
        subscribemain = findViewById(R.id.subscribemain)
        subscribemain.setOnClickListener {
            test.postMainEvent()
        }
        subscribeasync = findViewById(R.id.subscribeasync)
        subscribeasync.setOnClickListener {
            test.postAsyncEvent()
        }
        subscribeback = findViewById(R.id.subscribeback)
        subscribeback.setOnClickListener {
            test.postBackEvent()
        }
        subscribeotheractivity = findViewById(R.id.subscribeotheractivity)
        subscribeotheractivity.setOnClickListener {
            startActivity(Intent(this,MainActivity2::class.java))
        }
        subscribesticky = findViewById(R.id.subscribesticky)
        subscribesticky.setOnClickListener {
            BuctBus.getInstance().postSticky(StickyEvent("666"))
            startActivity(Intent(this,MainActivity2::class.java))
        }
        subscriblist = findViewById(R.id.subscriblist)
        subscriblist.setOnClickListener {
            BuctBus.getInstance()
        }
        subscribeothermodule = findViewById(R.id.subscribeothermodule)
        subscribeothermodule.setOnClickListener {

        }
    }

    @Subscribe
    fun handleEvnet(event: BaseEvent) {
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleMainEvent(event: MainEvent){
        Toast.makeText(this, event.str, Toast.LENGTH_SHORT).show()
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun handleasyncEvent(event: BackgroundEvent){
        runOnUiThread {
            Toast.makeText(this,"background"+ event.str, Toast.LENGTH_SHORT).show()
        }
    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun handlestickyEvent(event: AsyncEvent){
        runOnUiThread {
            Toast.makeText(this,"async"+ event.str, Toast.LENGTH_SHORT).show()
        }
    }
    @Subscribe
    fun handleEvent(event: ActivityEvent){
            Toast.makeText(this,"anotherActivity"+ event.str, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        BuctBus.getInstance().unregister(this)
        test.unregister()
    }
}