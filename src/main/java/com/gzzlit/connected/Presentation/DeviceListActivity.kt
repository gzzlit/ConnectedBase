package com.gzzlit.connected.Presentation

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.gzzlit.connected.R
import kotlinx.android.synthetic.main.device_list_container.*
import java.lang.ref.WeakReference

class DeviceListActivity: AppCompatActivity() {

    companion object{
        var closure: ((DeviceListMaker) -> Unit)? = null
        var self:WeakReference<DeviceListActivity>? = null
    }

    private var deviceListMaker: DeviceListMaker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_list_container)
        self = WeakReference(this)
        if (supportActionBar!=null){
            rlActionBar.visibility = View.GONE
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_back_gray)
            supportActionBar?.title = "设备列表"
        }else{
            for (compoundDrawable in tvBack.compoundDrawables) {
                if (compoundDrawable != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        compoundDrawable.setTint(resources.getColor(R.color.BAR_TINT))
                    } else {
                        compoundDrawable.setColorFilter(resources.getColor(R.color.BAR_TINT), PorterDuff.Mode.SRC_ATOP)
                    }
                }
            }
        }
        val tag = DeviceListMaker::class.simpleName
        deviceListMaker = supportFragmentManager.findFragmentByTag(tag) as? DeviceListMaker ?:DeviceListMaker()
        closure?.invoke(deviceListMaker!!)
        if (savedInstanceState == null) // 开启一个事务，通过调用beginTransaction方法开启
            supportFragmentManager.beginTransaction().add(R.id.container, deviceListMaker!!, tag).commit() //使用android.R.id.content有bug
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
            R.id.refresh -> deviceListMaker?.refresh()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.refresh, menu);
        return super.onCreateOptionsMenu(menu)
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.tvBack -> finish()
            R.id.tvRefresh -> deviceListMaker?.refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closure = null
        self = null
        deviceListMaker = null
    }
}