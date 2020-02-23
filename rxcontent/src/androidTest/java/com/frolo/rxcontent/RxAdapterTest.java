package com.frolo.rxcontent;

import android.Manifest;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.test.platform.app.InstrumentationRegistry;

import com.frolo.BlockingExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

import static org.junit.Assert.assertTrue;


@RunWith(JUnit4.class)
public class RxAdapterTest {

    /**
     * Max time that {@link ContentResolver} may take to dispatch Uri changes to its observers.
     */
    private static final long CONTENT_UPDATE_TIMEOUT = 1_000;

    private static final String[] PERMISSIONS =
            {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

//    @Rule
//    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(PERMISSIONS);

    private ContentResolver mResolver;

    private Executor mQueryExecutor;

    /**
     * Executes <code>cmd</code> last in {@link RxAdapter#ObserverHandler#sInstance}'s looper.
     * Do call this when you want to run some code after all the posted callbacks are performed in {@link RxAdapter#ObserverHandler#sInstance}.
     * For example, if you want to wait until
     * {@link android.database.ContentObserver#onChange(boolean, Uri)} gets called.
     * @param cmd callback to run.
     */
    private void runOnNextLoop(Runnable cmd) {
        RxAdapter.ObserverHandler.sInstance.post(cmd);
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getContext();
        mResolver = context.getContentResolver();

        mQueryExecutor = BlockingExecutor.getInstance();
    }

    @Test
    public void test_createFlowable() {
        final Object waiter = new Object();

        // Testing on playlists in the media store
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Flowable<Object> source = RxAdapter.createFlowable(mResolver, uri);
        final TestSubscriber<Object> subscriber = TestSubscriber.create();

        source.subscribe(subscriber);

        subscriber.assertSubscribed();

        // At least one item should fire
        subscriber.assertValueCount(1);

        mResolver.notifyChange(uri, null);

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueCount(2);

                mResolver.notifyChange(uri, null);
            }
        });

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueCount(3);

                subscriber.cancel();
            }
        });

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                assertTrue(subscriber.isCancelled());

                // Notify that the last loop command completes
                synchronized (waiter) {
                    waiter.notify();
                }
            }
        });

        // Wait until the last loop command completes
        try {
            synchronized (waiter) {
                waiter.wait();
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void test_query() {
        final Object waiter = new Object();

        // Testing on playlists in the media store
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Flowable<List<Playlist>> source = RxAdapter.query(
                mResolver, uri, Playlist.PROJECTION, null, null, null, mQueryExecutor, Playlist.BUILDER);
        final TestSubscriber<List<Playlist>> subscriber = TestSubscriber.create();

        source.subscribe(subscriber);

        subscriber.assertSubscribed();

        // At least one item should fire
        subscriber.assertValueCount(1);

        mResolver.notifyChange(uri, null);

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueCount(2);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Playlists.NAME, "test_name1");
                mResolver.insert(Playlist.URI, values);
            }
        });

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueCount(3);

                subscriber.cancel();
            }
        });

        sleepSafely(CONTENT_UPDATE_TIMEOUT);

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                assertTrue(subscriber.isCancelled());

                // Notify that the last loop command completes
                synchronized (waiter) {
                    waiter.notify();
                }
            }
        });

        // Wait until the last loop command completes
        try {
            synchronized (waiter) {
                waiter.wait();
            }
        } catch (InterruptedException ignored) {
        }
    }

}
