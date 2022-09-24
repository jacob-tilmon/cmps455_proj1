import java.util.Stack;
import java.util.concurrent.Semaphore;

public class Mailbox {
    int id;
    Stack<String> contents;
    Semaphore openSlots;
    Semaphore fullSlots = new Semaphore(0);
    //Semaphore mutex = new Semaphore(1);

    public Mailbox(int id, int size){
        this.id = id;
        contents = new Stack<>();
        openSlots = new Semaphore(size);
    }

    public void addMail(String message){
        try {
            //mutex.acquire();
            openSlots.acquire();
            contents.add(message);
            fullSlots.release();
            //mutex.release();
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }

    public String removeMail(){
        String returnMessage = "";
        try{
            //mutex.acquire();
            fullSlots.acquire();
            returnMessage = contents.pop();
            openSlots.release();
            //mutex.release();
        }
        catch (Exception e) {System.out.println(e.getMessage());}
        return returnMessage;
    }

    public boolean hasMail(){
        return !contents.empty();
    }
}
