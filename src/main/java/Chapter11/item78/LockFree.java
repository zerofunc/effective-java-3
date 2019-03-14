package Chapter11.item78;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class LockFree {
    private static final AtomicLong nextSerialNum = new AtomicLong();
    public static long generateSerialNumber() {
        return nextSerialNum.getAndIncrement();
    }
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            Thread backgroundThread = new Thread(() -> {
                System.out.println(generateSerialNumber());
            });
            backgroundThread.start();
        }

    }
}
