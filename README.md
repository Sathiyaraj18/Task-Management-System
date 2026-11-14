# Task Management Application (Android – Java)

A clean, modern, and production-ready **Task Management Android Application** built with **Java**, following **MVVM & Repository Architecture Patterns**, **SQLite Local Storage**, and **Material Design 3 Components**.

---

## 📱 Features & Requirements Implemented

| Requirement | Implementation Details |
| :--- | :--- |
| **Authentication Screen** | Clean Login screen with floating hint validation (empty, length checks), password visibility toggle, **"Fill Demo Account"** quick action, and registration screen. |
| **Session Management** | `SessionManager` (using `SharedPreferences`) automatically persists session and redirects logged-in users directly to Dashboard. |
| **Dashboard Metrics** | Live KPI counter cards for: <br>• **Total Tasks** <br>• **Pending Tasks** <br>• **In Progress Tasks** <br>• **Completed Tasks** <br>Tapping any stat card automatically opens the Task List pre-filtered by that status. |
| **Task Creation & Editing** | Reusable `AddEditTaskActivity` with: <br>• Task Title & Description <br>• Start Date (`DatePickerDialog`) <br>• Due Date (`DatePickerDialog` with validation against Start Date) <br>• Priority (High, Medium, Low) <br>• Status (Pending, In Progress, Completed) |
| **RecyclerView Display** | Material Design card items featuring Priority left color stripes, Priority badges, Status badges, Date range, Overdue tags, and quick edit/delete actions. |
| **Search & Filtering** | Real-time search query watcher filtering by Title and Description + Interactive horizontal chips for Status (`All`, `Pending`, `In Progress`, `Completed`) and Priority (`All`, `High`, `Medium`, `Low`). |
| **Task Sorting** | Dialog option to sort by Earliest Due Date, Latest Due Date, Priority (High to Low), or Recently Added. |
| **Edit & Delete Tasks** | Full editing capabilities and safe deletion with confirmation `AlertDialog`. |
| **Update Task Status** | 1-tap quick status transition on Task Detail screen or directly from the task card. |
| **Local Database** | Thread-safe `DatabaseHelper` (`SQLiteOpenHelper`) with background threading via `AppExecutors` (disk I/O off the main UI thread). Includes automatic database seeding with realistic demo tasks and admin user. |
| **Screen Navigation** | Smooth intent transitions and back stack handling across Splash, Auth, Dashboard, Task List, Add/Edit Task, and Task Detail activities. |

---

## 🏛 Architecture & Package Structure

```
com.example.taskmanagement
├── adapter
│   └── TaskAdapter.java                // RecyclerView adapter with visual status/priority badges
├── data
│   ├── DatabaseContract.java           // Schema definitions, table constants, SQL creation scripts
│   └── DatabaseHelper.java             // SQLiteOpenHelper singleton, CRUD queries, dashboard stats aggregation
├── model
│   ├── Priority.java                   // Enum: HIGH, MEDIUM, LOW
│   ├── Task.java                       // Domain entity for tasks (implements Serializable)
│   ├── TaskStatus.java                 // Enum: PENDING, IN_PROGRESS, COMPLETED
│   └── User.java                       // Domain entity for user accounts
├── repository
│   ├── TaskRepository.java             // Asynchronous repository for task operations
│   └── UserRepository.java             // Asynchronous repository for user auth and registration
├── ui
│   ├── SplashActivity.java             // Launcher screen with session routing
│   ├── auth
│   │   ├── LoginActivity.java          // Username/Password authentication & demo credentials
│   │   └── RegisterActivity.java       // New user registration
│   ├── dashboard
│   │   └── DashboardActivity.java      // KPI metric cards, greeting, recent tasks preview
│   └── task
│       ├── AddEditTaskActivity.java    // Create and edit form with DatePickerDialogs
│       ├── TaskDetailActivity.java     // Full detail view with 1-click status update
│       └── TaskListActivity.java       // RecyclerView, search bar, status & priority chips, sort dialog
└── util
    ├── AppExecutors.java               // Disk I/O & Main Thread executor pool
    ├── Constants.java                  // Intent extra keys & request constants
    ├── DateUtils.java                  // Date formatting, parsing, overdue detection
    ├── SessionManager.java             // SharedPreferences user session persistence
    └── ValidationUtils.java            // Form validation utility methods
```

---

## 🔑 Demo Account Credentials

The local SQLite database is automatically seeded upon first launch with a ready-to-use account and sample tasks:

- **Username:** `admin`
- **Password:** `password123`

*(You can also tap the **"Use Demo Account"** button on the Login screen to automatically fill these credentials, or create your own account via the Register screen.)*

---

## 🛠 Tech Stack & Dependencies

- **Language:** Java (JDK 17)
- **Min SDK:** 24 (Android 7.0+)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM + Repository Pattern
- **UI Components:** Google Material Components (`com.google.android.material:material:1.12.0`)
- **Database:** Android SQLite Database (`android.database.sqlite.SQLiteOpenHelper`)
- **Unit Testing:** JUnit 4

---

## 🚀 How to Open and Run in Android Studio

1. Open **Android Studio**.
2. Select **Open**, and navigate to this project directory (`visitor_management_system` or its `android` folder).
3. Android Studio will automatically recognize the Gradle configuration and sync dependencies.
4. Select an Android Emulator (API 24+) or connected physical device.
5. Click **Run 'app'** (`Shift + F10`).
