package com.frolo.rxcontent.example;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.rxcontent.Builder;

final class Song {
    final long id;
    final String title;
    final String source;

    private Song(long id, String title, String source) {
        this.id = id;
        this. title = title;
        this.source = source;
    }

    @Override
    public String toString() {
        return "Song[" + source + "]";
    }

    static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA};

    static final Builder<Song> BUILDER = new Builder<Song>() {
        @Override
        public Song build(Cursor cursor) {
            long id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]));
            String title = cursor.getString(cursor.getColumnIndex(PROJECTION[1]));
            String source = cursor.getString(cursor.getColumnIndex(PROJECTION[2]));
            return new Song(id, title, source);
        }
    };
}
