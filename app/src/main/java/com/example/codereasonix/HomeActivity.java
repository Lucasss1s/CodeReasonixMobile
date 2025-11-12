package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.codereasonix.adapter.DesafioAdapter;
import com.example.codereasonix.model.Desafio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerDesafios;
    private DesafioAdapter adapter;
    private final List<Desafio> listaDesafios = new ArrayList<>();

    private Button btnMisDesafios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnMisDesafios = findViewById(R.id.btnMisDesafios);
        if (btnMisDesafios != null) {
            btnMisDesafios.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, MisDesafiosActivity.class))
            );
        }

        swipeRefresh = findViewById(R.id.swipeRefresh);

        recyclerDesafios = findViewById(R.id.recyclerDesafios);
        if (recyclerDesafios == null) {
            recyclerDesafios = findViewById(R.id.recyclerPreguntas);
        }
        recyclerDesafios.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DesafioAdapter(listaDesafios, desafio -> {
            int id = desafio.getIdDesafio();
            if (id <= 0) {
                Toast.makeText(this, "ID de desafío inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, DesafioDetalleActivity.class);
            intent.putExtra("id_desafio", id);
            startActivity(intent);
        });
        recyclerDesafios.setAdapter(adapter);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::cargarDesafios);
        }

        cargarDesafios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDesafios();
    }

    private void setRefreshing(final boolean refreshing) {
        if (swipeRefresh == null) return;
        runOnUiThread(() -> swipeRefresh.setRefreshing(refreshing));
    }

    private void cargarDesafios() {
        setRefreshing(true);
        new Thread(() -> {
            HttpURLConnection conexion = null;
            try {
                URL url = new URL(Config.BASE_URL + "/desafios");
                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");
                conexion.connect();

                int code = conexion.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conexion.getInputStream()
                        : conexion.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String body = sb.toString();
                runOnUiThread(() -> {
                    procesarDesafios(body);
                    setRefreshing(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setRefreshing(false);
                    Toast.makeText(HomeActivity.this, "Error cargando desafíos", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conexion != null) conexion.disconnect();
            }
        }).start();
    }

    private void procesarDesafios(String jsonTexto) {
        try {
            JSONArray arr = new JSONArray(jsonTexto);
            listaDesafios.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Desafio d = new Desafio(obj);
                listaDesafios.add(d);
            }

            adapter.notifyDataSetChanged();

            if (listaDesafios.isEmpty()) {
                Toast.makeText(this, "No hay desafíos disponibles", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error procesando datos de desafíos", Toast.LENGTH_SHORT).show();
        }
    }
}
