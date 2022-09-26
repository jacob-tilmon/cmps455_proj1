import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int philosopherNum = 0;
        int mealNum = 0;
        int mailPeople = 0;
        int mailboxSize = 0;
        int numMessages =0;
        int numReaders =0;
        int numWriters =0;
        int maxWriters =0;

        if (args.length == 1) {
            System.out.println("Missing Argument, please type 1,2, or 3 after -A");
        }
        else if (args[0].equals("-A")) {
            if (args[1].equals("1")) {
                do{
                    System.out.print("Enter a number of philosophers (positive int 2+): ");
                    if(input.hasNextInt()) philosopherNum = input.nextInt();
                    else System.out.println("Incorrect input. Please Enter a Positive Integer.");

                    if (philosopherNum == 1) System.out.println("Input cannot be one.");
                    input.nextLine();
                } while (philosopherNum <=1);
                do {
                    System.out.print("Enter a number of meals (int): ");
                    if(input.hasNextInt()) mealNum = input.nextInt();
                    else System.out.println("Incorrect input. Please Enter a Positive Integer.");

                    if (mealNum <= 0) System.out.println("Please enter a Positive Integer");
                    input.nextLine();
                } while (mealNum <= 0);

                long startTime = System.nanoTime();
                diningPhilosophers(philosopherNum, mealNum);
                long endTime = System.nanoTime();
                System.out.println("Runtime in milliseconds: " + (endTime-startTime)/1000000.0);
            }
            else if (args[1].equals("2")) {
                do {
                    System.out.print("Enter a number of mail people (positive int 2+): ");
                    if(input.hasNextInt()) mailPeople = input.nextInt();
                    else{
                        System.out.println("Incorrect Input, please try again.");
                        input.nextLine();
                    }
                }while (mailPeople <=1);
                do{
                    System.out.print("Enter a number of mail slots (positive int): ");
                    if(input.hasNextInt()) mailboxSize = input.nextInt();
                    else{
                        System.out.println("Incorrect Input, please try again.");
                        input.nextLine();
                    }
                }while (mailboxSize <=0);
                do{
                    System.out.print("Enter a positive int for a number of messages to send: ");
                    if (input.hasNextInt()) numMessages = input.nextInt();
                    else{
                        System.out.println("Incorrect Input, please try again.");
                        input.nextLine();
                    }
                }while(numMessages <=0);

                postOfficeSim(mailPeople,mailboxSize,numMessages);
            }
            else if (args[1].equals("3")) {
                do{
                    System.out.print("Enter a number of Readers (int 1+): ");
                    if(input.hasNextInt()) numReaders = input.nextInt();
                    else{
                        System.out.println("Incorrect input, please try again.");
                        input.nextLine();
                    }
                }while(numReaders <=1);
                do{
                    System.out.print("Enter a number of Writers (int 1+): ");
                    if(input.hasNextInt()) numWriters = input.nextInt();
                    else{
                        System.out.println("Incorrect input, please try again.");
                        input.nextLine();
                    }
                }while(numWriters <=1);
                do{
                    System.out.print("Enter a max number of Readers in shared area (int 1 - "+numReaders+"): ");
                    if(input.hasNextInt()) maxWriters = input.nextInt();
                    else{
                        System.out.println("Incorrect input, please try again.");
                        input.nextLine();
                    }
                }while(maxWriters <=1 || maxWriters > numReaders);
                readersWritersProblem(numReaders,numWriters,maxWriters);
            }
            else System.out.println("Unexpected argument, please input 1, 2, or 3 after -A in the command prompt.");
        }
        else System.out.println("Unexpected tag, please type -A followed by 1,2 or 3 in command prompt.");
    }
    public static void diningPhilosophers(int p, int m){
        Semaphore barrier = new Semaphore(0);
        ArrayList<Semaphore> chopsticks = new ArrayList<>();
        for(int i = 0; i < p; i++) {
            Semaphore c = new Semaphore(1);
            chopsticks.add(c);//sets all semaphores to 1
        }

        ArrayList<Thread> philosophers = new ArrayList<>();
        for (int i = 0; i < p; i++){
            PhilosopherThread t1 = new PhilosopherThread(i,m,p,chopsticks.get(i),chopsticks.get((i+1)%p),barrier);
            Thread t2 = new Thread(t1);
            philosophers.add(t2);
        }
        for (Thread philosopher : philosophers) philosopher.start();
        for (Thread philosopher : philosophers) {
            try {philosopher.join();}
            catch (Exception e) { System.out.println(e.getMessage());}
        }
    }
    public static void postOfficeSim(int n, int s, int m){
        Semaphore mutex = new Semaphore(1);
        ArrayList<Mailbox> mailBoxes = new ArrayList<>();
        ArrayList<Thread> people = new ArrayList<>();
        for (int i = 0; i < n; i++){
            mailBoxes.add(new Mailbox(i,s));
        }
        for (int i = 0; i < n; i++){
            Runnable t1 = new MailPersonThread(i,mailBoxes,m,mutex);
            Thread t2 = new Thread(t1);
            people.add(t2);
        }
        for (Thread t : people) t.start();
        for (Thread t : people) {
            try {
                t.join();
            }
            catch (Exception e) {System.out.println(e.getMessage());}
        }
    }
    public static void readersWritersProblem(int r, int w, int n){
        ArrayList<Thread> readers = new ArrayList<>();
        ArrayList<Thread> writers = new ArrayList<>();
        Semaphore sharedArea = new Semaphore(1);
        Semaphore numReaders = new Semaphore(n);
        Semaphore numWriters = new Semaphore(0); //This way a reader always starts.
        AtomicInteger writerAtom = new AtomicInteger(w);
        AtomicInteger readerAtom = new AtomicInteger(r);

        for (int i = 0; i < r; i++){
            Runnable t1 = new ReaderThread(numReaders,sharedArea,i,numWriters,writerAtom,readerAtom);
            Thread t2 = new Thread(t1);
            readers.add(t2);
        }
        for (int i = 0; i < w; i++){
            Runnable t1 = new WriterThread(sharedArea,i,numReaders,n,numWriters,writerAtom,readerAtom);
            Thread t2 = new Thread(t1);
            writers.add(t2);
        }

        for(Thread reader : readers) reader.start();
        for(Thread writer : writers) writer.start();
        for(Thread writer: writers){
            try{writer.join();}
            catch(Exception e){System.out.println(e.getMessage());}
        }
        System.out.println("All Writers have finished execution!");
        for(Thread reader : readers){
            reader.interrupt();
        }
    }
}