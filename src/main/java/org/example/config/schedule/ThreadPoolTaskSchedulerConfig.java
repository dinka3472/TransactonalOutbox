package org.example.config.schedule;

import org.example.common.CommonSchedulingTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

@Configuration
public class ThreadPoolTaskSchedulerConfig {

    private static final String THREAD_NAME_PREFIX = "schedule-thread-";
    private static final int MIN_POOL_SIZE = 1;

    @Bean
    public ThreadPoolTaskScheduler pipRequestSchedulingTaskExecutor(List<CommonSchedulingTask> schedulingTasks) {
        int poolSize = Math.max(MIN_POOL_SIZE, calculatePoolSize(schedulingTasks));
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(poolSize);
        ts.setThreadNamePrefix(THREAD_NAME_PREFIX);

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
        return Math.min(sum, 128); //TODO хз чо за цифру я добавила, чото в проперти явно надо вынести
    }
}
