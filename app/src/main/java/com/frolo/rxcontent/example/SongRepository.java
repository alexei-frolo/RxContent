package com.frolo.rxcontent.example;


import android.content.ContentResolver;
import android.content.Context;

import com.frolo.rxcontent.RxAdapter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Flowable;

final class SongRepository {
    private final ContentResolver mResolver;
    private final Executor mQueryExecutor;

    SongRepository(Context ctx) {
        mResolver = ctx.getContentResolver();
        mQueryExecutor = Executors.newCachedThreadPool();
    }

    Flowable<List<Song>> getAllSongs() {
        return RxAdapter.query(mResolver, Song.URI, Song.PROJECTION,
                null, null, null, mQueryExecutor, Song.BUILDER);
    }
}
