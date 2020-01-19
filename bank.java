import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Dictionary;
import java.util.StringTokenizer;


class BankResponse{


    public  static String[] messageLines(DataInputStream dis){
        String line = "";
        String[] message = new String[150];



        for(int i=0; i<4; i++)
        {
            try {
                line = dis.readUTF();
                message[i]=line;
            }catch (IOException e)
            {
                System.out.println("messageLine thread: "+e);
            }

        }
        return message;
    }


    public  static boolean checkUser(String messages[], String database){

//        BufferedReader br;

        try {
            BufferedReader br = new BufferedReader( new FileReader("database.txt"));
            String line = br.readLine();

            while (line != null){

                StringTokenizer tokenizer = new StringTokenizer(line,",");
                String firstName, familyName, postCode, creditNo;

                firstName =  tokenizer.nextToken();
                familyName = tokenizer.nextToken();
                postCode = tokenizer.nextToken();
                creditNo = tokenizer.nextToken();


                // checking user is valid or not

                if(firstName.equals(messages[0]) && familyName.equals(messages[1])&& postCode.equals(messages[2]) && creditNo.equals(messages[3])){
                    return  true;
                }
                line = br.readLine();

            }


        }catch (Exception e)
        {
            System.out.println("checkUser thread: "+e);
        }
        return  false;
    }


    public static boolean checkCredit(String amount,String credit,String database){
        BufferedReader br;

        try {
            br= new BufferedReader(new FileReader(database));
            String line = br.readLine();

            while (line != null){
                StringTokenizer tokenizer = new StringTokenizer(line,",");  //spilt the string
                String userAmount="", usercreditNo="",userCredit="";

                for(int i=0; i<4; i++)usercreditNo= tokenizer.nextToken();

                userAmount = tokenizer.nextToken();
                userCredit = tokenizer.nextToken();
                if(credit.equals(usercreditNo))
                {
                    ///converting string to integer

                    int userAmountInt = Integer.parseInt(userAmount);
                    int productPrice = Integer.parseInt(amount);
                    int userCreditInt = Integer.parseInt(userCredit);

                    //checking credit for the requested transaction

                    if(productPrice <= (userCreditInt - userAmountInt)){
                        return true;
                    }
                }

                line = br.readLine();
            }

        }catch (Exception e){

            System.out.println("checkCredit thread:"+e);

        }

        return  false;
    }

    public static void updateDatabase(String amount, String creditNo, String database) {

        try{

            BufferedReader br = new BufferedReader(new FileReader(database));

            String line = br.readLine();
            String newRecord="";

            while (line != null)
            {
                String check[] = line.split(",");

                ///converting string to integer

                int userBalance = Integer.parseInt(check[4]);
                int inputAmount = Integer.parseInt(amount);
                int userCredit = Integer.parseInt(check[5]);

                String userCreditNo = check[3];

                if(creditNo.equals(userCreditNo)){

                    userCredit-= inputAmount;
                    userBalance +=  inputAmount;

                }

                check[4] = Integer.toString(userBalance);
                check[5] = Integer.toString(userCredit);

                line = String.join(",",check);
                newRecord += line+"\n";

                line = br.readLine();


            }

            br.close();
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(database));
            bw2.close();

            //updating in database file

            BufferedWriter bw = new BufferedWriter(new FileWriter(database,true));
            bw.write(newRecord);
            bw.close();


        } catch (Exception e) {

            System.out.println("checkCredit thread:" + e);

        }


    }

}


public class bank {



    public static void main(String[] args) throws  Exception {

        if(args.length != 1) {System.out.println("Incorrect Number of Arguments"); return;}
        int BANK_PORT = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(BANK_PORT);

        while (true)
        {
            System.out.println("Socket is created at port: "+BANK_PORT);
            Socket clientSocket = serverSocket.accept();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String[] messages = new String[150];
            DataInputStream   DIS = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            DataOutputStream DOS = new DataOutputStream(clientSocket.getOutputStream());

            messages = BankResponse.messageLines(DIS); // message request from store


            if(BankResponse.checkUser(messages,"database.txt")){
                DOS.writeUTF("Valid User");

                String amount, credit,pay;

                amount = DIS.readUTF();
                credit = DIS.readUTF();
                pay = DIS.readUTF();

                if(pay.equals("Purchase")) {
                    if(BankResponse.checkCredit(amount, credit, "database.txt") ){
                        BankResponse.updateDatabase(amount, credit, "database.txt");
                        DOS.writeUTF("Successfully purchased.");
                    }else{
                        DOS.writeUTF("Insufficient credit to purchase.");
                    }
                }
            }else {
                try {
                    DOS.writeUTF("The user information entered is invalid");
                }catch (IOException e){
                    System.out.println("Main thread: "+e);
                }

            }

            clientSocket.close();
            DIS.close();
            System.out.println("Connection Closed");
        }

    }

}
