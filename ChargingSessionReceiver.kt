package com.example.batteryapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class ChargingSessionReceiver : BroadcastReceiver() {
    private var startPercent = 0
    private var startChargeCounter = 0
    private val aiAnalyzer = AIBatteryAnalyzer()

    override fun onReceive(context: Context, intent: Intent) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                startPercent = getBatteryPercentage(context)
                startChargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                val endPercent = getBatteryPercentage(context)
                val endChargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                val percentAdded = endPercent - startPercent
                
                if (percentAdded >= 20) { // Sạc tối thiểu 20% để triệt tiêu sai số
                    val chargeAdded_mAh = (endChargeCounter - startChargeCounter) / 1000.0
                    val actualCapacity = (chargeAdded_mAh / percentAdded) * 100
                    val designCapacity = getDesignCapacity(context)
                    
                    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
                    
                    val trueHealth = aiAnalyzer.calculateTrueHealth(designCapacity, actualCapacity, temp)
                    
                    // TODO: Trong thực tế, bạn lưu trueHealth vào SharedPreferences để MainActivity đọc và hiển thị
                    Log.d("BatteryAI", "Độ chai thực tế (AI phân tích): $trueHealth %")
                }
            }
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale.toFloat()).toInt()
    }

    private fun getDesignCapacity(context: Context): Double {
        return try {
            val clazz = Class.forName("com.android.internal.os.PowerProfile")
            val profile = clazz.getConstructor(Context::class.java).newInstance(context)
            clazz.getMethod("getBatteryCapacity").invoke(profile) as Double
        } catch (e: Exception) { 5000.0 } // Mặc định 5000mAh nếu không đọc được
    }
}
