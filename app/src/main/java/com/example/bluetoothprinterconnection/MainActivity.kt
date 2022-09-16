package com.example.bluetoothprinterconnection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var device: Set<BluetoothDevice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.printButton).setOnClickListener {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            }

            val intent1 = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (!bluetoothAdapter.isEnabled) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        ), 101
                    )
                }
            }
            startActivityForResult(intent1, 101)

            showPairedDevices()
        }
    }

    private fun showPairedDevices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ), 101
            )
        }
        device = bluetoothAdapter.bondedDevices
        val list = ArrayList<String>()
        val list1 = ArrayList<BluetoothDevice>()
        for (devices: BluetoothDevice in device) {
            list.add(devices.name)
            list1.add(devices)
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_expandable_list_item_1,
            list
        )

        findViewById<ListView>(R.id.rv).adapter = adapter
        findViewById<ListView>(R.id.rv).setOnItemClickListener { adapterView, view, i, l ->
            val device = list1[i]
            val address = device.address
            val uuid =
                if (device.uuids != null && device.uuids.isNotEmpty())
                    device.uuids[0].uuid
                else
                    "00001101-0000-1000-8000-00805f9b34fb"
            val intent = Intent(this, ConnectActivity::class.java)
            intent.putExtra("address", address)
            intent.putExtra("uuid", uuid.toString())
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                if (bluetoothAdapter.isEnabled) {
                    Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

