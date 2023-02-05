# coding=utf-8
import atexit
import socket
import sys
import sqlite3
from _socket import SOL_SOCKET, SO_REUSEADDR
from sqlite3 import Error
import random
import difflib
import os, time
import datetime
import filecmp
import codecs
from datetime import timedelta
from datetime import date
import filecmp
import pandas as pd
from googleapiclient.discovery import build
from google_auth_oauthlib.flow import InstalledAppFlow,Flow
from google.auth.transport.requests import Request
import os
import pickle

import threading
from datetime import datetime
from threading import Timer
from shutil import copyfile

# If modifying these scopes, delete the file token.pickle.
SCOPES = ['https://www.googleapis.com/auth/spreadsheets.readonly']

SPREADSHEET_ID = '1hIFXcOUSllSV7KZ8t-xcNPuWNpYEIdswFZcCXheFJN0'
RANGE_NAME = 'Sheet1!A2:D'

def update_month():
    print ("updating month")
    # prev = date.today().replace(day=1) - timedelta(days=1)
    # print prev.month
    with open("M:\AttendenceApp\clock.txt") as infile, open('michpal_month.txt', 'a') as outfile:
        for line in infile:
            if not line.strip(): continue  # skip the empty line
            outfile.write(line)  # non-empty line. Write it to output
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
    fileMonth = open("M:\AttendenceApp\clock.txt").read().splitlines()
    i=0
    print (str(today.day) + "/")
    while str(today.day) + "/" not in fileMonth[i]:
        i=i+1
    print (i)
    with open('M:\AttendenceApp\clock.txt', 'w') as fout:
        fout.writelines(map(lambda s: s + '\n', fileMonth[i:]))
        print ("done")

def delete_file_txt():
    open('M:\AttendenceApp\clock1.txt', 'w').close()

# Checks if there are new employees and adds them to the "employee" table
def new_employee_insertion():
    emp_file = open("employees.txt").read()
    emp_new_file = open("employees_new.txt").read()
    result = filecmp.cmp("employees.txt", "employees_new.txt", shallow=False)
    # print (result)
    if result: # Checks if there has been no changes to file. TRUE: exits / FALSE: continues
        print ("No changes in employees!")
        return
    print ("There have been changes in the employees. Updating now!")
    old_employee_deletion() # First: delete old employees
    a = emp_new_file.splitlines()
    new_emps = []
    # print(a[1].decode("iso-8859-8"))
    for i in range(0, len(a)):
        if a[i] not in emp_file:
            # print (i)
            new_emps.append(a[i].split())
        a[i] = a[i].split()
    # print (new_emps)
    # print new_emps[0][1].decode("iso-8859-8")
    query = '''INSERT INTO employees VALUES (?, ?, ? , ?, ?, ?)'''
    for i in range(0, len(new_emps)):
        id = new_emps[i][0]
        name = new_emps[i][1] + " " + new_emps[i][2]
        department = ''
        for j in range(4,len(new_emps[i])):
            department = department + new_emps[i][j] + " "
        filepath = new_emps[i][3]
        favorite = 0
        print ("Adding ID: " + id)
        print (name)
        print (department)
        c.execute(query, (id, name, filepath, favorite, department, 0))
    db.commit()
    with open("employees_new.txt") as f:
        with open("employees.txt", "w") as f1:
            for line in f:
                f1.write(line)
    return "Added mew employees successfully"

# Checks if there are old employees that left and removes them to the "employee" table
def old_employee_deletion():
    emp_file = open("employees.txt").read()
    emp_new_file = open("employees_new.txt").read()
    a = emp_file.splitlines()
    old_emps = []
    for i in range(0, len(a)):
        if a[i] not in emp_new_file: #Searches for the whole employee row that way even if only part of it has changed it will update it
            old_emps.append(a[i].split())
        a[i] = a[i].split()
    # if len(old_emps) == 0:
    #     return "no old employees to remove"
    # print old_emps
    # print old_emps[0][1].decode("iso-8859-8")
    query = '''DELETE FROM employees WHERE id=?'''
    for i in range(0, len(old_emps)):
        id = old_emps[i][0]
        print ("removing ID: " + id)
        c.execute(query, (id,))
    db.commit()
    return "removed old employees successfully"

#Gets the list of employees from the google sheet and inserts it into the new employees file
def update_employee_file():
    global values, service
    creds = None
    if os.path.exists('token.pickle'):
        with open('token.pickle', 'rb') as token:
            creds = pickle.load(token)
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                'credentials.json', SCOPES) # here enter the name of your downloaded JSON file
            creds = flow.run_local_server(port=0)
        with open('token.pickle', 'wb') as token:
            pickle.dump(creds, token)

    service = build('sheets', 'v4', credentials=creds)

    # Call the Sheets API
    sheet = service.spreadsheets()
    result_input = sheet.values().get(spreadsheetId=SPREADSHEET_ID,
                                range=RANGE_NAME).execute()
    values = result_input.get('values', [])
    finalReturnMessages = ""
    if not values:
        return ('000')
    else:
        for row in values:
            if row:
                if len(row)==4:
                    finalReturnMessages = finalReturnMessages + row[0] + "\t" + row[1] + "\t" + row[3] + "\t" + row[2] + "\n"
                    #row[0] = id / row[1] = name / row[2] = department / row[3] = imgPath
                else:
                    finalReturnMessages = finalReturnMessages + row[0] + "\t" + row[1] + "\t" + "-" + "\t" + row[2] + "\n"

        if finalReturnMessages.rstrip() == open('employees_new.txt').read():
            print('No change has occurred!')
        else:
            print('The Google Sheets file has been changed!')
            text_file = open("employees_new.txt", "w")
            n = text_file.write(finalReturnMessages.rstrip())
            text_file.close()
        return (finalReturnMessages)

if __name__ == '__main__':
    db = sqlite3.connect('example.db', check_same_thread=False)
    c = db.cursor()
    update_employee_file()
    # query = "DELETE FROM employees"
    # db.execute(query)
    # c.execute('''INSERT INTO employees VALUES (00000001, 'test1', '00000001.jpg' , 1)''')
    new_employee_insertion()
    # update_month()
    # delete_not_relevent()
    # delete_file_txt()
    db.commit()