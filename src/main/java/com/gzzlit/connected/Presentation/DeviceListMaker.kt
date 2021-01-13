package com.gzzlit.connected.Presentation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.gzzlit.connected.Command.CommandDependencies
import com.gzzlit.connected.Command.UIPresentedMaker
import com.gzzlit.connected.Entities.ConnectedCompletion
import com.gzzlit.connected.Entities.ConnectedSuccess
import com.gzzlit.connected.Entities.Device
import com.gzzlit.connected.Entities.Success
import com.gzzlit.connected.R
import com.gzzlit.connected.Utils.BLEService
import kotlinx.android.synthetic.main.device_list_view.*
import org.jetbrains.anko.find
import java.lang.ref.WeakReference


class DeviceListMaker : Fragment(),UIPresentedMaker {
    override var completion: ConnectedCompletion? = null
    override var entrySuccess: ConnectedSuccess? = null
    override var dependencies: CommandDependencies? =null

    private var bleService:BLEService? = null
    private var adapter:MyAdapter?  = null

    private var handler:Handler? = null
    private var runnable:Runnable? = null

    private var isRefresh = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.device_list_view, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        bleService = dependencies?.makeBLEService()

        adapter = MyAdapter(context, bleService!!.deviceList)

        val weakSelf = WeakReference(this)
        bleService?.notification = {
            weakSelf.get()?.adapter?.notifyDataSetChanged()
        }
        handler = Handler()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_view.adapter = adapter
        val weakSelf = WeakReference(this)
        list_view.setOnItemClickListener { _, _, position, _ ->
            weakSelf.get()?.bleService?.stopScan()//重要，否则大量扫码和通知动作导致UI卡死
            val device = weakSelf.get()?.bleService?.deviceList!![position]
            weakSelf.get()?.completion?.invoke(Success(device))
        }
        refresh()
    }

    @Synchronized fun refresh(){
        if (isRefresh&&bleService!!.deviceList.size>0) {
            dependencies?.makeDelayHUD("刷新中!")
            return
        }
        bleService?.startScan()
        isRefresh = true
        val weakSelf = WeakReference(this)
        runnable = Runnable {
            weakSelf.get()?.isRefresh = false
        }
        handler?.postDelayed(runnable, 10000)
    }

    override fun onDetach() {
        super.onDetach()
        dependencies?.makeHideHUD()//避免内容泄漏
        bleService?.stopScan()
        bleService?.notification = null
        bleService?.disconnect()
        bleService = null
        completion?.invoke(null)
        completion = null
        entrySuccess = null
        dependencies = null
        handler?.removeCallbacks(runnable)
        handler = null
        runnable = null
        adapter = null
    }
}

class MyAdapter(var context: Context?, var devices: MutableList<Device>): BaseAdapter() {
    override fun getCount(): Int {
        return devices.count()
    }

    override fun getItem(position: Int): Any {
        return devices[position]
    }

    override fun getItemId(position: Int): Long {
        return  position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.device_list_item, parent, false)
        val text1 = view.find<TextView>(R.id.text1)
        val text2 = view.find<TextView>(R.id.text2)
        val text3 = view.find<TextView>(R.id.text3)
        val device = devices[position]
        text1.text = device.rawName
        text2.text = device.mac
        text3.text = "${device.rssi}"
        return view
    }
}