package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.proyectofinal.OnboardingAdapter.OnboardingSlide;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView btnSkip;
    private TextView btnNext;
    private LinearLayout dotsContainer;
    private ImageView[] dots;

    private final OnboardingSlide[] slides = {
            new OnboardingSlide(
                    R.drawable.ic_onboarding_train,
                    "ENTRENA",
                    "Accede a rutinas diseñadas para ti. Ejercicios guiados con temporizador y seguimiento en tiempo real."
            ),
            new OnboardingSlide(
                    R.drawable.ic_onboarding_progress,
                    "PROGRESA",
                    "Visualiza tu evolución con gráficos detallados. Registra tu peso, racha de días y nivel de actividad."
            ),
            new OnboardingSlide(
                    R.drawable.ic_onboarding_trophy,
                    "DOMINA",
                    "Supera tus límites. Completa entrenamientos, celebra tus logros y conviértete en tu mejor versión."
            )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InsetsHelper.enable(this);
        setContentView(R.layout.activity_onboarding);
        InsetsHelper.padBoth(findViewById(R.id.onboardingRoot));

        viewPager = findViewById(R.id.viewPager);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);
        dotsContainer = findViewById(R.id.dotsContainer);

        viewPager.setAdapter(new OnboardingAdapter(slides));

        createDots();
        updateDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == slides.length - 1) {
                    btnNext.setText("Empezar");
                } else {
                    btnNext.setText("Siguiente");
                }
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < slides.length - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });
    }

    private void createDots() {
        dots = new ImageView[slides.length];
        for (int i = 0; i < slides.length; i++) {
            dots[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            dots[i].setLayoutParams(params);
            dotsContainer.addView(dots[i]);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void finishOnboarding() {
        TokenManager.setOnboardingCompleted(this, true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
