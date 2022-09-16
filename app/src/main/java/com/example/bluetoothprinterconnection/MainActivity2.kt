package com.example.bluetoothprinterconnection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import java.util.*


class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                ),
                101
            )
        }

        val connection = BluetoothPrintersConnections.selectFirstPaired()
        val printer = EscPosPrinter(connection, 203, 48f, 32)
        var device: BluetoothDevice? = null
        val adapter = BluetoothAdapter.getDefaultAdapter()
        var socket: BluetoothSocket? = null

        var pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
        val bluetoothDevice = ArrayList<BluetoothDevice>()
        for (devices: BluetoothDevice in pairedDevices) {
            bluetoothDevice.add(devices)
        }
        var bluetoothAddress: String = ""
        var uuid: String = ""

        if (bluetoothDevice.isNotEmpty()) {
            bluetoothAddress = bluetoothDevice[0].address
            uuid =
                if (bluetoothDevice[0].uuids != null && bluetoothDevice[0].uuids.isNotEmpty())
                    bluetoothDevice[0].uuids[0].uuid.toString()
                else
                    "00001101-0000-1000-8000-00805f9b34fb"
        }

        device = adapter.getRemoteDevice(bluetoothAddress)
//        device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

        if (connection != null) {
            Log.i("TAG", "onCreate: Connected")
        } else {
            try {
                if (socket == null) {
                    Log.e("", "trying fallback...")
                    socket = device?.javaClass?.getMethod(
                        "createRfcommSocket",
                        (Int::class.javaPrimitiveType)
                    )?.invoke(device, 1) as BluetoothSocket
                    socket.connect()
                }

                Log.i("TAG", "Connected")
            } catch (e2: Exception) {
                Log.e("", "Couldn't establish Bluetooth connection!")
                Log.e("TAG", "onCreate: ${e2.message}", )
            }
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            val text = "[C]Hello World!\n"
            printer.printFormattedText(text)
        }
    }
}