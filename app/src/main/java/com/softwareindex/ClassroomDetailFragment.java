package com.softwareindex;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.softwareindex.databinding.DialogAddEditComputerBinding;
import com.softwareindex.databinding.FragmentClassroomDetailBinding;

import java.util.List;

/**
 * Фрагмент детального просмотра аудитории/класса.
 * Отображает общие данные класса, перечень всех ЭВМ в классе
 * и предоставляет функционал для добавления, редактирования и удаления ЭВМ.
 */
public class ClassroomDetailFragment extends Fragment implements ComputerAdapter.OnComputerActionListener {

    public static final String ARG_CLASSROOM_ID = "classroom_id";

    private FragmentClassroomDetailBinding binding;
    private DatabaseHelper dbHelper;
    private ComputerAdapter adapter;
    private long classroomId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classroomId = getArguments().getLong(ARG_CLASSROOM_ID, -1);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentClassroomDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        setupListeners();

        loadClassroomDetails();
        loadComputersData();
    }

    /**
     * Настройка списка ЭВМ.
     */
    private void setupRecyclerView() {
        adapter = new ComputerAdapter(requireContext(), this);
        binding.rvComputers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvComputers.setAdapter(adapter);
    }

    /**
     * Настройка обработчиков нажатий для кнопок добавления ЭВМ.
     */
    private void setupListeners() {
        binding.fabAddComputer.setOnClickListener(v -> showAddEditComputerDialog(null));
    }

    /**
     * Загрузка подробных сведений о классе из БД.
     */
    private void loadClassroomDetails() {
        ComputerRoom room = dbHelper.getRoomById(classroomId);
        if (room != null) {
            binding.tvDetailRoomNumber.setText(getString(R.string.room_format, room.getRoomNumber()));
            binding.tvDetailDepartment.setText(room.getDepartmentName());

            String notes = room.getNotes();
            if (TextUtils.isEmpty(notes)) {
                binding.tvDetailNotes.setVisibility(View.GONE);
            } else {
                binding.tvDetailNotes.setVisibility(View.VISIBLE);
                binding.tvDetailNotes.setText(notes);
            }
        }
    }

    /**
     * Загрузка списка компьютеров для данного класса.
     */
    public void loadComputersData() {
        if (dbHelper == null || binding == null) return;

        List<Computer> computers = dbHelper.getComputersForRoom(classroomId);
        adapter.setComputers(computers);

        // Обновление общего счетчика ЭВМ
        binding.tvDetailPcCount.setText(getString(R.string.pcs_count_format, computers.size()));

        if (computers.isEmpty()) {
            binding.layoutEmptyComputers.setVisibility(View.VISIBLE);
            binding.rvComputers.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyComputers.setVisibility(View.GONE);
            binding.rvComputers.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Отображение диалогового окна добавления или редактирования ЭВМ.
     */
    public void showAddEditComputerDialog(@Nullable Computer computerToEdit) {
        DialogAddEditComputerBinding dialogBinding = DialogAddEditComputerBinding.inflate(getLayoutInflater());

        boolean isEditing = computerToEdit != null;
        if (isEditing) {
            dialogBinding.tvDialogTitle.setText(R.string.title_edit_computer);
            dialogBinding.etCompName.setText(computerToEdit.getName());
            dialogBinding.etCompSpecs.setText(computerToEdit.getSpecs());
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.title_add_computer);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.etCompName.getText() != null ? dialogBinding.etCompName.getText().toString().trim() : "";
                String specs = dialogBinding.etCompSpecs.getText() != null ? dialogBinding.etCompSpecs.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(requireContext(), R.string.error_empty_comp_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEditing) {
                    computerToEdit.setName(name);
                    computerToEdit.setSpecs(specs);
                    dbHelper.updateComputer(computerToEdit);
                    Toast.makeText(requireContext(), R.string.toast_computer_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Computer newComp = new Computer(classroomId, name, specs);
                    dbHelper.addComputer(newComp);
                    Toast.makeText(requireContext(), R.string.toast_computer_added, Toast.LENGTH_SHORT).show();
                }

                loadComputersData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEditClick(Computer computer) {
        showAddEditComputerDialog(computer);
    }

    @Override
    public void onDeleteClick(Computer computer) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_comp_msg, computer.getName()))
                .setPositiveButton(R.string.btn_delete, (d, which) -> {
                    dbHelper.deleteComputer(computer.getId());
                    Toast.makeText(requireContext(), R.string.toast_computer_deleted, Toast.LENGTH_SHORT).show();
                    loadComputersData();
                })
                .setNegativeButton(R.string.btn_cancel, (d, which) -> d.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
