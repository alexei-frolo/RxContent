package com.frolo.rxpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.test.platform.app.InstrumentationRegistry;

import com.frolo.BlockingExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import io.reactivex.subscribers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(JUnit4.class)
public class RxPreferenceTest {

    private static final String PREFS_NAME = "com.frolo.rxpreference.test";

    private SharedPreferences mPreferences;
    private Handler mMainHandler;
    private Executor mPrefsExecutor;

    private Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    private boolean equals(Set<String> set1, Set<String> set2) {
        return set1.containsAll(set2) && set2.containsAll(set1);
    }

    /**
     * Executes <code>cmd</code> last in {@link RxPreferenceTest#mMainHandler}'s looper.
     * Do call this when you want to run some code after all the posted callbacks are performed in {@link RxPreferenceTest#mMainHandler}.
     * For example, if you want to wait until
     * {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(SharedPreferences, String)} gets called.
     * @param cmd callback to run.
     */
    private void runOnNextLoop(Runnable cmd) {
        mMainHandler.post(cmd);
    }

    @Before
    public void setUp() {
        final Context ctx = InstrumentationRegistry.getInstrumentation().getContext();
        mPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPreferences.edit().clear().apply();

        mMainHandler = new Handler(ctx.getMainLooper());

        mPrefsExecutor = BlockingExecutor.getInstance();
    }

    //region Test preference of Boolean
    @Test
    public void test_BooleanBlocking() {
        RxPreference<Boolean> preference = RxPreference.ofBoolean(mPreferences,"bool_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        Boolean defaultValue = new Boolean(true);
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        preference.blockingSet(false);

        assertFalse(preference.blockingGet());

        preference.blockingSet(true);

        assertTrue(preference.blockingGet());
    }

    @Test
    public void test_BooleanRx() {
        final RxPreference<Boolean> preference = RxPreference.ofBoolean(mPreferences,"bool_key", mPrefsExecutor);

        final TestSubscriber<Boolean> subscriber = TestSubscriber.create();

        preference.get(false).subscribe(subscriber);

        subscriber.assertValue(false);

        preference.set(true).subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(1, true);

                preference.set(false).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(2, false);

                preference.set(true).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(3, true);

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

    //region Test preference of Int
    @Test
    public void test_IntBlocking() {
        RxPreference<Integer> preference = RxPreference.ofInt(mPreferences,"int_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        Integer defaultValue = new Integer(101);
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        preference.blockingSet(137);

        assertEquals(preference.blockingGet(), Integer.valueOf(137));

        preference.blockingSet(100);

        assertEquals(preference.blockingGet(), Integer.valueOf(100));
    }

    @Test
    public void test_IntRx() {
        final RxPreference<Integer> preference = RxPreference.ofInt(mPreferences,"int_key", mPrefsExecutor);

        final TestSubscriber<Integer> subscriber = TestSubscriber.create();

        preference.get(100).subscribe(subscriber);

        subscriber.assertValue(100);

        preference.set(200).subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(1, 200);

                preference.set(300).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(2, 300);

                preference.set(400).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(3, 400);

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

    //region Test preference of Long
    @Test
    public void test_LongBlocking() {
        RxPreference<Long> preference = RxPreference.ofLong(mPreferences,"long_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        Long defaultValue = new Long(201);
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        preference.blockingSet(1L);

        assertEquals(preference.blockingGet(), Long.valueOf(1L));

        preference.blockingSet(2L);

        assertEquals(preference.blockingGet(), Long.valueOf(2L));
    }

    @Test
    public void test_LongRx() {
        final RxPreference<Long> preference = RxPreference.ofLong(mPreferences,"long_key", mPrefsExecutor);

        final TestSubscriber<Long> subscriber = TestSubscriber.create();

        preference.get(1L).subscribe(subscriber);

        subscriber.assertValue(1L);

        preference.set(2L).subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(1, 2L);

                preference.set(3L).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(2, 3L);

                preference.set(4L).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(3, 4L);

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

    //region Test preference of Float
    @Test
    public void test_FloatBlocking() {
        RxPreference<Float> preference = RxPreference.ofFloat(mPreferences,"float_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        Float defaultValue = new Float(301);
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        preference.blockingSet(100f);

        assertEquals(preference.blockingGet(), Float.valueOf(100f));

        preference.blockingSet(200f);

        assertEquals(preference.blockingGet(), Float.valueOf(200f));
    }

    @Test
    public void test_FloatRx() {
        final RxPreference<Float> preference = RxPreference.ofFloat(mPreferences,"float_key", mPrefsExecutor);

        final TestSubscriber<Float> subscriber = TestSubscriber.create();

        preference.get(10f).subscribe(subscriber);

        subscriber.assertValue(10f);

        preference.set(20f).subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(1, 20f);

                preference.set(30f).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(2, 30f);

                preference.set(40f).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(3, 40f);

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

    //region Test preference of String
    @Test
    public void test_StringBlocking() {
        RxPreference<String> preference = RxPreference.ofString(mPreferences,"string_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        String defaultValue = new String("default_string");
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        preference.blockingSet("test1");

        assertEquals(preference.blockingGet(), "test1");

        preference.blockingSet("test2");

        assertEquals(preference.blockingGet(), "test2");
    }

    @Test
    public void test_StringRx() {
        final RxPreference<String> preference = RxPreference.ofString(mPreferences,"string_key", mPrefsExecutor);

        final TestSubscriber<String> subscriber = TestSubscriber.create();

        preference.get("test1").subscribe(subscriber);

        subscriber.assertValue("test1");

        preference.set("test2").subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(1, "test2");

                preference.set("test3").subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(2, "test3");

                preference.set("test4").subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                subscriber.assertValueAt(3, "test4");

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

    //region Test preference of String Set
    @Test
    public void test_StringSetBlocking() {
        RxPreference<Set<String>> preference = RxPreference.ofStringSet(mPreferences,"string_set_key", mPrefsExecutor);

        assertNull(preference.blockingGet());
        Set<String> defaultValue = setOf("default_1", "default_2", "default_3");
        assertEquals(defaultValue, preference.blockingGet(defaultValue));

        final Set<String> set1 = setOf("test1", "test2");

        preference.blockingSet(set1);

        assertTrue(equals(preference.blockingGet(), set1));

        final Set<String> set2 = setOf("1test", "2test", "3test");

        preference.blockingSet(set2);

        assertTrue(equals(preference.blockingGet(), set2));
    }

    @Test
    public void test_StringSetRx() {
        final RxPreference<Set<String>> preference = RxPreference.ofStringSet(mPreferences,"string_set_key", mPrefsExecutor);

        final TestSubscriber<Set<String>> subscriber = TestSubscriber.create();

        final Set<String> set1 = setOf("test", "t");
        final Set<String> set2 = setOf("1test1", "2test2", "3test3");
        final Set<String> set3 = setOf("1", "2", "3", "4", "test1000000");
        final Set<String> set4 = setOf("TEST");

        preference.get(set1).subscribe(subscriber);

        assertTrue(RxPreferenceTest.this.equals(set1, subscriber.values().get(0)));

        preference.set(set2).subscribe();

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                assertTrue(RxPreferenceTest.this.equals(set2, subscriber.values().get(1)));

                preference.set(set3).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                assertTrue(RxPreferenceTest.this.equals(set3, subscriber.values().get(2)));

                preference.set(set4).subscribe();
            }
        });

        runOnNextLoop(new Runnable() {
            @Override
            public void run() {
                assertTrue(RxPreferenceTest.this.equals(set4, subscriber.values().get(3)));

                subscriber.assertValueCount(4);
            }
        });
    }
    //endregion

}
