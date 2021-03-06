package ec.edu.espol.cvr.paipayapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ec.edu.espol.cvr.paipayapp.utils.Invariante;

/**
 * Esta activity es para manejar el inicio de sesión y la configuración de ip y puerto.
 * @author: Mauricio Leiton Lázaro(mdleiton)
 * @version: 1.0
 */
public class Login extends Activity {
    private int port = 8081;
    private String ip = "192.168.100.8";
    private boolean test_mode = false;  //sacar test
    private SharedPreferences sharedpreferences;
    Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        sharedpreferences = getSharedPreferences(Invariante.MyPREFERENCES, this.MODE_PRIVATE);
        ip = sharedpreferences.getString("ip", ip);
        port = sharedpreferences.getInt("port", port);
        /*
        String rol = sharedpreferences.getString("rol","");
        if(rol != ""){
            get_menu(rol);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        sharedpreferences = getSharedPreferences(Invariante.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("test_mode", test_mode);  //sacar
        editor.putString("ip", ip);
        editor.putInt("port", port);
        editor.apply();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Funcion que se ejecuta cuando se da tap en boton iniciar sesión
     * @param view
        */
    public void verificarUsuario(View view){
        String email = ((TextView) findViewById(R.id.user)).getText().toString().trim();
        String password = ((TextView) findViewById(R.id.password)).getText().toString().trim();
        if (!email.contains("@")){
            Toast.makeText(this, Invariante.ERROR_CORREO, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.isEmpty() && !password.isEmpty()) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("email", email.toLowerCase());
            editor.apply();

            if (test_mode){
                Toast.makeText(this, Invariante.PRUEBA, Toast.LENGTH_SHORT).show();
                String rol = Invariante.USUARIO_ADMIN;
                get_menu(rol);
            }else{
                api_login(email, password);
            }
        } else {
            Toast.makeText(this, Invariante.ERROR_LOGIN_1, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Método que redireccionar a específica activity deacuerdo al rol del usuario que ha iniciado sesión-
     * @param rol rol del usuario que ha iniciado sesión
    */
    public void get_menu(String rol){
        Intent intent;
        if (rol.equals(Invariante.USUARIO_ADMIN)){
            intent = new Intent(Login.this, MenuAdmin.class);
        }else if (rol.equals(Invariante.USUARIO_REPARTIDOR)){
            intent = new Intent(Login.this, ListarPedidosRepartidor.class);
        }else{
            Toast.makeText(this, Invariante.ERROR_LOGIN_ROL, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        finish();
    }

    /**
     * Funcion consulta al api y devuelve el token y rol del usuario
     * @param email correo del usuario
     * @param password contraseña del usuario
     */
    public void api_login(String email, String password){
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("email", email);
            parameters.put("password", password);
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            final String server = Invariante.get_server(ip, port);
            System.out.println(server+ "/api/v1/auth/login/");
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST,server+ "/api/v1/auth/login/" , parameters, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String rol = response.getString("role");
                                int id = response.getInt("id");
                                //String rol = Invariante.USUARIO_ADMIN;
                                String token = response.getString(Invariante.TOKEN);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("rol",rol);
                                editor.putString(Invariante.TOKEN, token);
                                System.out.println(token);
                                editor.putInt("id", id);
                                editor.apply();
                                get_menu(rol);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Login.this, Invariante.ERROR_LOGIN_RED, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                int code = error.networkResponse.statusCode;
                                JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                String message = "Error " + String.valueOf(code) + json.getString("message");
                                Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                            }catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(Login.this, Invariante.ERROR_LOGIN_RED_ACCESO, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funcion que actualiza ip y puertos
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_conf_server:
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(Login.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.config_server, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(Login.this);
                alertDialogBuilderUserInput.setView(mView);
                final EditText userInputIP = (EditText) mView.findViewById(R.id.userInputIp);
                final EditText userInputPort = (EditText) mView.findViewById(R.id.userInputPort);
                ip = sharedpreferences.getString("ip", ip);
                port = sharedpreferences.getInt("port", port);

                userInputIP.setText(ip);
                userInputPort.setText(String.valueOf(port));

                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String port_new = userInputPort.getText().toString();
                                String ip_new = userInputIP.getText().toString();
                                if (!port_new.isEmpty() && !ip_new.isEmpty()){
                                    try{
                                        int port_int = Integer.parseInt(port_new);
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("ip", ip_new.toLowerCase());
                                        editor.putInt("port", port_int);
                                        editor.apply();
                                        ip = ip_new.toLowerCase();
                                        port = port_int;

                                        Toast.makeText(Login.this, Invariante.CONF_ACTUALIZADO, Toast.LENGTH_LONG).show();
                                    }catch (Exception e){
                                        Toast.makeText(Login.this, Invariante.CONF_ERROR_1, Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(Login.this, Invariante.CONF_ERROR_2, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });
                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                return true;
            default:
                return true;
        }
    }
}