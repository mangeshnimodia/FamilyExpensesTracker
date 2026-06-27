# Introduction
Read [README.md](/README.md) in home folder of this repository to know the details of this app.

# Development Plan
Its in currently in development and being developed as per the plan prepared in docs/prompt_conversation_history.md

# Current Status
We are following the development plan in (prompt_conversation)[docs/prompt_conversation_history.md]
Completed the PoC to print HelloWorld.
Complete Step 2 of POC - build basic Google sheet integration with a basic usecase.
Next task - Support entering fields in Add entry

# Tech stack
Component | Technology | Version

Language | Kotlin | 1.9+
UI | Jetpack Compose | Latest stable

# Architecture
Refer [README.md](/README.md)

# Design
Follow SRP strictly.
For every functionality a new class/file exists.

## Don'ts
No helper classes. Its violation of SRP

## Tests
Have unit tests for each file. The test should reside in different plugin but follow same package fragments as its production class.