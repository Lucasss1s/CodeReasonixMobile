package com.example.codereasonix.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codereasonix.R;
import com.example.codereasonix.model.Comentario;

import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private final List<Comentario> comentarios;

    public ComentarioAdapter(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ComentarioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioAdapter.ViewHolder holder, int position) {
        Comentario c = comentarios.get(position);
        holder.txtAutor.setText(c.getAutor() != null ? c.getAutor().getNombre() : "Anónimo");
        holder.txtContenido.setText(c.getContenido());
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAutor, txtContenido;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAutor = itemView.findViewById(R.id.txtAutorComentario);
            txtContenido = itemView.findViewById(R.id.txtContenidoComentario);
        }
    }
}
