package com.frolo.rxpreference;

import android.content.SharedPreferences;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public final class RxPreference<T> {

    private final static Object NOTHING = new Object();

    /**
     * For storing {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener} triggers
     * to prevent them from GC.
     */
    private final Collection<SharedPreferences.OnSharedPreferenceChangeListener> mTriggers =
            new ArrayBlockingQueue<>(1);

    private final SharedPreferences mPreferences;
    private final String mKey;
    private final Executor mExecutor;
    private final PreferenceType mType;

    /**
     * Factory method for creating RxPreference of type Boolean.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type Boolean
     */
    public static RxPreference<Boolean> ofBoolean(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.BOOLEAN);
    }

    /**
     * Factory method for creating RxPreference of type Boolean.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type Boolean
     */
    public static RxPreference<Boolean> ofBoolean(SharedPreferences preferences, String key) {
        return ofBoolean(preferences, key, null);
    }

    /**
     * Factory method for creating RxPreference of type Integer.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type Integer
     */
    public static RxPreference<Integer> ofInt(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.INT);
    }

    /**
     * Factory method for creating RxPreference of type Boolean.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type Integer
     */
    public static RxPreference<Integer> ofInt(SharedPreferences preferences, String key) {
        return ofInt(preferences, key, null);
    }

    /**
     * Factory method for creating RxPreference of type Long.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type Long
     */
    public static RxPreference<Long> ofLong(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.LONG);
    }

    /**
     * Factory method for creating RxPreference of type Long.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type Long
     */
    public static RxPreference<Long> ofLong(SharedPreferences preferences, String key) {
        return ofLong(preferences, key, null);
    }

    /**
     * Factory method for creating RxPreference of type Float.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type Float
     */
    public static RxPreference<Float> ofFloat(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.FLOAT);
    }

    /**
     * Factory method for creating RxPreference of type Float.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type Float
     */
    public static RxPreference<Float> ofFloat(SharedPreferences preferences, String key) {
        return ofFloat(preferences, key, null);
    }

    /**
     * Factory method for creating RxPreference of type String.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type String
     */
    public static RxPreference<String> ofString(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.STRING);
    }

    /**
     * Factory method for creating RxPreference of type String.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type String
     */
    public static RxPreference<String> ofString(SharedPreferences preferences, String key) {
        return ofString(preferences, key, null);
    }

    /**
     * Factory method for creating RxPreference of type Set<String>.
     * Get/set operations are performed on {@link Schedulers#from(Executor)} scheduler created from <code>executor</code>.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @return RxPreference of type Set<String>
     */
    public static RxPreference<Set<String>> ofStringSet(SharedPreferences preferences, String key, Executor executor) {
        return new RxPreference<>(preferences, key, executor, PreferenceType.STRING_SET);
    }

    /**
     * Factory method for creating RxPreference of type Set<String>.
     * Get/set operations are performed on {@link Schedulers#io()} scheduler.
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @return RxPreference of type Set<String>
     */
    public static RxPreference<Set<String>> ofStringSet(SharedPreferences preferences, String key) {
        return ofStringSet(preferences, key, null);
    }

    //endregion

    /**
     * Constructor is private to prevent wrong combination of generic type <code>T</code> and preference type <code>type</code>.
     * To get an instance of RxPreference use one of the factory methods above.
     *
     * @param preferences for modifying and accessing the preference data
     * @param key of the preference
     * @param executor on which thread get/set operations are performed
     * @param type of the preference
     */
    private RxPreference(SharedPreferences preferences, String key, Executor executor, PreferenceType type) {
        this.mPreferences = preferences;
        this.mKey = key;
        this.mExecutor = executor;
        this.mType = type;
    }

    /**
     * Returns scheduler to perform get/set operations on.
     * @return scheduler to perform get/set operations on.
     */
    private Scheduler getScheduler() {
        if (mExecutor != null) return Schedulers.from(mExecutor);
        else return Schedulers.io();
    }

    //region blocking methods
    /*package*/ boolean blockingExists() {
        return mPreferences.contains(mKey);
    }

    /**
     * Blocking gets current value of the preference.
     * Returns null if the preference data does not exists.
     * @return current value of the preference
     */
    /*package*/ T blockingGet() {
        return blockingGet(null);
    }

    /**
     * Blocking gets current value of the preference.
     * @param defaultValue to be returned if the preference has no value
     * @return current value of the preference, or <code>defaultValue</code> if no value present
     */
    /*package*/ T blockingGet(T defaultValue) {
        if (!mPreferences.contains(mKey)) {
            return defaultValue;
        }

        switch(mType) {
            case BOOLEAN: {
                boolean safeDefaultValue = defaultValue != null ? (Boolean) defaultValue : false;
                return (T) Boolean.valueOf(mPreferences.getBoolean(mKey, safeDefaultValue));
            }
            case INT: {
                int safeDefaultValue = defaultValue != null ? (Integer) defaultValue : 0;
                return (T) Integer.valueOf(mPreferences.getInt(mKey, safeDefaultValue));
            }
            case LONG: {
                long safeDefaultValue = defaultValue != null ? (Long) defaultValue : 0;
                return (T) Long.valueOf(mPreferences.getLong(mKey, safeDefaultValue));
            }
            case FLOAT: {
                float safeDefaultValue = defaultValue != null ? (Float) defaultValue : 0;
                return (T) Float.valueOf(mPreferences.getFloat(mKey, safeDefaultValue));
            }
            case STRING: {
                String safeDefaultValue = defaultValue != null ? (String) defaultValue : null;
                return (T) String.valueOf(mPreferences.getString(mKey, safeDefaultValue));
            }
            case STRING_SET: {
                Set<String> safeDefaultValue = defaultValue != null ? (Set<String>) defaultValue : null;
                return (T) mPreferences.getStringSet(mKey, safeDefaultValue);
            }
            default: {
                throw new IllegalStateException("Cannot handle preference type: " + mType);
            }
        }
    }

    /**
     * Blocking sets the preference value.
     * @param value to be set in the preference
     */
    /*package*/ void blockingSet(T value) {
        if (value == null) {
            mPreferences.edit().remove(mKey).apply();
            return;
        }

        switch(mType) {
            case BOOLEAN: {
                mPreferences.edit().putBoolean(mKey, (Boolean) value).apply();
                break;
            }
            case INT: {
                mPreferences.edit().putInt(mKey, (Integer) value).apply();
                break;
            }
            case LONG: {
                mPreferences.edit().putLong(mKey, (Long) value).apply();
                break;
            }
            case FLOAT: {
                mPreferences.edit().putFloat(mKey, (Float) value).apply();
                break;
            }
            case STRING: {
                mPreferences.edit().putString(mKey, (String) value).apply();
                break;
            }
            case STRING_SET: {
                mPreferences.edit().putStringSet(mKey, (Set<String>) value).apply();
                break;
            }
            default: {
                throw new IllegalStateException("Cannot handle preference type: " + mType);
            }
        }
    }

    /**
     * Blocking removes the preference value.
     */
    /*package*/ void blockingRemove() {
        mPreferences.edit().remove(mKey).apply();
    }
    //endregion

    //region Rx Wrappers

    /**
     * Returns a flowable source that emits {@link RxOptional} with the preference value when it is changed.
     * On the subscribe, an RxOptional with the current value is emitted.
     * @return flowable source
     */
    public Flowable<RxOptional<T>> get() {
        final Scheduler scheduler = getScheduler();

        Flowable<Object> reactor = Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(final FlowableEmitter<Object> emitter) {
                if (!emitter.isCancelled()) {
                    final SharedPreferences.OnSharedPreferenceChangeListener trigger =
                            new SharedPreferences.OnSharedPreferenceChangeListener() {
                                @Override
                                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                    if (mKey.equals(key)) {
                                        emitter.onNext(NOTHING);
                                    }
                                }
                            };

                    mPreferences.registerOnSharedPreferenceChangeListener(trigger);
                    mTriggers.add(trigger);

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            clear();
                        }

                        @Override
                        protected void finalize() {
                            clear();
                        }

                        void clear() {
                            mPreferences.unregisterOnSharedPreferenceChangeListener(trigger);
                            mTriggers.remove(trigger);
                        }
                    }));

                    emitter.onNext(NOTHING);
                }
            }
        }, BackpressureStrategy.LATEST);

        final Single<RxOptional<T>> single = Single.fromCallable(new Callable<RxOptional<T>>() {
            @Override
            public RxOptional<T> call() {
                final T value = blockingGet(null);
                return RxOptional.ofNullable(value);
            }
        });

        return reactor
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .unsubscribeOn(scheduler)
                .flatMapSingle(new Function<Object, SingleSource<? extends RxOptional<T>>>() {
                    @Override
                    public SingleSource<? extends RxOptional<T>> apply(Object o) {
                        return single;
                    }
                });
    }

    /**
     * Returns a flowable source that emits the preference value when it is changed.
     * On the subscribe, the current value is emitted.
     * If no value present, then the source emits <code>defaultValue</code>.
     * <code>defaultValue</code> is not allowed to be null, since RxJava2 doesn't accept nulls in streams.
     * @param defaultValue to be emitted if no preference value present, non-null.
     * @return flowable source
     */
    public Flowable<T> get(final T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("Null values are not allowed in RxJava2");
        }

        return get()
                .flatMapMaybe(new Function<RxOptional<T>, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(RxOptional<T> optional) {
                        return Maybe.just(optional.orElse(defaultValue));
                    }
                });
    }

    /**
     * Returns a completable source that sets the preference value to <code>value</code>.
     * @param value to be set in the preference
     * @return completable source
     */
    public Completable set(final T value) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                blockingSet(value);
            }
        });
    }

    /**
     * Returns a completable source that removes the preferences value.
     * @return completable source
     */
    public Completable remove() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                blockingRemove();
            }
        });
    }
    //endregion
}
