package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {

    private final OnboardingSlide[] slides;

    public OnboardingAdapter(OnboardingSlide[] slides) {
        this.slides = slides;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        OnboardingSlide slide = slides[position];
        holder.icon.setImageResource(slide.iconRes);
        holder.title.setText(slide.title);
        holder.description.setText(slide.description);
    }

    @Override
    public int getItemCount() {
        return slides.length;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView description;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.slideIcon);
            title = itemView.findViewById(R.id.slideTitle);
            description = itemView.findViewById(R.id.slideDescription);
        }
    }

    public static class OnboardingSlide {
        final int iconRes;
        final String title;
        final String description;

        public OnboardingSlide(int iconRes, String title, String description) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
        }
    }
}
