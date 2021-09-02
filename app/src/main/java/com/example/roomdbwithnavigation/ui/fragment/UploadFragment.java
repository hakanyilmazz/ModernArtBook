package com.example.roomdbwithnavigation.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.example.roomdbwithnavigation.database.AppDatabase;
import com.example.roomdbwithnavigation.database.dao.ArtDao;
import com.example.roomdbwithnavigation.databinding.FragmentUploadBinding;
import com.example.roomdbwithnavigation.model.Art;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UploadFragment extends Fragment {
    private FragmentUploadBinding binding;
    private ArtDao artDao;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Bitmap selectedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = Room.databaseBuilder(
                getActivity().getApplicationContext(),
                AppDatabase.class,
                AppDatabase.DATABASE_NAME
        ).build();

        artDao = db.artDao();

        registerActivityResults();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            UploadFragmentArgs args = UploadFragmentArgs.fromBundle(bundle);

            if (args.getIsNew()) {
                binding.saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveImage(view);
                    }
                });
                binding.imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectImage(view);
                    }
                });
            } else {
                int selectedArtId = args.getArtId();
                setViewsForReadable();
                setViewsFromDatabase(selectedArtId);
            }
        }
    }

    private void registerActivityResults() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(intentToGallery);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intentData = result.getData();

                    if (intentData != null) {
                        Uri imageData = intentData.getData();

                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), imageData);
                                    selectedImage = ImageDecoder.decodeBitmap(source);
                                } else {
                                    selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageData);
                                }

                                binding.imageView2.setImageBitmap(selectedImage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setViewsFromDatabase(int selectedArtId) {
        compositeDisposable.add(
                artDao.getArtById(selectedArtId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleData)
        );
    }

    private void handleData(Art art) {
        byte[] imageData = art.image;
        Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        binding.imageView2.setImageBitmap(image);
        binding.artNameText.setText(art.artName);
        binding.painterNameText.setText(art.painterName);
        binding.yearText.setText(String.valueOf(art.year));
    }

    private void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission Needed For Gallery!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("GIVE PERMISSION", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intentToGallery);
        }
    }

    private void saveImage(View view) {
        if (selectedImage != null) {
            String artName = binding.artNameText.getText().toString().trim();
            String painterName = binding.painterNameText.getText().toString().trim();
            String yearText = binding.yearText.getText().toString().trim();

            if (TextUtils.isEmpty(artName) || TextUtils.isEmpty(painterName)
                    || TextUtils.isEmpty(yearText) || !TextUtils.isDigitsOnly(yearText)
            ) {
                return;
            }

            Art art = new Art();

            art.artName = artName;
            art.painterName = painterName;
            art.year = Integer.parseInt(yearText);

            Bitmap smallImage = makeSmallerImage(selectedImage, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);

            art.image = outputStream.toByteArray();

            compositeDisposable.add(
                    artDao.insert(art)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::closeFragment)
            );
        }
    }

    private void closeFragment() {
        NavDirections action = UploadFragmentDirections.actionUploadFragmentToArtFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    private Bitmap makeSmallerImage(Bitmap selectedImage, int max) {
        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();
        float ratio = (float) width / (float) height;

        if (ratio > 1) {
            width = max;
            height = (int) (width / ratio);
        } else {
            height = max;
            width = (int) (height * ratio);
        }

        return Bitmap.createScaledBitmap(selectedImage, width, height, true);
    }

    private void setViewsForReadable() {
        binding.saveButton.setVisibility(View.GONE);
        binding.imageView2.setClickable(false);
        binding.artNameText.setFocusable(false);
        binding.painterNameText.setFocusable(false);
        binding.yearText.setFocusable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}