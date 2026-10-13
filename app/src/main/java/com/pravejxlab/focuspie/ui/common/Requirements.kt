package com.pravejxlab.focuspie.ui.common

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build

fun getRequiredPermissions(): Array<String> {
    val permissions = mutableListOf<String>()

    permissions.addAll(
        elements = listOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    )

    if (Build.VERSION.SDK_INT <= 30) {
        permissions.addAll(
            elements = listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    if (Build.VERSION.SDK_INT >= 31) {
        permissions.addAll(
            elements = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        )
    }

    if (Build.VERSION.SDK_INT >= 33) {
        permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    return permissions.toTypedArray()
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return locationManager.isLocationEnabled
}