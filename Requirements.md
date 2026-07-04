# Family Expense Tracker — Requirements

## Overview

A personal Android app for tracking family expenses across multiple accounts, backed by a shared Google Sheet. Multiple people on multiple devices see the same live data. No Play Store deployment; personal use only with no ongoing cost.

---

## Functional Requirements

### FR1 — Transaction Management

- **Add** a new transaction (expense, income, or account transfer).
- **Edit** an existing transaction; all fields are editable.
- **Delete** a transaction by its unique ID.
- Every transaction has a unique `txnId` (UUID) generated at creation time.

### FR2 — Transaction Types

| Type | Behaviour |
|---|---|
| **Expense** | Amount stored as negative (`-amount`). |
| **Income** | Amount stored as positive (`+amount`). |
| **Account Transfer** | Creates **two** linked rows sharing the same `transferId` UUID: one expense row from the source account, one income row ("Account Transfer" subcategory) into the destination account. |

### FR3 — Google Sheets Backend

- Primary data store is a Google Sheet (`Transactions` tab, columns A–I).
- Multiple app instances on different devices read/write the same sheet concurrently.
- An `admin` tab holds lookup data: expense categories (A:B), accounts (D:D), income categories (F:G).
- Adding new accounts or categories is done by editing the `admin` sheet directly; the app reads them at runtime.

### FR4 — Multiple Accounts

- The list of valid accounts is fetched from the `admin` sheet.
- Every transaction is linked to exactly one account (two accounts for a transfer).
- Default account: **Passbook**.

### FR5 — Category and Subcategory System

- Each transaction has a **category** and optional **subcategory**.
- Expense categories and their subcategories are maintained in the `admin` sheet.
- Income categories and subcategories are maintained separately in the `admin` sheet.
- Defaults for expense: category = **Daily Living**, subcategory = **Groceries**.
- Defaults for income: category = **Income**, subcategory = **Salary**.

### FR6 — Date Range Filtering

Transactions are always fetched and displayed for a selected date range:

| Range type | Description |
|---|---|
| **Daily** | A single calendar day. |
| **Monthly** | A single calendar month (year + 0-indexed month). |
| **Financial Year** | April 1 of `startYear` through March 31 of `startYear + 1`. Labeled `FY{YY}-{YY+1}` (e.g., FY25-26). |
| **Custom** | Arbitrary start date to end date (inclusive). |
| **All** | No date restriction; returns all rows in the sheet. |

### FR7 — Period Navigation

- For Daily, Monthly, and Financial Year ranges the user can navigate to the previous or next period with `<` and `>` buttons.
- Navigation is disabled for **All** and **Custom** ranges.

### FR8 — Account Filter

- A filter chip in the top bar lets the user restrict the view to one specific account.
- Default state is **Passbook**.

### FR9 — Transaction Type Filter

Three mutually exclusive filter chips are always visible:

| Chip | What it shows |
|---|---|
| **Expense** | Only negative-amount transactions. |
| **Income** | Only positive-amount transactions. |
| **Balance** | All transactions (net view). |

Default: **Balance**.

### FR10 — Category Summary and Drill-Down

- Transactions are always shown grouped by **category**, not as a flat list.
- Each category row shows: name, transaction count, total amount.
- Tapping a category opens a **subcategory detail screen** showing subcategory groups.
- Each subcategory group can be expanded to show individual transaction cards.
- Individual cards have Edit, Copy and Delete actions.

### FR11 — Yearly Date Breakdown

- When the period tab is **Yearly**, the user can toggle between two sub-views:
  - **Category view** (default): one row per category, sorted by absolute amount.
  - **Date view**: one row per month of the financial year (all 12 months always shown, zero-filled). Tapping a month navigates to the Monthly view for that month.

---

## Non-Functional Requirements

| Attribute | Requirement |
|---|---|
| **Platform** | Android, minimum SDK 26, target SDK 34. |
| **Distribution** | Sideloaded APK on 2–3 personal devices. Not on Google Play. |
| **Cost** | Zero ongoing cost. Google Sheets API free tier. |
| **Authentication** | Google OAuth 2.0 via Google Play Services / Android Credentials Manager. |
| **Concurrency** | All network calls run in coroutines on `viewModelScope`; UI remains responsive. |
| **Offline** | Not supported. All data operations require network access. |
| **Language** | Kotlin only. |

---

## Data Model

### Transaction (Sheets columns A–I)

| Column | Field | Notes |
|---|---|---|
| A | `txnId` | UUID string, primary key. |
| B | `date` | `yyyy/MM/dd` format. |
| C | `amount` | Double. Negative = expense, positive = income. |
| D | `category` | String. |
| E | `subcategory` | String, may be empty. |
| F | `paymentMethod` | String (e.g., "Cash", "Card"). |
| G | `description` | String, optional free-text. |
| H | `account` | String. |
| I | `transferId` | UUID string linking the two rows of an account transfer; empty otherwise. |

### Category Summary (computed in-app)

Derived from filtered transactions:

- `category` — group key.
- `totalAmount` — sum of all transaction amounts in this category.
- `transactionCount` — count of transactions.
- `subcategories` — list of `SubcategoryGroup`, each with its own total, count, and transaction list.

### Month Summary (computed in-app)

- `year`, `month` (0-indexed Calendar month).
- `totalAmount`, `transactionCount`.
- For Financial Year view: all 12 months in FY order (Apr–Mar) are always present, even if count = 0.

### Features to be implemented

1. Format amount with commas for ex; 1,10,00,000.00 (1 crore)
2. Remove 'Date' string from Date on add screen as its clear from the Date format
3. Payment method dropdown. Dropdown values to be fetched from 'admin' sheets "PaymentMethod" table with "Cash" as default.
4. Multiple add support on Add screen with two options - "Add" and "Add more". "Add more" should add existing entry to sheet and clear of the fields to defaults to allow another add.
5. Advanced Search - Provide filters like Accounts, Amount, Categories, SubCategories, PaymentMethod, ExactMatch.
The filters should be applied alongwith the search when user hits refresh button. Not after user hits the refresh button.

#### Nice to have

1. Give a nice app icon
2. Split sheet by year into multiple sheets
3. App options to move all hardcodes including Google sheet id
4. App options to add accounts, categories, subcategories
5. Search results in descending order. Search should group the results based on Accounts which can be then expanded to categories and so on like already supported


---

## Architecture

- **MVVM**: `ExpenseViewModel` is the single source of truth for UI state, exposed as `StateFlow` properties.
- **Repository pattern**: one repository class per data operation (fetch, add, update, delete, fetch accounts, fetch categories).
- **Manual DI**: dependencies are wired in `MainActivity` with no DI framework.
- **SRP**: one class/file per responsibility. No helper or utility classes.
- See [TECHSTACK.md](TECHSTACK.md) for the full tech stack.

---

## Testing Requirements

- Unit tests for every class with 100% coverage (`src/test/`).
- UI instrumented tests for every screen and composable component (`src/androidTest/`).
- Follow TDD: write tests first, then implement.
- Run unit tests: `./gradlew :app:testDebugUnitTest`
- Run UI tests: `./gradlew :app:connectedDebugAndroidTest`
