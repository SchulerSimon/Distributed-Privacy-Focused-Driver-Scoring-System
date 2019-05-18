import socket
import json


server_socket = socket.socket()
port = 50505
server_socket.bind(('', port))

server_socket.listen(5)
print('listening on port', port)
while True:
    try:
        client, address = server_socket.accept()
        echo = client.recv(1000)
        response = b'Hello@' + address[0].encode() + b' Echo:' + echo
        client.send(response)
        client.close()
        print(response)
    except KeyboardInterrupt:
        try:
            if server_socket:
                server_socket.close()
        except:
            pass
    break
server_socket.shutdown
server_socket.close()
