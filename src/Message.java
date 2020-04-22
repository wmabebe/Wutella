import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
/**
 * This class represents the messages that
 * are transmitted amongst nodes. There are
 * several types of messages, PING, PONG,
 * QUERY, QUERYHIT, PUSH, DOWNLOAD.
 * @author wmabebe
 *
 */
public class Message implements Serializable {
	/**
	 * This random id value is used by nodes
	 * to avoid servicing the same message.
	 * Prev_id is important if the current
	 * message is a response to an earlier message.
	 */
	private long id,prev_id;
	/**
	 * Message descriptor values are final ints
	 */
	public static final int PING = 1 , PONG = 2, QUERY = 4, QUERYHIT = 8;
	private int descriptor;
	
	/**
	 * Query search string associated with a QUERY message.
	 * Also, response search string associated with REPLY.
	 */
	private String search;
	
	/**
	 * Time to live value for current message;
	 * This value decrements every time it's passed
	 * on to a new node. This message must die after
	 * 5 forwardings.
	 */
	private int TTL = 5;
	
	/**
	 * PONGs must return a list of neighbors associated
	 * with the PONGer.
	 */
	ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
	
	/**
	 * This variable identifies the originator of this
	 * message. It will contain Node.id, node.host, node.port.
	 * The latter two fields are redundant since message
	 * already has host and port fields.
	 */
	private Neighbor originator;
	
	/**
	 * Constructor takes in message descriptor value.
	 * @param descriptor
	 * @param port
	 * @throws UnknownHostException, IllegalArgumentException 
	 */
	public Message(Neighbor n,int descriptor,int port) throws UnknownHostException {
		if (descriptor != 1 && descriptor != 2 && descriptor != 4 && descriptor != 8 && descriptor != 16 && descriptor != 32 && descriptor != 64)
			throw new IllegalArgumentException("Unknown message descriptor value!\n Choose from [1,2,4,8,16]");
		if (port < 1024 || port > 64535)
			throw new IllegalArgumentException("Port has to be in range 1024 - 64535!");
		Random rand = new Random();
		//Safe to assume 0 id collision
		this.id = rand.nextLong();
		this.descriptor = descriptor;
		this.originator = n;
	}
	
	/**
	 * This method returns the originator
	 * of the message.
	 * @return originator
	 */
	public Neighbor getOriginator() {
		return this.originator;
	}
	
	
	/**
	 * Get message description value. 
	 * This action will decrement the TTL of the message.
	 * @return descriptor
	 */
	public int getDescriptor() {
		this.TTL --;
		return this.descriptor;
	}
	
	/**
	 * Get the time to live for this message.
	 * @return TTL
	 */
	public int getTTL() {
		return this.TTL;
	}
	
	/**
	 * Set previous message id, if any.
	 * @param id
	 */
	public void setPrevId(long id) {
		this.prev_id = id;
	}
	
	/**
	 * Get node id address
	 * @return id
	 */
	public long getId() {
		return this.id;
	}
	
	/**
	 * Get node prev_id
	 * @return id
	 */
	public long getPrevId() {
		return this.prev_id;
	}
	
	/**
	 * Set the message search string.
	 * @param str
	 */
	public void setSearchString(String str) {
		this.search = str;
	}
	
	/**
	 * Get the search string associated with
	 * this message.
	 * @return search
	 */
	public String getSearchString() {
		return this.search;
	}
}
