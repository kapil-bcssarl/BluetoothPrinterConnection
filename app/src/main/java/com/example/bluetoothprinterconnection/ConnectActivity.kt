package com.example.bluetoothprinterconnection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import kotlinx.coroutines.*
import java.util.*


class ConnectActivity : AppCompatActivity() {
    companion object {
        lateinit var MY_UUID: String
        var socket: BluetoothSocket? = null
        var isConnected = false
        lateinit var adapter: BluetoothAdapter
        lateinit var address: String
    }

    private var connectSuccess: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContentView(R.layout.activity_connect)
        address = intent.getStringExtra("address").toString()
        MY_UUID = intent.getStringExtra("uuid").toString()
//        ConnectToDevice(this).execute()
        someFun()
//        findViewById<Button>(R.id.button).setOnClickListener {
        sendCommand("on he")
//        }
//        findViewById<Button>(R.id.button2).setOnClickListener {
        disconnect()
//        }
    }

    private fun sendCommand(input: String) {
        if (socket != null) {
            try {
                socket?.outputStream?.write(input.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (socket != null) {
            try {
                socket!!.close()
                socket = null
                isConnected = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        finish()
    }

    fun CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: () -> BluetoothConnection?,
        onPostExecute: (BluetoothConnection?) -> Unit
    ) = launch {
        onPreExecute() // runs in Main Thread
        val result = withContext(Dispatchers.IO) {
            doInBackground() // runs in background thread without blocking the Main Thread
        }
        onPostExecute(result) // runs in Main Thread
    }

    fun someFun() {
        lateinit var printer: EscPosPrinter
        var connection: BluetoothConnection? = null

        GlobalScope.executeAsyncTask(onPreExecute = {

        }, doInBackground = {
            adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(address)
            try {
                if (socket == null || !isConnected) {
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
                    socket =
                        device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
//                    socket!!.connect()
                    Log.i("TAG", "someFun: $socket")
//                    if (socket?.isConnected == true) {
//                        Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
//                    } else
//                        Toast.makeText(applicationContext, "Not Connected", Toast.LENGTH_SHORT).show()

                    connection = BluetoothConnection(device, socket).connect()
//                    Log.i("TAG", "someFun: $device")
                }

                connection
            } catch (e: Exception) {
                connectSuccess = false
                e.printStackTrace()
                try {
                    Log.e("TAG", "trying fallback...")
                    socket = device.javaClass.getMethod(
                        "createRfcommSocket",
                        (Int::class.javaPrimitiveType)
                    ).invoke(device, 1) as BluetoothSocket
//                    socket!!.connect()
                    connection = BluetoothConnection(device, socket).connect()
                    connectSuccess = true
                    Log.i("TAG", "someFun: $socket")

//                    if (socket?.isConnected == true) {
//                        Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
//                    } else
//                        Toast.makeText(applicationContext, "Not Connected", Toast.LENGTH_SHORT).show()

                    Log.i("TAG", "Connected")
                } catch (e2: Exception) {
                    Log.e("TAG", "Couldn't establish Bluetooth connection!")
                }
                connection
            }
        }, onPostExecute = {
            if (!connectSuccess) {
                Log.i("TAG", "onPostExecute: couldn't connect")
            } else {
                isConnected = true
                Log.i("TAG", "onPostExecute: connected")
                printer = EscPosPrinter(connection, 203, 48f, 32)
                printer.printFormattedTextAndCut("[C]Hello World")
            }
        })
    }

    private inner class ConnectToDevice(context: Context) : AsyncTask<Void, Void, String>() {
        private var context: Context

        init {
            this.context = context
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (socket == null || !isConnected) {
                    adapter = BluetoothAdapter.getDefaultAdapter()
                    val device = adapter.getRemoteDevice(address)
                    if (ActivityCompat.checkSelfPermission(
                            context,
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
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    socket!!.connect()
                }
            } catch (e: Exception) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("TAG", "onPostExecute: couldn't connect")
            } else {
                isConnected = true
                Log.i("TAG", "onPostExecute: connected")
            }
        }
    }
}