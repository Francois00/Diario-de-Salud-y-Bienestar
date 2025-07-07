package com.example.diariodesaludybienestar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.RegistroViewHolder> {

    private List<Registro> registros;

    public HistorialAdapter(List<Registro> registros) {
        this.registros = registros;
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        Registro registro = registros.get(position);
        holder.bind(registro);
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFecha, tvDesayuno, tvEstadoAnimo, tvEjercicio;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDesayuno = itemView.findViewById(R.id.tvDesayuno);
            tvEstadoAnimo = itemView.findViewById(R.id.tvEstadoAnimo);
            tvEjercicio = itemView.findViewById(R.id.tvEjercicio);
        }

        public void bind(Registro registro) {
            tvFecha.setText(registro.getFecha());
            tvDesayuno.setText("Desayuno: " + registro.getDesayuno());
            tvEstadoAnimo.setText("Ansiedad: " + registro.getEstadoAnimo());
            tvEjercicio.setText("Ejercicio: " + registro.getEjercicio());
        }
    }
}