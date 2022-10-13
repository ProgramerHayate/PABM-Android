package com.example.daloga1.aplicacionmovil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConexionBaseDeDatos {

    //Instancaimos Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map dato = new HashMap();

    public void SubirDato(float datoRaw){

        //Creamos el objeto dato que vamos a subir
        Map<String, Object> dato = new HashMap<>();
        dato.put("Dato", datoRaw);

        //Subimos el dato a firebase
        db.collection("Medidas")
                .add(dato)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("TAG", "Documento añadido con ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error añadiendo documento", e);
                    }
                });

    }
    //leemos un dato de Firebase
    public String leerDatoBD() {


        DocumentReference medida = db.collection("Medidas").document("70eQm1nhnRJGnIoujbOC");
        medida.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        dato = document.getData();
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
        return dato.toString();
    }
}
