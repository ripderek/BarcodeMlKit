package com.example.myappbarcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    Bitmap mSelectedImage;
    public ImageView mImageView;
    public Button btCamera, btGaleria;
    public ArrayList<String> permisosNoAprobados;
    public TextView txtResults;
    //clase para los permisos
    Permisos permisos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResults = findViewById(R.id.txtresults);
        mImageView = findViewById(R.id.image_view);
        btCamera = findViewById(R.id.btCamera);
        btGaleria = findViewById(R.id.btGallery);
        ArrayList<String> permisos_requeridos = new ArrayList<String>();
        permisos_requeridos.add(android.Manifest.permission.CAMERA);
        permisos_requeridos.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        permisos_requeridos.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        permisos = new Permisos(this);
        permisosNoAprobados = permisos.getPermisosNoAprobados(permisos_requeridos);
        requestPermissions(permisosNoAprobados.toArray(new String[permisosNoAprobados.size()]),
                100);
        //configurar la deteccion de codigos que queremos
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                //Codigo QR
                                Barcode.FORMAT_QR_CODE,
                                //Cofigo Barra
                                Barcode.FORMAT_CODABAR)
                        .build();

    }
    //92 106
    public void abrirGaleria (View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }
    public void abrirCamera (View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    //mostrar la imagen en el control
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //realizar el proceso de la lectura del codigo
    public void process(View view) {
        if (mSelectedImage == null) {
            Toast.makeText(this, "No hay imagen", Toast.LENGTH_SHORT).show();
        }
        else {
            InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            String  resultado = "";
                            if (barcodes.size() == 0) {
                                resultado= "No se encontró codigo legible";
                            } else {
                                StringBuilder resultText = new StringBuilder();
                                for (Barcode barcode : barcodes) {
                                    int valueType = barcode.getValueType();
                                    switch (valueType) {
                                        case Barcode.TYPE_WIFI:
                                            // Procesar código WiFi
                                            String ssid = barcode.getWifi().getSsid();
                                            String password = barcode.getWifi().getPassword();
                                            int tipo = barcode.getWifi().getEncryptionType();
                                            resultText.append(" RED WIFI\n")
                                                    .append(" Nombre: ").append(ssid).append("\n")
                                                    .append(" Contraseña: ").append(password).append("\n")
                                                    .append(" Tipo: ").append(tipo).append("\n");
                                            break;
                                        case Barcode.TYPE_URL:
                                            // Procesar URL
                                            String title = barcode.getUrl().getTitle();
                                            String url = barcode.getUrl().getUrl();
                                            resultText.append(" URL\n")
                                                    .append(" Titulo: ").append(title).append("\n")
                                                    .append(" Url: ").append(url).append("\n");
                                            break;
                                        default:
                                            //Procesar codigo de barra
                                            if (Barcode.FORMAT_CODABAR == 8) {
                                                String barra = barcode.getRawValue();
                                                resultText.append(" CODIGO DE BARRA\n" +
                                                        " Codigo: " + barra);
                                                break;
                                            }
                                            // Otros tipos de códigos
                                            resultText.append("Tipo de código no reconocido\n");
                                            break;
                                    }
                                }
                                resultado = resultText.toString();
                                txtResults.setText(resultado);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Error", e.getMessage().toString());
                            txtResults.setText("La imagen no tiene un codigo legible");
                        }
                    });
        }
    }
}