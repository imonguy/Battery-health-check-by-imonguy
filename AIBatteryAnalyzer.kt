package com.example.batteryapp

class AIBatteryAnalyzer {
    fun calculateTrueHealth(
        designCapacity: Double,
        measuredCapacity: Double,
        averageTemp: Double
    ): Double {
        // Áp dụng hệ số phạt nếu sạc khi máy quá nóng (>38 độ C)
        val tempPenalty = if (averageTemp > 38.0) (averageTemp - 38.0) * 0.5 else 0.0
        
        // AI tinh chỉnh dung lượng thực tế
        val aiAdjustedCapacity = measuredCapacity * 0.98 // Bù trừ hao phí truyền tải
        var trueHealth = (aiAdjustedCapacity / designCapacity) * 100.0 + tempPenalty

        if (trueHealth > 100.0) trueHealth = 100.0
        if (trueHealth < 0.0) trueHealth = 0.0

        return trueHealth
    }
}
