package com.frolo.rxcontent;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

final class Playlist {

    final long id;
    final String name;

    private Playlist(long id, String name) {
        this.id = id;
        this.name = name;
    }

    static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    static final String[] PROJECTION = {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
    };

    static final CursorMapper<Playlist> CURSOR_MAPPER = new CursorMapper<Playlist>() {
        @Override
        public Playlist map(Cursor cursor) {
            long id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]));
            String name = cursor.getString(cursor.getColumnIndex(PROJECTION[1]));
            return new Playlist(id, name);
        }
    };

}
