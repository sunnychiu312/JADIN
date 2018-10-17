JADIN

Compile Java files

CD into the src/Client and then src/Server directory
and use the following command to compile:
javac -cp '.:json-simple-1.1.1.jar' *.java

CD into the src/Hub director and use the following
command to compile:
javac *.java

Execute Java program for Server, Hub, and Client

1) Server program

There are three server main files setup to run three
different servers. To run each server, cd into the
server directory and run the commands below.

java ServerConHub
java ServerConServer
java ServerConServer2

Other servers can be made with different ip address and
port like the three server main files. The server will
connect to a server listed in the server_address.conf.

2) Hub program

The hub program can be executed with three different
aliases: hubone, hubtwo, hubthree. Each alias
corresponds to a ip address and port in the file.conf.
To run a hub program, cd into the hub direct and run
the command below.

java Main file.conf alias

The file.conf contains information about the all the
hub address, client keys, and server address.

3) Client program

Multiple Client programs can be ran at the same time as
long as the correct keys are input in execution:
keyone, keytwo, keythree. To run the client, cd into
the client directory and run the command below.

java ClientConHub key

The program will start with a "write, read, or quit"
prompt. Please follow the prompts to continue using the
client application. The client request will be
automatically processed by a hub and server.
