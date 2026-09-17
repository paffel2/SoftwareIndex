package com.softwareindex;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softwareindex.databinding.ItemComputerRoomBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка классов/аудиторий в RecyclerView.
 */
public class ComputerRoomAdapter extends RecyclerView.Adapter<ComputerRoomAdapter.ViewHolder> {

    /**
     * Интерфейс слушателя событий на карточке аудитории (клик по карточке, кнопкам изменить/удалить).
     */
    public interface OnRoomActionListener {
        void onItemClick(ComputerRoom room);
        void onEditClick(ComputerRoom room);
        void onDeleteClick(ComputerRoom room);
    }

    private final Context context;
    private List<ComputerRoom> roomList = new ArrayList<>();
    private final OnRoomActionListener listener;

    public ComputerRoomAdapter(Context context, OnRoomActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setRooms(List<ComputerRoom> rooms) {
        this.roomList = rooms != null ? rooms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComputerRoomBinding binding = ItemComputerRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComputerRoom room = roomList.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemComputerRoomBinding binding;

        ViewHolder(ItemComputerRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ComputerRoom room) {
            // Установка номера класса и количества ЭВМ
            binding.tvRoomNumber.setText(context.getString(R.string.room_format, room.getRoomNumber()));
            binding.tvPcCount.setText(context.getString(R.string.pcs_count_format, room.getComputerCount()));

            // Отображение наименования кафедры
            String dept = room.getDepartmentName();
            if (TextUtils.isEmpty(dept)) {
                binding.tvDepartment.setText(context.getString(R.string.department_default));
            } else {
                binding.tvDepartment.setText(dept);
            }

            // Отображение дополнительного примечания/назначения аудитории
            String notes = room.getNotes();
            if (TextUtils.isEmpty(notes)) {
                binding.tvNotes.setVisibility(View.GONE);
            } else {
                binding.tvNotes.setVisibility(View.VISIBLE);
                binding.tvNotes.setText(notes);
            }

            // Нажатие на карточку открывает подробный экран класса
            binding.cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(room);
                }
            });

            // Нажатие на кнопку редактирования класса
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(room);
                }
            });

            // Нажатие на кнопку удаления класса
            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(room);
                }
            });
        }
    }
}
