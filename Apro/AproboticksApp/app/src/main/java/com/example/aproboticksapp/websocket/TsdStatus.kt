package com.example.aproboticksapp.websocket

import androidx.lifecycle.MutableLiveData

class TsdStatus {
    val isRequestToTurnOffScannerFromComputer = false
    val action:MutableLiveData<String?> = MutableLiveData(null)
}