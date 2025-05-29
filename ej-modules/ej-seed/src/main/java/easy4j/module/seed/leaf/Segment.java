package easy4j.module.seed.leaf;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Segment
 *
 * @author bokun.li
 * @date 2025-05
 */
public class Segment {
    private AtomicLong value = new AtomicLong(0);
    private volatile long max;
    private volatile long step;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer segmentBuffer) {
        this.buffer = segmentBuffer;
    }

    public long getIdle() {
        return this.getMax() - getValue().get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Segment(");
        sb.append("value:");
        sb.append(value);
        sb.append(",max:");
        sb.append(max);
        sb.append(",step:");
        sb.append(step);
        sb.append(")");
        return sb.toString();
    }

    public AtomicLong getValue() {
        return value;
    }

    public void setValue(AtomicLong value) {
        this.value = value;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public SegmentBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(SegmentBuffer buffer) {
        this.buffer = buffer;
    }
}