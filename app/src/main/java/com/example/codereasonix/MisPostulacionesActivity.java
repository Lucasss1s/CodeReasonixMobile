package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.codereasonix.adapter.PostulacionAdapter;

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

public class MisPostulacionesActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipePostulaciones;
    private RecyclerView recyclerPostulaciones;
    private PostulacionAdapter adapter;
    private final List<JSONObject> listaPostulaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_postulaciones);

        swipePostulaciones    = findViewById(R.id.swipePostulaciones);
        recyclerPostulaciones = findViewById(R.id.recyclerPostulaciones);

        recyclerPostulaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostulacionAdapter(listaPostulaciones, this);
        recyclerPostulaciones.setAdapter(adapter);

        if (swipePostulaciones != null) {
            swipePostulaciones.setOnRefreshListener(this::cargarPostulaciones);
        }

        cargarPostulaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPostulaciones();
    }

    private void setRefreshing(boolean refreshing) {
        if (swipePostulaciones == null) return;
        runOnUiThread(() -> swipePostulaciones.setRefreshing(refreshing));
    }

    private void cargarPostulaciones() {
        setRefreshing(true);

        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                int idCliente = prefs.getInt("id_cliente", -1);
                if (idCliente == -1) {
                    runOnUiThread(() -> {
                        setRefreshing(false);
                        Toast.makeText(this, "Iniciá sesión para ver tus postulaciones", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                URL url = new URL(Config.BASE_URL + "/postulaciones/mias/" + idCliente);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int code = con.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String body = sb.toString();
                runOnUiThread(() -> {
                    procesarPostulaciones(body);
                    setRefreshing(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setRefreshing(false);
                    Toast.makeText(this, "Error cargando postulaciones", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void procesarPostulaciones(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            listaPostulaciones.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                listaPostulaciones.add(obj);
            }

            adapter.notifyDataSetChanged();

            if (listaPostulaciones.isEmpty()) {
                Toast.makeText(this, "Todavía no tenés postulaciones.", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error procesando datos de postulaciones", Toast.LENGTH_SHORT).show();
        }
    }
}
