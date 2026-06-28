# Introduction
Read [README.md](/README.md) in home folder of this repository to know the details of this app.
MOST IMPORTANT - CODING AGENT SHOULD NOT MODIFY THIS FILE UNLESS EXPLICITLY ASKED FOR

# Development Plan
It's in currently in development and being developed as per the plan prepared in docs/prompt_conversation_history.md

# Current Status
Completed -
1. Add entry
2. Show all entries for given date with sum at the top
3. Delete an entry
4. Add income
5. Show income in Green and expenses in Red
6. GroupBy feature (WIP)-
   1. A toggle/button to switch the view between GroupBy/Activity. Activity is list of all entries (what we currently show)
   2. In GroupBy, group all the entries by Accounts first. This shows the account name and the subtotal of all entries for that account.
   3. Expand button on each account, should further group by categories with subtotal of that category
   4. Expand on each category shows all entries of that category. (Note - no subcategory grouping required)

# Architecture
Refer [README.md](/README.md)

# Design
Follow SRP strictly.
For every functionality a new class/file exists.

## Don'ts
No helper classes. Its violation of SRP

## Tests
Have unit tests for each file. The test should reside in different plugin but follow same package fragments as its production class.