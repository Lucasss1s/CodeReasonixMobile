package com.example.codereasonix.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codereasonix.OfertaDetalleActivity;
import com.example.codereasonix.R;

import org.json.JSONObject;

import java.util.List;

public class PostulacionAdapter extends RecyclerView.Adapter<PostulacionAdapter.ViewHolder> {

    private final List<JSONObject> lista;
    private final Context context;

    public PostulacionAdapter(List<JSONObject> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloOferta, txtEstado, txtFecha;
        Button btnVerOferta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloOferta = itemView.findViewById(R.id.txtTituloPostulacion);
            txtEstado       = itemView.findViewById(R.id.txtEstadoPostulacion);
            txtFecha        = itemView.findViewById(R.id.txtFechaPostulacion);
            btnVerOferta    = itemView.findViewById(R.id.btnVerOferta);
        }
    }

    @NonNull
    @Override
    public PostulacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_postulacion, parent, false);

        v.setClickable(false);
        v.setOnClickListener(null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostulacionAdapter.ViewHolder holder, int position) {
        JSONObject obj = lista.get(position);

        String estado = obj.optString("estado", "pendiente");
        String fecha  = obj.optString("fecha", "");

        JSONObject ofertaObj = obj.optJSONObject("oferta");
        String titulo = "";
        int idOferta  = obj.optInt("id_oferta", -1);

        if (ofertaObj != null) {
            titulo  = ofertaObj.optString("titulo", "");
            if (idOferta == -1) {
                idOferta = ofertaObj.optInt("id_oferta", -1);
            }
        }

        holder.txtTituloOferta.setText(titulo.isEmpty() ? "Oferta sin título" : titulo);
        holder.txtEstado.setText("Estado: " + estado);
        holder.txtFecha.setText(fecha);

        final int finalIdOferta = idOferta;
        holder.btnVerOferta.setOnClickListener(v -> {
            if (finalIdOferta > 0) {
                Intent i = new Intent(context, OfertaDetalleActivity.class);
                i.putExtra("id_oferta", finalIdOferta);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
