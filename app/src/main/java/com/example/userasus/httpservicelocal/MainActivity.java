package com.example.userasus.httpservicelocal;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView campo;
    EditText byId;
    String IP="http://192.168.43.87/WebServicesAlumnos/";
    String IP_CONSULTAR_TODOS  =IP+"obtener_alumnos.php";
    String IP_BORRAR           =IP+"borrar_alumno.php";
    String IP_CONSULTAR_POR_ID =IP+"obtener_alumno_por_id.php";
    LlamarServicio obj=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        campo = (TextView) findViewById(R.id.tv1);
        byId = (EditText) findViewById(R.id.tvById);
    }

    public  void Limpiar(View  w){
        campo.setText("");
    }

    public void Consultar(View t){
        /////////////- DETERMINAR EL TIPO DE CONEXIÓN
        boolean isWiFi=false;
        boolean isMobile=false;
        boolean Isconnect=false;
        ConnectivityManager val = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net= val.getActiveNetworkInfo();
        if(net!=null){
            Isconnect=net.isConnectedOrConnecting();
        }
        if(net!=null && Isconnect){
            isWiFi = net.getType() == ConnectivityManager.TYPE_WIFI;
            isMobile = net.getType() == ConnectivityManager.TYPE_MOBILE;
            //Toast.makeText(this,"Iniciando : "+Isconnect+" wifi:"+isWiFi+" movil: "+isMobile,Toast.LENGTH_SHORT).show();
            if(isMobile){
                AlertDialog.Builder ventana = new AlertDialog.Builder(this);
                ventana.setTitle("Confirmación");
                ventana.setMessage("¿Esta seguro que desea inciar la conexión con datos móviles?");
                ventana.setCancelable(false);
                ventana.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ConsultarServicio(); //  SI EL USUARIO ACEPTA ENTONCES SE LLAMA AL METODO ConsultarServicio
                    }
                });
                ventana.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface q, int i) {
                        q.dismiss();
                    }
                });
                ventana.show();
            }else{
                ConsultarServicio();
            }
        }else{
            Toast.makeText(this,"No fue posible establecer conexión.",Toast.LENGTH_LONG).show();
        }
        /////////////- DETERMINAR EL TIPO DE CONEXIÓN
    }
    public void ConsultarServicio(){
        obj = new LlamarServicio();
        obj.execute(1);
    }
    public void ConsultarPorId(View v){
        obj = new LlamarServicio();
        String byIdAux = byId.getText().toString();
        obj.execute(2);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class LlamarServicio extends AsyncTask<Integer,Void,String>{

        @Override
        protected String doInBackground(Integer... voids) {
            StringBuilder result = new StringBuilder();
            String resultAux="";
            String ip="";
            if (voids[0]==1){
             ip=IP_CONSULTAR_TODOS;
            }else if (voids[0]==2){
                ip=IP_CONSULTAR_POR_ID;
            }
            try {
                URL url = new URL(ip);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

                //añadir parametro.
                int resultado = conexion.getResponseCode();

                if(resultado== HttpURLConnection.HTTP_OK){//HAY CONEXIÓN

                    InputStream datos = new BufferedInputStream (conexion.getInputStream());
                    BufferedReader lector = new BufferedReader(new InputStreamReader(datos));

                    String linea;
                    while ((linea = lector.readLine()) !=null){
                       result.append(linea);
                    }
                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados
                    String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON

                    if(voids[0]==1) {///CONSULTAR TODOS
                        if (resultJSON == "1") {      // hay alumnos a mostrar
                            JSONArray alumnosJSON = respuestaJSON.getJSONArray("alumnos");   // estado es el nombre del campo en el JSON
                            for (int i = 0; i < alumnosJSON.length(); i++) {
                                resultAux = resultAux + alumnosJSON.getJSONObject(i).getString("idAlumno") + " " +
                                        alumnosJSON.getJSONObject(i).getString("nombre") + " " +
                                        alumnosJSON.getJSONObject(i).getString("direccion") + "\n";
                                //publishProgress(resultAux);
                            }
                        } else if (resultJSON == "2") {
                            resultAux = "No hay alumnos";
                        }
                    }else if(voids[0]==2) {// Consultar por ID
                        if(resultJSON=="3"){
                            resultAux="Se necesita un identificador";
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultAux;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            campo.setText(s);
        }
    }
}
