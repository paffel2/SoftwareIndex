package com.softwareindex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softwareindex.databinding.ItemComputerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка компьютеров/ЭВМ в рамках конкретного класса.
 */
public class ComputerAdapter extends RecyclerView.Adapter<ComputerAdapter.ViewHolder> {

    /**
     * Интерфейс слушателя событий карточки ЭВМ (редактирование и удаление).
     */
    public interface OnComputerActionListener {
        void onEditClick(Computer computer);
        void onDeleteClick(Computer computer);
    }

    private final Context context;
    private List<Computer> computerList = new ArrayList<>();
    private final OnComputerActionListener listener;

    public ComputerAdapter(Context context, OnComputerActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setComputers(List<Computer> computers) {
        this.computerList = computers != null ? computers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComputerBinding binding = ItemComputerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Computer computer = computerList.get(position);
        holder.bind(computer);
    }

    @Override
    public int getItemCount() {
        return computerList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemComputerBinding binding;

        ViewHolder(ItemComputerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Computer computer) {
            // Установка названия и технических характеристик компьютера
            binding.tvComputerName.setText(computer.getName());
            binding.tvComputerSpecs.setText(computer.getSpecs());

            // Кнопка редактирования данных компьютера
            binding.btnEditComp.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(computer);
                }
            });

            // Кнопка удаления компьютера из класса
            binding.btnDeleteComp.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(computer);
                }
            });
        }
    }
}
