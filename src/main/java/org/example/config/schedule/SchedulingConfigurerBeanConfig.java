package org.example.config.schedule;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.common.CommonBackOffWrp;
import org.example.common.CommonSchedulingTask;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulingConfigurerBeanConfig implements SchedulingConfigurer {

    @NonNull
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @NonNull
    private final List<CommonSchedulingTask> commonSchedulingTasks;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(threadPoolTaskScheduler);
        for (final CommonSchedulingTask task : commonSchedulingTasks) {
            for (int i = 0; i < task.getThreadCount(); i++) {
                taskRegistrar.addTriggerTask(new CommonBackOffWrp(task), schedulingTrigger(task));
            }
        }
    }

    //TODO переделать период на Duration
    private Trigger schedulingTrigger(final CommonSchedulingTask task) {
        final PeriodicTrigger periodicTrigger = new PeriodicTrigger(task.getPeriod());
        periodicTrigger.setInitialDelay((task.getInitialDelay()));
        periodicTrigger.setFixedRate(task.isFixRate());
        return periodicTrigger;
    }
}
