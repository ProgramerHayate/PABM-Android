package com.example.daloga1.aplicacionmovil;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;


import java.util.ArrayList;

import java.util.List;

public class Servicio extends IntentService {

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    private static final String ETIQUETA_LOG = "Dispositivo";

    private long tiempoDeEspera = 10000;

    private boolean seguir = true;
    private String dispositivoEscuchando;
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public Servicio() {
        super("HelloIntentService");
        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.constructor: termina");
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    // Detiene el servicio en segundo plano
    // -> parar() ->
    public void parar() {
        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.parar() ");
        if (this.seguir == false) {
            return;
        }
        this.seguir = false;
        this.stopSelf();
        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.parar() : acaba ");
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    public void onDestroy() {

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onDestroy() ");
        this.detenerBusquedaDispositivosBTLE();


        this.parar(); // posiblemente no haga falta, si stopService() ya se carga el servicio
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onHandleIntent(Intent intent) {

        inicializarServicio(intent);

        try {

            while (this.seguir) {
                Thread.sleep(tiempoDeEspera);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    } //()

    // Inicializa el servicio, busca automaticamente el dispositivo llamado a
    // @params Recoge los ajustes de busqueda del dispositivo
    ConexionBaseDeDatos conexionBaseDeDatos = new ConexionBaseDeDatos();

    private void inicializarServicio(Intent intent) {
        this.tiempoDeEspera = intent.getLongExtra("tiempoDeEspera", /* default */ 50000);
        this.seguir = true;
        this.dispositivoEscuchando = intent.getStringExtra("nombreDelDispositivo");
        Log.d(ETIQUETA_LOG, " dispositivoEscuchando=" + dispositivoEscuchando);
        inicializarBlueTooth();
        buscarEsteDispositivoBTLE(dispositivoEscuchando);
    } //()

    // Inicializa el bluetooth
    @SuppressLint("MissingPermission")
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled());

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState());

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");


    } // ()

    // Busca el dispositivo llamado a
    // @params El nombre del dispositivo que buscar
    @SuppressLint("MissingPermission")
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {

                mostrarInformacionDispositivoBTLE(resultado);

                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                byte[] bytes = resultado.getScanRecord().getBytes();

                TramaBeacon tramaBeacon = new TramaBeacon(bytes);

                // Si lo hemos encontrado y su UUID es -EPSG-GTIDLGCO2- recibiremos en el major el valor del CO2
                if (Utilidades.bytesToString(tramaBeacon.getUUID()).equals("10c32e81-343e-428f-9aac-1a3818eb6999")) {

                    // Instanciamos la variable que usaremos para recopilar el dato
                    float datoC02;
                    String texto;

                    // Deconstruimos el major
                    texto = Integer.toString(Utilidades.bytesToInt(tramaBeacon.getMajor()));
                    datoC02 = Float.parseFloat(texto);
                    Log.d("Dispositivo",texto);

                    conexionBaseDeDatos.SubirDato(datoC02);

                    // Lo mostramos en el texbox de activity main
                    MainActivity.getInstance().ActualizarTextoDato(datoC02);

                }

            }


            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter sf = new ScanFilter.Builder().setDeviceName(dispositivoBuscado).build();
        filters.add(sf);


        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);


        this.elEscanner.startScan(filters, settings, this.callbackDelEscaneo);

    } // ()

    // Detiene la busqueda del dispositivo
    @SuppressLint("MissingPermission")
    private void detenerBusquedaDispositivosBTLE() {

        if (this.callbackDelEscaneo == null) {
            return;
        }

        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // ()

    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaBeacon tramaBeacon = new TramaBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tramaBeacon.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tramaBeacon.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tramaBeacon.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tramaBeacon.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          Beacon type = " + Integer.toHexString(tramaBeacon.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          Beacon length 0x = " + Integer.toHexString(tramaBeacon.getiBeaconLength()) + " ( "
                + tramaBeacon.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tramaBeacon.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tramaBeacon.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tramaBeacon.getMajor()) + "( "
                + Utilidades.bytesToInt(tramaBeacon.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tramaBeacon.getMinor()) + "( "
                + Utilidades.bytesToInt(tramaBeacon.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tramaBeacon.getTxPower()) + " ( " + tramaBeacon.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()
} // class
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
