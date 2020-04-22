import java.io.IOException;
import java.net.UnknownHostException;

/**
 * This class accepts a network message and
 * handles it. Network message could be a PING or
 * a QUERY.
 * @author wmabebe
 *
 */
class MessageHandler extends Thread{
	/**
	 * Message that was sent.
	 */
	private Message message;
	/**
	 * Node that is handling the incoming
	 * message.
	 */
	private Node node;
	/**
	 * Neighbor representing the originator
	 * of the incoming message.
	 */
	private Neighbor origin;
	public MessageHandler(Message m,Node n) {
		this.message = m;
		this.node = n;
		this.origin = message.getOriginator();
	}
	
	/**
	 * This run method is responsible for
	 * checking the message type, and dealing
	 * with it appropriately.
	 */
	@Override
	public void run() {
		if (this.message.getTTL() > 0) {
			//Notice switching file descriptor will decrement message's TTL
			switch(this.message.getDescriptor()) {
				case Message.PING:
					//Shan't receive PINGs from thyself
					if (!this.node.toNeighbor().equals(origin)) {
						//If neighbors list not full, add PINGer
						System.out.println("\nReceieved PING from: " + origin.getHost() +":" + origin.getPort());
						try {
							//Try adding to neighbors list if possible
							this.node.addNeighbor(this.origin);
						}
						catch (Exception ex) {
							//We do nothing here
						}
						//Given a PING message, reply with a PONG
						//message containing a list of own neighbors.
						try {
							Message pong = new Message(this.node.toNeighbor(),2,this.node.getPort());
							//Set previous_id for PONG message as current PING message
							pong.setPrevId(this.message.getId());
							//Add all neighbors to PONG list
							pong.neighbors.addAll(this.node.getNeighbors());
							//Reply with PONG
							this.node.send(pong,this.origin);
							System.out.println("\nSent PONG to: " + origin.getHost() +":" + origin.getPort());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				break;
				case Message.PONG:
					//PONG must be sent in response to an earlier PING
					//Drop otherwise
					if (this.node.PING_CACHE.contains(message.getPrevId())) {
						System.out.print("\nReceieved PONG from: " + origin.getHost() +":" + origin.getPort() + "\n\t Remote neighbors: ");
						//Update liveliness of neighbor
						this.node.live.put(origin, System.currentTimeMillis());
						//Add neighbors of neighbors to pool
						for (Neighbor n: message.neighbors) {
							//Add n only if this.node != n and n is not already in the pool
							if (this.node.getId() != n.getId() && !this.node.pool.contains(n)) {
								this.node.pool.add(n);
							}
							System.out.print(n + "\t");
						}
					}
				break;
				//If the message is a QUERY, check if record exists
				//If not, forward the QUERY to neighbors
				case Message.QUERY:
					System.out.println("Receieved QUERY: " + message.getSearchString() + " : " + origin.getHost() +":" + origin.getPort());
					//QUERY message must not originate from this node
					if (! this.node.QUERY_CACHE.contains(message.getId())) {
						//Check if record exists
						if (this.node.search(message.getSearchString())) {
							try {
								//Create a QUERYHIT message
								//Message sender directly
								Message queryHit = new Message(this.node.toNeighbor(),8,this.node.getPort());
								//Set query string
								queryHit.setSearchString(message.getSearchString());
								//Set prev_id
								queryHit.setPrevId(message.getId());
								//Enqueue fileName to requested file queue
								this.node.fileQueue.add(message.getSearchString());
								//Reply with QHIT to sender
								this.node.send(queryHit, origin);
								System.out.println("\nSent HIT to: " + origin.getHost() +":" + origin.getPort());
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}
						//If record doesn't exist, forward
						else{
							//Forward QUERY to all neighbors (including originator)
							System.out.print("\tForward query : ");
							for (Neighbor n: this.node.getNeighbors()) {
								try {
									this.node.send(this.message,n);
									System.out.print(n + "\t");
								}
								catch (Exception ex) {
									System.err.print("Unable to forward message!\n" + ex.getMessage());
								}
							}
						}
					}
				break;
				case Message.QUERYHIT:
					//Check if query was initiated by self
					if (this.node.QUERY_CACHE.contains(message.getPrevId())) {
						//Direct download from message.getHost():message.getPort()
						try {
							System.out.println("Receieved HIT : " + origin.getHost() +":" + origin.getPort());
							//Fire up file downloader
							new Downloader(this.node,origin,message.getSearchString()).start();
																			
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				default:
				break;
			}
		}
	}
}