package com.frolo.rxcontent;

import android.database.Cursor;


/**
 * This builds objects of type <code>T</code> using {@link Cursor}.
 */
public interface CursorMapper<T> {

    /**
     * Builds an object of type <code>T</code> using <code>cursor</code>.
     * It is safe to access the cursor, since the cursor must always be checked for null before calling this method.
     *
     * @param cursor from which to build an object
     * @return an object of type T
     */
    T map(Cursor cursor);
}
