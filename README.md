# S-Emulator: A Client-Server Simulation Platform

## 1. Project Overview

This project is a sophisticated, multi-user, client-server application for loading, executing, and debugging custom S-programs. It features a powerful server-side engine that manages all core logic and a rich JavaFX-based desktop client that provides a user-friendly graphical interface.

Designed from the ground up to support multiple users concurrently, it has a strong emphasis on thread safety, robust state management, and providing a real-time, collaborative-like experience.

## 2. System Architecture

The application is divided into four distinct modules that work in concert:

*   **`engine` (The Core)**: The heart of the entire system. A standalone module containing all logic for understanding and running S-Language programs. It handles parsing, execution, debugging, and state management.
*   **`engineWeb` (The Server)**: This module wraps the core `engine` in a Java-based web server. It exposes the engine's functionality through a RESTful API and manages user sessions, concurrent access, and system-wide state.
*   **`system` (The Communication Bridge)**: This crucial module handles all client-side communication. It contains the HTTP client logic, manages session cookies, and acts as the single, reliable bridge between the client UI and the server API.
*   **`uiWeb` (The Client)**: A JavaFX desktop application providing the user interface. It is a "thin client" that delegates all core operations to the server via the `system` module.

This decoupled architecture was a deliberate choice to centralize logic on the server, abstract away the complexities of network communication, and allow for a lightweight, responsive client.

## 3. The Core (`engine` module)

The `engine` module is where the fundamental logic of the S-Language simulation resides.

*   **JAXB-Powered Parsing**: The engine uses JAXB to automatically unmarshal XML-based S-programs into a tree of Java objects (like `SProgram` and `SFunction`), providing a robust and type-safe way to handle program loading.
*   **Instruction & Function Management**: It processes the parsed objects to create executable instruction sequences, correctly resolving function calls and managing the program's structure.
*   **Execution and Debugging**: The engine contains the complete logic for running programs, managing breakpoints, and stepping through code (forwards and backwards) in a debug session.
*   **The Credit & Architecture System**: This feature introduces a virtual economy into the simulation. The `ArchitectureType` enum represents different virtual machine configurations (`BASIC`, `INTERMEDIATE`, `ADVANCED`).
    *   **Entry Cost**: To begin an execution, a user must first pay an initial "credit" cost based on the chosen `ArchitectureType`. This simulates the cost of acquiring a certain level of computing power.
    *   **Instruction Cost**: Furthermore, **every single instruction** has its own credit cost. As the program runs, credits are consumed for each operation performed. This creates a nuanced resource management challenge, where both the initial setup and the program's efficiency matter.
*   **Robust Error Handling**: The engine is designed to fail gracefully. If an error occurs (e.g., a missing label, insufficient credits, invalid arguments), it generates a precise, user-friendly error message explaining exactly what went wrong and where.

## 4. The Server (`engineWeb` module)

The server acts as the central hub, managing clients and leveraging the `engine` to fulfill their requests.

*   **RESTful API**: The server uses Java Servlets to expose a clear API for the client. Endpoints like `/uploadProgram`, `/runProgram`, and `/startDebugProgram` allow the client to access the engine's features.
*   **Multi-Threading & Concurrency**: A primary responsibility of the server is to handle many clients at once. It uses `ReadWriteLock`s to ensure that shared data structures (like the list of active users and available programs) are accessed in a thread-safe manner, preventing race conditions.
*   **Singleton Managers**: Server-side logic is organized into thread-safe singleton managers (`UserManager`, `ProgramManager`, etc.) to provide a single, consistent point of access to system resources.

## 5. The Client (`uiWeb`) & Communication (`system`)

The client is a responsive JavaFX desktop application that serves as the user's window into the simulation environment. It relies entirely on the `system` module for its connection to the server.

#### The Communication Bridge: `system` Module

*   **Centralized HTTP Logic**: All HTTP communication is centralized in the `HttpEngineController` class within the `system` module. This class uses the popular **OkHttp** library to execute all network requests.
*   **Session Management**: It is responsible for managing the user's session by handling and persisting **cookies**, ensuring that the user remains authenticated across multiple requests.
*   **Protocol**: The client and server primarily communicate using **JSON**, with the **Gson** library used for serialization and deserialization. In some simple cases, plain text is used.
*   **Smart Threading Model**: The `HttpEngineController` is designed to be efficient.
    *   **Async Calls**: When a request is initiated by a user action on the JavaFX Application Thread (JAT), the call is made **asynchronously**. This keeps the UI from freezing while waiting for the server's response.
    *   **Sync Calls**: For background tasks, like the data refreshers, calls are made **synchronously**. Since these background threads have no other work to do, this simplifies the logic without harming UI responsiveness.

#### Real-Time Updates: The Refresher System

The client stays constantly up-to-date with the server's state using a "pulling" mechanism. Several background threads, or **"refreshers"**, run on a timer. They periodically poll the server (via the `system` module) for the latest information, such as:
*   The list of currently logged-in users.
*   The list of all programs and functions available in the system.
*   Updates to execution history and statistics.

This ensures that a user can see new programs uploaded by other users or see when new users log in, creating a dynamic and shared environment.

## 6. Key Application Features

*   **View Others' Executions**: A user can click on any other user in the user panel to view a complete history of their program executions.
*   **Detailed Execution Insights**: By selecting a past execution (their own or someone else's), a user can see a snapshot of the entire run, including the final state of all variables and the specific `expandLevel` that was used.
*   **The "Rerun" Feature**: This powerful feature makes it incredibly easy to replicate any past execution. By clicking "Rerun" on a historical entry, the system automatically loads all of its original parameters—the arguments, `expandLevel`, and `ArchitectureType`. The user can then instantly choose to run or debug the program with that exact configuration, making it simple to reproduce results or debug issues found by others.

