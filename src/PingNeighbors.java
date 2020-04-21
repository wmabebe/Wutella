import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * This thread class periodically PINGS
 * neighbors.
 * @author wmabebe
 *
 */
class PingNeighbors extends Thread{
	private Node node;
	private Message pingMessage;
	public PingNeighbors(Node node) throws UnknownHostException {
		this.node = node;
		System.out.println("Pinging neighbors...");
	}
	
	/**
	 * This method indefinitely runs, pinging all neighbors
	 * then sleeping for a predefined number of seconds.
	 */
	@Override
	public void run() {
		while (true) {
			
			synchronized(this.node.pool) {
				//If neighbors list not full, try adding
				//new neighbors from the pool.
				for (Neighbor n: this.node.pool) {
					try {
						this.node.addNeighbor(n);
					}
					catch (Exception ex) {
						//Exception raised when neighbors set is full
						break;
					}
				}
			}
			
			synchronized(this.node.getNeighbors()) {
			
				//Fire a new ping to neighbors list
				//Use iterator to avoid concurrentmodificaation exception
				Iterator<Neighbor> iter = this.node.getNeighbors().iterator();
				while (iter.hasNext()) {
					Neighbor n = iter.next();
					//Thou shan't PING thyself
					if (!this.node.toNeighbor().equals(n)) {
						try {
							//Compose PING message
							pingMessage = new Message(this.node.toNeighbor(),1,node.getPort());
							//Send message
							node.send(this.pingMessage,n);
							//Add ping id into PING cache. Routinely clear ping cache
							//to free up memory. Reason we cache is to cross check against
							//incoming PONG messages.
							synchronized(this.node.PING_CACHE) {
								node.PING_CACHE.add(pingMessage.getId());
							}
							System.out.println("\nSent PING to: " + n);
						}
						catch (Exception ex) {
							System.err.print("Unable to PING !" + n + "\n" + ex.getMessage());
						}
					}
				}
			
			}
			
			try {
				//Rest every 100 PINGS for 2 x SLEEP_TIME and clear caches
				Thread.sleep(node.PING_CACHE.size() >= 100 ? 1000 * Node.SLEEP_TIME : 1000 * Node.SLEEP_TIME * 2);
				//Reset PING, QUERY and GET message caches.
				//These PINGs QUERies and GETs are sent by this node.
				synchronized(this.node.PING_CACHE) {
					if (node.PING_CACHE.size() >= 100) {
						node.PING_CACHE.clear();
					}
				}
				synchronized(this.node.QUERY_CACHE) {
					if (node.QUERY_CACHE.size() >= 100){
						node.QUERY_CACHE.clear();
					}
				}
				synchronized(this.node.GET_CACHE) {
					if (node.GET_CACHE.size() >= 100) {
						node.GET_CACHE.clear();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}