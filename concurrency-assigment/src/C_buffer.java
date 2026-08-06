import java.util.*;

public class C_buffer {

    // The buffer has no input limit so unlike it acts like a blocking que
    // But only blocks the getter which will block if the buffer is empty
    private Vector<Object> data;

    public C_buffer (){
    	data = new Vector<Object>();
    }

    public synchronized int size(){
    	return data.size();
    }

    public synchronized void saveRequest (String[] r){
    	data.add(r[0]);
    	data.add(r[1]);
        notifyAll();
    }

    public synchronized void show(){
		for (int i=0; i<data.size();i++)
		    System.out.print(" "+data.get(i)+" ");
		System.out.println(" ");
    }

    public synchronized void add(Object o){
    	// Any new item add = notify the get method to wake up
        data.add(o);
        notifyAll();
    }

    public synchronized Object get() throws InterruptedException{
        while(data.isEmpty()){
            // This will block que until new item added
            wait();
        }

		return data.removeFirst();
    }
}
