package me.zhongmingmao.zmrpc.core.governance;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class SlidingTimeWindow {

  public static final int DEFAULT_SIZE = 30;

  private final int size;
  private final RingBuffer ringBuffer;
  private int sum = 0;

  private int _curr_mark = -1;

  private long _start_ts = -1L;
  private long _curr_ts = -1L;

  public SlidingTimeWindow() {
    this(DEFAULT_SIZE);
  }

  public SlidingTimeWindow(int _size) {
    this.size = _size;
    this.ringBuffer = new RingBuffer(this.size);
  }

  public synchronized void record(long millis) {
    log.debug("window before: " + this);
    log.debug("window.record(" + millis + ")");
    long ts = millis / 1000;
    if (_start_ts == -1L) {
      initRing(ts);
    } else {
      if (ts == _curr_ts) {
        log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
        this.ringBuffer.incr(_curr_mark, 1);
      } else if (ts > _curr_ts && ts < _curr_ts + size) {
        int offset = (int) (ts - _curr_ts);
        log.debug(
            "window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size + ", offset:" + offset);
        this.ringBuffer.reset(_curr_mark + 1, offset);
        this.ringBuffer.incr(_curr_mark + offset, 1);
        _curr_ts = ts;
        _curr_mark = (_curr_mark + offset) % size;
      } else if (ts >= _curr_ts + size) {
        log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
        this.ringBuffer.reset();
        initRing(ts);
      }
    }
    this.sum = this.ringBuffer.sum();
    log.debug("window after: " + this);
  }

  private void initRing(long ts) {
    log.debug("window initRing ts:" + ts);
    this._start_ts = ts;
    this._curr_ts = ts;
    this._curr_mark = 0;
    this.ringBuffer.incr(0, 1);
  }

  public int getSize() {
    return size;
  }

  public int getSum() {
    return sum;
  }

  public RingBuffer getRingBuffer() {
    return ringBuffer;
  }

  public int get_curr_mark() {
    return _curr_mark;
  }

  public long get_start_ts() {
    return _start_ts;
  }

  public long get_curr_ts() {
    return _curr_ts;
  }
}
