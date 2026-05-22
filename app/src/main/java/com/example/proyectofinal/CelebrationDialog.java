package com.example.proyectofinal;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Random;

public class CelebrationDialog extends DialogFragment {

    private static final String[] ADJETIVOS = {"BESTIA", "MÁQUINA", "TITÁN", "FENÓMENO", "ÉLITE"};
    private final Random rnd = new Random();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_celebration, container, false);

        ConfettiView confetti = new ConfettiView(requireContext());
        ((ViewGroup) view).addView(confetti, 0);
        confetti.start();

        AnimatedBorderView border = view.findViewById(R.id.animatedBorder);
        border.startAnimation();

        TextView tvAdjetivo = view.findViewById(R.id.tvAdjetivo);
        tvAdjetivo.setText("¡" + ADJETIVOS[rnd.nextInt(ADJETIVOS.length)] + "!");

        view.findViewById(R.id.btnCerrarCelebracion).setOnClickListener(v -> dismiss());

        return view;
    }
}
