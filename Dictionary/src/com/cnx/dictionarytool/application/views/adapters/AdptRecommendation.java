package com.cnx.dictionarytool.application.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cnx.dictionarytool.R;
import com.cnx.dictionarytool.application.views.models.DictonaryData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdptRecommendation extends RecyclerView.Adapter<AdptRecommendation.MyViewHolder> {

    private List<DictonaryData> moviesList;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
        }
    }


    public AdptRecommendation(List<DictonaryData> moviesList) {
        this.moviesList = moviesList;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DictonaryData movie = moviesList.get(position);
        holder.title.setText(movie.getTitle());
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
