package com.example.codereasonix.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codereasonix.R;
import com.example.codereasonix.model.Desafio;

import java.util.List;

public class DesafioAdapter extends RecyclerView.Adapter<DesafioAdapter.ViewHolder> {
    private final List<Desafio> lista;
    private final OnDesafioClickListener listener;

    public interface OnDesafioClickListener {
        void onClick(Desafio desafio);
    }

    public DesafioAdapter(List<Desafio> lista, OnDesafioClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBossThumb;
        TextView nombre, descripcion, hp;
        ProgressBar hpBar;
        TextView txtDificultad, txtLenguaje;

        public ViewHolder(View v) {
            super(v);
            imgBossThumb = v.findViewById(R.id.imgBossThumb);
            nombre       = v.findViewById(R.id.txtNombre);
            descripcion  = v.findViewById(R.id.txtDescripcion);
            hp           = v.findViewById(R.id.txtHp);
            hpBar        = v.findViewById(R.id.hpBar);
            txtDificultad = v.findViewById(R.id.txtDificultad);
            txtLenguaje   = v.findViewById(R.id.txtLenguaje);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_desafio, parent, false);
        return new ViewHolder(v);
    }

    private String formatDificultad(String dif) {
        if (dif == null) return "Sin dificultad";
        switch (dif.toLowerCase()) {
            case "facil": return "Fácil";
            case "intermedio": return "Intermedio";
            case "dificil": return "Difícil";
            case "experto": return "Experto";
            default: return dif;
        }
    }

    private String formatLenguaje(String lang) {
        if (lang == null) return "Sin lenguaje";
        switch (lang.toLowerCase()) {
            case "java": return "Java";
            case "python": return "Python";
            case "javascript": return "JavaScript";
            case "php": return "PHP";
            default: return lang;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Desafio d = lista.get(position);

        holder.nombre.setText(d.getNombre());
        holder.descripcion.setText(d.getDescripcion() != null ? d.getDescripcion() : "");

        int hpTotal = Math.max(0, d.getHpTotal());
        int hpRest  = Math.max(0, d.getHpRestante());
        if (hpTotal == 0) {
            holder.hp.setText("HP: 0 / 0");
            holder.hpBar.setMax(1);
            holder.hpBar.setProgress(0);
        } else {
            holder.hp.setText("HP: " + hpRest + " / " + hpTotal);
            holder.hpBar.setMax(hpTotal);
            holder.hpBar.setProgress(Math.min(hpRest, hpTotal));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.hpBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#00BFA6")));
            holder.hpBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#203C38")));
        }

        String difText = formatDificultad(d.getDificultad());
        String langText = formatLenguaje(d.getLenguaje());

        holder.txtDificultad.setText(difText);
        holder.txtLenguaje.setText(langText);

        int verde = Color.parseColor("#00BFA6");
        holder.txtDificultad.setTextColor(verde);
        holder.txtLenguaje.setTextColor(verde);

        Glide.with(holder.itemView.getContext())
                .load(d.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgBossThumb);

        holder.itemView.setOnClickListener(v -> listener.onClick(d));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
