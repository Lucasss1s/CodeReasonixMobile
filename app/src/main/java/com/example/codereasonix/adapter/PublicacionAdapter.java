package com.example.codereasonix.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codereasonix.R;
import com.example.codereasonix.model.Comentario;
import com.example.codereasonix.model.Publicacion;
import com.example.codereasonix.model.Reaccion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.ViewHolder> {

    public interface OnPublicacionListener {
        void onReaccionar(Publicacion publicacion, String tipo);
        void onComentar(Publicacion publicacion, String contenido);
        void onEliminar(Publicacion publicacion);
        void onVerPerfil(int idClienteAutor);
    }

    private final List<Publicacion> publicaciones;
    private final int idClienteActual;
    private final OnPublicacionListener listener;

    public PublicacionAdapter(List<Publicacion> publicaciones, int idClienteActual, OnPublicacionListener listener) {
        this.publicaciones = publicaciones;
        this.idClienteActual = idClienteActual;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PublicacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_publicacion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionAdapter.ViewHolder holder, int position) {
        Publicacion p = publicaciones.get(position);

        String nombreAutor = (p.getAutor() != null && p.getAutor().getNombre() != null)
                ? p.getAutor().getNombre()
                : "Usuario";

        holder.txtAutor.setText(nombreAutor);

        if (nombreAutor != null && !nombreAutor.isEmpty()) {
            holder.txtAvatarInitial.setText(
                    String.valueOf(Character.toUpperCase(nombreAutor.charAt(0)))
            );
        } else {
            holder.txtAvatarInitial.setText("?");
        }

        View.OnClickListener perfilClick = v -> {
            if (listener != null && p.getAutor() != null) {
                listener.onVerPerfil(p.getAutor().getIdCliente());
            }
        };
        holder.txtAutor.setOnClickListener(perfilClick);
        holder.txtAvatarInitial.setOnClickListener(perfilClick);

        holder.txtFecha.setText(formatearFecha(p.getFecha()));
        holder.txtContenido.setText(p.getContenido() != null ? p.getContenido() : "");

        String imagenUrl = p.getImagenUrl();
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            holder.imgPublicacion.setVisibility(View.VISIBLE);
            holder.imgPublicacion.setImageDrawable(null);

            Glide.with(holder.itemView.getContext())
                    .load(imagenUrl)
                    .into(holder.imgPublicacion);
        } else {
            holder.imgPublicacion.setVisibility(View.GONE);
            holder.imgPublicacion.setImageDrawable(null);
        }

        List<Comentario> comentarios = p.getComentarios() != null
                ? p.getComentarios()
                : new ArrayList<>();

        holder.txtComentariosCount.setText(comentarios.size() + " comentarios");
        ComentarioAdapter comentarioAdapter = new ComentarioAdapter(comentarios);
        holder.recyclerComentarios.setLayoutManager(
                new LinearLayoutManager(holder.itemView.getContext())
        );
        holder.recyclerComentarios.setAdapter(comentarioAdapter);

        List<Reaccion> reacciones = p.getReacciones() != null
                ? p.getReacciones()
                : new ArrayList<>();

        int likes = 0;
        int dislikes = 0;
        String tipoActual = null;

        for (Reaccion r : reacciones) {
            if ("like".equalsIgnoreCase(r.getTipo())) likes++;
            else if ("dislike".equalsIgnoreCase(r.getTipo())) dislikes++;

            if (r.getAutor() != null && r.getAutor().getIdCliente() == idClienteActual) {
                tipoActual = r.getTipo();
            }
        }

        holder.txtLikesCount.setText(likes + " 👍");
        holder.txtDislikesCount.setText(dislikes + " 👎");

        holder.txtLikesCount.setAlpha("like".equalsIgnoreCase(tipoActual) ? 1.0f : 0.7f);
        holder.txtDislikesCount.setAlpha("dislike".equalsIgnoreCase(tipoActual) ? 1.0f : 0.7f);

        holder.txtLikesCount.setOnClickListener(v -> {
            if (listener != null) listener.onReaccionar(p, "like");
        });

        holder.txtDislikesCount.setOnClickListener(v -> {
            if (listener != null) listener.onReaccionar(p, "dislike");
        });

        holder.btnEnviarComentario.setOnClickListener(v -> {
            if (listener == null) return;
            String texto = holder.edtNuevoComentario.getText().toString().trim();
            if (!texto.isEmpty()) {
                listener.onComentar(p, texto);
                holder.edtNuevoComentario.setText("");
            }
        });

        if (p.getAutor() != null && p.getAutor().getIdCliente() == idClienteActual) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(v.getContext(), v);
                menu.getMenu().add("Eliminar publicación");
                menu.setOnMenuItemClickListener(item -> {
                    if (listener != null) listener.onEliminar(p);
                    return true;
                });
                menu.show();
            });
        } else {
            holder.btnMenu.setVisibility(View.GONE);
        }
    }

    private String formatearFecha(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        try {
            String base = raw;
            if (base.length() >= 19) {
                base = base.substring(0, 19);
            }

            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = iso.parse(base);
            if (date == null) return raw;

            SimpleDateFormat out = new SimpleDateFormat(
                    "dd 'de' MMMM 'de' yyyy",
                    new Locale("es", "ES")
            );
            return out.format(date);

        } catch (Exception e) {
            try {
                String soloFecha = raw.length() >= 10 ? raw.substring(0, 10) : raw;
                SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = isoDate.parse(soloFecha);
                if (date == null) return raw;

                SimpleDateFormat out = new SimpleDateFormat(
                        "dd 'de' MMMM 'de' yyyy",
                        new Locale("es", "ES")
                );
                return out.format(date);
            } catch (Exception ex) {
                return raw;
            }
        }
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAvatarInitial;
        TextView txtAutor, txtFecha, txtContenido;
        TextView txtLikesCount, txtDislikesCount, txtComentariosCount;
        TextView btnMenu;
        ImageView imgPublicacion;
        EditText edtNuevoComentario;
        Button btnEnviarComentario;
        RecyclerView recyclerComentarios;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAvatarInitial    = itemView.findViewById(R.id.txtAvatarInitial);
            txtAutor            = itemView.findViewById(R.id.txtAutor);
            txtFecha            = itemView.findViewById(R.id.txtFecha);
            txtContenido        = itemView.findViewById(R.id.txtContenido);
            btnMenu             = itemView.findViewById(R.id.btnMenu);
            imgPublicacion      = itemView.findViewById(R.id.imgPublicacion);
            txtLikesCount       = itemView.findViewById(R.id.txtLikesCount);
            txtDislikesCount    = itemView.findViewById(R.id.txtDislikesCount);
            txtComentariosCount = itemView.findViewById(R.id.txtComentariosCount);
            edtNuevoComentario  = itemView.findViewById(R.id.edtNuevoComentario);
            btnEnviarComentario = itemView.findViewById(R.id.btnEnviarComentario);
            recyclerComentarios = itemView.findViewById(R.id.recyclerComentarios);
        }
    }
}
