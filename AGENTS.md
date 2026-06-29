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
6. GroupBy feature -
   1. A toggle/button to switch the view between GroupBy/Activity. Activity is list of all entries (what we currently show)
   2. In GroupBy, group all the entries by Accounts first. This shows the account name and the subtotal of all entries for that account.
   3. Expand button on each account, should further group by categories with subtotal of that category
   4. Expand on each category shows all entries of that category. (Note - no subcategory grouping required)
7. Date range feature -
   1. The transactions for the Year, Month, or custom range can be selected by user.
   2. A year means a financial year beginning from April. So current year is FY26-27. This is how year should be displayed.
   3. The transactions should be grouped as per the GroupBy feature.
8. Edit entry (WIP)
9. Filter transactions by Accounts and Income/Expenses/Balance (WIP)

First implement the Year and Month and then switch to custom range.
# Architecture
Refer [README.md](/README.md)

# Design
Follow TDD. Add tests → fail them → implement → tests passing
Follow SRP strictly.
For every functionality a new class/file exists.

## Don'ts
No helper classes. Its violation of SRP.
No Deprecated classes/methods/libraries

## Tests
Have unit tests for each file with 100% code coverage.

### How to run tests
Run Unit Tests: ./gradlew :app:testDebugUnitTest
Run UI Tests: ./gradlew :app:connectedDebugAndroidTest
