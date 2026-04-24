    package com.example.okazio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

    public class MainActivity extends AppCompatActivity {
        private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        TextView pageTitle = findViewById(R.id.pagetitle);
        TextView subTitle = findViewById(R.id.sub_title);
        fab = findViewById(R.id.fab);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new StatFragment())
                .commit();


        fab.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_add);
        });
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            String title;
            String subTitleText;

            int id = item.getItemId();

            if (id == R.id.nav_stat){
                fragment = new StatFragment();
                title = "Tableau de bord";
                subTitleText = "Inventaire en temps réel des matériels";
            } else if (id == R.id.nav_add) {
                fragment = new AddFragment();
                title = "Ajout matériel";
                subTitleText = "Entrer les informations du matériel";
            } else {
                fragment = new ListFragment();
                title = "Inventaires";
                subTitleText = "Liste de tous les matériels";
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            pageTitle.setText(title);
            subTitle.setText(subTitleText);

            visibilityFloatingButton(fragment);
            return true;
        });
    }

    private void visibilityFloatingButton(Fragment currentFragment){
        if (currentFragment instanceof AddFragment){
            fab.hide();
        }else {
            fab.show();
        }
    }

    private void navigationAddByFloatingButton(View view){

    }
}