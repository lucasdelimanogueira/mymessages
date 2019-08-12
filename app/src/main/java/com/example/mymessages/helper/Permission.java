package com.example.mymessages.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {

    public static boolean validatePermissions(String[] permissions, Activity activity, int requestCode){

        /*verificar se a versao do usuario é maior que a do marshmallow
        pois essa verificação em tempo real só é necessária a partir dessa versão*/
        if(Build.VERSION.SDK_INT >= 23){

            List<String> permissionsList = new ArrayList<>();

            /*percorrer permissões passadas
            verificando uma a uma se já tem permissão liberada
             */
            for(String permission : permissions){
               Boolean permissionGranted =  ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
               //caso nao tenha permissao garantida, adicionar ela em permissionList
               if(!permissionGranted){
                   permissionsList.add(permission);
               }
            }

            //caso a lista esteja vazia, não é necessário solicitar permissão
            if(permissionsList.isEmpty()){
                return true;
            }

            //converter de Lista de strings para Array de strings
            String[] newPermissions = new String[permissionsList.size()];
            permissionsList.toArray(newPermissions);

            //solicitar permissões
            ActivityCompat.requestPermissions(activity, newPermissions, requestCode);

        }

        return true;
    }
}
