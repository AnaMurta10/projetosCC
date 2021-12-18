import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FFSync {
    private static File folder = null;
    private static String peerID = null;
    private static int peerPort;
    private static int myPort;

    public static void main(String[] args) {
        System.out.println("Started FFSync!");

        if(!checkParams(args)) return;

        try {
            //FileManager responsible for all the file related operations
            FileManager fileManager = new FileManager(folder);

            //TCP connection to handle HTTP requests
            TCPConnection tcpConnection = new TCPConnection(fileManager);
            tcpConnection.start();

            //UPD connection to handle FTRapid Protocol
            DatagramSocket ds = new DatagramSocket(myPort);
            FTRapidRead udpRead = new FTRapidRead(ds, fileManager);
            FTRapidWrite udpWrite = new FTRapidWrite(ds, fileManager, InetAddress.getByName(peerID), peerPort);
            udpRead.start();
            udpWrite.start();

            tcpConnection.join();
            udpRead.join();
            udpWrite.join();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static Boolean checkParams(String[] args) {
        System.out.println("-> Checking params");

        int numArgs = args.length;

        if(numArgs == 0 || args[0] == null || args[0].trim().isEmpty()) {
            System.out.println("ERROR: Folder directory is missing.");
            return false;
        }
        File f = new File(args[0]);
        if(!f.exists()) {
            try {
                f.mkdirs();
                System.out.println("Folder Created.");
            } catch (Exception e) {
                System.out.println("ERROR: Folder directory invalid.");
                return false;
            }
        } else if(!f.isDirectory()) {
            System.out.println("ERROR: Folder directory invalid.");
            return false;
        }
        FFSync.folder = f;

        if(numArgs < 2 || args[1] == null || args[1].trim().isEmpty()) {
            System.out.println("ERROR: Peer IP is missing.");
            return false;
        }
        FFSync.peerID = args[1];

        if(numArgs < 3 || args[2] == null || args[2].trim().isEmpty()) {
            System.out.println("ERROR: Peer Port is missing.");
            return false;
        }
        FFSync.peerPort = Integer.parseInt(args[2]);

        if(numArgs < 4 || args[3] == null || args[3].trim().isEmpty()) {
            System.out.println("ERROR: My Port is missing.");
            return false;
        }
        FFSync.myPort = Integer.parseInt(args[3]);

        System.out.println("Folder Directory: " + folder.getAbsolutePath());
        System.out.println("Peer IP: " + peerID);
        System.out.println("Peer Port: " + peerPort);
        System.out.println("My Port: " + myPort);
        return true;
    }
}
