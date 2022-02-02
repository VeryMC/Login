package fr.verymc.login.utils;

import com.velocitypowered.api.scheduler.ScheduledTask;

public class SchedulerUtil {
    private ScheduledTask task;

    public ScheduledTask getTask() {
        return task;
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    public void cancel()
    {
        this.task.cancel();
    }
}