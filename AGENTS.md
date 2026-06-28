# Introduction
Read [README.md](/README.md) in home folder of this repository to know the details of this app.
MOST IMPORTANT - CODING AGENT SHOULD NOT MODIFY THIS FILE UNLESS EXPLICITLY ASKED FOR

# Development Plan
It's in currently in development and being developed as per the plan prepared in docs/prompt_conversation_history.md

# Current Status
We are following the development plan in (prompt_conversation)[docs/prompt_conversation_history.md]
Completed -
1. Add entry
2. Show all entries for given date with sum at the top
3. Delete an entry
4. Add income
5. Show income in Green and expenses in Red

# Architecture
Refer [README.md](/README.md)

# Design
Follow SRP strictly.
For every functionality a new class/file exists.

## Don'ts
No helper classes. Its violation of SRP

## Tests
Have unit tests for each file. The test should reside in different plugin but follow same package fragments as its production class.