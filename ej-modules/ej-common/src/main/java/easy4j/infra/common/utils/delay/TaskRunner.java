package easy4j.infra.common.utils.delay;

public interface TaskRunner<T> {

    void run(T data);

}
