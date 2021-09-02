package com.example.roomdbwithnavigation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.example.roomdbwithnavigation.R;
import com.example.roomdbwithnavigation.adapter.ArtAdapter;
import com.example.roomdbwithnavigation.database.AppDatabase;
import com.example.roomdbwithnavigation.database.dao.ArtDao;
import com.example.roomdbwithnavigation.databinding.FragmentArtBinding;
import com.example.roomdbwithnavigation.model.Art;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ArtFragment extends Fragment {
    private FragmentArtBinding binding;
    private ArtDao artDao;
    private List<Art> artList;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = Room.databaseBuilder(
                getActivity().getApplicationContext(),
                AppDatabase.class,
                AppDatabase.DATABASE_NAME
        ).build();

        artDao = db.artDao();
        setHasOptionsMenu(true);

        compositeDisposable = new CompositeDisposable();

        artList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArtBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        compositeDisposable.add(
                artDao.all()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleData)
        );
    }

    private void handleData(List<Art> arts) {
        this.artList = arts;
        ArtAdapter artAdapter = new ArtAdapter(artList);
        binding.recyclerView.setAdapter(artAdapter);
        artAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.upload_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_upload:
                ArtFragmentDirections.ActionArtFragmentToUploadFragment action = ArtFragmentDirections.actionArtFragmentToUploadFragment();
                action.setArtId(0);
                action.setIsNew(true);
                Navigation.findNavController(requireView()).navigate(action);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}