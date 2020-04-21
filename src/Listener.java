import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This thread runs indefinitely listening to incoming
 * requests, and firing a handler thread.
 * @author wmabebe
 *
 */
public class Listener extends Thread {
	Node node;
	public Listener(Node n) {
		this.node = n;
		System.out.println("Listening on port: " + this.node.getPort());
	}
	
	/**
	 * This method listens to messages on the
	 * UDP port. When a message is received, it
	 * checks whether it has been received before,
	 * and fires up a message handler thread if not.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Override
	public void run() {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket(node.getPort());
			DatagramPacket packet = null;
			byte[] receive = new byte[1024];
			while(true) {
				packet = new DatagramPacket(receive, receive.length);
				ds.receive(packet);
				ByteArrayInputStream inputStream = new ByteArrayInputStream(receive);
				ObjectInput in = new ObjectInputStream(inputStream);
				Message message = (Message) in.readObject();
				synchronized(this.node.SEEN_MSGS) {
					if (! node.SEEN_MSGS.contains(message.getId())) {
						node.SEEN_MSGS.add(message.getId());
						new MessageHandler(message,node).start();
					}
					//Reset seen messages cache
					if (node.SEEN_MSGS.size() >= 1000) {
						node.SEEN_MSGS.clear();
					}
				}
				receive = new byte[1024];
			}
			
				
		}
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
