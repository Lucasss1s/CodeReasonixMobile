package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codereasonix.adapter.OfertaAdapter;
import com.example.codereasonix.model.OfertaLaboral;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OfertasActivity extends AppCompatActivity {

    private RecyclerView recyclerOfertas;
    private ProgressBar progressOfertas;
    private TextView txtEmpty;

    private final List<OfertaLaboral> listaOfertas = new ArrayList<>();
    private OfertaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofertas);

        recyclerOfertas = findViewById(R.id.recyclerOfertas);
        progressOfertas = findViewById(R.id.progressOfertas);
        txtEmpty        = findViewById(R.id.txtEmptyOfertas);

        recyclerOfertas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfertaAdapter(listaOfertas, oferta -> {
            Intent i = new Intent(OfertasActivity.this, OfertaDetalleActivity.class);
            i.putExtra("id_oferta", oferta.getIdOferta());
            startActivity(i);
        });
        recyclerOfertas.setAdapter(adapter);

        cargarOfertas();
    }

    private void cargarOfertas() {
        progressOfertas.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        recyclerOfertas.setVisibility(View.GONE);

        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(Config.BASE_URL + "/ofertas");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int code = con.getResponseCode();
                if (code < 200 || code >= 300) {
                    throw new RuntimeException("HTTP " + code);
                }

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());
                listaOfertas.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    listaOfertas.add(new OfertaLaboral(obj));
                }

                runOnUiThread(() -> {
                    progressOfertas.setVisibility(View.GONE);
                    if (listaOfertas.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerOfertas.setVisibility(View.GONE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                        recyclerOfertas.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressOfertas.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                    txtEmpty.setText("Error cargando ofertas laborales");
                    Toast.makeText(OfertasActivity.this,
                            "Error cargando ofertas", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }
}
