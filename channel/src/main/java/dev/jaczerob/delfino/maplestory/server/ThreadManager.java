package dev.jaczerob.delfino.maplestory.server;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class ThreadManager {
    private static final ThreadManager instance = new ThreadManager();
    private ThreadPoolExecutor tpe;

    private ThreadManager() {
    }

    public static ThreadManager getInstance() {
        return instance;
    }

    public void newTask(Runnable r) {
        tpe.execute(r);
    }

    @PostConstruct
    public void start() {
        RejectedExecutionHandler reh = new RejectedExecutionHandlerImpl();
        ThreadFactory tf = Executors.defaultThreadFactory();

        tpe = new ThreadPoolExecutor(20, 1000, 77, SECONDS, new ArrayBlockingQueue<>(50), tf, reh);
    }

    @PreDestroy
    public void stop() {
        tpe.shutdown();
        try {
            tpe.awaitTermination(5, MINUTES);
        } catch (InterruptedException ie) {
        }
    }

    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Thread t = new Thread(r);
            t.start();
        }

    }

}
