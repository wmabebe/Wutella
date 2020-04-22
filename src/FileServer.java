import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is responsible for sending requested
 * files over a tcp connection.
 * @author wmabebe
 * 
 * Code adapted from
 * http://www.codebytes.in/2014/11/file-transfer-using-tcp-java.html
 */
public class FileServer extends Thread {
	private Node node;
	public FileServer(Node n) {
		this.node = n;
	}
	
	/**
	 * This method first checks if the requested
	 * file exists in the node's shared directory,
	 * If YES, it starts a tcp connection.
	 */
	@Override
	public void run() {
		//Node's tcpPort =  udpPort + 1000
		ServerSocket ssock;
		try {
			ssock = new ServerSocket(node.getPort() + 1000);
			System.out.println("File server up...");
			while(true) {
				Socket socket = ssock.accept();
				System.out.println("Recieved download request!");
				//Remove and serve fileName from top of the queue
				String fileName = node.fileQueue.remove();
				new FileHandler(node,socket,fileName).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class FileHandler extends Thread{
	private Node node;
	private File file;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private OutputStream os;
	private Socket socket;
	public FileHandler (Node node,Socket socket,String fileName) throws IOException {
		this.socket = socket;
		this.node = node;
		this.file = new File(node.getDir() + "/" + fileName);
		this.fis = new FileInputStream(file);
		this.bis = new BufferedInputStream(fis);
		this.os = socket.getOutputStream();
	}
	
	@Override
	public void run() {
		byte[] contents;
        long fileLength = file.length(); 
        long current = 0;
         
        System.out.println("Sending file....");
        //long start = System.nanoTime();
        while(current!=fileLength){ 
            int size = Node.BUFFER_SIZE;
            if(fileLength - current >= size)
                current += size;    
            else{ 
                size = (int)(fileLength - current); 
                current = fileLength;
            } 
            contents = new byte[size]; 
            try {
				bis.read(contents, 0, size);
				os.write(contents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            
            System.out.println((current*100)/fileLength+"% complete!");
        }   
        
        try {
			os.flush();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}