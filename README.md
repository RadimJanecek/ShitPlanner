# Shift Planner

An intelligent, Java-based desktop application designed to automate shift scheduling for small and medium-sized businesses (cafes, restaurants, retail shops, or receptions). 

Say goodbye to messy Excel spreadsheets. Shift Planner automatically enforces rest period laws, working time funds, and employee availability preferences while generating clean, ready-to-print PDF schedules for your noticeboard.

---

## Key Features

* ** Smart Scheduling Algorithm:** Automatically assigns shifts based on employee capacity, approved time-off, and daily coverage requirements.
* ** Built-in Compliance:** Strictly enforces minimum rest periods between shifts (e.g., 11 hours) and monitors working time limits (FTE).
* ** Advanced Rules:** Set up "Forbidden Pairs" (employees who cannot work together) or enforce policies like ensuring part-timers are never left alone on a shift.
* ** Professional Exports:** Instant high-performance **PDF printing** (with dynamic layout scaling and landscape support) or **CSV (Excel)** export for payroll accountants.
* ** Drafts & History:** Save work-in-progress schedules as "Drafts" or securely lock approved months into the permanent "History" archive.
* ** Full Localization:** Seamlessly switch between **English and Czech** UI in real-time.

---

## Tech Stack

This project was built to demonstrate clean Object-Oriented Programming (OOP) principles and efficient local data management:

* **Language:** Java (JDK 11+)
* **GUI Framework:** Java Swing
* **Architecture:** MVC (Model-View-Controller) separating data, business logic, and UI layers.
* **Data Persistence:** **Gson** library for reliable, fast JSON serialization/deserialization.
* **Printing Engine:** Optimized `javax.print` and Java 2D Graphics utilization featuring custom UI caching (color and holiday caching for near-instant export performance).
* **Localization (i18n):** Custom dynamic dictionary engine for instant language switching.

---

## How to Run

### For End-Users (Windows)
Download the ready-to-use `ShiftPlanner.exe` from the **[Releases](#)** page and run it directly. No Java installation required!

### For Developers
1. Clone the repository:
   ```bash
   git clone [https://github.com/your_username/shift-planner.git](https://github.com/your_username/shift-planner.git)
