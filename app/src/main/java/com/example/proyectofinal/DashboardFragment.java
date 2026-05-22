package com.example.proyectofinal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private TextView tvCalorias, tvRutinas, tvPesoActual, tvImc;
    private LinearLayout dotsContainer;
    private ViewPager2 imageCarousel;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private int imageCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetsHelper.padTop(view.findViewById(R.id.dashboardScroll));

        tvCalorias = view.findViewById(R.id.tvCalorias);
        tvPesoActual = view.findViewById(R.id.tvPesoActual);
        tvRutinas = view.findViewById(R.id.tvRutinas);
        dotsContainer = view.findViewById(R.id.dotsContainer);
        imageCarousel = view.findViewById(R.id.imageCarousel);
        tvImc = view.findViewById(R.id.tvImc);

        setupCarousel();
        cargarDashboard();
    }

    private void setupCarousel() {
        List<Object> images = new ArrayList<>();
        images.add(R.drawable.banner_1);
        images.add(R.drawable.banner_2);
        images.add(R.drawable.banner_3);
        images.add(R.drawable.banner_4);
        images.add(R.drawable.banner_5);
        images.add(R.drawable.banner_6);

        setupCarouselWithSources(images);
    }

    private void setupCarouselWithSources(List<Object> imageSources) {
        imageCount = imageSources.size();
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageSources);
        imageCarousel.setAdapter(adapter);

        addDots(imageSources.size());
        highlightDot(0);

        imageCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                highlightDot(position);
            }
        });

        setupAutoSlide(imageCarousel, imageSources.size());
    }

    private void setupAutoSlide(ViewPager2 viewPager, int itemCount) {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (viewPager.getCurrentItem() + 1) % itemCount;
                viewPager.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 2000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    private void addDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dotsContainer.addView(dot, params);
        }
    }

    private void highlightDot(int index) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            ImageView dot = (ImageView) dotsContainer.getChildAt(i);
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                    i == index ? R.drawable.dot_active : R.drawable.dot_inactive));
        }
    }

    private void cargarDashboard() {
        ApiService.getDashboard(requireActivity(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Error al cargar dashboard", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject data = new JSONObject(json);

                        double peso = data.optDouble("peso", 0);
                        int entrenamientos = data.optInt("entrenamientos_completados", 0);
                        int calorias = data.optInt("calorias_quemadas", 0);
                        double imc = data.optDouble("imc", 0);

                        requireActivity().runOnUiThread(() -> {
                            tvPesoActual.setText(peso + " kg");
                            tvRutinas.setText(String.valueOf(entrenamientos));
                            tvCalorias.setText(String.valueOf(calorias));

                            String categoria;
                            if (imc < 18.5) categoria = "Bajo peso";
                            else if (imc < 25) categoria = "Normal";
                            else if (imc < 30) categoria = "Sobrepeso";
                            else categoria = "Obesidad";

                            tvImc.setText(imc + " (" + categoria + ")");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
