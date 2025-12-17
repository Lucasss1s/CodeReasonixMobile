package com.example.codereasonix;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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

public class OfertasActivity extends BaseActivity {

    private RecyclerView recyclerOfertas;
    private ProgressBar progressOfertas;
    private TextView txtEmpty;
    private EditText edtBuscarEmpresa;

    private final List<OfertaLaboral> listaOfertas = new ArrayList<>();
    private final List<OfertaLaboral> listaFiltrada = new ArrayList<>();
    private OfertaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofertas);

        enableImmersiveMode();
        setupTopBar();
        setupBottomNav();

        recyclerOfertas = findViewById(R.id.recyclerOfertas);
        progressOfertas = findViewById(R.id.progressOfertas);
        txtEmpty        = findViewById(R.id.txtEmptyOfertas);
        edtBuscarEmpresa = findViewById(R.id.edtBuscarEmpresa);

        recyclerOfertas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfertaAdapter(listaFiltrada, oferta -> {
            Intent i = new Intent(OfertasActivity.this, OfertaDetalleActivity.class);
            i.putExtra("id_oferta", oferta.getIdOferta());
            startActivity(i);
        });
        recyclerOfertas.setAdapter(adapter);

        configurarBuscador();
        cargarOfertas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarOfertas();
    }

    private void configurarBuscador() {
        edtBuscarEmpresa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPorEmpresa(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filtrarPorEmpresa(String texto) {
        String query = texto.toLowerCase().trim();

        List<OfertaLaboral> filtradas = new ArrayList<>();

        for (OfertaLaboral o : listaOfertas) {
            if (o.getEmpresa() != null &&
                    o.getEmpresa().getNombre() != null &&
                    o.getEmpresa().getNombre().toLowerCase().contains(query)) {

                filtradas.add(o);
            }
        }

        adapter.actualizarLista(filtradas);
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
                listaFiltrada.clear();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    OfertaLaboral oferta = new OfertaLaboral(obj);
                    listaOfertas.add(oferta);
                    listaFiltrada.add(oferta);
                }

                runOnUiThread(() -> {
                    progressOfertas.setVisibility(View.GONE);

                    if (listaFiltrada.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        txtEmpty.setText("No hay ofertas disponibles por el momento.");
                    } else {
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
                    Toast.makeText(this, "Error cargando ofertas", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }
}
