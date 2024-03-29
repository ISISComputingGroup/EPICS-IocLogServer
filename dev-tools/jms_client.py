"""A simple demonstration JMS client. Can be used to test the functionality
of the pyOC Log Server."""
import stomp
import time
import logging

import jms_details as JMS

logging.basicConfig()

RETRY_DELAY = 1


class MyListener(stomp.ConnectionListener):
    total_message_count = 1

    def on_error(self, frame):
        print('received an error %s' % frame.body)

    def on_message(self, frame):
        print(str(MyListener.total_message_count) + '. %s'% frame.body)
        MyListener.total_message_count += 1


def start_stomp_connection(stomp_connection):
    started = False
    while not started:
        try:
            jms_connection.connect(wait=True)
            jms_connection.subscribe(destination=JMS.MESSAGE_TOPIC, id=12, ack='auto')
            print("Connected to JMS at " + host_and_port)
            print("Listening for messages...")
            started = True
        except stomp.exception.ConnectFailedException:
            print("Could not establish connection to JMS at " + host_and_port)
            print("Will retry in {} seconds.".format(RETRY_DELAY))
            time.sleep(RETRY_DELAY)

    return started


if __name__ == '__main__':
    print("\n------- ISIS IOC Log - Demonstration JMS Client -------")
    jms_connection = stomp.Connection([(JMS.HOST, JMS.STOMP_PORT)])
    jms_connection.set_listener('somename', MyListener())

    host_and_port = "'" + JMS.HOST + ":" + str(JMS.STOMP_PORT) + "'"
    print("Attempting to connect to JMS at " + host_and_port)

    start_stomp_connection(jms_connection)

    while True:
        time.sleep(RETRY_DELAY)
        if not jms_connection.is_connected():
            print("Lost connection to JMS at " + (host_and_port+".") + "Attempting to restablish...")
            start_stomp_connection(jms_connection)

    print("Disconnected from JMS at " + host_and_port)
