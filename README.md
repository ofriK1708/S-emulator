# 📘 S-Emulator

## 🔹 Motivation
As programmers, we are familiar with many programming languages.  
Ultimately, every language runs on a processor: source code is transformed into a set of basic instructions the processor can execute.

Interestingly, even with a **small set of basic instructions**, it is possible to implement the entire digital world we interact with daily: e-commerce, LLMs, traffic control, space missions, social media, etc.

This raises an intriguing theoretical question:  
**What is the minimal set of instructions needed to express all computable functions?**

This is where **computational models** from theoretical computer science come into play. A computational model is a simplified, mathematical mechanism that can represent any computable function, while being easy to understand and reason about.

One such model is the **S-language**. It proposes **three core instructions** (plus a trivial fourth for convenience), and using just these instructions, it is possible to express **all computable functions**, essentially representing the building blocks of all digital systems.

In this project, we explore the S-language:
- Implementing its basic units.
- Building higher-level constructs (syntactic sugar) to ease programming.
- Creating an environment to execute and debug S-programs.
- Ultimately, supporting a serverless model to run multiple users’ programs on diverse processor architectures.

---

## 🔹 System Overview
**S-Emulator** is a Java-based emulator for executing programs described in **XML format**.  
The system is capable of:
- Loading and validating program descriptions.
- Expanding instructions into different abstraction levels.
- Executing instructions step by step or in full.
- Producing execution statistics and insights.

The system follows a **modular architecture**, separating core logic, user interaction, and data transfer, making it maintainable and extendable.

---

## 🔹 S-Language Overview
**S-Language** is a simple theoretical programming language designed to illustrate minimal computation. Programs consist of instructions that operate only on **natural numbers**.

### 1. Variables
- **Input Variables (`x1, x2, … xn`)**: Represent program inputs.
- **Work Variables (`z1, z2, …`)**: Temporary variables for computation.
- **Output Variable (`y`)**: Holds the final program result.

All variables are initialized to `0` unless otherwise specified.

### 2. Labels
- Labels mark instructions for **jumping**.
- Regular labels: `L1, L2, …`
- Special label: `EXIT` → stops program execution.
- Each instruction can have at most **one label**.

### 3. Instructions
S-language has **four basic instructions**:

```text
1. V ← V + 1    // Increment variable V
2. V ← V - 1    // Decrement variable V (never below 0)
3. IF V ≠ 0 GOTO L    // Conditional jump to label L if V is not 0
4. V ← V        // No-op (does nothing)
```

---

## 🔹 Program Execution
- Instructions are executed in order unless a jump occurs.
- **Initialization:**
  - Input variables (`x1…xn`) are set to input values, or `0` if no input value is provided.
  - Work variables (`z1, z2, …`) and `y` are set to `0`.
- Execution stops when the last instruction is reached or an `EXIT` label is executed.
- The **final output** is the value of `y`.

---

## 🔹 Design Choices
1. **Modular Architecture**
   - Modules: `Engine` (core logic), `Controller` (system coordinator), `UI` (user interface), `DTOs` (data transfer objects).

2. **DTO Usage**
   - Shared across all modules to decouple layers.

3. **Robust Exception Handling**
   - Meaningful errors for user guidance.

4. **User-Friendly Features**
   - Save/load program state and execution history.
   - Execute programs at different expansion levels.

---

## 🔹 Main Classes and Their Roles
- **Engine** → Executes program instructions, manages variables, labels, and execution statistics.
- **Controller** → Coordinates the flow between UI and Engine, knows about DTOs.
- **UI** → Entry point for users; sends commands to Controller.
- **DTOs** → Shared structured objects used for communication across layers.

---

## 🔹 Additional Notes
- Execution statistics are generated.
- Validation ensures correctness of program input and labels.
