package com.frolo.rxcontent.example;

import android.content.Context;
import android.content.SharedPreferences;

import com.frolo.rxpreference.RxPreference;


final class PrefsRepository {

    private static final String PREFS_NAME = "com.frolo.rxcontent.example";

    final RxPreference<Boolean> flag;

    PrefsRepository(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        flag = RxPreference.ofBoolean(prefs, "flag");
    }
}
