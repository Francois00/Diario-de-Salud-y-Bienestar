package com.example.diariodesaludybienestar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.RegistroViewHolder> {

    public interface OnRegistroClickListener {
        void onRegistroClick(int position);
        void onEditarClick(int position);
        void onEliminarClick(int position);
    }

    private List<RegistroCompleto> registros;
    private OnRegistroClickListener listener;

    public HistorialAdapter(List<RegistroCompleto> registros, OnRegistroClickListener listener) {
        this.registros = registros;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro_completo, parent, false);
        return new RegistroViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        RegistroCompleto registro = registros.get(position);
        holder.bind(registro);
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFecha;
        private TextView tvResumen;
        private ImageButton btnEditar, btnEliminar;

        public RegistroViewHolder(@NonNull View itemView, OnRegistroClickListener listener) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvResumen = itemView.findViewById(R.id.tvResumen);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRegistroClick(position);
                    }
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditarClick(position);
                    }
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEliminarClick(position);
                    }
                }
            });
        }

        public void bind(RegistroCompleto registro) {
            tvFecha.setText(registro.getFecha());
            tvResumen.setText(registro.getResumen());
        }
    }
}