package dev.jaczerob.delfino.maplestory.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveTask {
    @Scheduled(fixedRate = 300_000)
    public void performKeepAlive() {

    }
}
