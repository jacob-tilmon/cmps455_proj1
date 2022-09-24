import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class MailPersonThread implements Runnable{
    Random random = new Random();
    int id;
    ArrayList<Mailbox> mailboxes;
    Mailbox myMail;
    String[] potentialMessages = {"HI","Hello","Sault","Goodbye","Bye!"};
    static AtomicInteger messagesSent;
    Semaphore mutex;

    public MailPersonThread(int id, ArrayList<Mailbox> mBox,int m,Semaphore mutex){
        this.id = id;
        mailboxes = mBox;
        myMail = mailboxes.get(id);
        messagesSent = new AtomicInteger(m);
        this.mutex = mutex;
    }
    @Override
    public void run(){
        while (messagesSent.get() > 0){
            System.out.println("MailPerson "+id+" entered the post office");
            while(myMail.hasMail()){
                System.out.println("MailPerson " + id + " has mail!");
                String message = myMail.removeMail();
                System.out.println("MailPerson "+id+" has a message: " + message);
                for(int i = 0; i < random.nextInt(4)+3; i++){Thread.yield();}
            }
            if(messagesSent.intValue() <= 0){
                System.out.println("No more mail can be sent!");
                break;
            }
            int idToSend;
            do { idToSend = random.nextInt(mailboxes.size()); } while(idToSend == this.id);
            System.out.println("MailPerson "+id+" is writing a message to MailPerson "+idToSend);
            String messageToSend = "From ("+id+"): " + potentialMessages[random.nextInt(potentialMessages.length)];
            mailboxes.get(idToSend).addMail(messageToSend);
            try{
                mutex.acquire();
                messagesSent.getAndDecrement();
            } catch (Exception e) {System.out.println(e.getMessage());}
            mutex.release();
            System.out.println("MailPerson "+id+" has sent a message to MailPerson "+idToSend);
            //System.out.println(messagesSent.get() + " more to send.");
            System.out.println("MailPeron "+id+" has left the post office.");
            for(int i = 0; i < random.nextInt(4)+3;i++) Thread.yield();
        }
        System.out.println("MailPerson "+id+" has done a final leave.");
    }
}
