import java.util.Iterator;

/**
 * This class reaps stale neighbors from the.
 * neighborhood (Neighbors list :). Neihbors are
 * considered stale, if their last PONG was more than
 * 4X older than acceptable (Node SLEEP time).
 * @author wmabebe
 *
 */
public class Reaper extends Thread {
	private Node node;
	public Reaper(Node n) {
		this.node = n;
		System.out.println("Reaping stale neighbors...");
	}
	
	/**
	 * Infinite loop removes stale nodes from the
	 * neighbor's list;
	 */
	@Override
	public void run() {
		while(true) {
			
			synchronized(this.node.getNeighbors()) {
			
				//Use an iterator to avoid concurrent modification exception
				Iterator<Neighbor> iter = this.node.getNeighbors().iterator();
				while (iter.hasNext()) {
					Neighbor n = iter.next();
					if (node.live.get(n) != null && System.currentTimeMillis() - node.live.get(n)  >= (4 * 1000 * Node.SLEEP_TIME)) {
						//Remove from neighbors list
						iter.remove();
						//Remove from pool so it won't resurface into in the
						//neighbors list.
						synchronized(this.node.pool) {
							node.pool.remove(n);
						}
						System.out.println("\nTIMEOUT: Reaped stale neighbor " + n);
					}
				}
			
			}
			
			try {
				Thread.sleep(Node.SLEEP_TIME * 1000 * 2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
