package com.example.codereasonix;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codereasonix.adapter.PublicacionAdapter;
import com.example.codereasonix.model.Publicacion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComunidadActivity extends BaseActivity
        implements PublicacionAdapter.OnPublicacionListener {

    private static final int REQ_IMAGE_PICK = 101;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerPublicaciones;
    private EditText edtContenidoPublicacion, edtBuscarHashtag;
    private Button btnPublicar, btnClearFiltro, btnAdjuntarImagen;
    private TextView txtEmptyFeed;
    private ImageView imgPreviewPublicacion;

    private final List<Publicacion> listaPublicaciones = new ArrayList<>();
    private final List<Publicacion> listaFiltrada = new ArrayList<>();
    private PublicacionAdapter adapter;

    private int idClienteActual = -1;

    private Uri imagenSeleccionadaUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunidad);

        enableImmersiveMode();
        setupTopBar();
        setupBottomNav();

        swipeRefresh = findViewById(R.id.swipeRefreshComunidad);
        recyclerPublicaciones = findViewById(R.id.recyclerPublicaciones);
        edtContenidoPublicacion = findViewById(R.id.edtContenidoPublicacion);
        edtBuscarHashtag = findViewById(R.id.edtBuscarHashtag);
        btnPublicar = findViewById(R.id.btnPublicar);
        btnClearFiltro = findViewById(R.id.btnClearFiltro);
        txtEmptyFeed = findViewById(R.id.txtEmptyFeed);
        btnAdjuntarImagen = findViewById(R.id.btnAdjuntarImagen);
        imgPreviewPublicacion = findViewById(R.id.imgPreviewPublicacion);

        SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
        idClienteActual = prefs.getInt("id_cliente", -1);

        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicacionAdapter(listaFiltrada, idClienteActual, this);
        recyclerPublicaciones.setAdapter(adapter);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::cargarFeed);
        }

        btnPublicar.setOnClickListener(v -> enviarPublicacion());

        btnAdjuntarImagen.setOnClickListener(v -> abrirSelectorImagen());

        edtBuscarHashtag.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String texto = s.toString();
                btnClearFiltro.setVisibility(texto.isEmpty() ? Button.GONE : Button.VISIBLE);
                aplicarFiltroActual();
            }
        });

        btnClearFiltro.setOnClickListener(v -> {
            edtBuscarHashtag.setText("");
            aplicarFiltroActual();
        });

        cargarFeed();
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), REQ_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                imagenSeleccionadaUri = uri;
                imgPreviewPublicacion.setVisibility(ImageView.VISIBLE);
                imgPreviewPublicacion.setImageURI(uri);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarFeed();
    }

    private void setRefreshing(boolean refreshing) {
        if (swipeRefresh == null) return;
        runOnUiThread(() -> swipeRefresh.setRefreshing(refreshing));
    }

    private void cargarFeed() {
        setRefreshing(true);
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "/feed");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String body = sb.toString();
                procesarFeed(body);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setRefreshing(false);
                    Toast.makeText(ComunidadActivity.this,
                            "Error cargando feed", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void procesarFeed(String jsonTexto) {
        runOnUiThread(() -> {
            try {
                JSONArray arr = new JSONArray(jsonTexto);
                listaPublicaciones.clear();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Publicacion p = Publicacion.fromJson(obj);
                    listaPublicaciones.add(p);
                }

                aplicarFiltroActual();
                setRefreshing(false);

            } catch (Exception e) {
                e.printStackTrace();
                setRefreshing(false);
                Toast.makeText(this, "Error procesando feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aplicarFiltroActual() {
        String query = edtBuscarHashtag.getText().toString();
        String filtro = query.trim().toLowerCase();
        if (filtro.startsWith("#")) {
            filtro = filtro.substring(1);
        }

        listaFiltrada.clear();

        if (filtro.isEmpty()) {
            listaFiltrada.addAll(listaPublicaciones);
        } else {
            for (Publicacion p : listaPublicaciones) {
                if (coincideHashtag(p.getContenido(), filtro)) {
                    listaFiltrada.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();
        actualizarMensajeVacio(filtro.isEmpty());
    }

    private boolean coincideHashtag(String contenido, String filtroNorm) {
        if (contenido == null || filtroNorm.isEmpty()) return false;
        for (String tag : extraerHashtags(contenido)) {
            if (tag.startsWith(filtroNorm)) return true;
        }
        return false;
    }

    private List<String> extraerHashtags(String texto) {
        List<String> tags = new ArrayList<>();
        if (texto == null) return tags;

        Pattern pattern = Pattern.compile("#([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }

    private void actualizarMensajeVacio(boolean filtroVacio) {
        if (listaFiltrada.isEmpty()) {
            txtEmptyFeed.setVisibility(TextView.VISIBLE);
            if (listaPublicaciones.isEmpty()) {
                txtEmptyFeed.setText("Todavía no hay publicaciones. ¡Sé el primero en escribir algo!");
            } else if (!filtroVacio) {
                txtEmptyFeed.setText("No hay publicaciones que coincidan con ese hashtag.");
            } else {
                txtEmptyFeed.setText("No hay publicaciones.");
            }
        } else {
            txtEmptyFeed.setVisibility(TextView.GONE);
        }
    }

    private void enviarPublicacion() {
        String contenido = edtContenidoPublicacion.getText().toString().trim();
        if (contenido.isEmpty()) {
            Toast.makeText(this, "Escribí algo para publicar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idClienteActual <= 0) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublicar.setEnabled(false);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "/publicaciones");
                String boundary = "----CRBoundary" + System.currentTimeMillis();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());

                writeFormField(os, boundary, "id_cliente", String.valueOf(idClienteActual));
                writeFormField(os, boundary, "contenido", contenido);

                if (imagenSeleccionadaUri != null) {
                    try {
                        ContentResolver cr = getContentResolver();
                        String mime = cr.getType(imagenSeleccionadaUri);
                        if (mime == null) mime = "image/jpeg";

                        String extension = "jpg";
                        if (mime.endsWith("png")) extension = "png";
                        else if (mime.endsWith("webp")) extension = "webp";

                        String fileName = "publicacion_" + idClienteActual + "_" +
                                System.currentTimeMillis() + "." + extension;

                        InputStream isImg = cr.openInputStream(imagenSeleccionadaUri);
                        byte[] dataImg = readAllBytes(isImg);
                        if (isImg != null) isImg.close();

                        writeFileField(os, boundary, "imagen", fileName, mime, dataImg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    runOnUiThread(() -> {
                        edtContenidoPublicacion.setText("");

                        imagenSeleccionadaUri = null;
                        imgPreviewPublicacion.setImageDrawable(null);
                        imgPreviewPublicacion.setVisibility(ImageView.GONE);

                        Toast.makeText(ComunidadActivity.this,
                                "Publicación creada", Toast.LENGTH_SHORT).show();
                        cargarFeed();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                            "Error creando publicación", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                        "Error creando publicación", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) conn.disconnect();
                runOnUiThread(() -> btnPublicar.setEnabled(true));
            }
        }).start();
    }

    private void writeFormField(OutputStream os, String boundary,
                                String name, String value) throws Exception {
        String part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" +
                value + "\r\n";
        os.write(part.getBytes(StandardCharsets.UTF_8));
    }

    private void writeFileField(OutputStream os, String boundary,
                                String fieldName, String fileName,
                                String mimeType, byte[] data) throws Exception {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        os.write(data);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (is == null) return new byte[0];
        byte[] data = new byte[4096];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    @Override
    public void onComentar(Publicacion publicacion, String contenido) {
        if (idClienteActual <= 0) {
            Toast.makeText(this, "Iniciá sesión para comentar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contenido == null || contenido.trim().isEmpty()) return;
        enviarComentario(publicacion.getIdPublicacion(), contenido.trim());
    }

    private void enviarComentario(int idPublicacion, String contenido) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "/comentarios");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("id_publicacion", idPublicacion);
                body.put("id_cliente", idClienteActual);
                body.put("contenido", contenido);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    runOnUiThread(() -> {
                        Toast.makeText(ComunidadActivity.this,
                                "Comentario enviado", Toast.LENGTH_SHORT).show();
                        cargarFeed();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                            "Error enviando comentario", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                        "Error enviando comentario", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    @Override
    public void onReaccionar(Publicacion publicacion, String tipo) {
        if (idClienteActual <= 0) {
            Toast.makeText(this, "Iniciá sesión para reaccionar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "/reacciones");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("id_publicacion", publicacion.getIdPublicacion());
                body.put("id_cliente", idClienteActual);
                body.put("tipo", tipo);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    runOnUiThread(this::cargarFeed);
                } else {
                    runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                            "Error al reaccionar", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                        "Error al reaccionar", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    @Override
    public void onEliminar(Publicacion publicacion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar publicación")
                .setMessage("¿Seguro que querés eliminar esta publicación?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarPublicacion(publicacion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPublicacion(Publicacion publicacion) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "/publicaciones/" + publicacion.getIdPublicacion());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("id_cliente", idClienteActual);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    runOnUiThread(() -> {
                        Toast.makeText(ComunidadActivity.this,
                                "Publicación eliminada", Toast.LENGTH_SHORT).show();
                        cargarFeed();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                            "Error eliminando publicación", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ComunidadActivity.this,
                        "Error eliminando publicación", Toast.LENGTH_SHORT).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    @Override
    public void onVerPerfil(int idClienteAutor) {
        if (idClienteAutor <= 0) return;

        if (idClienteAutor == idClienteActual) {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PerfilPublicoActivity.class);
            intent.putExtra("id_cliente", idClienteAutor);
            startActivity(intent);
        }
    }
}
