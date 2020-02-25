package com.frolo.rxcontent;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public final class RxContent {

    private static final Object NOTHING = new Object();

    /**
     * Holder for a {@link ObserverHandler#sInstance}.
     * This handler is used for dispatching uri changes in {@link ContentResolver}.
     * @see ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)
     *
     * The global instance is supposed to be lazy initialized.
     */
    static class ObserverHandler {
        final static Handler sInstance;
        static {
            HandlerThread thread = new HandlerThread("RxContentObserver");
            thread.start();
            sInstance = new Handler(thread.getLooper());
        }
    }

    /*No instances*/
    private RxContent() {
    }

    /**
     * Checks that <code>cursor</code> returned by the query to <code>uri</code> is not null.
     * If the cursor is null then this throws {@link NullPointerException} with an appropriate message.
     * @param cursor returned by the query
     * @param uri queried uri
     * @throws NullPointerException if <code>cursor</code> is null
     */
    private static void checkCursor(Cursor cursor, Uri uri) {
        if (cursor == null) {
            throw new NullPointerException("Cursor of the query to " + uri + " is null");
        }
    }

    /**
     * Creates a flowable that emits {@link RxContent#NOTHING} each time the specified <code>uri</code> changes.
     * On the subscribe, {@link RxContent#NOTHING} is emitted at least once.
     * <code>uri</code> is observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The first object is not dispatched on a particular thread, but on the thread of the subscription.
     * Objects emitted on Uri changes are dispatched on {@link ObserverHandler#sInstance} thread.
     *
     * @param contentResolver to register Uri observer
     * @param uri to observe data changes
     * @return flowable source
     */
    public static Flowable<Object> createFlowable(
            final ContentResolver contentResolver,
            final Uri uri
    ) {
        return Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(final FlowableEmitter<Object> emitter) {
                if (!emitter.isCancelled()) {
                    final boolean notifyForDescendants = true;
                    final ContentObserver trigger = new ContentObserver(ObserverHandler.sInstance) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            if (!emitter.isCancelled()) {
                                emitter.onNext(NOTHING);
                            }
                        }
                    };

                    contentResolver.registerContentObserver(
                            uri,
                            notifyForDescendants,
                            trigger);

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            contentResolver.unregisterContentObserver(trigger);
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(NOTHING);
                }
            }
        }, BackpressureStrategy.LATEST);
    }

    /**
     * Creates a flowable that emits {@link RxContent#NOTHING} each time a uri from the specified <code>uris</code> collection changes.
     * On the subscribe, {@link RxContent#NOTHING} is emitted at least once.
     * <code>uris</code> are observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The first object is not dispatched on a particular thread, but on the thread of the subscription.
     * Objects emitted on Uri changes are dispatched on {@link ObserverHandler#sInstance} thread.
     *
     * @param contentResolver to register Uri observers
     * @param uris to observe data changes
     * @return flowable source
     */
    public static Flowable<Object> createFlowable(
            final ContentResolver contentResolver,
            final List<Uri> uris
    ) {
        return Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(final FlowableEmitter<Object> emitter) {
                if (!emitter.isCancelled()) {
                    final boolean notifyForDescendants = true;
                    final List<ContentObserver> triggers = new ArrayList<>(uris.size());

                    for (Uri uri : uris) {
                        final ContentObserver trigger = new ContentObserver(ObserverHandler.sInstance) {
                            @Override
                            public void onChange(boolean selfChange, Uri uri) {
                                if (!emitter.isCancelled()) {
                                    emitter.onNext(NOTHING);
                                }
                            }
                        };

                        contentResolver.registerContentObserver(
                                uri,
                                notifyForDescendants,
                                trigger);

                        triggers.add(trigger);
                    }

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            for (ContentObserver trigger : triggers) {
                                contentResolver.unregisterContentObserver(trigger);
                            }
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(NOTHING);
                }
            }
        }, BackpressureStrategy.LATEST);
    }

    /**
     * Creates a flowable that emits objects of type {@link T} returned by <code>callable</code> query.
     * The query is triggered the first time when subscribing and then each time the specified <code>uri</code> is changed.
     *
     * <code>uri</code> is observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The query is performed on <code>scheduler</code> thread.
     *
     * @param contentResolver to register Uri observer
     * @param uri to observe data changes
     * @param scheduler on which the query is performed
     * @param callable query
     * @return flowable source
     */
    public static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final Uri uri,
            final Scheduler scheduler,
            final Callable<T> callable
    ) {
        final Maybe<T> maybe = Maybe.fromCallable(callable);
        return createFlowable(contentResolver, uri)
                .subscribeOn(scheduler)
                .unsubscribeOn(scheduler)
                .observeOn(scheduler)
                .flatMapMaybe(new Function<Object, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(Object o) {
                        return maybe;
                    }
                });
    }

    /**
     * Creates same flowable as {@link RxContent#createFlowable(ContentResolver, Uri, Scheduler, Callable)}
     * passing as the scheduler {@link Schedulers#from(Executor)} from <code>queryExecutor</code>.
     *
     * @param contentResolver @see {@link RxContent#createFlowable(ContentResolver, Uri, Scheduler, Callable)}
     * @param uri @see {@link RxContent#createFlowable(ContentResolver, Uri, Scheduler, Callable)}
     * @param queryExecutor from which the query scheduler is created
     * @param callable @see {@link RxContent#createFlowable(ContentResolver, Uri, Scheduler, Callable)}
     * @return flowable source
     */
    public static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final Uri uri,
            final Executor queryExecutor,
            final Callable<T> callable
    ) {
        final Scheduler scheduler = Schedulers.from(queryExecutor);
        return createFlowable(contentResolver, uri, scheduler, callable);
    }

    /**
     * Creates a flowable that emits objects of type {@link T} returned by <code>callable</code> query.
     * The query is triggered the first time when subscribing and then each time a uri from the specified <code>uris</code> collection changes.
     *
     * <code>uris</code> are observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The query is performed on <code>scheduler</code> thread.
     *
     * @param contentResolver to register Uri observers
     * @param uris to observe data changes
     * @param scheduler on which the query is performed
     * @param callable query
     * @return flowable source
     */
    public static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final List<Uri> uris,
            final Scheduler scheduler,
            final Callable<T> callable
    ) {
        final Maybe<T> maybe = Maybe.fromCallable(callable);
        return createFlowable(contentResolver, uris)
                .subscribeOn(scheduler)
                .unsubscribeOn(scheduler)
                .observeOn(scheduler)
                .flatMapMaybe(new Function<Object, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(Object o) {
                        return maybe;
                    }
                });
    }

    /**
     * Creates same flowable as {@link RxContent#createFlowable(ContentResolver, List, Scheduler, Callable)}
     * passing as the scheduler {@link Schedulers#from(Executor)} from <code>queryExecutor</code>.
     *
     * @param contentResolver @see {@link RxContent#createFlowable(ContentResolver, List, Scheduler, Callable)}
     * @param uris @see {@link RxContent#createFlowable(ContentResolver, List, Scheduler, Callable)}
     * @param queryExecutor from which the query scheduler is created
     * @param callable @see {@link RxContent#createFlowable(ContentResolver, List, Scheduler, Callable)}
     * @return flowable source
     */
    public static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final List<Uri> uris,
            final Executor queryExecutor,
            final Callable<T> callable
    ) {
        final Scheduler scheduler = Schedulers.from(queryExecutor);
        return createFlowable(contentResolver, uris, scheduler, callable);
    }

    /**
     * Creates a flowable that emits lists of objects of type {@link T} returned by the query to <code>uri</code>.
     * The query is constructed from the cursor returned by {@link ContentResolver#query(Uri, String[], String, String[], String)} method
     * and mapped with <code>builder</code> to objects of type {@link T}.
     * 
     * The query is triggered the first time when subscribing and then each time a uri from the specified <code>uris</code> collection changes.
     *
     * <code>uris</code> are observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The query is performed on <code>queryExecutor</code> thread.
     *
     * @param resolver to perform the query and observe Uri changes
     * @param uri to query and observe
     * @param projection @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param selection @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param selectionArgs @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param sortOrder @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param queryExecutor on which the query is performed
     * @param cursorMapper for mapping the query cursor to objects of type {@link T}
     * @param <T> type of the query
     * @return flowable source
     */
    public static <T> Flowable<List<T>> query(
            final ContentResolver resolver,
            final Uri uri,
            final String[] projection,
            final String selection,
            final String[] selectionArgs,
            final String sortOrder,
            final Executor queryExecutor,
            final CursorMapper<T> cursorMapper
    ) {
        return createFlowable(
                resolver,
                uri,
                queryExecutor,
                new Callable<List<T>>() {
                    @Override
                    public List<T> call() {
                        Cursor cursor = resolver.query(
                                uri, projection, selection, selectionArgs, sortOrder);

                        checkCursor(cursor, uri);

                        List<T> items = new ArrayList<>(cursor.getCount());

                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    items.add(cursorMapper.map(cursor));
                                } while (cursor.moveToNext());
                            }
                        } finally {
                            cursor.close();
                        }

                        return items;
                    }
                }
        );
    }

    /**
     * Creates a flowable that emits objects of type {@link T} returned by the query to <code>uri</code>.
     * The query is constructed from the cursor returned by {@link ContentResolver#query(Uri, String[], String, String[], String)} method
     * and mapped with <code>builder</code> to objects of type {@link T}.
     *
     * The query is triggered the first time when subscribing and then each time a uri from the specified <code>uris</code> collection changes.
     *
     * <code>uris</code> are observed using {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method.
     * On the source cancellation, observation is terminated using {@link ContentResolver#unregisterContentObserver(ContentObserver)} method.
     *
     * The query is performed on <code>queryExecutor</code> thread.
     *
     * @param resolver to perform the query and observe Uri changes
     * @param uri to query and observe
     * @param projection @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param itemId @see {@link ContentResolver#query(Uri, String[], String, String[], String)}
     * @param queryExecutor on which the query is performed
     * @param cursorMapper for mapping the query cursor to objects of type {@link T}
     * @param <T> type of the query
     * @return flowable source
     */
    public static <T> Flowable<T> queryItem(
            final ContentResolver resolver,
            final Uri uri,
            final String[] projection,
            final long itemId,
            final Executor queryExecutor,
            final CursorMapper<T> cursorMapper
    ) {
        final Uri itemUri = ContentUris.withAppendedId(uri, itemId);
        return createFlowable(
                resolver,
                itemUri,
                queryExecutor,
                new Callable<T>() {
                    @Override
                    public T call() {
                        Cursor cursor = resolver.query(
                                itemUri, projection, null, null, null);

                        checkCursor(cursor, uri);

                        T item = null;

                        try {
                            if (cursor.moveToFirst()) {
                                item = cursorMapper.map(cursor);
                            }
                        } finally {
                            cursor.close();
                        }

                        if (item == null) {
                            throw new NullPointerException("Item not found: uri=" + uri);
                        }

                        return item;
                    }
                }
        );
    }
}
