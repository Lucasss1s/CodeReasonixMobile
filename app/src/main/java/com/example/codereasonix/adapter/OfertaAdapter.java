package com.example.codereasonix.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codereasonix.R;
import com.example.codereasonix.model.OfertaLaboral;

import java.util.List;

public class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.ViewHolder> {

    public interface OnOfertaClickListener {
        void onClick(OfertaLaboral oferta);
    }

    private final List<OfertaLaboral> lista;
    private final OnOfertaClickListener listener;

    public OfertaAdapter(List<OfertaLaboral> lista, OnOfertaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtEmpresa, txtUbicacion, txtFecha;

        public ViewHolder(@NonNull View v) {
            super(v);
            txtTitulo   = v.findViewById(R.id.txtTituloOferta);
            txtEmpresa  = v.findViewById(R.id.txtEmpresaOferta);
            txtUbicacion= v.findViewById(R.id.txtUbicacionOferta);
            txtFecha    = v.findViewById(R.id.txtFechaOferta);
        }
    }

    @NonNull
    @Override
    public OfertaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oferta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OfertaAdapter.ViewHolder holder, int position) {
        OfertaLaboral o = lista.get(position);

        holder.txtTitulo.setText(o.getTitulo());

        String nombreEmpresa = (o.getEmpresa() != null && o.getEmpresa().getNombre() != null)
                ? o.getEmpresa().getNombre()
                : "Empresa sin nombre";
        holder.txtEmpresa.setText(nombreEmpresa);

        holder.txtUbicacion.setText(
                (o.getUbicacion() != null && !o.getUbicacion().isEmpty())
                        ? o.getUbicacion()
                        : "Ubicación no especificada"
        );

        holder.txtFecha.setText(
                (o.getFechaPublicacion() != null && !o.getFechaPublicacion().isEmpty())
                        ? o.getFechaPublicacion()
                        : ""
        );

        holder.itemView.setOnClickListener(v -> listener.onClick(o));
    }

    public void actualizarLista(List<OfertaLaboral> nuevaLista) {
        lista.clear();
        lista.addAll(nuevaLista);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }
}
