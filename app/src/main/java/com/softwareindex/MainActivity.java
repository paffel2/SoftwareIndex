package com.softwareindex;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.softwareindex.databinding.ActivityMainBinding;

/**
 * Главное активити приложения «Реестр ЭВМ кафедры».
 * Инициализирует панель действий (Toolbar), навигационный граф и плавающую кнопку (FAB).
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Настройка системных отступов EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        // Связывание панели действий с компонентом Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Динамическая адаптация FAB под активный экран
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.ClassroomDetailFragment) {
                    binding.fab.setContentDescription(getString(R.string.title_add_computer));
                } else {
                    binding.fab.setContentDescription(getString(R.string.title_add_room));
                }
            });
        }

        // Обработка нажатия на плавающую кнопку добавления аудитории или ЭВМ
        binding.fab.setOnClickListener(view -> {
            if (navHostFragment != null) {
                Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().isEmpty()
                        ? null
                        : navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (currentFragment instanceof ClassroomListFragment) {
                    ((ClassroomListFragment) currentFragment).showAddEditRoomDialog(null);
                } else if (currentFragment instanceof ClassroomDetailFragment) {
                    ((ClassroomDetailFragment) currentFragment).showAddEditComputerDialog(null);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        boolean handled = false;
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            handled = NavigationUI.navigateUp(navController, appBarConfiguration);
        }
        return handled || super.onSupportNavigateUp();
    }
}
