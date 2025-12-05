package com.example.codereasonix;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.codereasonix.adapter.LogroAdapter;
import com.example.codereasonix.model.Logro;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PerfilPublicoActivity extends BaseActivity {

    private ImageView imgBanner, imgAvatar;
    private TextView txtTituloPerfil, txtNombreUsuario, txtUsername, txtEmailUsuario;
    private TextView txtNivel, txtXpTotal, txtRacha, txtProgresoDetalle;
    private EditText editBiografia, editSkills, editRedes;
    private ProgressBar progressNivel;
    private View layoutGuardarPerfil;
    private ImageButton btnGuardarPerfil;
    private RecyclerView recyclerLogros;
    private TextView btnToggleLogros;

    private final List<Logro> listaLogrosTodas = new ArrayList<>();
    private final List<Logro> listaLogrosVisibles = new ArrayList<>();
    private LogroAdapter logroAdapter;
    private boolean mostrandoTodosLogros = false;

    private int idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        enableImmersiveMode();
        setupTopBar();
        setupBottomNav();

        idCliente = getIntent().getIntExtra("id_cliente", -1);
        if (idCliente <= 0) {
            Toast.makeText(this, "Perfil no disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imgBanner = findViewById(R.id.imgBanner);
        imgAvatar = findViewById(R.id.imgAvatar);

        txtTituloPerfil   = findViewById(R.id.txtTituloPerfil);
        txtNombreUsuario  = findViewById(R.id.txtNombreUsuario);
        txtUsername       = findViewById(R.id.txtUsername);
        txtEmailUsuario   = findViewById(R.id.txtEmailUsuario);

        txtNivel          = findViewById(R.id.txtNivel);
        txtXpTotal        = findViewById(R.id.txtXpTotal);
        txtRacha          = findViewById(R.id.txtRacha);
        txtProgresoDetalle = findViewById(R.id.txtProgresoDetalle);

        editBiografia = findViewById(R.id.editBiografia);
        editSkills    = findViewById(R.id.editSkills);
        editRedes     = findViewById(R.id.editRedes);

        progressNivel       = findViewById(R.id.progressNivel);
        layoutGuardarPerfil = findViewById(R.id.layoutGuardarPerfil);
        btnGuardarPerfil    = findViewById(R.id.btnGuardarPerfil);

        recyclerLogros = findViewById(R.id.recyclerLogros);
        btnToggleLogros = findViewById(R.id.btnToggleLogros);

        if (layoutGuardarPerfil != null) layoutGuardarPerfil.setVisibility(View.GONE);
        if (btnGuardarPerfil != null) btnGuardarPerfil.setEnabled(false);

        deshabilitarEditText(editBiografia);
        deshabilitarEditText(editSkills);
        deshabilitarEditText(editRedes);

        txtTituloPerfil.setText("Perfil");

        recyclerLogros.setLayoutManager(new LinearLayoutManager(this));
        logroAdapter = new LogroAdapter(listaLogrosVisibles);
        recyclerLogros.setAdapter(logroAdapter);

        btnToggleLogros.setOnClickListener(v -> {
            mostrandoTodosLogros = !mostrandoTodosLogros;
            actualizarLogrosVisibles();
        });

        cargarDatosPublico();
    }

    private void deshabilitarEditText(EditText et) {
        if (et == null) return;
        et.setEnabled(false);
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setClickable(false);
    }

    private void cargarDatosPublico() {
        new Thread(() -> {
            HttpURLConnection connPerfil = null;
            HttpURLConnection connUsuario = null;
            HttpURLConnection connGami = null;
            HttpURLConnection connLogros = null;

            JSONObject perfilJson = null;
            JSONObject usuarioJson = null;
            JSONObject gamiJson = null;
            JSONObject logrosJson = null;

            try {
                URL urlUsuario = new URL(Config.BASE_URL + "/usuarios/by-cliente/" + idCliente);
                connUsuario = (HttpURLConnection) urlUsuario.openConnection();
                connUsuario.setRequestMethod("GET");
                usuarioJson = leerJson(connUsuario);

                URL urlPerfil = new URL(Config.BASE_URL + "/perfil/" + idCliente);
                connPerfil = (HttpURLConnection) urlPerfil.openConnection();
                connPerfil.setRequestMethod("GET");
                perfilJson = leerJson(connPerfil);

                URL urlGami = new URL(Config.BASE_URL + "/gamificacion/me/" + idCliente);
                connGami = (HttpURLConnection) urlGami.openConnection();
                connGami.setRequestMethod("GET");
                gamiJson = leerJson(connGami);

                URL urlLogros = new URL(Config.BASE_URL + "/logros/me/" + idCliente);
                connLogros = (HttpURLConnection) urlLogros.openConnection();
                connLogros.setRequestMethod("GET");
                logrosJson = leerJson(connLogros);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(PerfilPublicoActivity.this,
                                "Error cargando perfil", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (connPerfil != null) connPerfil.disconnect();
                if (connUsuario != null) connUsuario.disconnect();
                if (connGami != null) connGami.disconnect();
                if (connLogros != null) connLogros.disconnect();
            }

            JSONObject finalPerfilJson = perfilJson;
            JSONObject finalUsuarioJson = usuarioJson;
            JSONObject finalGamiJson = gamiJson;
            JSONObject finalLogrosJson = logrosJson;

            runOnUiThread(() -> {
                if (finalUsuarioJson != null) mostrarUsuario(finalUsuarioJson);
                if (finalPerfilJson != null)  mostrarPerfilPublico(finalPerfilJson);
                if (finalGamiJson != null)    mostrarGamificacion(finalGamiJson);
                if (finalLogrosJson != null)  mostrarLogros(finalLogrosJson);
            });
        }).start();
    }

    private JSONObject leerJson(HttpURLConnection conn) {
        try {
            conn.connect();
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) return null;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea);
            br.close();

            String body = sb.toString();
            if (body.isEmpty()) return null;

            if (!body.trim().startsWith("{") && !body.trim().startsWith("[")) {
                return null;
            }

            return new JSONObject(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarUsuario(JSONObject usuarioJson) {
        String nombre = usuarioJson.optString("nombre", "");
        String email  = usuarioJson.optString("email", "");

        txtNombreUsuario.setText(nombre.isEmpty() ? "Usuario sin nombre" : nombre);
        txtEmailUsuario.setText(email.isEmpty() ? "Sin email" : email);

        if (txtTituloPerfil.getText() == null || txtTituloPerfil.getText().length() == 0) {
            txtTituloPerfil.setText(nombre.isEmpty() ? "Perfil" : ("Perfil de " + nombre));
        }
    }

    private void mostrarPerfilPublico(JSONObject perfilJson) {
        String biografia     = perfilJson.optString("biografia", "");
        String skills        = perfilJson.optString("skills", "");
        String redes         = perfilJson.optString("redes_sociales", "");
        String displayName   = perfilJson.optString("display_name", "");
        String username      = perfilJson.optString("username", "");
        String fotoPerfilUrl = perfilJson.optString("foto_perfil", "");
        String bannerUrl     = perfilJson.optString("banner_url", "");

        editBiografia.setText(biografia);
        editSkills.setText(skills);
        editRedes.setText(redes);

        if (!displayName.trim().isEmpty()) {
            txtTituloPerfil.setText("Perfil de " + displayName);
            txtNombreUsuario.setText(displayName);
        }

        if (!username.trim().isEmpty()) {
            txtUsername.setText("@" + username);
        } else {
            txtUsername.setText("");
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            Glide.with(this)
                    .load(bannerUrl)
                    .centerCrop()
                    .into(imgBanner);
        }

        if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfilUrl)
                    .circleCrop()
                    .into(imgAvatar);
        }
    }

    private void mostrarGamificacion(JSONObject gamiJson) {
        int nivel   = gamiJson.optInt("nivel", 1);
        int xpTotal = gamiJson.optInt("xp_total", 0);
        int streak  = gamiJson.optInt("streak", 0);

        txtNivel.setText(String.valueOf(nivel));
        txtXpTotal.setText(String.valueOf(xpTotal));
        txtRacha.setText("🔥 " + streak);

        JSONObject progreso = gamiJson.optJSONObject("progreso");
        int xpEnNivel   = 0;
        int xpParaSubir = 100;
        if (progreso != null) {
            xpEnNivel   = progreso.optInt("xpEnNivel", 0);
            xpParaSubir = progreso.optInt("xpParaSubir", 100);
        }

        progressNivel.setMax(xpParaSubir <= 0 ? 100 : xpParaSubir);
        progressNivel.setProgress(Math.max(0, Math.min(xpEnNivel, progressNivel.getMax())));

        txtProgresoDetalle.setText(
                String.format(Locale.getDefault(), "%d / %d XP", xpEnNivel, xpParaSubir)
        );
    }

    private void mostrarLogros(JSONObject logrosJson) {
        JSONArray defs      = logrosJson.optJSONArray("defs");
        JSONArray obtenidos = logrosJson.optJSONArray("obtenidos");

        listaLogrosTodas.clear();

        if (defs == null) {
            actualizarLogrosVisibles();
            return;
        }

        Set<Integer> idsDesbloqueados = new HashSet<>();
        if (obtenidos != null) {
            for (int i = 0; i < obtenidos.length(); i++) {
                JSONObject obj = obtenidos.optJSONObject(i);
                if (obj == null) continue;
                int id = obj.optInt("id_logro", -1);
                if (id > 0) idsDesbloqueados.add(id);
            }
        }

        for (int i = 0; i < defs.length(); i++) {
            JSONObject def = defs.optJSONObject(i);
            if (def == null) continue;
            int id = def.optInt("id_logro", -1);
            if (id <= 0) continue;

            String icono       = def.optString("icono", "🏅");
            String titulo      = def.optString("titulo", "Logro");
            String descripcion = def.optString("descripcion", "");
            int xp             = def.optInt("xp_otorgado", 0);
            boolean desbloqueado = idsDesbloqueados.contains(id);

            Logro logro = new Logro(id, icono, titulo, descripcion, xp, desbloqueado);
            listaLogrosTodas.add(logro);
        }

        actualizarLogrosVisibles();
    }

    private void actualizarLogrosVisibles() {
        listaLogrosVisibles.clear();

        if (listaLogrosTodas.isEmpty()) {
            logroAdapter.notifyDataSetChanged();
            btnToggleLogros.setVisibility(View.GONE);
            TextView tituloLogros = findViewById(R.id.txtTituloLogros);
            if (tituloLogros != null) {
                tituloLogros.setText("Logros (0)");
            }
            return;
        }

        if (mostrandoTodosLogros || listaLogrosTodas.size() <= 3) {
            listaLogrosVisibles.addAll(listaLogrosTodas);
        } else {
            listaLogrosVisibles.addAll(listaLogrosTodas.subList(0, 3));
        }

        logroAdapter.notifyDataSetChanged();

        TextView tituloLogros = findViewById(R.id.txtTituloLogros);
        if (tituloLogros != null) {
            tituloLogros.setText("Logros (" + listaLogrosTodas.size() + ")");
        }

        if (listaLogrosTodas.size() > 3) {
            btnToggleLogros.setVisibility(View.VISIBLE);
            btnToggleLogros.setText(mostrandoTodosLogros ? "Ver menos" : "Ver todos");
        } else {
            btnToggleLogros.setVisibility(View.GONE);
        }
    }
}
