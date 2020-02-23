package com.frolo.rxcontent.example;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends Activity {

    private static final String LOG_TAG = "RxContentExample";

    private static final String READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    private static final int RC_RETRIEVE_SONGS = 1337;

    private ListView mListView;
    private SongAdapter mAdapter;

    private CheckBox mCheckBoxFlag;
    private TextView mTextViewFlag;

    private SongRepository mSongRepository;
    private PrefsRepository mPrefsRepository;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.lv_songs);
        mAdapter = new SongAdapter();
        mListView.setAdapter(mAdapter);

        mCheckBoxFlag = findViewById(R.id.chb_flag);
        mTextViewFlag = findViewById(R.id.tv_flag_status);

        mSongRepository = new SongRepository(this);
        mPrefsRepository = new PrefsRepository(this);

        fetchSongs();

        observeFlag();
    }

    private boolean isReadStoragePermissionGranted() {
        return getPackageManager().checkPermission(READ_STORAGE_PERMISSION, getPackageName())
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    RC_RETRIEVE_SONGS);
        }
    }

    private void fetchSongs() {
        if (!isReadStoragePermissionGranted()) {
            requestReadStoragePermission();
            return;
        }

        if (mDisposable != null) mDisposable.dispose();
        mDisposable = mSongRepository.getAllSongs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs) {
                        Log.d(LOG_TAG, String.valueOf(songs));
                        mAdapter.setItems(songs);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(LOG_TAG, "", throwable);
                        if (throwable instanceof SecurityException) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                        RC_RETRIEVE_SONGS);
                            }
                        }
                    }
                });
    }

    private void observeFlag() {
        mCheckBoxFlag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefsRepository.flag.set(isChecked)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });

        mPrefsRepository.flag.get(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        mCheckBoxFlag.setChecked(aBoolean);

                        final String statusText;
                        if (aBoolean) statusText = "Flag is true";
                        else statusText = "Flag is false";
                        mTextViewFlag.setText(statusText);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    fetchSongs();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) mDisposable.dispose();
        super.onDestroy();
    }
}
