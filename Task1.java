// MailSimulation.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class Parcel { // model of package
    String sender;
    String receiver;
    String content;

    public Parcel(String sender, String receiver, String content) { // constructor
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    @Override // overriding toString method to return package info 
    public String toString() {
        return "Package from " + sender + " to " + receiver + ": \"" + content + "\"";
    }
}

class PostOffice implements Runnable { // immitation of Post Office
    private final BlockingQueue<Parcel> queue; // creating a final queue
    private volatile boolean open = true; // is office open flag

    public PostOffice(BlockingQueue<Parcel> queue) { // constructor
        this.queue = queue;
    }

    public void closeOffice() {
        open = false;
        System.out.println("Post office is close!");
    }

    @Override // overriding standart run method
    public void run() {
        System.out.println("Post office is open!");
        while (open || !queue.isEmpty()) { // to work there must be queue or open time
            try { // secure process by try - catch block
                Parcel parcel = queue.poll(1, TimeUnit.SECONDS); // waiting a second for a package
                if (parcel != null) { // is package is correct
                    System.out.println("Worker is processing: " + parcel);
                    Thread.sleep(1500); // working...
                    System.out.println("Shipped: " + parcel);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All packages are done. Post office is closing...");
    }
}

class Sender implements Runnable { // create sender with 3 package each
    private final String name;
    private final BlockingQueue<Parcel> queue;

    public Sender(String name, BlockingQueue<Parcel> queue) { // constructor
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void run() { // overriding standart run method
        try { // secure process by try - catch block
            for (int i = 1; i <= 3; i++) {
                Parcel p = new Parcel(name, "Reciever_" + i, "Package №" + i); // creating new Parcel object
                queue.put(p); // put this object into the queue
                System.out.println("Package " + name + " sent by: " + p);
                Thread.sleep((int)(Math.random() * 2000 + 1000)); // creating radom delay
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Task1 {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Parcel> queue = new LinkedBlockingQueue<>();

        // Creating post office (1 worker - 1 thread)
        PostOffice postOffice = new PostOffice(queue); // set queue for this post office
        Thread officeThread = new Thread(postOffice); // create thread for it
        officeThread.start();

        // Create three senders
        Thread s1 = new Thread(new Sender("Sender_1", queue));
        Thread s2 = new Thread(new Sender("Sender_2", queue));
        Thread s3 = new Thread(new Sender("Sender_3", queue));

        s1.start();
        s2.start();
        s3.start();

        // Work time for the post  is 7 sec
        Thread.sleep(7000);
        postOffice.closeOffice(); // closing after wortime has ended

        // Waitinh for senders to end
        s1.join();
        s2.join();
        s3.join();

        // Wait for post`s office work to end
        officeThread.join();
    }
}
