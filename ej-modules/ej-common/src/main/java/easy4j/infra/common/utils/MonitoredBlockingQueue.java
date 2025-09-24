package easy4j.infra.common.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 带监控功能的阻塞队列包装类
 * @author bokun.li
 * @date 2025/9/24
 */
public class MonitoredBlockingQueue<T> implements BlockingQueue<T> {
    private final BlockingQueue<T> target;
    // 入队总数
    private long enqueueCount = 0;
    // 出队总数
    private long dequeueCount = 0;
    // 最大数量
    private long maxSize = 0;

    public MonitoredBlockingQueue(BlockingQueue<T> target) {
        this.target = target;
    }

    // 监控入队操作
    @Override
    public boolean offer(T t) {
        boolean result = target.offer(t);
        if (result) {
            enqueueCount++;
            updateMaxSize();
        }
        return result;
    }

    // 监控出队操作
    @Override
    public T poll() {
        T item = target.poll();
        if (item != null) {
            dequeueCount++;
        }
        return item;
    }

    // 其他方法也需要类似包装（put、take等）
    @Override
    public void put(T t) throws InterruptedException {
        target.put(t);
        enqueueCount++;
        updateMaxSize();
    }

    @Override
    public T take() throws InterruptedException {
        T item = target.take();
        dequeueCount++;
        return item;
    }

    // 更新队列最大尺寸
    private void updateMaxSize() {
        int currentSize = target.size();
        if (currentSize > maxSize) {
            maxSize = currentSize;
        }
    }



    // 其他未实现的方法需要委托给target（省略）
    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return target.contains(o);
    }

    @Override
    public Object[] toArray() {
        return target.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return target.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return target.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return target.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return target.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return target.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return target.retainAll(c);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        T item = target.poll(timeout, unit);
        if (item != null) dequeueCount++;
        return item;
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = target.offer(t, timeout, unit);
        if (result) {
            enqueueCount++;
            updateMaxSize();
        }
        return result;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return target.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return target.drainTo(c, maxElements);
    }

    @Override
    public boolean add(T t) {
        return target.add(t);
    }

    @Override
    public int remainingCapacity() {
        return target.remainingCapacity();
    }

    @Override
    public T remove() {
        return target.remove();
    }

    @Override
    public T element() {
        return target.element();
    }

    @Override
    public T peek() {
        return target.peek();
    }

    @Override
    public Iterator<T> iterator() {
        return target.iterator();
    }


    // 监控数据获取方法
    public QueueMetrics getMetrics() {
        return new QueueMetrics(
                target.size(),
                target.remainingCapacity(),
                enqueueCount,
                dequeueCount,
                maxSize
        );
    }

    // 监控指标封装类
    public static class QueueMetrics {
        private final int currentSize;
        private final int remainingCapacity;
        private final long enqueueCount;
        private final long dequeueCount;
        private final long maxSize;

        public QueueMetrics(int currentSize, int remainingCapacity,
                            long enqueueCount, long dequeueCount, long maxSize) {
            this.currentSize = currentSize;
            this.remainingCapacity = remainingCapacity;
            this.enqueueCount = enqueueCount;
            this.dequeueCount = dequeueCount;
            this.maxSize = maxSize;
        }

        // getter方法
        @Override
        public String toString() {
            return "QueueMetrics{" +
                    "currentSize=" + currentSize +
                    ", remainingCapacity=" + remainingCapacity +
                    ", enqueueCount=" + enqueueCount +
                    ", dequeueCount=" + dequeueCount +
                    ", maxSize=" + maxSize +
                    '}';
        }
    }
}