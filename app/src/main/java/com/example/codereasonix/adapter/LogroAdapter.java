package com.example.codereasonix.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codereasonix.R;
import com.example.codereasonix.model.Logro;

import java.util.List;

public class LogroAdapter extends RecyclerView.Adapter<LogroAdapter.LogroViewHolder> {

    private final List<Logro> listaLogros;

    public LogroAdapter(List<Logro> listaLogros) {
        this.listaLogros = listaLogros;
    }

    @NonNull
    @Override
    public LogroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_logro, parent, false);
        return new LogroViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull LogroViewHolder holder, int position) {
        Logro logro = listaLogros.get(position);
        holder.txtIcono.setText(logro.getIcono());
        holder.txtTitulo.setText(logro.getTitulo());
        holder.txtDescripcion.setText(logro.getDescripcion());
        holder.txtXp.setText("+" + logro.getXpOtorgado() + " XP");
        holder.itemView.setAlpha(logro.isDesbloqueado() ? 1f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return listaLogros.size();
    }

    static class LogroViewHolder extends RecyclerView.ViewHolder {
        TextView txtIcono, txtTitulo, txtDescripcion, txtXp;

        public LogroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIcono = itemView.findViewById(R.id.txtIconoLogro);
            txtTitulo = itemView.findViewById(R.id.txtTituloLogroItem);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionLogro);
            txtXp = itemView.findViewById(R.id.txtXpLogro);
        }
    }
}
