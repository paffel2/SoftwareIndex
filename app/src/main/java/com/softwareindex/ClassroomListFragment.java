package com.softwareindex;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.softwareindex.databinding.DialogAddDepartmentBinding;
import com.softwareindex.databinding.DialogAddEditRoomBinding;
import com.softwareindex.databinding.FragmentClassroomListBinding;

import java.util.List;

/**
 * Фрагмент отображения главного списка классов/аудиторий кафедры.
 * Предоставляет возможности поиска, выбора режима сортировки,
 * добавления новых кафедр и аудиторий.
 */
public class ClassroomListFragment extends Fragment implements ComputerRoomAdapter.OnRoomActionListener {

    private FragmentClassroomListBinding binding;
    private DatabaseHelper dbHelper;
    private ComputerRoomAdapter adapter;

    private String currentSortMode = DatabaseHelper.SORT_ROOM_ONLY;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentClassroomListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        setupRecyclerView();
        setupSearchAndSort();
        setupMenu();

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновление списка при возврате на экран
        loadData();
    }

    /**
     * Инициализация RecyclerView и связывание с адаптером.
     */
    private void setupRecyclerView() {
        adapter = new ComputerRoomAdapter(requireContext(), this);
        binding.rvComputerRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvComputerRooms.setAdapter(adapter);
    }

    /**
     * Настройка поиска в реальном времени и чипов переключения сортировки.
     */
    private void setupSearchAndSort() {
        // Слушатель ввода текста в поле поиска
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString() : "";
                loadData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Слушатель переключения чипов сортировки
        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentSortMode = DatabaseHelper.SORT_ROOM_ONLY;
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_sort_count_desc) {
                    currentSortMode = DatabaseHelper.SORT_COUNT_DESC;
                } else if (id == R.id.chip_sort_count_asc) {
                    currentSortMode = DatabaseHelper.SORT_COUNT_ASC;
                } else {
                    currentSortMode = DatabaseHelper.SORT_ROOM_ONLY;
                }
            }
            loadData();
        });
    }

    /**
     * Подключение меню Toolbar с опциями добавления кафедры и сброса данных.
     */
    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_reset_data) {
                    showResetConfirmationDialog();
                    return true;
                } else if (menuItem.getItemId() == R.id.action_add_room) {
                    showAddEditRoomDialog(null);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_add_department) {
                    showAddDepartmentDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Загрузка списка аудиторий из базы данных с учетом поиска и выбранной сортировки.
     */
    public void loadData() {
        if (dbHelper == null || binding == null) return;

        List<ComputerRoom> rooms = dbHelper.getAllRooms(currentSortMode, currentSearchQuery);
        adapter.setRooms(rooms);

        if (rooms.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvComputerRooms.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvComputerRooms.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Отображение диалогового окна для создания новой кафедры.
     */
    public void showAddDepartmentDialog() {
        DialogAddDepartmentBinding dialogBinding = DialogAddDepartmentBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.etDepartmentName.getText() != null ? dialogBinding.etDepartmentName.getText().toString().trim() : "";
                String notes = dialogBinding.etDepartmentNotes.getText() != null ? dialogBinding.etDepartmentNotes.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(requireContext(), R.string.error_empty_dept_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                long id = dbHelper.addDepartment(new Department(name, notes));
                if (id != -1) {
                    Toast.makeText(requireContext(), R.string.toast_department_added, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Кафедра с таким названием уже существует", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    /**
     * Отображение диалогового окна для добавления или редактирования аудитории.
     * Кафедра выбирается из списка выпадающего меню на основе записей в БД.
     */
    public void showAddEditRoomDialog(@Nullable ComputerRoom roomToEdit) {
        List<Department> departments = dbHelper.getAllDepartments();
        if (departments.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Нет кафедр")
                    .setMessage(R.string.error_no_departments)
                    .setPositiveButton(R.string.action_add_department, (d, which) -> showAddDepartmentDialog())
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
            return;
        }

        DialogAddEditRoomBinding dialogBinding = DialogAddEditRoomBinding.inflate(getLayoutInflater());

        ArrayAdapter<Department> deptAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, departments);
        dialogBinding.actvDepartment.setAdapter(deptAdapter);

        final Department[] selectedDepartmentHolder = new Department[1];

        boolean isEditing = roomToEdit != null;
        if (isEditing) {
            dialogBinding.tvDialogTitle.setText(R.string.title_edit_room);
            dialogBinding.etRoomNumber.setText(roomToEdit.getRoomNumber());
            dialogBinding.etNotes.setText(roomToEdit.getNotes());

            for (Department d : departments) {
                if (d.getId() == roomToEdit.getDepartmentId()) {
                    selectedDepartmentHolder[0] = d;
                    dialogBinding.actvDepartment.setText(d.getName(), false);
                    break;
                }
            }
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.title_add_room);
            if (!departments.isEmpty()) {
                selectedDepartmentHolder[0] = departments.get(0);
                dialogBinding.actvDepartment.setText(departments.get(0).getName(), false);
            }
        }

        dialogBinding.actvDepartment.setOnItemClickListener((parent, view, position, id) -> {
            selectedDepartmentHolder[0] = deptAdapter.getItem(position);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String roomNum = dialogBinding.etRoomNumber.getText() != null ? dialogBinding.etRoomNumber.getText().toString().trim() : "";
                String notes = dialogBinding.etNotes.getText() != null ? dialogBinding.etNotes.getText().toString().trim() : "";

                if (TextUtils.isEmpty(roomNum)) {
                    Toast.makeText(requireContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedDepartmentHolder[0] == null) {
                    Toast.makeText(requireContext(), R.string.error_select_dept, Toast.LENGTH_SHORT).show();
                    return;
                }

                long deptId = selectedDepartmentHolder[0].getId();

                if (isEditing) {
                    roomToEdit.setRoomNumber(roomNum);
                    roomToEdit.setDepartmentId(deptId);
                    roomToEdit.setNotes(notes);
                    dbHelper.updateRoom(roomToEdit);
                    Toast.makeText(requireContext(), R.string.toast_room_updated, Toast.LENGTH_SHORT).show();
                } else {
                    ComputerRoom newRoom = new ComputerRoom(roomNum, deptId, notes);
                    dbHelper.addRoom(newRoom);
                    Toast.makeText(requireContext(), R.string.toast_room_added, Toast.LENGTH_SHORT).show();
                }

                loadData();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    /**
     * Обработка нажатия на карточку аудитории — переход к детальному экрану класса.
     */
    @Override
    public void onItemClick(ComputerRoom room) {
        Bundle args = new Bundle();
        args.putLong(ClassroomDetailFragment.ARG_CLASSROOM_ID, room.getId());
        NavHostFragment.findNavController(ClassroomListFragment.this)
                .navigate(R.id.action_ClassroomListFragment_to_ClassroomDetailFragment, args);
    }

    /**
     * Обработка нажатия кнопки «Изменить» аудитории.
     */
    @Override
    public void onEditClick(ComputerRoom room) {
        showAddEditRoomDialog(room);
    }

    /**
     * Обработка нажатия кнопки «Удалить» аудитории с диалогом подтверждения.
     */
    @Override
    public void onDeleteClick(ComputerRoom room) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg, room.getRoomNumber(), room.getComputerCount()))
                .setPositiveButton(R.string.btn_delete, (d, which) -> {
                    dbHelper.deleteRoom(room.getId());
                    Toast.makeText(requireContext(), R.string.toast_room_deleted, Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton(R.string.btn_cancel, (d, which) -> d.dismiss())
                .show();
    }

    /**
     * Окно подтверждения сброса базы данных к исходным демонстрационным значениям.
     */
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Сброс данных")
                .setMessage("Восстановить исходные тестовые данные реестра аудиторий, кафедр и ЭВМ?")
                .setPositiveButton("Сбросить", (d, which) -> {
                    dbHelper.resetToDefaultData();
                    Toast.makeText(requireContext(), R.string.toast_data_reset, Toast.LENGTH_SHORT).show();
                    loadData();
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
