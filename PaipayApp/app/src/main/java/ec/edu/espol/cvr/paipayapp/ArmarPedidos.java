package ec.edu.espol.cvr.paipayapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import ec.edu.espol.cvr.paipayapp.adapters.PedidoAdapter;
import ec.edu.espol.cvr.paipayapp.model.Pedido;
import ec.edu.espol.cvr.paipayapp.utils.Invariante;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ArmarPedidos extends Activity {
    private ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
    private PedidoAdapter pedidoadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_armar_pedidos);

        ListView listview_pedido = (ListView) findViewById(R.id.listapedidos);
        listview_pedido.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            Intent intent = new Intent(ArmarPedidos.this, Info_pedido.class);
            Pedido tmp_pedido = pedidos.get(position);
            pedidos.clear();
            pedidoadapter.notifyDataSetChanged();
            intent.putExtra("id_pedido", tmp_pedido.getCodigo());
            DateFormat dateFormat = new SimpleDateFormat(Invariante.format_date);
            intent.putExtra("fecha",dateFormat.format(tmp_pedido.getFecha()));
            intent.putExtra("detalle",tmp_pedido.getDetallePedidos());
            startActivity(intent);
            finish();
            }
        });
        pedidoadapter = new PedidoAdapter(this, pedidos);
        listview_pedido.setAdapter(pedidoadapter);
        if (!update_list()){
            System.out.println("error al actualizar la lista");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Menu.class));
        finish();
    }

    boolean update_list(){
        try {
            SharedPreferences sharedpreferences = getSharedPreferences(Invariante.MyPREFERENCES, this.MODE_PRIVATE);
            String ip = sharedpreferences.getString("ip","");
            int port = sharedpreferences.getInt("port",0);
            boolean test_mode = sharedpreferences.getBoolean("test_mode",true);
            if(port != 0 && ip != ""){
                JSONObject pedidos_json = Pedido.get_pedidos(ip, port, "POR EMPAQUETAR");
                if(pedidos_json.getInt("response_code") == 200){
                    JSONArray jsonarr = pedidos_json.getJSONArray("pedidos");
                    for (int i = 0; i < jsonarr.length(); i++) {
                        JSONObject pedido = jsonarr.getJSONObject(i);
                        Date fecha = new SimpleDateFormat(Invariante.format_date).parse(pedido.getString("fecha"));
                        pedidos.add(new Pedido(fecha, pedido.getInt("codigo")));
                    }
                    return true;
                }else{
                    Toast.makeText(this, pedidos_json.getString("error"), Toast.LENGTH_LONG).show();
                    if (test_mode){
                        for (int i = 0; i < 3; i++) {
                            Date fecha = new SimpleDateFormat(Invariante.format_date).parse(i+"/03/2019");
                            pedidos.add(new Pedido(fecha, i+1));
                            pedidoadapter.notifyDataSetChanged();
                        }
                        return true;
                    }
                }
            }else{
                Toast.makeText(this, "IP y/o puerto del servidor no configurado. ", Toast.LENGTH_LONG).show();
            }
            pedidoadapter.notifyDataSetChanged();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
