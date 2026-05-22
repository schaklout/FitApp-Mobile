package com.example.proyectofinal;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

public class NotificationsSheet extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.TOP);

            int statusBarH = 0;
            int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (id > 0) statusBarH = getResources().getDimensionPixelSize(id);
            int headerH = (int) (64 * getResources().getDisplayMetrics().density);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = statusBarH + headerH;
            window.setAttributes(params);

            window.getAttributes().windowAnimations = R.style.NotifAnim;
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.NotifDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_notifications, container, false);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        LinearLayout list = view.findViewById(R.id.notifList);
        MaterialButton btnMarkAll = view.findViewById(R.id.btnMarkAllRead);

        String[][] data = {
            {"Entrenamiento completado", "Has finalizado tu rutina de hoy. ¡Buen trabajo!", "Hace 2h"},
            {"Nuevo logro desbloqueado", "Has alcanzado 7 días de racha.", "Hace 5h"},
            {"Recordatorio", "No olvides registrar tu peso de esta semana.", "Ayer"}
        };

        for (String[] n : data) {
            View item = inflater.inflate(R.layout.item_notification, list, false);
            TextView title = item.findViewById(R.id.tvNotifTitle);
            TextView body = item.findViewById(R.id.tvNotifBody);
            TextView time = item.findViewById(R.id.tvNotifTime);
            View dot = item.findViewById(R.id.dotUnread);

            title.setText(n[0]);
            body.setText(n[1]);
            time.setText(n[2]);

            item.setOnClickListener(v -> {
                dot.setVisibility(View.INVISIBLE);
                title.setTextColor(Color.parseColor("#888888"));
            });

            list.addView(item);
        }

        btnMarkAll.setOnClickListener(v -> {
            for (int i = 0; i < list.getChildCount(); i++) {
                View child = list.getChildAt(i);
                View dot = child.findViewById(R.id.dotUnread);
                TextView title = child.findViewById(R.id.tvNotifTitle);
                if (dot != null) dot.setVisibility(View.INVISIBLE);
                if (title != null) title.setTextColor(Color.parseColor("#888888"));
            }
        });

        return view;
    }
}
