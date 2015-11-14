import socket
import sys
import select
import Queue
HOST = sys.argv[1]    # The remote host
try:
    PORT = int(sys.argv[2])
except:
    PORT = ''

def promp_me(msg) :
    #sys.stdout.write('<You> ')
    #sys.stdout.flush()
    print "<You>: " + msg
def promp_him(msg):
    #sys.stdout.write('\t\t<Him> ')
    #sys.stdout.flush()
    print "\t\t\t<Him>: " + msg
def client():
    
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect((HOST, int(PORT)))
    s.settimeout(2)
    while True:
        socket_list = [sys.stdin, s]
        read_sockets, write_sockets, error_sockets = select.select(socket_list , [], [])
        for sock in read_sockets:
            #incoming message from remote server
            if sock == s:
                data, addr = sock.recvfrom(1024)
                if not data :
                    print '\nDisconnected from chat server'
                    sys.exit()
                else :
                    #sys.stdout.write(data)
                    promp_him(data)
             
            #user entered a message
            else :
                msg = sys.stdin.readline()
                s.send(msg+"dfgjgfgjgdfhjffhjdjjddf")
#                promp_me(msg)

def servidor():
    
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind((HOST, int(PORT)))
    #s.listen(1)
    #conn= s.accept()
    s.settimeout(2)
    while True:
        socket_list = [sys.stdin, s]
        read_sockets, write_sockets, error_sockets = select.select(socket_list , [], [])
        for sock in read_sockets:
            #incoming message from remote server
            if sock == s:
                data, addr = sock.recvfrom(1024)
                if not data :
                    print '\nDisconnected from chat server'
                    sys.exit()
                else :
                    #print data
                    # sys.stdout.write(data)
                    promp_him(data)
                    if data == "GPS":
                        s.sendto("GPS!54.322;32.123;2.3;4.5;",addr)
                    elif data == "Weather":
                        s.sendto("Weather!54.322;32.123;091123;400.3",addr)
                    elif data == "Stop":
                        s.sendto("OK",addr)
             
            #user entered a message
            else :
                msg = sys.stdin.readline()
                s.sendto(msg,addr)
 #               promp_me(msg)



if __name__ == "__main__":

    if PORT == '':
        PORT = HOST
        HOST = ''
        servidor()
    elif HOST != '' and PORT != '':
        client()
    else:
        print "ERROR"

