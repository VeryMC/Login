package fr.verymc.login.utils;

import com.velocitypowered.api.scheduler.ScheduledTask;

public class SchedulerUtil {
    private ScheduledTask task;

    public ScheduledTask getTask() {
        return task;
    }

    public SchedulerUtil setTask(ScheduledTask task) {
        this.task = task;
        return this;
    }

    public void cancel()
    {
        this.task.cancel();
    }
}