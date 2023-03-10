package ir.morteza_aghighi.chargingalert.model

class BatteryInfoModel {
    companion object {
        private var batLevel = 0
        private var batHealth = "Good"
        private var batPercentage = "0%"
        private var batVoltage = "0V"
        private var batType = "NaN"
        private var batChargingType = "AC"
        private var batTemp = "0°"
    }

    fun setBatHealth(batHealth: String) {
        Companion.batHealth = batHealth
    }

    fun getBatHealth(): String = batHealth

    fun setBatPercentage(batPercentage: String) {
        Companion.batPercentage = batPercentage
    }

    fun getBatPercentage(): String = batPercentage

    fun setBatVoltage(batVoltage: String) {
        Companion.batVoltage = batVoltage
    }

    fun getBatVoltage(): String = batVoltage

    fun setBatType(batType: String) {
        Companion.batType = batType
    }

    fun getBatType(): String = batType

    fun setBatChargingType(batChargingType: String) {
        Companion.batChargingType = batChargingType
    }

    fun getBatChargingType(): String = batChargingType

    fun setBatTemp(batTemp: String) {
        Companion.batTemp = batTemp
    }

    fun getBatTemp(): String = batTemp

    fun setBatLevel(batLevel: Int) {
        Companion.batLevel = batLevel
    }

    fun getBatLevel(): Int = batLevel
}