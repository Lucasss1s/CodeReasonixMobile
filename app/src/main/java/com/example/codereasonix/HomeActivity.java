package com.example.codereasonix;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerDesafios;
    private DesafioAdapter adapter;
    private final List<Desafio> listaDesafios = new ArrayList<>();
    private Spinner spDificultad, spLenguaje;
    private String selectedDificultad = "";
    private String selectedLenguaje  = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        enableImmersiveMode();

        setupTopBar();
        setupBottomNav();

        swipeRefresh     = findViewById(R.id.swipeRefresh);
        recyclerDesafios = findViewById(R.id.recyclerDesafios);
        spDificultad     = findViewById(R.id.spDificultad);
        spLenguaje       = findViewById(R.id.spLenguaje);

        recyclerDesafios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DesafioAdapter(listaDesafios, desafio -> {
            int id = desafio.getIdDesafio();
            if (id <= 0) {
                Toast.makeText(this, "ID de desafío inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(
                    new android.content.Intent(HomeActivity.this, DesafioDetalleActivity.class)
                            .putExtra("id_desafio", id)
            );
        });
        recyclerDesafios.setAdapter(adapter);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::cargarDesafios);
        }

        configurarSpinnersFiltros();
        cargarDesafios();
    }

    private void configurarSpinnersFiltros() {
        final int VERDE = android.graphics.Color.parseColor("#00BFA6");

        if (spDificultad != null) {
            final String[] labelsDif = {"Todas", "Fácil", "Intermedio", "Difícil", "Experto"};
            final String[] valuesDif = {"", "facil", "intermedio", "dificil", "experto"};

            ArrayAdapter<String> difAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    labelsDif
            );
            difAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDificultad.setAdapter(difAdapter);

            spDificultad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    selectedDificultad = valuesDif[position];

                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(VERDE);
                    }

                    cargarDesafios();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (spLenguaje != null) {
            final String[] labelsLang = {"Todos", "Java", "Python", "JavaScript", "PHP"};
            final String[] valuesLang = {"", "java", "python", "javascript", "php"};

            ArrayAdapter<String> langAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    labelsLang
            );
            langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spLenguaje.setAdapter(langAdapter);

            spLenguaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    selectedLenguaje = valuesLang[position];

                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(VERDE);
                    }

                    cargarDesafios();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
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

    private String buildDesafiosUrl() {
        try {
            String base = Config.BASE_URL + "/desafios";
            List<String> params = new ArrayList<>();

            if (!TextUtils.isEmpty(selectedDificultad)) {
                params.add("dificultad=" + URLEncoder.encode(selectedDificultad, "UTF-8"));
            }
            if (!TextUtils.isEmpty(selectedLenguaje)) {
                params.add("lenguaje=" + URLEncoder.encode(selectedLenguaje, "UTF-8"));
            }

            if (params.isEmpty()) {
                return base;
            } else {
                return base + "?" + TextUtils.join("&", params);
            }
        } catch (Exception e) {
            return Config.BASE_URL + "/desafios";
        }
    }

    private void cargarDesafios() {
        setRefreshing(true);
        new Thread(() -> {
            HttpURLConnection conexion = null;
            try {
                String urlStr = buildDesafiosUrl();
                URL url = new URL(urlStr);
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
