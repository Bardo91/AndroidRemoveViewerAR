import numpy as np
import cv2

import socket


# create an INET, STREAMing socket
serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# bind the socket to a public host, and a well-known port
serversocket.bind((socket.gethostname(), 9009))
# become a server socket
serversocket.listen(5)

print("Waiting for an input connection")
(clientsocket, address) = serversocket.accept()
print("Connected to client")
print(address)
cap = cv2.VideoCapture(0)
i = 1
while(True):
	if(i>6):
		i=1
			
	data = clientsocket.recv(1024)
	print("Received petition of data")

	#img = cv2.imread('C:/Users/Bardo91/Desktop/opcion'+str(i)+'.jpg',0)
	ret, img = cap.read()

	imgCompressed = cv2.imencode('.jpg', img, [int(cv2.IMWRITE_JPEG_QUALITY) , 30])[1]
	
	sizeStr = str(len(imgCompressed))
	print(sizeStr)
	clientsocket.sendall(sizeStr)
	print("Sent size of image")
	data = clientsocket.recv(1024)

	i = i+1
	clientsocket.sendall(imgCompressed)
	print("Sent  data")
	
	
	
