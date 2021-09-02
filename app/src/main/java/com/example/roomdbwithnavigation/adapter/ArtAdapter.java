package com.example.roomdbwithnavigation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roomdbwithnavigation.databinding.ArtRowItemBinding;
import com.example.roomdbwithnavigation.model.Art;
import com.example.roomdbwithnavigation.ui.fragment.ArtFragmentDirections;

import java.util.List;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    private List<Art> artList;

    public ArtAdapter(List<Art> artList) {
        this.artList = artList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArtRowItemBinding binding = ArtRowItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );

        return new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        ArtRowItemBinding binding = holder.binding;

        binding.textView.setText(artList.get(position).artName);

        binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArtFragmentDirections.ActionArtFragmentToUploadFragment action = ArtFragmentDirections.actionArtFragmentToUploadFragment();
                action.setIsNew(false);
                action.setArtId(artList.get(position).id);

                Navigation.findNavController(binding.getRoot()).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder {
        private ArtRowItemBinding binding;

        public ArtHolder(@NonNull ArtRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
