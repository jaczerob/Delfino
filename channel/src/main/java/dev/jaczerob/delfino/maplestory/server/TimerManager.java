package dev.jaczerob.delfino.maplestory.server;

import dev.jaczerob.delfino.maplestory.net.server.Server;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Component
public class TimerManager {
    private static final Logger log = LoggerFactory.getLogger(TimerManager.class);
    private static TimerManager INSTANCE = null;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory());

    public TimerManager() {
        this.scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
    }

    public static TimerManager getInstance() {
        return INSTANCE;
    }

    @PostConstruct
    public void init() {
        INSTANCE = this;
        log.info("Timer Manager initialized with {} threads.", this.scheduledThreadPoolExecutor.getCorePoolSize());
    }

    @PreDestroy
    public void destroy() {
        this.purge();
        this.scheduledThreadPoolExecutor.shutdownNow();
    }

    public void purge() {
        Server.getInstance().forceUpdateCurrentTime();
        this.scheduledThreadPoolExecutor.purge();
    }

    public ScheduledFuture<?> register(final Runnable runnable, final long repeatTime, final long delay) {
        return this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new LoggingSaveRunnable(runnable), delay, repeatTime, MILLISECONDS);
    }

    public ScheduledFuture<?> register(final Runnable runnable, final long repeatTime) {
        return this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new LoggingSaveRunnable(runnable), 0, repeatTime, MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(final Runnable runnable, final long delay) {
        return this.scheduledThreadPoolExecutor.schedule(new LoggingSaveRunnable(runnable), delay, MILLISECONDS);
    }

    private record LoggingSaveRunnable(Runnable runnable) implements Runnable {

        @Override
        public void run() {
            try {
                this.runnable.run();
            } catch (final Throwable exc) {
                log.error("Error in scheduled task", exc);
            }
        }
    }
}
