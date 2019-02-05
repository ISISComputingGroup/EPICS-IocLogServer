import socket
import argparse
import time
from six.moves import input
from datetime import datetime

DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 7004

RETRY_DELAY = 1

MSG_START = "<message>" \
            "<clientName>IOC_DEMO</clientName>" \
            "<type>SIM_MSG</type>" \
            "<severity>{severity}</severity>" \
            "<contents><![CDATA[{contents}]]></contents>" \
            "<eventTime>{time}</eventTime>" \
            "</message>"


def connect(host, port):
    started = False
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    host_and_port = host + ":" + str(port)
    addr = (host, port)
    
    while not started:
        try:
            sock.connect(addr)
            print("Connected to IOC log server at " + host_and_port)
            started = True
        except Exception:
            print("Could not establish connection to IOC log server at " + host_and_port)
            print("Will retry in {} seconds".format(RETRY_DELAY))
            time.sleep(RETRY_DELAY)

    return sock


def send_many(severity, num=1000):
    for i in range(num):
        if i % 100 == 0:
            print(i)
        sock.send((MSG_START.format(severity=severity, contents= str(i) + " auto gen message") + "\n").encode())

    if num % 100 != 0:
        print(i)


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('-p', '--port', nargs=1, type=int, default=[DEFAULT_PORT], help="The receiver's port")
    parser.add_argument('-host', '--host', nargs=1, type=str, default=[DEFAULT_HOST], help="The receiver's host address")
    args = parser.parse_args()

    port = args.port[0]
    host = args.host[0]

    severity = "MAJOR"

    print("\n------- ISIS IOC Log - Demonstration JMS Client -------")
    print("\n")
    
    # connect to target
    sock = connect(host, port)

    print("Enter message to send or 'exit' to exit, 'send many X' - to send X message, 'severity X' - to set severiy (MAJOR, MINOR or INFO):")
    print()

    while True:
        try:
            data = input("msg> ")
            if data.startswith("send many"):
                num = 1000
                try:
                    num = int(data.replace("send many ", ""))
                except:
                    print("Not a number using 1000")
                send_many(severity, num)
            elif data.startswith("severity "):
                severity = data.replace("severity ", "").upper()
            elif data == "exit":
                break    
            else:
                time_str = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f+00:00")
                sock.send((MSG_START.format(severity=severity, contents=data, time=time_str) + "\n").encode())
                
        except Exception as e:
            print("Lost connection to IOC Log server due to {}.".format(e))
            print("Attempting to reestablish")
            sock = connect(host, port)

    sock.close()
