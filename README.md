# Networking-TCP_CD_Store

Goal: In this assignment your task is to design and implement a simple Online Purchase System, which will
consist of an on-line music CD store, and a banking system. A user should be able to visit your music store
website using a regular web browser (Firefox). The user can then purchase CDs to his/her liking and pay for
them using his/her credit card. You have to design a simple web server for this store and also another
independent program that implements the banking system, for verifying the user credit card information before
allowing the purchase to be approved.

Specification:
The following illustration will clarify the whole system:You will implement the following two programs:

Bank
 The bank program will accept one argument: BANK_PORT, which is the port number on which it has to
open a listening TCP socket. Note that this argument will have to be greater than 1023 (recall that 0 to
1023 are the well-known ports).
 This program is a simple server that verifies if the current transaction should be processed. The server
maintains a database, which contains the following information about each user: first name, family
name, postcode, credit card number, balance and available credit (in this exact order). The database will
be maintained as a simple text file. The following shows an example entry of the database:
First Family Postcode Credit Card Balance Credit
Jim Morrison 2052 12345678 100 1000
Eric Clapton 2019 53466207 40 500
You have to maintain a simple database file , called “database.txt”. Make sure that your server can read
and write into this file. Note that, the format of each entry in the database is exactly similar to the above.
The postcode and credit card numbers will be 4 and 8 digit numbers respectively. Also the balance and

Socket 4
HOST: BANK_HOST_IP
PORT: BANK_PORT
BANK STORE

Client
Browser

TCP connection
(exchange HTTP
request/response)

Socket 2
HOST: STORE_HOST_IP
PORT: STORE_PORT

TCP connection
(request/response
to verify purchase
information)

Socket 1
(Created by
browser)

Socket 3
HOST: STORE_HOST_IP
PORT: let OS pick this

credit will be specified as whole integers (no fractional part). We will use this database.txt file while
marking your assignment.
The bank server will open a listening socket on the specified port (BANK_PORT) and wait for a TCP
connection from the store. When a user initiates a purchase, the store program opens a TCP connection
with this listening socket. The store program will then send all the user information obtained from the
web form (see the description of the Store program for more details about the form) over this
connection. There is no fixed format for the messages that will be exchanged between the bank and the
store. You may choose any format that you think is suitable for this.
 The bank server then has to verify the transaction and send an appropriate response message to the store.
There can be three possible responses:
(a) The bank has to first check if the first four fields (first and family name, postcode and credit card
number) match any of the entries in the database. If no correct match is found for all the four fields, an
error indication is sent back to the store. The store should send back an HTML message to the client
browser indicating a message along the following lines: “The user information entered is invalid”. The
wordings of the message could be different but it should convey that the account data did not generate a
match.
(b) If the above check is OK, the bank server next checks if the user has sufficient credit available to pay
for the current purchase. If this is not true, i.e. for example the user is requesting to purchase items worth
100$ when he/she only has credit for 50$, then an appropriate error message is sent back to the store. On
receiving this error message, the store should send back an appropriate HTML message to the client
browser stating that “Your account does not have sufficient credit for the requested transaction” or
something along these lines.
(c) If the user has available credit for the purchase, a message indicating purchase approval is sent back
to the store. The store will then send back a “Transaction Approved” HTML message (or something
similar) back to the client browser. Note that if the purchase has been approved, the bank program
should also modify the available credit and balance for the user accordingly. For example, if the current
purchase was worth 20$, then 20$ should be added to the balance and 20$ should be subtracted from the
available credit for that user.
To summarize the response message sent from the bank to store must belong to one of the above 3
categories. The store on receiving this message should inform the user about the status of the transaction
(The message sent by the store to the user’s browser needs to be in the form of an HTML page, which
can be displayed by the browser).
 Once the bank server has sent back one of the above messages to the store, the on-going TCP connection
between the bank and the store should be closed. The bank should then open another listening socket and
wait for a new TCP connection from the store.
 Note that, the bank program need only handle one TCP connection from the store at any given time (i.e.
the bank does not have to deal with multiple connections at the same time). This simplifies your task
considerably.
 To terminate the bank server, the user should type CTRL-C at the command prompt to kill the process.
Store
 The store program will accept 3 command-line arguments: (i) STORE_PORT- the port number that the
store web server will listen for an incoming connection. (ii) BANK_HOST_IP – the IP address of the
host machine for the bank server and (iii) BANK_PORT – the port number at which the bank server will
be listening for a new connection. (This must correspond to the BANK_PORT command line argument
used with the bank program)
 This program will primarily implement the web server for your Internet CD store. The store server will
open a listening TCP socket on port number STORE_PORT and wait for a connection from the user
browser. Your server may implement either version 1.0 or 1.1 HTTP (i.e. non-persistent HTTP or
persistent HTTP). (Version 1.0 is easier)

 The user will open a web browser (Firefox) and type in the following URL:
http://STORE_HOST_IP:STORE_PORT/index.html into the URL field, where STORE_HOST_IP is the
IP address of the machine on which the store server is currently running and STORE_PORT is the first
command-line argument. This will result in a TCP connection being established with the web server and
a GET request for the index.html will be sent to the store server over this connection.
 The default web page (index.html) that you have to use for the web server must be designed by you.
This page must be in the same directory, which contains the store program. When we mark your
assignment, we will use your index.html page to run all the tests.
 Upon receiving the GET HTTP request (as discussed above), your store server should send back a HTTP
response message containing the index.html page to the browser. If you are implementing version 1.1 of
HTTP then the connection between the store and bank must be closed following the transfer of this
response message.
 The index page provides the user with the information about available CDs, cost, etc. There is also a
form included, which allows the user to enter the desired item number, the quantity, first and last name,
postcode and credit card information.
 Once this information has been entered, the user will hit the submit button (If implementing HTTP
version 1.0, a new TCP connection will be established between the browser and the store server).
Following this, the POST method is executed, which will send all the user information to the store
server. Your server needs to implement a procedure to process the POST message received from the
browser.
 You may assume that all information entered by the user is in the correct format i.e. the name fields will
always contain alphabets; item number, quantity, postcode and credit card number are all numbers. You
do not have to worry about handling these errors. However, note that the bank still has to verify that all
the four fields (first and last name, postcode and credit card number) for the current user match an entry
in the database.
 Upon receiving the information about the current transaction, the store needs to verify with the bank if
the transaction can be processed. The store establishes a TCP connection with the bank (recall that the
bank upon start-up opens a listening socket and awaits a TCP connection request) Note that, the
information about the bank socket is available as the second and third command-line argument:
BANK_HOST and BANK_PORT.
 After establishing the connection, the store will send all the user information to the bank, which will
then verify the authenticity of the user information and also if there is sufficient credit available. The
bank will respond with an appropriate response to the store. Recall from the specification of the bank
program that there are 3 possible responses. Once the message is received from the bank the connection
between the bank and the store should be closed.
 On receiving the bank response, the store web server has to send back a HTTP response message to the
user browser containing an HTML page with a simple text message. The specification of the bank
discusses this in further detail. The exact text to be included within the HTML page is for you to decide.
This text should convey the appropriate information to the customer (e.g: “Transaction Approved”,
“Insufficient Credit”, “User Information Invalid”).
 Following this, the TCP connection with the browser should be closed (for both HTTP version 1.0 and
1.1). The store should then open a new listening TCP socket on port number STORE_PORT.
 Your store server should only serve one client (browser) at any given time. You do not have to worry
about dealing with multiple clients.
 As explained in the bank specification, you are free to define the message formats of the messages
exchanged between the store and the bank.
 To terminate the store server one should type CTRL-C to kill the process.
