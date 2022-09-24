import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
public class ReaderThread implements Runnable{
    static Semaphore maxReaders;
    static AtomicInteger currentReaders = new AtomicInteger(0);
    static Semaphore mutex = new Semaphore(1);
    static Semaphore sharedArea;
    static Semaphore writersSem;
    static AtomicInteger numWriters;
    static AtomicInteger numReaders;
    int id;

    public ReaderThread(Semaphore n, Semaphore s,int i,Semaphore numW,AtomicInteger w,AtomicInteger r){
        maxReaders = n;
        sharedArea = s;
        id = i;
        writersSem = numW;
        numWriters = w;
        numReaders = r;
    }

    @Override
    public void run() {
        if (numWriters.get() != 0) {
            try {
                maxReaders.acquire();
                //System.out.println(maxReaders.availablePermits());
                mutex.acquire();
                currentReaders.getAndIncrement();
                if (currentReaders.get() == 1) {
                    sharedArea.acquire();
                }
                mutex.release();

                System.out.println("Reader " + id + " is reading.");
                for (int i = 0; i < 5; i++) Thread.yield();

                mutex.acquire();
                currentReaders.getAndDecrement();
                numReaders.getAndDecrement();
                System.out.println("Reader " + id + " has finished reading.");
                if (    (currentReaders.get() == 0 &&
                        maxReaders.availablePermits() == 0 &&
                        writersSem.availablePermits() <= 0) ||
                        numReaders.get() == 0) {
                    sharedArea.release();
                    writersSem.release();
                    System.out.println("Reader has released writer");
                }
                mutex.release();
            } catch (Exception e) {
                //System.out.println(e.getMessage());
            }
        }
        else writersSem.release();
    }
}