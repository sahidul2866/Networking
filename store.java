import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class store implements Runnable{  // We are using threading(though it was not necessary) because we collected the basic web server idea(with a portion of code) from medium.com

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "error.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    // port to listen connection

    // verbose mode
    static final boolean verbose = true;
    static int PORT,BankPort;
    static String BankHost;

    // Client Connection via Socket Class
    private Socket connect;

    public store(Socket c) {
        connect = c;
    }

    public static void main(String[] args) {
        try {
            //Checking number of arguments
            if(args.length != 3) {System.out.println("Incorrect Number of Arguments"); return;}
            PORT = Integer.parseInt(args[0]);
            BankHost = args[1];
            BankPort = Integer.parseInt(args[2]);

            //Creating a server socket to wait for a connection from browser
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {

               store myServer = new store(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken().toLowerCase();


            if (input.contains("POST")) {
                String str = null;
                // Reading header part of post method
                while ((str = in.readLine()).length() != 0) System.out.println(in.readLine());

                //Reading payload of post method. BufferedReader is not working here for a blank line between header and payload or between two parts of payload. So WE USED STRINGBUILDER.
                StringBuilder payload = new StringBuilder();
                while (in.ready()) {
                    payload.append((char) in.read());
                }
                String name = "", fam = "", post = "", creno = "", itemno = "", qun = "";
                String userinfo = payload.toString();
                //Getting value of inputs given by user.
                String infos[] = userinfo.split("=");
                for (int i = 1; i < infos.length; i++) {
                    String info[] = infos[i].split("&");
                    if (i == 1) name = info[0];
                    if (i == 2) fam = info[0];
                    if (i == 3) post = info[0];
                    if (i == 4) creno = info[0];
                    if (i == 5) itemno = info[0];
                    if (i == 6) qun = info[0];
                }

                //Establishing connection with the bank
                Socket s = new Socket(BankHost, BankPort);
                DataInputStream din = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                //Send first four inputs to verify wheather user is valid or not.
                for (int i = 0; i < 4; i++) {
                    if (i == 0) dout.writeUTF(name);
                    if (i == 1) dout.writeUTF(fam);
                    if (i == 2) dout.writeUTF(post);
                    if (i == 3) dout.writeUTF(creno);
                    dout.flush();
                }
                String reply = din.readUTF();  //getting reply from bank
                //if user isinvalid then showing a html file which shows invalid user
                if (reply.equals("The user information entered is invalid")) {
                    dataOut.write(ProcessHTML("InvalidUser.html").getBytes());
                    dataOut.flush();
                } else {
                    //Sending total required amount by multiplying the product price with quantity. Note:Here amount of CDs is hardcoded(40,100,120) and quantity is taken as input by the user.
                    if (Integer.parseInt(itemno) == 1) { Integer a = Integer.parseInt(qun)*40; dout.writeUTF(a.toString()); }
                    if (Integer.parseInt(itemno) == 2) { Integer a = Integer.parseInt(qun)*100; dout.writeUTF(a.toString()); }
                    if (Integer.parseInt(itemno) == 3) { Integer a = Integer.parseInt(qun)*120; dout.writeUTF(a.toString()); }
                    dout.writeUTF(creno);
                    dout.writeUTF("Purchase");
                    reply = din.readUTF();
                }
                System.out.println(reply);
                // Just showing simple html file wheather purchase is completed or not.
                if (reply.equals("Successfully purchased.")) {
                    dataOut.write(ProcessHTML("TransactionApproved.html").getBytes());
                    dataOut.flush();
                } else {
                    dataOut.write(ProcessHTML("InsufficientCredit.html").getBytes());
                    dataOut.flush();
                }
                //finally closing the connection
                din.close();
                dout.close();
                s.close();
                connect.close();
                System.out.println("Connection closed.\n");

            }

            // we support only GET and HEAD methods, we check
            else if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                // we return the not supported file to the client
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                //read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                // we send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println(); // blank line between headers and content, very important !
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }

                if (verbose) {
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }

            }

        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }


    //Returns the html code of the file "filename"
    public String ProcessHTML(String filename)
    {
        String data="";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));

            String str = "";
            while ((str = br.readLine()) != null) {
                data += str+"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message="";
        message += "HTTP/1.1 200 OK\n";
        message += "Content-Length: " + data.length() + "\n";
        message += "Content-Type: text/html\n";
        message += "Connection: keep-alive\r\n";
        message += "\r\n";
        message += data;
        return message;
    }
}
