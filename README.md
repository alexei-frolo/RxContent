# RxContent
Simple and lightweight library for adapting ContentResolver/SharedPreferences in Android to RxJava.

The library is supposed to wrap queries to ContentResolver and SharedPreferences into Rx source objects of type Flowable.
For example, this may be useful if you want to observe changes in Contacts storage.
So there are two packages: **rxcontent** package for adapting ContentResolver and **rxpreference** package for adapting SharedPreferences.

## Getting started

### Setting up the dependency

The first step is to include the library in your project.
In Gradle, this looks like:

```groovy
implementation 'com.github.alexei-frolo:RxContent:1.0.2'
```

### RxContent example

Here is an example of how you integrate **rxcontent** package in your project.
Image, you want to retrieve all songs on your device: With RxContent, this may be done as shown below:

```java
package com.frolo.rxcontent.example;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.rxcontent.CursorMapper;

final class Song {
    final long id;
    final String title;

    private Song(long id, String title) {
        this.id = id;
        this.title = title;
    }

    static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE
            };

    static final Builder<Song> CURSOR_MAPPER = new Builder<Song>() {
        @Override
        public Song build(Cursor cursor) {
            long id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]));
            String title = cursor.getString(cursor.getColumnIndex(PROJECTION[1]));
            return new Song(id, title);
        }
    };
}

...

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ContentResolver resolver = getContentResolver();
    Executor executor = Executors.newSingleThreadExecutor();
    Flowable<List<Song>> source = RxAdapter.query(resolver, Song.URI, Song.PROJECTION,
                    null, null, null, executor, Song.CURSOR_MAPPER);

    source.subscribe(new Consumer<List<Song>>() {
        @Override
        public void accept(List<Song> songs) throws Exception {
            // Got all songs on the device
        }
    });
}

...
```

### RxPreference example

Here is an example of how you integrate **rxpreference** package in your project.
Let's say you have a SharedPreferences instance that contains some 'flag' key of type boolean.
To observe changes of the value of the 'flag' key you may use the following code:


```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SharedPreferences prefs =
        getSharedPreferences("com.frolo.rxcontent.example", Context.MODE_PRIVATE);
    RxPreference<Boolean> flag = RxPreference.ofBoolean(prefs, "flag");
    Flowable<Boolean> source = flag.get(/*default value*/ false);
    source.subscribe(new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            // Got current value of the flag
        }
    });
}
```
