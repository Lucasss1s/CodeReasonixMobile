package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import com.example.codereasonix.adapter.DesafioAdapter;
import com.example.codereasonix.model.Desafio;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DesafiosActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private List<Desafio> lista = new ArrayList<>();
    private DesafioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desafios);

        recycler = findViewById(R.id.recyclerDesafios);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DesafioAdapter(lista, d -> {
            Intent intent = new Intent(DesafiosActivity.this, DesafioDetalleActivity.class);
            intent.putExtra("id_desafio", d.getIdDesafio());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        cargarDesafios();
    }

    private void cargarDesafios() {
        new Thread(() -> {
            try {
                URL url = new URL(Config.BASE_URL + "/desafios");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                InputStream in = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());
                lista.clear();
                for (int i = 0; i < arr.length(); i++) {
                    lista.add(new Desafio(arr.getJSONObject(i)));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error cargando desafíos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
