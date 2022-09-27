import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PhilosopherThread implements Runnable{
    int id;
    int totalPhil;
    Semaphore leftChopstick;
    Semaphore rightChopstick;
    Semaphore barrier;
    static Semaphore mutex = new Semaphore(1);
    static AtomicInteger arriveCnt = new AtomicInteger(0);
    static AtomicInteger meals;
    static AtomicInteger leaveCnt = new AtomicInteger(0);
    public PhilosopherThread(int i, int m, int p,Semaphore l, Semaphore r, Semaphore b){
        this.id = i;
        meals = new AtomicInteger(m);
        this.totalPhil = p;
        this.leftChopstick = l;
        this.rightChopstick = r;
        this.barrier = b;
    }

    @Override
    public void run() {
        Random random = new Random();
        System.out.println("Philosopher " + id + " is waiting for everyone.");
        arriveCnt.getAndIncrement();
        if (arriveCnt.get() == totalPhil){
            System.out.println("\nAll Philosophers have arrived.\n");
            barrier.release();
        }
        try {
            barrier.acquire();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        barrier.release(); // one thread is woken up, and runs
                           // this code to wake up another, etc.

        while (meals.get() > 0) { //loop of while there are still meals.
            try {
                if (!leftChopstick.tryAcquire(random.nextInt(2)+1, TimeUnit.SECONDS)) {
                    System.out.println("Philosopher " + id + " can't grab their left chopstick. Trying again..." );
                    continue;
                }
            }
            catch (Exception e) {System.out.println(e.getMessage());}
            System.out.println("Philosopher " + id + " has grabbed their left chopstick");
            try {
                if (!rightChopstick.tryAcquire(random.nextInt(2)+1, TimeUnit.SECONDS)) {
                    System.out.println("Philosopher " + id + " can't grab their right chopstick. Trying again...");
                    leftChopstick.release();
                    continue;
                }
            }
            catch (Exception e) {System.out.println(e.getMessage());}
            System.out.println("Philosopher " + id + " has grabbed their right chopstick");

            if (meals.get() <=0) {
                System.out.println("Philosopher " + id + " was too busy waiting to see there were no more meals. Setting down chopsticks.");
                leftChopstick.release();rightChopstick.release();break;} // some threads could be waiting when no more meals are left and keep going.
            try{
                mutex.acquire();
                meals.getAndDecrement();
                System.out.println("Philosopher " + id + " is eating. There are " + meals.get() + " meals left.");
                mutex.release();
            }catch (Exception e){System.out.println(e.getMessage());}

            for (int i = 0; i < random.nextInt(4)+3; i++){
                Thread.yield();
            }
            leftChopstick.release();
            System.out.println("Philosopher " + id + " has put down their left chopstick.");
            rightChopstick.release();
            System.out.println("Philosopher " + id + " has put down their right chopstick.");

            System.out.println("Philosopher " + id + " is thinking.");
            for (int i = 0; i < random.nextInt(4)+3; i++){
                Thread.yield();
            }
        }
        barrier.drainPermits();
        System.out.println("Philosopher " + id + " is waiting for everyone to leave.");

        leaveCnt.getAndIncrement();
        if (leaveCnt.get() == totalPhil){
            System.out.println("\nAll Philosophers are ready to leave.\n");
            barrier.release();
        }
        try {
            barrier.acquire();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        barrier.release();
        System.out.println("Philosopher " + id + " has left the table.");
    }
}
