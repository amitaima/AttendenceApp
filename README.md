Gvanim Attendance App

An Android attendance management app for Gvanim Painting Factory.

Overview

The Gvanim Attendance App connects to the factory’s fingerprint attendance machine and retrieves live attendance data every 5 minutes.
It displays all employees with their profile pictures and a status indicator (red / yellow / green) showing their current attendance state.

Features

🔄 Real-Time Attendance — Updates automatically every 5 minutes from the fingerprint system.

🧑‍🤝‍🧑 Team View — Split employees into different teams for easier tracking.

⭐ Favorites — Mark specific employees for quick access.

🧾 Google Sheets Integration — Employee list and profile image links are managed through a shared Google Sheet.

➕ Add / Remove Employees — Automatically sync new or removed employees from the Google Sheet.

🕒 Nightly Sync Service — A microservice runs every night to:

Update employee records from Google Sheets

Store daily attendance history

Tech Stack

Android App: Built in Android Studio (Java)

Backend Microservice: Handles nightly updates and history tracking

Data Source: Google Sheets (employee information and image links)

Status Indicators

🟥 Red: Absent

🟨 Yellow: Left work early or uncertain status

🟩 Green: Present
