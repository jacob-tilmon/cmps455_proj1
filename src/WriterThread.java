import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterThread implements Runnable{

    static Semaphore sharedArea;
    static Semaphore maxReaders;
    int id;
    int numReaders;
    static Semaphore writersSem;
    static AtomicInteger numWriters;
    static AtomicInteger numReadersA;
    Random random = new Random();

    public WriterThread(Semaphore s,int i,Semaphore m, int n,Semaphore numW,AtomicInteger w,AtomicInteger r){
        id = i;
        numReaders = n;
        sharedArea = s;
        maxReaders = m;
        writersSem = numW;
        numWriters = w;
        numReadersA = r;
    }

    @Override
    public void run(){
        try {
            writersSem.acquire();
            sharedArea.acquire();
            System.out.println("Writer " + id + " is writing.");
            for (int i = 0; i < random.nextInt(4) + 3; i++) {
                Thread.yield();
            }
            System.out.println("Writer " + id + " has finished writing.");
            //for (int i = 0; i < numReaders; i++) {
            //    maxReaders.release();
            //    System.out.println("Writer "+id+" has released a reader.");
            //}
            numWriters.getAndDecrement();
            if (numWriters.get() != 0)
                maxReaders.release(numReaders);
            if (numReadersA.get()==0)
                writersSem.release();
            sharedArea.release();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
