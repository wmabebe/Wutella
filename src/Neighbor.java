import java.io.Serializable;

/**
 * Concise representation of a Node to avoid 
 * having to maintain an ArrayList of bulky 
 * Nodes in the neighbors list.
 * @author wmabebe
 *
 */
class Neighbor implements Comparable,Serializable{
	private long id;
	private int port;
	private String host;
	public Neighbor(long id,String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Override this method to avoid
	 * equal instances being added to
	 * a HashSet.
	 */
	@Override
	public int hashCode() {
		return host.hashCode() + port;
	}
	
	/**
	 * Check if argument equal to this
	 * object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Neighbor) {
			Neighbor n = (Neighbor) obj;
			return this.id == n.id;
		}
		return false;
	}
	
	/**
	 * Compare this neighbor to another neighbor
	 * object.
	 */
	@Override
	public int compareTo(Object obj) {
		Neighbor n = (Neighbor) obj;
		if (this.id == n.id)
			return 0;
		return this.id - n.id > 0 ? 1 : -1;
	}
	
	/**
	 * Get the string representation of a neighbor.
	 */
	@Override
	public String toString() {
		return "N_" + (this.id % 1000) + " - " + this.getHost() + ":" + this.getPort();
	}
}
