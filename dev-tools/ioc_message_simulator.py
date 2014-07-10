import socket
import argparse
import time

DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 7004

RETRY_DELAY = 1


def connect(host, port):
    started = False
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    host_and_port = host + ":" + str(port)
    addr = (host, port)
    
    while not started:
        try:
            sock.connect(addr)
            print "Connected to IOC log server at", host_and_port
            started = True
        except Exception:
            print "Could not establish connection to IOC log server at", host_and_port
            print "Will retry in", str(RETRY_DELAY), "seconds."
            time.sleep(RETRY_DELAY)

    return sock

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('-p', '--port', nargs=1, type=int, default=[DEFAULT_PORT], help="The receiver's port")
    parser.add_argument('-host', '--host', nargs=1, type=str, default=[DEFAULT_HOST], help="The receiver's host address")
    args = parser.parse_args()

    port = args.port[0]
    host = args.host[0]

    print "\n------- ISIS IOC Log - Demonstration JMS Client -------"
    
    # connect to target
    sock = connect(host, port)
    
    print "Enter message to send or type 'exit': "
    while True:
        data = raw_input("msg> ")

        sock.send(data + "\n")

        if data == "exit":
            break

    sock.close()

