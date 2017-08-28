package com.project.ada.silversparro.core;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.ada.silversparro.R;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/27/17.
 */

public class ClassSelectionAdapter extends RecyclerView.Adapter<ClassSelectionAdapter.ClassSelectionViewHolder> {

    List<String> data;
    ItemClickListener listener;

    public ClassSelectionAdapter(List<String> classes, ItemClickListener clickListener){
        this.data = classes;
        this.listener = clickListener;
    }

    @Override
    public ClassSelectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_select_list_item, parent, false);
        return new ClassSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassSelectionViewHolder holder, int position) {
        holder.bindData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ClassSelectionViewHolder extends RecyclerView.ViewHolder {

        TextView classNameView;
        View container;

        public ClassSelectionViewHolder(View itemView) {
            super(itemView);
            classNameView = (TextView) itemView.findViewById(R.id.tv_rect_class);
            container = itemView.findViewById(R.id.container_list_item);
        }

        public void bindData(final String className){
            classNameView.setText(className);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClassSelected(className);
                }
            });
        }
    }

    public interface ItemClickListener{
        void onClassSelected(String className);
    }
}
