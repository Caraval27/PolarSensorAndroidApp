package com.example.bluetoothapp.domain

data class Device(
    val deviceId: String,
    val name: String?,
    val isConnectable: Boolean
)