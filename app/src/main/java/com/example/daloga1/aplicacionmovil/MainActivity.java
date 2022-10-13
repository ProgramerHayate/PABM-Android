package com.example.daloga1.aplicacionmovil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instancia;
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private TextView tvMedidaOzono;
    private Button inicarServicio;
    private Button finalizarServicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMedidaOzono = (TextView)findViewById(R.id.textView_MedidaOzono);
        inicarServicio =(Button)findViewById(R.id.botonInicarServicio);
        finalizarServicio =(Button)findViewById(R.id.botonFinalizarServicio);
        // Arrancamos el servicio
        startService(new Intent(MainActivity.this, Servicio.class));
        instancia = this;
    }

    public static MainActivity getInstance() {
        return instancia;
    }

    public void ActualizarTextoDato(float dato){

    tvMedidaOzono.setText(String.valueOf(dato));
    }

    public void OnclickEnviarMedida(View view){
        ConexionBaseDeDatos conexionBaseDeDatos = new ConexionBaseDeDatos();
        conexionBaseDeDatos.SubirDato(222222);
    }
    public void OnclickLeerMedida(View view){
        ConexionBaseDeDatos conexionBaseDeDatos = new ConexionBaseDeDatos();
        String dato = conexionBaseDeDatos.leerDatoBD();


        tvMedidaOzono.setText(dato);
        Log.d("TAG",dato);

    }
    public void OnclickIniciarServicio(View view){
        startService(new Intent(MainActivity.this, Servicio.class));
        // Desactiva el boton arrancar y los cambia de color
        inicarServicio.setClickable(false);
        finalizarServicio.setClickable(true);
        inicarServicio.setBackgroundColor(0xFF1F7004);
        finalizarServicio.setBackgroundColor(0xFFE60505);
    }
    public void OnclickFinalizarServicio(View view){
        stopService(new Intent(MainActivity.this, Servicio.class));
        // Desactiva el boton detener y los cambia de color
        inicarServicio.setClickable(true);
        finalizarServicio.setClickable(false);
        inicarServicio.setBackgroundColor(0xFF42ED09);
        finalizarServicio.setBackgroundColor(0xFF700404);
    }

    private void inicializarPermisosBluetooth() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }  {
            Log.d("Test", " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    }// ()

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // Si se cancela la peticion el permiso es denegado.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("Test", " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // El permiso es garantizado.
                }  else {

                    Log.d("Test", " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
    } // ()
}