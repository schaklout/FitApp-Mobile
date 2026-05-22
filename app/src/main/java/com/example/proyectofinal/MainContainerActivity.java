package com.example.proyectofinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainContainerActivity extends AppCompatActivity {

    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_RUTINAS = "rutinas";
    private static final String TAG_PROGRESO = "progreso";
    private static final String TAG_PERFIL = "perfil";

    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InsetsHelper.enable(this);
        setContentView(R.layout.activity_container);
        InsetsHelper.padTop(findViewById(R.id.appHeader));
        InsetsHelper.liftBottomNav(findViewById(R.id.bottom_navigation));

        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            String tag = null;

            if (id == R.id.nav_inicio) {
                tag = TAG_DASHBOARD;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) fragment = new DashboardFragment();
            } else if (id == R.id.nav_rutinas) {
                tag = TAG_RUTINAS;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) fragment = new RutinasFragment();
            } else if (id == R.id.nav_progreso) {
                tag = TAG_PROGRESO;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) fragment = new ProgresoFragment();
            } else if (id == R.id.nav_perfil) {
                tag = TAG_PERFIL;
                fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) fragment = new PerfilFragment();
            } else {
                return false;
            }

            Fragment current = fragmentManager.findFragmentById(R.id.fragment_container);
            if (current != null) {
                fragmentManager.beginTransaction().hide(current).commit();
            }

            if (fragment.isAdded()) {
                fragmentManager.beginTransaction().show(fragment).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.fragment_container, fragment, tag).commit();
            }

            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        } else {
            bottomNav.setSelectedItemId(bottomNav.getSelectedItemId());
        }
    }

    public void navigateTo(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }
}
