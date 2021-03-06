package cn.neyzoter.module.concurrent.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 线程池的测试
 * @author Neyzoter Song
 * @date 2020-1-29
 */
public class ThreadPool {
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque(10);
    /**
     * corePoolSize: 线程池的一直存在着的线程数量，如果线程个数超过这个数目，则需要创建新的线程
     * maximumPoolSize: 线程池最大线程数量
     * keepAliveTime: 如果要执行的任务多于线程池线程数，则一个空闲任务（没有任务执行）保持多久才会停止，直到线程个数等于corePoolSize
     * unit: 参数keepAliveTime的时间单位，TimeUnit.NANOSECONDS、TimeUnit.MICROSECONDS等
     * workQueue: 队列，用于保存等待执行的任务（可选择ArrayBlockingQueue、LinkedBlockingQueue、SynchronousQueue）
     * threadFactory: 线程工厂，用于创建线程
     * handler：表示当拒绝处理任务时的策略，以下是策略：
     *      ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
     *      ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
     *      ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
     *      ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
     */
    public ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,9,100, TimeUnit.MICROSECONDS,workQueue);

    /**
     * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程
     * ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
     * 应用场景：执行很多短期异步的小程序或者负载较轻的服务器
     */
    public ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    /**
     * 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
     * 而且保持线程池的核心线程数和最大线程数相同
     * ThreadPoolExecutor(nThreads, nThreads,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
     * 应用场景：执行长期的任务，性能好很多
     */
    public ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);

    /**
     * 创建一个定长(核心线程数目)线程池，支持定时及周期性任务执行
     * ScheduledThreadPoolExecutor(corePoolSize)
     * 应用场景：周期性执行任务的场景，不同于fixedThreadPool，最大的线程个数不限定
     */
    public ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);

    /**
     * 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
     * DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1))
     * 应用场景：一个任务一个任务执行的场景
     */
    public ScheduledExecutorService singleThreadSPool = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool();
//        testThreadPoolExecutor(threadPool);
//        testSubmit(threadPool);

        testFutureTask(threadPool);
    }

    public static void testSubmit (ThreadPool threadPool) {
        System.out.println("\n ======  testSubmit Start  =====");
        Future<?> future = threadPool.threadPoolExecutor.submit(new Task(10 * 100000 * (int)Math.pow(-1,10)));
        getFutureInfo (future);

    }

    public static void testThreadPoolExecutor (ThreadPool threadPool) {
        System.out.println(" ======  testThreadPoolExecutor Start  =====");
        for (int i = 1; i < 20 ; i++) {
            try {
                threadPool.threadPoolExecutor.execute(new Task(i * 100000 * (int)Math.pow(-1,i)));
                System.out.println(String.format("[i = %d] active count = %d , completed = %d",i,threadPool.threadPoolExecutor.getActiveCount(),threadPool.threadPoolExecutor.getCompletedTaskCount()));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testFutureTask (ThreadPool threadPool) {
        System.out.println(" ======  testFutureTask Start  =====");
        // FutureTask 继承了Future和Runnable
        FutureTask<String> futureTask = new FutureTask<>(new Call(10000));
        threadPool.threadPoolExecutor.submit(futureTask);
        // 不再接受新的任务
//        threadPool.threadPoolExecutor.shutdown();
        getFutureInfo(futureTask);
    }

    public static void getFutureInfo (Future future) {
        System.out.println("isDone?" + future.isDone());

        while (!future.isDone()) {

        }
        System.out.println("isDone?" + future.isDone());
        try {
            // future.get()会阻塞到返回结果
            System.out.println("Future result : " + future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 任务
 */
class Task implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(Task.class);
    int count;
    int maxcount;
    public Task(int cnt) {
        count = cnt;
        maxcount = cnt;
    }
    @Override
    public void run() {
        while (true) {
            if (count > 0) {
                count --;
            } else if (count < 0) {
                count ++;
            } else {
                System.out.println(String.format("[Task(%d)] count dec to 0", maxcount));
                break;
            }
        }

    }
}

/**
 * 任务
 */
class Call implements Callable<String> {
    private final static Logger logger = LoggerFactory.getLogger(Task.class);
    int count;
    int maxcount;
    public Call(int cnt) {
        count = cnt;
        maxcount = cnt;
    }
    @Override
    public String call() {
        while (true) {
            if (count > 0) {
                count --;
            } else if (count < 0) {
                count ++;
            } else {
                System.out.println(String.format("[Call(%d)] count dec to 0", maxcount));
                break;
            }
        }
        return "Finished";
    }
}

