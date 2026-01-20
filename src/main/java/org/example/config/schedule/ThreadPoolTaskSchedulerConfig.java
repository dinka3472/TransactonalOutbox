package org.example.config.schedule;

import org.example.common.CommonSchedulingTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

@Configuration
public class ThreadPoolTaskSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler pipRequestSchedulingTaskExecutor(List<CommonSchedulingTask> schedulingTasks) {
        int poolSize = Math.max(1, calculatePoolSize(schedulingTasks));
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(poolSize);
        ts.setThreadNamePrefix("schedule-thread-");

        ts.setWaitForTasksToCompleteOnShutdown(true);
        ts.setAwaitTerminationSeconds(30);

        ts.initialize();

        return ts;
    }

    private int calculatePoolSize(List<CommonSchedulingTask> tasks) {
        int sum = 0;
        for (CommonSchedulingTask task : tasks) {
            int threadCount = task.getThreadCount();
            sum += threadCount;
        }
        return Math.min(sum, 128); //TODO хз чо за цифру я добавила
    }
}
