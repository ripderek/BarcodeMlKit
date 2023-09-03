package com.example.myappbarcode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Permisos {
    private AppCompatActivity activity;

    public Permisos(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        for(int i=0; i<permissions.length; i++){
            if(permissions[i].equals(Manifest.permission.CAMERA)){
                //btnCamara.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            } else if(permissions[i].equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                    permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                //btnGaleria.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String>  listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23)
            for(String permiso: listaPermisos) {
                if (activity.checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                }
            }
        return list;
    }
}
