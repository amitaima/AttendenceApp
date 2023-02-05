# coding=utf-8
import atexit
import socket
import sys
import sqlite3
from _socket import SOL_SOCKET, SO_REUSEADDR
from sqlite3 import Error
import random
import difflib
# from difflib_data import *
import os, time
import datetime
import filecmp
import codecs
from datetime import timedelta
from datetime import date
import threading
from datetime import datetime
from threading import Timer
from shutil import copyfile
from sqlalchemy.pool import SingletonThreadPool
# REMINDER: To install modules enter pip install line after "python -m" in cmd!

# HOST = '89.139.208.233'
# HOST = '192.168.43.43' #Phone Internet
HOST = '0.0.0.0'
# HOST = '192.168.5.200'
# HOST = '10.100.102.116' #Home Router
PORT = 443


#inserts employees from given file of employees into the table "employees"
def employee_insertion():
    emp_file = open("employees.txt").read()
    a = emp_file.splitlines()
    print(a)
    for i in range(0,59):
        a[i] = a[i].split()
    print(a)
    print (a[0][1])
    query = '''INSERT INTO employees VALUES (?, ?, ? , ?, ?)'''
    for i in range(0,59):
        id = a[i][0]
        name = a[i][1] + " " + a[i][2]
        departure = ''
        for j in range(4,len(a[i])):
            departure = departure + a[i][j] + " "
        filepath = str(i+1) + '.jpg'
        favorite = 0
        print (id)
        print (name)
        print (departure)
        c.execute(query, (id, name, filepath, favorite, departure))
    return "Added employees successfully"


#Get Employees for start of app
def get_employees():
    global before
    before=''
    finalSend=''
    query = "SELECT * FROM employees"
    all = db.execute(query)
    allEmps = all.fetchall()
    if allEmps != None:
        for i in range(0,len(allEmps)):
            send = str(allEmps[i][0]) + " " + allEmps[i][1] + " " + str(allEmps[i][3]) + " " #allEmps[i][1].encode('utf-8')
            if u"משרד" in (allEmps[i][4]):
                send += "0 "
            elif u"פלסטיק" in allEmps[i][4]:
                send += "1 "
            elif u"יצור" in allEmps[i][4]:
                # if u"עפולה" in allEmps[i][4]:
                #     send += "4 "
                # else:
                send += "2 "
            else:
                send += "3 "
            # send += str(allEmps[i][5]) + " "
            send += "0 "
            if len(allEmps[i][2])>2:
                send += allEmps[i][2][20:-4] + " "
            else:
                send += "- "
            finalSend = finalSend+send+","
            print(i)
        j=0
        finalLen = len(finalSend)
        sendNow=""
        print(finalLen)
        while 1:
            if finalLen-j>500:
                sendNow = finalSend[j:500+j]
                print ("sending: " + "3")
                conn.send("3".encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                print (("sending: " + str(500 + len(sendNow[:250]))))
                conn.send(str(500 + len(sendNow[:250])).encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                print ("sending: finalSend["+str(j)+":" +str(500 + j)+"]")
                print(sendNow)
                conn.sendall((sendNow+"/"+sendNow[:250]).encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                j+=500
            else:
                sendNow = finalSend[j-2:] + "/" + finalSend[j-2:]
                print ("sending last: " + str(len(str(len(sendNow)))))
                conn.send(str(len(str(len(sendNow)))).encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                print ("sending: " + str(len(sendNow)))
                conn.send(str(len(sendNow)).encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                print ("sending: finalSend")
                print(sendNow)
                conn.send(sendNow.encode('utf-8'))
                data1 = conn.recv(10)
                print (data1)
                break
        conn.send("0".encode('utf-8'))
        print (conn.recv(10))


# def get_employees():
#     global before
#     before=''
#     finalSend=''
#     query = "SELECT * FROM employees"
#     all = db.execute(query)
#     allEmps = all.fetchall()
#     if allEmps != None:
#         for i in range(0,len(allEmps)):
#             send = str(allEmps[i][0]) + " " + allEmps[i][1] + " " + str(allEmps[i][3]) + " " #allEmps[i][1].encode('utf-8')
#             if u"משרד" in (allEmps[i][4]):
#                 send += "0 "
#             elif u"יצור" in allEmps[i][4]:
#                 if u"עפולה" in allEmps[i][4]:
#                     send += "4 "
#                 else:
#                     send += "2 "
#             else:
#                 send += "3 "
#             # send += str(allEmps[i][5]) + " "
#             send += "0" + " "
#             finalSend = finalSend+send+","
#         j=0
#         finalLen = len(finalSend)
#         print(finalLen)
#         while 1:
#             if finalLen-j>700:
#                 print ("sending: " + "3")
#                 conn.send("3".encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 print (("sending: " + "699"))
#                 conn.send("699".encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 print ("sending: finalSend["+str(j)+":" +str(699 + j)+"]")
#                 print(finalSend[j:699+j])
#                 conn.sendall(finalSend[j:699+j].encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 j+=699
#             else:
#                 print ("sending last: " + str(len(str(finalLen-j-2))))
#                 conn.send(str(len(str(finalLen-j-2))).encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 print ("sending: " + str(len(finalSend[j-1:])))
#                 conn.send(str(len(finalSend[j-2:])).encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 print ("sending: finalSend")
#                 print(finalSend[j-2:])
#                 conn.send(finalSend[j-2:].encode('utf-8'))
#                 data1 = conn.recv(10)
#                 print (data1)
#                 break
#         conn.send("0".encode('utf-8'))
#         print (conn.recv(10))

#recieves line of attendence from file and creates a message with the neede data to send to the client
def get_attendence(added_line):
    id = added_line[1]
    query = "SELECT * FROM employees WHERE id=" + id;
    curEmp = db.execute(query)
    currentEmp = curEmp.fetchall()
    if len(currentEmp)==0:
        print ("The employee ID = " + id + " is not in the system, please add")
        return 0
    print (currentEmp[0][4])
    now = datetime.now()
    date = added_line[2].split('/')
    time = added_line[3].split(':')
    if added_line[4] == "IN":
        in_or_out = 1
    elif added_line[4] == "OT":
        in_or_out = 0
    else:
        print ("There has been a problem with the program")
    if int(date[0]) == now.day and int(date[1]) == now.month and (int(date[2])+2000) == now.year: #checks if the date is good for today
        if u"משרד" in (currentEmp[0][4]) or u"ניקיון" in (currentEmp[0][4]) or u"נקיון" in (currentEmp[0][4]):
            if time[0] < '08' or (time[0] == '08' and time[1] == '00'):
                on_time = 1
            else:
                on_time = 0
        else:
            if time[0] < '07' or (time[0] == '07' and time[1] == '00'):
                on_time = 1
            else:
                on_time = 0
    else:
        print ("The given report line does not match todays date and will not be sent")
        return 0
    if in_or_out == 1:
        status = on_time+1
    else:
        if time[0] > '16' or (time[0] == '16' and time[1] > '00'):
            status = 0
        else:
            status = 3
    send = id + " " + str(status)
    query = "UPDATE employees SET status=" + str(status) + " WHERE id=" + str(id)
    db.execute(query)
    print (send)
    conn.send(send.encode())
    return 1

#gets message with person to change and 1 - to add and 0 - to remove
def change_favorites(message): #message exapmle: "5 1"
    print (message)
    splitmsg = message.split()
    id = splitmsg[0]
    add_or_remove = splitmsg[1]
    add_favorite="UPDATE employees SET favorites = " + add_or_remove + " WHERE id = " + id #updates the table
    db.execute(add_favorite)
    db.commit()
    conn.send("next".encode())
    print ("sent next request")

def get_week_attendence(message): #message example: "012"
    monthFile = open("michpal_month.txt").read().splitlines()
    firstIn = ""
    lastOut = ""
    print ("entered get week")
    # now = datetime.datetime.now()
    today = date.today()
    for i in range(0,len(monthFile)):
        for j in range(0,7):
            checkDay = today - timedelta(days=j)
            splitDay = str(checkDay).split("-")
            splitFile = monthFile[i].split(" ")
            splitFileDay = splitFile[2].split("/")
            if int(splitFileDay[1]) == int(splitDay[1]) and int(splitFileDay[0]) == int(splitDay[2]):
                if int(message) == int(splitFile[1]):
                    if "IN" in monthFile[i]:
                        firstIn = splitFile[3]
                        print(firstIn)
                    else:
                        lastOut = splitFile[3]
                        print(lastOut)
                        conn.send((splitFile[2] + " " + firstIn + " " + lastOut).encode())
                        print (splitFile[2] + " " + firstIn + " " + lastOut)
                        print (conn.recv(10))

def get_month_attendence(message): #message example: "012"
    monthFile = open("michpal_month.txt").read().splitlines()
    firstIn = ""
    lastOut = ""
    print ("entered get month")
    for i in range(0,len(monthFile)):
        splitFile = monthFile[i].split(" ")
        if int(message) == int(splitFile[1]):
            if "IN" in monthFile[i]:
                firstIn = splitFile[3]
            else:
                lastOut = splitFile[3]
                conn.send((splitFile[2] + " " + firstIn + " " + lastOut).encode())
                print (conn.recv(10))

def get_favorites():
    query = "SELECT * FROM employees WHERE favorites = 1"
    fav = db.execute(query)
    allfav = fav.fetchall()
    favlist = []
    if allfav != None:
        for i in range(0,len(allfav)):
            favlist.append(allfav[i][0]) #creates list of id's of favorites
    print (' '.join(str(e) for e in favlist))
    return ' '.join(str(e) for e in favlist)


def update_month():
    print ("updating month")
    with open("michpal_month.txt", "a") as myfile:
        myfile.write(open("michpal.txt").read())
    # with open("M:\AttendenceApp\clock.txt") as infile, open('michpal_month.txt', 'a') as outfile:
    #     for line in infile:
    #         if not line.strip(): continue  # skip the empty line
    #         outfile.write(line)  # non-empty line. Write it to output
    fileMonth = open("michpal_month.txt").read().splitlines()
    splitedLine = fileMonth[0].split(" ")
    i=1
    print(splitedLine[2])
    print(len(fileMonth))
    while splitedLine[2] in fileMonth[i]:
        i=i+1
        print (i)
    with open('michpal_month.txt', 'w') as fout:
        fout.writelines(map(lambda s: s + '\n', fileMonth[i:]))
        print ("done")

def delete_not_relevent():
    print ("deleting wrong dates")
    today = date.today()
    fileMonth = open("michpal.txt").read().splitlines() #"M:\AttendenceApp\clock.txt"
    i=0
    print (str(today.day) + "/")
    while str(today.day) + "/" not in fileMonth[i]:
        i=i+1
    print (i)
    with open('michpal.txt', 'w') as fout: #"M:\AttendenceApp\clock.txt"
        fout.writelines(map(lambda s: s + '\n', fileMonth[i:]))
        print ("done")


def manage_data(conn,addr):
    global before
    while 1:
            print ("recieving...")
            data = str(conn.recv(25).decode('utf_8'))
            if not data:
                # client closed the connection
                break
            # while data == None or data == "":
            #     time.sleep(2)
            #     print "recieving again..."
            #     data = conn.recv(25)
            print (data)
            if "get favorites" in data:
                print ("recieved get favorites command")
                conn.send(get_favorites().encode()) #sends list of favorites id
                break
            elif "change favorites" in data: #data example: "change favorites 5 1"
                change_favorites(data[19:])
            elif "recieved" in data:
                print (data + " faga")
            elif "get week" in data:
                get_week_attendence(data[11:])
                conn.send("done".encode())
                print ("sent done")
            elif "get month" in data:
                get_month_attendence(data[12:])
                conn.send("done".encode())
                print ("sent done")
            elif "get attendence" in data:
                after = open("michpal.txt").read().splitlines() #"M:\AttendenceApp\clock.txt"
                added = [f for f in after if not f in before]
                if added:
                    for i in range(0,len(added)):
                        if added[i] == '\n':
                            print ("Blank line")
                        else:
                            upToDate = get_attendence(added[i].split())
                            if upToDate == 1:
                                data1 = conn.recv(10)
                                print (data1)
                    before = after
                    db.commit()
                conn.send("done".encode())
                print ("sent done")
            elif "get employees" in data:
                print ("sending: recieved")
                conn.send("recieved".encode())
                get_employees()
            elif "closing s" in data or "done" in data:
                conn.send("closing".encode())
                break
            else:
                print ("error: Unknown command")
                conn.send("Unknown command".encode())
                time.sleep(1)
    db.commit()

before = ''
if __name__ == '__main__':
    db = sqlite3.connect('example.db', check_same_thread=False)
    c = db.cursor()
    # c.execute('''CREATE TABLE `employees` ( `id` INT NOT NULL , `name` TEXT NOT NULL , `photo` TEXT unique ,\
    #  `favorites` boolean NOT NULL)''')
    #c.execute('''CREATE TABLE `employees` ( `id` INT NOT NULL , `name` TEXT NOT NULL , `favorites` boolean NOT NULL)''')
    # c.execute('''INSERT INTO employees VALUES (00000001, 'test1', '00000001.jpg' , 1)''')
    #employee_insertion()
    # f = open("michpal_test.txt", "r")
    # for i in range(1,7):
    #     get_attendence(i)
    # get_attendence(1)
    #before = open("michpal_test3.txt").read().splitlines()
    # addColumn = "ALTER TABLE employees ADD COLUMN status INT DEFAULT 0" # adds the departure column to the existing table
    # c.execute(addColumn)
    #changeValue = "UPDATE employees SET status=0"
    # change_favorites("4 0")
    # employee_insertion()
    # query = "SELECT * FROM employees"
    # allBefore = db.execute(query)
    # allEmpsBefore = allBefore.fetchall()

    # x=datetime.today() #Updates "michpal_month.txt" every day at 23:01.
    # y=x.replace(day=x.day, hour=23, minute=1, second=0, microsecond=0)
    # delta_t=y-x
    #
    # secs=delta_t.total_seconds()+1
    # t1 = Timer(secs, update_month)
    # t1.start()
    #
    # unreleventX=datetime.today() #Deletes all unupdated lines from "michpal.txt" every day at 05:01.
    # unreleventY=unreleventX.replace(day=unreleventX.day, hour=5, minute=1, second=0, microsecond=0)
    # unrelevent_delta_t=unreleventY-unreleventX
    #
    # unrelevent_secs=unrelevent_delta_t.total_seconds()+1
    # t2 = Timer(unrelevent_secs, delete_not_relevent)
    # t2.start()

    while 1:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print ("Socket created")
        try:
            s.bind((HOST,PORT))
        except socket.error as msg:
            print ("Bind failed. Error code : " + str(msg) + " message " + msg[1])
            sys.exit()
        print ("Socket bind complete")
        s.listen(10)
        print ("Socket now listening")
        while True:
            conn, addr = s.accept() # sever connection
            print ("Connected with " + addr[0] + ":" + str(addr[1]))
            threading.Thread(target = manage_data, args = (conn, addr,)).start()


    db.commit()
    s.close()
    atexit.register(db.close)