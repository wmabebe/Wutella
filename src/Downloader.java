import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class  is responsible for downloading
 * files over tcp from remote nodes.
 * @author wmabebe
 *
 *Code adapted from http://www.codebytes.in/2014/11/file-transfer-using-tcp-java.html
 */
public class Downloader extends Thread {
	private Socket socket;
	private Node node;
	private Neighbor remote;
	private FileOutputStream fos;
    private BufferedOutputStream bos;
    private InputStream is;
    private String fileName;
	public Downloader(Node node,Neighbor remote,String fileName) throws UnknownHostException, IOException {
		this.fileName = fileName;
		this.node = node;
		this.remote = remote;
		//Connect to remote's tcp server
		//Notice remote's tcpPort = remote's udpPort + 1000
		this.socket = new Socket(InetAddress.getByName(remote.getHost()),remote.getPort() + 1000);
		this.fos = new FileOutputStream(node.getDir() +"/" + fileName);
		this.bos = new BufferedOutputStream(fos);
		this.is = socket.getInputStream();
	}
	
	@Override
	public void run() {
		byte[] contents = new byte[Node.BUFFER_SIZE];
		int bytesRead = 0; 
		System.out.println("Downloading...");
        try {
			while((bytesRead=is.read(contents))!=-1)
			    bos.write(contents, 0, bytesRead);
			bos.flush();
			socket.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        
        
        System.out.println(this.fileName + " downloaded...!");
	}
}
