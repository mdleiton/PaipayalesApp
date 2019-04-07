package com.trimble.paipay.utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequestApi {
    private static String ip;
    private static int port;
    private static URL url;
    static String server;

    public static void set_network(String ip, int port){
        ip = ip;
        port = port;
        server = "http://" + ip + ":" + port; 

    }

    public static boolean login(String email, String password){
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", email);
        parameters.put("password", password);
        try {
            JSONObject response = request("/api/v1/auth/login/", "POST", parameters);
            if(response.getInt("response_code") == 200){
                return true;        
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    return false;
    }

    public static JSONObject request(String path, String type, Map<String, String> parameters){
        {
            try {
                url = new URL(server + path);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setRequestMethod(type);
                httpCon.setConnectTimeout(3000);
                httpCon.setRequestProperty("Accept", "application/json");
                httpCon.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(httpCon.getOutputStream());
                out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
                out.flush();
                out.close();
                httpCon.connect();
                int httpResponse = httpCon.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader((httpCon.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null)
                    sb.append(output);
                JSONObject json = new JSONObject();
                json.put("response_code", new Integer(httpResponse));                
                json.put("data", sb.toString());                
                httpCon.disconnect();
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
