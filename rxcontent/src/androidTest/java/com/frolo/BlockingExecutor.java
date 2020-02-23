package com.frolo;

import java.util.concurrent.Executor;


/**
 * Executor that executes commands immediately on the caller thread.
 */
public final class BlockingExecutor implements Executor {

    private static final BlockingExecutor sInstance = new BlockingExecutor();

    public static BlockingExecutor getInstance() {
        return sInstance;
    }

    private BlockingExecutor() {
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
