# Family Expense Tracker — Full Conversation History

## Date: June 2026

---

## 1. Requirements & Feasibility Study
I want to build an Android mobile app to track the family expenses. These are my requirements -
1. The backend should be a Google sheet which is shared by multiple people and multiple people running app from different mobiles should be able to view and edit the live data
2. It should support multiple accounts and in each account I should be able to add, edit, delete entries
3. It should also support transfer entry from one account to another
4. I should be able to search any transaction based on description. It should support case insensitive search.
5. Addition of more accounts should be supported
6. The excel should have these fields -
Date, Amount, Category, Subcategory, PaymentMethod, Description, Account

This app is for personal use only. I don't intend to make it commercial. I should be able to install this app in 2-3 devices. It need not be hosted on playstore. It should NOT cost me anything.
First do a feasibility study considering all the requirements.

**AI**:
Feasibility Study: Family Expense Tracker App (Android + Google Sheets)

🎯 Overview
You want a free, personal-use Android app that uses Google Sheets as a live backend, supports multiple users across 2–3 devices, and provides full CRUD + transfer + search capabilities. Let's break this down carefully.

✅ Requirement-by-Requirement Feasibility Analysis
1. 📊 Google Sheets as a Live Backend (Multi-user, Real-time)



Aspect
Assessment



Google Sheets as DB
✅ Fully feasible


Multiple people viewing/editing
✅ Supported via Google Sheets sharing


Live sync across devices
⚠️ Near real-time (not instant push — requires manual refresh or polling)


Free to use
✅ Free via Google Sheets API


How it works: You'll use the Google Sheets API v4 to read/write data. The sheet is shared among family members. The app reads and writes rows via API calls.

⚠️ Caveat: Google Sheets is not a true real-time database (like Firebase). If two people edit simultaneously, there's a small risk of conflict. For a family expense tracker with low concurrent writes, this is perfectly acceptable.


2. 💳 Multiple Accounts — Add, Edit, Delete Entries



Aspect
Assessment



Multiple accounts in one sheet
✅ Feasible — filter rows by Account column


Add entries
✅ Append rows via Sheets API


Edit entries
✅ Update specific rows via Sheets API


Delete entries
✅ Delete rows via Sheets API


Sheet structure maps perfectly to your requirement. Each row = one transaction. The Account column acts as the account identifier.

3. 🔄 Transfer Entry Between Accounts



Aspect
Assessment



Transfer as two linked entries
✅ Feasible


Approach: A transfer will create two rows atomically:

One debit row in the source account (e.g., Category = Transfer, Description = Transfer to Savings)
One credit row in the destination account (e.g., Description = Transfer from Wallet)

A shared Transfer ID column can link the two entries together for traceability.

4. 🔍 Case-Insensitive Search by Description



Aspect
Assessment



Search support
✅ Fully feasible


Case-insensitive
✅ Handle in app logic (convert both to lowercase before comparing)


Approach: Fetch all rows from the sheet and filter on the app side using case-insensitive string matching. For a family expense tracker (likely a few thousand rows max), this is fast and efficient enough.

5. ➕ Adding More Accounts Dynamically



Aspect
Assessment



Dynamic account addition
✅ Feasible


Approach: Maintain a second sheet tab (e.g., Accounts) that lists all account names. The app reads this tab to populate account lists. Adding a new account = adding a row to the Accounts tab.

6. 📋 Excel Fields: Date, Amount, Category, Subcategory, PaymentMethod, Description, Account



Aspect
Assessment



All fields supported
✅ Straightforward


The Google Sheet will have these exact columns:



Date
Amount
Category
Subcategory
PaymentMethod
Description
Account



23/06/2026
500
Food
Groceries
UPI
Big Bazaar
Wallet



💰 Cost Analysis — Will It Be Free?



Component
Cost



Google Sheets API
✅ Free (generous quota: 300 requests/min)


Google Account
✅ Free


Android App Development
✅ Free (Android Studio is free)


App Distribution (APK sideload)
✅ Free — no Play Store needed


Hosting
✅ None needed — Sheets is the backend


OAuth 2.0 Authentication
✅ Free via Google Cloud Console



💡 Total Cost = ₹0 / $0 — Completely free for personal use on 2–3 devices.


📱 Installation on 2–3 Devices (Without Play Store)
This is called sideloading and is completely supported on Android:

Build a signed APK in Android Studio
Enable "Install from Unknown Sources" on each device
Transfer the APK via WhatsApp, Google Drive, USB, etc.
Install and log in with a Google account that has access to the shared sheet

✅ No Play Store. No hosting. No subscription.

⚠️ Limitations &amp; Risks to Be Aware Of



Limitation
Severity
Mitigation



Not truly real-time (polling-based sync)
🟡 Low
Add a manual "Refresh" button + auto-refresh on app open


Simultaneous write conflicts
🟡 Low
Rare for family use; acceptable risk


Google Sheets API quota (300 req/min)
🟢 Very Low
Family use will never hit this


No offline support
🟡 Medium
App requires internet to read/write


Google API setup requires some effort
🟡 Medium
One-time setup on Google Cloud Console


APK must be manually redistributed on updates
🟢 Low
Share updated APK via Drive/WhatsApp



🏗️ Recommended Tech Stack



Layer
Technology
Reason



Language
Kotlin
Modern, official Android language


UI Framework
Jetpack Compose
Clean, modern Android UI


Backend
Google Sheets API v4
Free, meets all requirements


Authentication
Google OAuth 2.0
Secure, free, ties to Google account


Networking
Retrofit + OkHttp
Industry standard for API calls


Build Tool
Android Studio
Free IDE


Distribution
Signed APK (sideload)
No Play Store needed



✅ Final Verdict

🟢 FULLY FEASIBLE — Go ahead and build it!

All 6 requirements are achievable with zero cost. The Google Sheets API is powerful enough for a family expense tracker. The only trade-off is that it's not real-time push (like Firebase), but for your use case — a family of 2–3 people tracking expenses — polling/refresh-on-open is completely sufficient.

🗺️ Suggested Build Phases
Phase 1 → Google Sheets setup + API authentication
Phase 2 → View transactions per account
Phase 3 → Add / Edit / Delete transactions
Phase 4 → Transfer between accounts
Phase 5 → Search by description
Phase 6 → Dynamic account management
Phase 7 → Polish UI + build signed APK


Would you like me to now proceed with the detailed technical design or jump straight into code implementation for any specific phase? 🚀

---

## 2. Technical Design & UX Design
**AI**
Technical Design &amp; UX Design
Family Expense Tracker — Android + Google Sheets

PART 1: TECHNICAL DESIGN

🏛️ 1. System Architecture
┌─────────────────────────────────────────────────────────────┐
│                    ANDROID APP (Kotlin)                      │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  UI Layer │  │ViewModel │  │Repository│  │ API Layer│   │
│  │ (Compose) │→ │ (State)  │→ │ (Logic)  │→ │(Retrofit)│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                    │         │
└────────────────────────────────────────────────────┼────────┘
                                                     │ HTTPS
                                          ┌──────────▼──────────┐
                                          │  Google Sheets API  │
                                          │       v4            │
                                          └──────────┬──────────┘
                                                     │
                                          ┌──────────▼──────────┐
                                          │    Google Sheet      │
                                          │  (Shared Workbook)  │
                                          │                     │
                                          │  Tab 1: Transactions│
                                          │  Tab 2: Accounts    │
                                          │  Tab 3: Categories  │
                                          └─────────────────────┘


🗂️ 2. Google Sheet Structure
Tab 1: Transactions



Col A
Col B
Col C
Col D
Col E
Col F
Col G
Col H
Col I



TxnID
Date
Amount
Category
Subcategory
PaymentMethod
Description
Account
TransferID


TXN001
23/06/2026
500.00
Food
Groceries
UPI
Big Bazaar
Wallet



TXN002
23/06/2026
-2000.00
Transfer

UPI
Transfer to Savings
Wallet
TRF001


TXN003
23/06/2026
2000.00
Transfer

UPI
Transfer from Wallet
Savings
TRF001



💡 TxnID — Unique ID per transaction (e.g., timestamp-based UUID)
💡 TransferID — Links the two rows of a transfer together
💡 Amount — Negative for debit, Positive for credit (or use a separate Type column — your choice)


Tab 2: Accounts



Col A
Col B
Col C



AccountID
AccountName
CreatedDate


ACC001
Wallet
01/01/2026


ACC002
Savings
01/01/2026


ACC003
Credit Card
01/01/2026



Tab 3: Categories



Col A
Col B



Category
Subcategory


Food
Groceries


Food
Restaurant


Transport
Fuel


Transport
Cab


Bills
Electricity


Bills
Internet


Transfer




🏗️ 3. App Architecture — MVVM
┌─────────────────────────────────────────────────────────────────┐
│                         UI LAYER                                │
│              (Jetpack Compose Screens)                          │
│   DashboardScreen  TransactionScreen  SearchScreen  etc.        │
└────────────────────────┬────────────────────────────────────────┘
                         │ observes State
┌────────────────────────▼────────────────────────────────────────┐
│                      VIEWMODEL LAYER                            │
│         DashboardViewModel  TransactionViewModel                │
│         SearchViewModel     AccountViewModel                    │
│                                                                 │
│   - Holds UI State (StateFlow / LiveData)                       │
│   - Handles user actions                                        │
│   - Calls Repository                                            │
└────────────────────────┬────────────────────────────────────────┘
                         │ calls
┌────────────────────────▼────────────────────────────────────────┐
│                     REPOSITORY LAYER                            │
│                   ExpenseRepository                             │
│                                                                 │
│   - Single source of truth                                      │
│   - Coordinates between API and local cache                     │
│   - Business logic (transfer = 2 rows, etc.)                    │
└────────────────────────┬────────────────────────────────────────┘
                         │ calls
┌────────────────────────▼────────────────────────────────────────┐
│                      API LAYER                                  │
│                  SheetsApiService (Retrofit)                    │
│                                                                 │
│   - appendRow()      → POST /values:append                      │
│   - getRows()        → GET  /values/{range}                     │
│   - updateRow()      → PUT  /values/{range}                     │
│   - deleteRow()      → POST /batchUpdate (deleteDimension)      │
└─────────────────────────────────────────────────────────────────┘


📦 4. Data Models (Kotlin)
// Core transaction model
data class Transaction(
    val txnId: String,
    val date: String,           // "DD/MM/YYYY"
    val amount: Double,         // negative = debit, positive = credit
    val category: String,
    val subcategory: String,
    val paymentMethod: String,
    val description: String,
    val account: String,
    val transferId: String? = null
)

// Account model
data class Account(
    val accountId: String,
    val accountName: String,
    val createdDate: String
)

// Category model
data class Category(
    val category: String,
    val subcategory: String
)

// Transfer request model
data class TransferRequest(
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val date: String,
    val paymentMethod: String,
    val description: String
)

// UI State model
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)


🔐 5. Authentication Design
┌─────────────────────────────────────────────────────┐
│              Google OAuth 2.0 Flow                  │
│                                                     │
│  1. User opens app                                  │
│  2. App checks for saved OAuth token                │
│       ├── Token exists + valid → Go to Dashboard    │
│       └── No token → Show Google Sign-In screen     │
│  3. User signs in with Google account               │
│  4. App requests scope:                             │
│     spreadsheets (read/write)                       │
│  5. Token saved securely in EncryptedSharedPrefs    │
│  6. Token auto-refreshed when expired               │
└─────────────────────────────────────────────────────┘


💡 Important: All family members sign in with their own Google account. The Sheet is shared with all their Google accounts (Editor access). Each person's app authenticates independently.


🔄 6. Key Operations — Logic Design
Add Transaction
User fills form
     ↓
Validate inputs (amount > 0, date valid, account selected)
     ↓
Generate TxnID = "TXN_" + timestamp
     ↓
Append row to Transactions sheet via API
     ↓
Refresh local transaction list

Edit Transaction
User selects transaction → Edit form pre-filled
     ↓
User modifies fields
     ↓
Find row number by TxnID (scan sheet)
     ↓
Update that specific row via Sheets API
     ↓
Refresh local list

Delete Transaction
User long-presses → Confirm dialog
     ↓
Find row number by TxnID
     ↓
If TransferID exists → Ask: "Delete both transfer entries?"
     ↓
Delete row(s) via Sheets API (deleteDimension)
     ↓
Refresh local list

Transfer Between Accounts
User selects: From Account, To Account, Amount, Date, PaymentMethod
     ↓
Generate TransferID = "TRF_" + timestamp
     ↓
Create Row 1: FromAccount, -Amount, "Transfer", "", PM, desc, TRF_ID
Create Row 2: ToAccount,  +Amount, "Transfer", "", PM, desc, TRF_ID
     ↓
Batch append both rows atomically
     ↓
Refresh local list

Search Transactions
User types in search bar
     ↓
Filter already-loaded transactions list in memory
     ↓
Compare: transaction.description.lowercase()
         .contains(query.lowercase())
     ↓
Show filtered results instantly (no API call needed)


📚 7. Tech Stack Summary



Component
Technology
Version



Language
Kotlin
1.9+


UI
Jetpack Compose
Latest stable


Architecture
MVVM + Repository
—


Navigation
Compose Navigation
—


API Client
Retrofit 2 + OkHttp
2.9+


JSON Parsing
Gson / Moshi
—


Auth
Google Sign-In + OAuth2
—


Async
Kotlin Coroutines + Flow
—


Secure Storage
EncryptedSharedPreferences
—


Build
Android Studio + Gradle
Latest


Min SDK
Android 8.0 (API 26)
Covers 95%+ devices




PART 2: UX DESIGN

🗺️ 1. App Navigation Map
┌─────────────────────────────────────────────────────────────────┐
│                        SPLASH SCREEN                           │
│                             ↓                                  │
│                    GOOGLE SIGN-IN SCREEN                       │
│                             ↓                                  │
│              ┌──────────────────────────────┐                  │
│              │         BOTTOM NAV BAR        │                  │
│              │  Dashboard | Txns | Search |  │                  │
│              │            Accounts           │                  │
│              └──────────────────────────────┘                  │
│                                                                 │
│   ┌─────────────┐  ┌─────────────┐  ┌──────────┐  ┌────────┐  │
│   │  Dashboard  │  │Transactions │  │  Search  │  │Accounts│  │
│   │   Screen    │  │   Screen    │  │  Screen  │  │ Screen │  │
│   └──────┬──────┘  └──────┬──────┘  └──────────┘  └───┬────┘  │
│          │                │                            │        │
│          ↓                ↓                            ↓        │
│   Account Summary   Add/Edit/Delete              Add Account    │
│   (per account)     Transaction Sheet            Screen         │
│                           ↓
│                     Transfer Screen                             │
└─────────────────────────────────────────────────────────────────┘


📱 2. Screen-by-Screen UX Design

Screen 1: Splash Screen
┌─────────────────────────┐
│                         │
│                         │
│       💰                │
│   Family Expense        │
│     Tracker             │
│                         │
│   [Loading spinner]     │
│                         │
│                         │
└─────────────────────────┘


Shows logo + app name
Checks auth token in background
Auto-navigates to Dashboard or Sign-In


Screen 2: Google Sign-In
┌─────────────────────────┐
│                         │
│       💰                │
│   Family Expense        │
│     Tracker             │
│                         │
│  Track together,        │
│  spend smarter.         │
│                         │
│  ┌─────────────────┐    │
│  │  G  Sign in with│    │
│  │     Google      │    │
│  └─────────────────┘    │
│                         │
│  v1.0 | Personal Use    │
└─────────────────────────┘


Screen 3: Dashboard
┌─────────────────────────┐
│ 👋 Hello, Rahul    🔄   │  ← Refresh button
├─────────────────────────┤
│  June 2026         ◀ ▶  │  ← Month navigator
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │  ALL ACCOUNTS       │ │
│ │  Total Balance      │ │
│ │  ₹ 45,230.00        │ │
│ │  Income ↑  Expense ↓│ │
│ │  ₹12,000  ₹8,500    │ │
│ └─────────────────────┘ │
├─────────────────────────┤
│ ACCOUNTS                │
│ ┌────────┐ ┌────────┐   │
│ │ Wallet │ │Savings │   │  ← Horizontal scroll
│ │₹5,230  │ │₹40,000 │   │
│ └────────┘ └────────┘   │
├─────────────────────────┤
│ RECENT TRANSACTIONS     │
│ ┌─────────────────────┐ │
│ │🛒 Groceries  -₹500  │ │
│ │   Big Bazaar  23 Jun│ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │🔄 Transfer  -₹2,000 │ │
│ │   To Savings  23 Jun│ │
│ └─────────────────────┘ │
│         [See All]       │
├─────────────────────────┤
│ 🏠    📋    🔍    👤    │  ← Bottom Nav
└─────────────────────────┘


Screen 4: Transactions Screen
┌─────────────────────────┐
│ ← Transactions     🔍   │
├─────────────────────────┤
│ [All Accounts ▼]        │  ← Account filter dropdown
├─────────────────────────┤
│ June 2026          ◀ ▶  │
├─────────────────────────┤
│ TODAY — 23 Jun          │
│ ┌─────────────────────┐ │
│ │ 🛒  Big Bazaar      │ │
│ │     Food > Grocery  │ │
│ │     UPI        -₹500│ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ ⛽  Fuel Station    │ │
│ │     Transport>Fuel  │ │
│ │     Cash      -₹800 │ │
│ └─────────────────────┘ │
│                         │
│ YESTERDAY — 22 Jun      │
│ ┌─────────────────────┐ │
│ │ 🔄  To Savings      │ │
│ │     Transfer        │ │
│ │     UPI    -₹2,000  │ │
│ └─────────────────────┘ │
│                         │
│         ➕              │  ← FAB (Floating Action Button)
├─────────────────────────┤
│ 🏠    📋    🔍    👤    │
└─────────────────────────┘


💡 Long press on any transaction → shows Edit / Delete options


Screen 5: Add / Edit Transaction (Bottom Sheet)
┌─────────────────────────┐
│ ▬▬▬▬▬                   │  ← Drag handle
│ Add Transaction         │
├─────────────────────────┤
│ TYPE:                   │
│ [Expense] [Income] [Transfer] │  ← Toggle tabs
├─────────────────────────┤
│ Account                 │
│ [Wallet              ▼] │
│                         │
│ Amount                  │
│ [₹ _______________]     │
│                         │
│ Date                    │
│ [23/06/2026      📅]    │
│                         │
│ Category                │
│ [Food                ▼] │
│                         │
│ Subcategory             │
│ [Groceries           ▼] │
│                         │
│ Payment Method          │
│ [UPI                 ▼] │
│                         │
│ Description             │
│ [Big Bazaar           ] │
│                         │
│ ┌─────────────────────┐ │
│ │     SAVE            │ │
│ └─────────────────────┘ │
└─────────────────────────┘


Screen 6: Transfer Screen (Bottom Sheet — when Transfer tab selected)
┌─────────────────────────┐
│ ▬▬▬▬▬                   │
│ Transfer Money          │
├─────────────────────────┤
│ TYPE:                   │
│ [Expense] [Income] [Transfer] │
├─────────────────────────┤
│ From Account            │
│ [Wallet              ▼] │
│                         │
│        ⬇️               │
│                         │
│ To Account              │
│ [Savings             ▼] │
│                         │
│ Amount                  │
│ [₹ _______________]     │
│                         │
│ Date                    │
│ [23/06/2026      📅]    │
│                         │
│ Payment Method          │
│ [UPI                 ▼] │
│                         │
│ Description (optional)  │
│ [                     ] │
│                         │
│ ┌─────────────────────┐ │
│ │     TRANSFER        │ │
│ └─────────────────────┘ │
└─────────────────────────┘


Screen 7: Search Screen
┌─────────────────────────┐
│ 🔍 Search Transactions  │
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │ 🔍 big bazaar       │ │  ← Live search bar
│ └─────────────────────┘ │
├─────────────────────────┤
│ Results for "big bazaar"│
│ 3 transactions found    │
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │ 🛒  Big Bazaar      │ │
│ │     Food > Grocery  │ │
│ │     23 Jun    -₹500 │ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ 🛒  Big Bazaar      │ │
│ │     Food > Grocery  │ │
│ │     15 Jun    -₹320 │ │
│ └─────────────────────┘ │
│                         │
│  [No more results]      │
├─────────────────────────┤
│ 🏠    📋    🔍    👤    │
└─────────────────────────┘


Screen 8: Accounts Screen
┌─────────────────────────┐
│ Accounts           ➕   │  ← Add new account
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │ 👛 Wallet           │ │
│ │    Balance: ₹5,230  │ │
│ │    Txns this month:8│ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ 🏦 Savings          │ │
│ │    Balance: ₹40,000 │ │
│ │    Txns this month:2│ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ 💳 Credit Card      │ │
│ │    Balance: -₹3,200 │ │
│ │    Txns this month:5│ │
│ └─────────────────────┘ │
│                         │
├─────────────────────────┤
│ 🏠    📋    🔍    👤    │
└─────────────────────────┘


Screen 9: Add Account (Dialog)
┌─────────────────────────┐
│  Add New Account        │
│                         │
│  Account Name           │
│  [________________]     │
│                         │
│  Icon (optional)        │
│  👛 🏦 💳 💵 📱        │
│                         │
│  [CANCEL]  [ADD]        │
└─────────────────────────┘


🎨 3. Design System
Color Palette



Element
Color
Usage



Primary
#1A73E8 (Google Blue)
Buttons, highlights


Income
#34A853 (Green)
Income amounts


Expense
#EA4335 (Red)
Expense amounts


Transfer
#FBBC04 (Amber)
Transfer entries


Background
#F8F9FA
Screen backgrounds


Surface
#FFFFFF
Cards


Text Primary
#202124
Main text


Text Secondary
#5F6368
Subtitles, hints


Typography



Style
Usage



Headline Large
Screen titles


Headline Medium
Balance amounts


Body Large
Transaction descriptions


Body Small
Dates, categories


Label
Button text


Component Standards

Cards: Rounded corners (12dp), subtle shadow
Buttons: Filled for primary actions, outlined for secondary
FAB: Circular, primary color, + icon
Bottom Sheet: For Add/Edit forms (feels native and smooth)
Dropdowns: Exposed dropdown menus (Material 3)


🔄 4. UX Flow Summary
Open App
   ↓
Splash (check auth)
   ├── Authenticated → Dashboard
   └── Not authenticated → Google Sign-In → Dashboard
         ↓
Dashboard (overview of all accounts + recent txns)
   ├── Tap account card → Filtered Transactions Screen
   ├── Tap "See All" → Transactions Screen
   ├── Bottom Nav: Transactions → Full list with filters
   ├── Bottom Nav: Search → Search screen
   └── Bottom Nav: Accounts → Accounts list
         ↓
Transactions Screen
   ├── FAB ➕ → Add Transaction Bottom Sheet
   │     ├── Expense/Income tab → Fill form → Save
   │     └── Transfer tab → Fill transfer form → Transfer
   └── Long press transaction → Edit / Delete


✅ Design Decisions Summary



Decision
Rationale



Bottom Sheet for Add/Edit
Feels native, no full screen navigation needed


FAB for adding transactions
Standard Android pattern, always accessible


In-memory search filtering
Fast, no extra API calls


Month navigator on Dashboard
Natural way to browse expense history


Negative amounts for debits
Simple single-column approach


TransferID linking both rows
Enables clean delete of both transfer entries


Material 3 design language
Modern, clean, familiar to Android users



🚀 Ready for Implementation?
The design is clean, practical, and covers all your requirements. Here's what we have locked in:

✅ 9 screens fully designed
✅ MVVM architecture defined
✅ Google Sheets structure finalized
✅ All data models defined
✅ All key operations logically designed
✅ Design system established

Shall we start Phase 1 — Google Sheets API setup + Authentication? 🎯

---

## 3. Implementation Plan
Now lets do the planning.
Since this is my 1st app development, I would like to do an end-to-end POC first to get handson with all the components.
Once PoC is successful, we will move on to feature by feature implementation again end-to-end. Prepare a plan and implementation strategy.

**AI**
Implementation Plan &amp; Strategy
Family Expense Tracker — First-Time Developer Roadmap

🎯 Overall Strategy
┌─────────────────────────────────────────────────────────────────┐
│                    TWO-PHASE APPROACH                           │
│                                                                 │
│  PHASE 1: POC          │  PHASE 2: FULL BUILD                  │
│  ─────────────────     │  ──────────────────                   │
│  Learn all components  │  Feature by feature                   │
│  End-to-end working    │  Production quality                   │
│  Minimal but complete  │  All 9 screens                        │
│  ~1-2 weeks            │  ~4-6 weeks                           │
└─────────────────────────────────────────────────────────────────┘


💡 Philosophy: The POC is your learning sandbox. You'll touch every component — Auth, API, UI, Data — in a simplified way. Once you're confident, Phase 2 builds the real app properly, one feature at a time.


📋 PHASE 1: Proof of Concept (POC)
🎯 POC Goal

Build a minimal but fully working app that:

Authenticates with Google
Reads transactions from Google Sheet
Adds a new transaction to Google Sheet
Displays transactions in a list
Deletes a transaction


That's it. No beautiful UI. No all screens. Just end-to-end data flow working.

🗺️ POC Architecture (Simplified)
┌──────────────────────────────────────────────┐
│              POC APP                         │
│                                              │
│  Screen 1: Sign-In Screen                   │
│  Screen 2: Transaction List Screen           │
│            - Shows all transactions          │
│            - Add button                      │
│            - Delete on long press            │
│  Screen 3: Add Transaction Screen            │
│            - Basic form (minimal fields)     │
└─────────────────┬────────────────────────────┘
                  │
                  ▼
        Google Sheets API v4
                  │
                  ▼
         Your Google Sheet
         (1 tab: Transactions)


📅 POC Step-by-Step Plan

🔷 POC Step 1: Environment Setup
Duration: Half a day
Tasks:
├── Install Android Studio
├── Install JDK 17
├── Create a new Android project
│     └── Template: Empty Activity (Compose)
│     └── Language: Kotlin
│     └── Min SDK: API 26
├── Run Hello World on emulator
├── Connect a physical device (optional)
└── Understand project structure:
      ├── app/src/main/
      ├── AndroidManifest.xml
      ├── build.gradle.kts
      └── MainActivity.kt

✅ Success Criteria: App runs on emulator showing "Hello World"

🔷 POC Step 2: Google Cloud + Sheets Setup
Duration: Half a day
Tasks:
├── Create Google Cloud Project
│     └── console.cloud.google.com
├── Enable Google Sheets API
├── Enable Google Drive API
├── Configure OAuth 2.0 Consent Screen
│     └── User Type: External
│     └── Add your email as test user
├── Create OAuth 2.0 Credentials
│     └── Type: Android
│     └── Add SHA-1 fingerprint of your debug keystore
├── Create your Google Sheet
│     └── Add headers: TxnID, Date, Amount, Category,
│                      Subcategory, PaymentMethod,
│                      Description, Account, TransferID
│     └── Add 3-4 dummy rows manually
└── Note down your Spreadsheet ID
      └── (from the sheet URL)

✅ Success Criteria: Sheet exists with dummy data, Cloud project configured

🔷 POC Step 3: Add Dependencies
Duration: 1-2 hours
// build.gradle.kts — dependencies to add:

// Google Sign-In
implementation("com.google.android.gms:play-services-auth:21.0.0")

// Google API Client
implementation("com.google.api-client:google-api-client-android:2.2.0")
implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")

// Retrofit for HTTP
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")

✅ Success Criteria: Project builds without errors after adding dependencies

🔷 POC Step 4: Google Sign-In
Duration: 1 day
Tasks:
├── Create SignInScreen.kt (Compose)
│     └── Simple screen with "Sign in with Google" button
├── Implement GoogleSignInClient setup
├── Handle sign-in intent and result
├── Extract OAuth token from signed-in account
├── Save token to SharedPreferences
├── Navigate to Transaction list on success
└── Test sign-in on emulator/device

What you'll learn:
✓ Compose UI basics
✓ Google Sign-In SDK
✓ Activity result handling
✓ SharedPreferences
✓ Basic navigation

✅ Success Criteria: Can sign in with Google, token saved, navigates to next screen

🔷 POC Step 5: Read from Google Sheet
Duration: 1 day
Tasks:
├── Create SheetsApiHelper.kt
│     └── Function: getTransactions()
│     └── Calls GET /spreadsheets/{id}/values/Transactions!A:I
│     └── Parses response into List<Transaction>
├── Create TransactionListScreen.kt
│     └── LazyColumn showing transaction rows
│     └── Each row: Date, Description, Amount
├── Create TransactionViewModel.kt
│     └── Calls getTransactions() in viewModelScope
│     └── Exposes StateFlow<List<Transaction>>
└── Wire ViewModel to Screen

What you'll learn:
✓ Google Sheets API calls
✓ JSON parsing
✓ Kotlin Coroutines
✓ ViewModel + StateFlow
✓ LazyColumn in Compose
✓ Loading states

✅ Success Criteria: Real data from Google Sheet appears in the app list

🔷 POC Step 6: Write to Google Sheet (Add Transaction)
Duration: 1 day
Tasks:
├── Create AddTransactionScreen.kt
│     └── Simple form: Amount, Description, Date, Account
│     └── Save button
├── Add appendTransaction() to SheetsApiHelper
│     └── Calls POST /spreadsheets/{id}/values:append
├── On Save:
│     └── Call appendTransaction()
│     └── Navigate back
│     └── Refresh list
└── Test adding a row and verifying in Google Sheet

What you'll learn:
✓ Compose Form inputs (TextField, Button)
✓ POST API calls
✓ Data flow: Form → ViewModel → API → Sheet
✓ Navigation with back stack

✅ Success Criteria: New row appears in Google Sheet after adding from app

🔷 POC Step 7: Delete from Google Sheet
Duration: Half a day
Tasks:
├── Add long-press gesture on transaction list item
├── Show confirmation dialog
├── Add deleteTransaction() to SheetsApiHelper
│     └── First: find row index by TxnID
│     └── Then: call batchUpdate with deleteDimension
└── Refresh list after deletion

What you'll learn:
✓ Gesture detection in Compose
✓ AlertDialog in Compose
✓ DELETE operation via Sheets API
✓ Row index management

✅ Success Criteria: Transaction deleted from app disappears from Google Sheet

🔷 POC Step 8: POC Review &amp; Learnings
Duration: Half a day
Review Checklist:
├── ✅ Can sign in with Google
├── ✅ Can read data from Sheet
├── ✅ Can add a transaction
├── ✅ Can delete a transaction
├── ✅ Data reflects in Google Sheet in real time
├── ✅ App navigates between 3 screens
│
Reflect on:
├── What was confusing?
├── What took longer than expected?
├── Any API issues faced?
└── Comfort level with Kotlin + Compose

✅ Success Criteria: All 5 POC goals working. You feel confident to build Phase 2.

📊 POC Summary Timeline
Week 1
─────────────────────────────────────────────────
Day 1  │ Step 1: Environment Setup
Day 1  │ Step 2: Google Cloud + Sheets Setup
Day 2  │ Step 3: Dependencies + Step 4: Sign-In
Day 3  │ Step 5: Read from Sheet
Day 4  │ Step 6: Write to Sheet
Day 5  │ Step 7: Delete + Step 8: POC Review
─────────────────────────────────────────────────


⏱️ Estimated: 5–10 days depending on your pace. No pressure — this is learning time!



📋 PHASE 2: Full Feature Implementation

Once POC is complete, you know all the components. Now we build feature by feature, each one end-to-end (UI → ViewModel → Repository → API → Sheet).


🗺️ Feature Implementation Order
┌─────────────────────────────────────────────────────────────────┐
│  Feature 1  │  Project Setup &amp; Architecture Foundation          │
│  Feature 2  │  Google Sign-In (Production Quality)             │
│  Feature 3  │  Account Management                              │
│  Feature 4  │  Transaction List (with filters)                 │
│  Feature 5  │  Add Transaction                                 │
│  Feature 6  │  Edit Transaction                                │
│  Feature 7  │  Delete Transaction                              │
│  Feature 8  │  Transfer Between Accounts                       │
│  Feature 9  │  Search Transactions                             │
│  Feature 10 │  Dashboard                                       │
│  Feature 11 │  Polish, Error Handling &amp; APK Build              │
└─────────────────────────────────────────────────────────────────┘


🔷 Feature 1: Project Setup &amp; Architecture Foundation
What: Clean project structure, all dependencies, base classes, navigation graph, design system
Deliverables:
├── Proper package structure
│     ├── ui/screens/
│     ├── ui/components/
│     ├── viewmodel/
│     ├── repository/
│     ├── api/
│     ├── model/
│     └── utils/
├── Navigation graph (all routes defined)
├── Theme.kt (colors, typography, shapes)
├── Base ViewModel class
└── All data models (Transaction, Account, Category)


🔷 Feature 2: Google Sign-In (Production Quality)
What: Polished sign-in screen, token management, auto sign-in, sign-out
Deliverables:
├── Beautiful Sign-In screen (per UX design)
├── Auto sign-in if token exists
├── Token refresh handling
├── EncryptedSharedPreferences for token storage
├── Sign-out functionality
└── Error handling (no internet, sign-in failed)


🔷 Feature 3: Account Management
What: View accounts, add new account, accounts screen
Deliverables:
├── AccountsScreen.kt
├── AddAccountDialog.kt
├── AccountViewModel.kt
├── Read accounts from Sheet (Accounts tab)
├── Add new account to Sheet
└── Account list with balance calculation


🔷 Feature 4: Transaction List
What: Full transaction list with account filter and month navigation
Deliverables:
├── TransactionsScreen.kt
├── TransactionListItem.kt (card component)
├── Account filter dropdown
├── Month navigator (◀ June 2026 ▶)
├── Grouped by date display
├── Pull-to-refresh
└── Empty state UI


🔷 Feature 5: Add Transaction
What: Full add transaction bottom sheet with all fields
Deliverables:
├── AddTransactionBottomSheet.kt
├── All fields: Date, Amount, Category, Subcategory,
│              PaymentMethod, Description, Account
├── Category → Subcategory dependency (dynamic)
├── Date picker
├── Input validation
└── Success/error feedback (Snackbar)


🔷 Feature 6: Edit Transaction
What: Pre-filled edit form, update row in sheet
Deliverables:
├── Reuse AddTransactionBottomSheet (edit mode)
├── Pre-fill all fields from selected transaction
├── Update specific row in Google Sheet
├── Handle transfer entries (edit both rows)
└── Optimistic UI update


🔷 Feature 7: Delete Transaction
What: Delete with confirmation, handle transfer pairs
Deliverables:
├── Long-press to select transaction
├── Delete confirmation dialog
├── Single delete
├── Transfer delete (both rows with same TransferID)
└── Undo via Snackbar (optional but nice)


🔷 Feature 8: Transfer Between Accounts
What: Transfer tab in Add sheet, creates two linked rows
Deliverables:
├── Transfer tab in AddTransactionBottomSheet
├── From/To account selectors
├── TransferID generation
├── Atomic write of two rows
└── Transfer display in transaction list (special UI)


🔷 Feature 9: Search Transactions
What: Full search screen with live filtering
Deliverables:
├── SearchScreen.kt
├── Search bar with live filtering
├── Case-insensitive description search
├── Result count display
├── Empty search state UI
└── Tap result → view transaction detail


🔷 Feature 10: Dashboard
What: Full dashboard with summaries, account cards, recent transactions
Deliverables:
├── DashboardScreen.kt
├── Total balance across all accounts
├── Income vs Expense summary for month
├── Account cards (horizontal scroll)
├── Recent transactions list
├── Month navigator
└── Per-account balance calculation


🔷 Feature 11: Polish, Error Handling &amp; APK Build
What: Production readiness
Deliverables:
├── Global error handling (no internet, API errors)
├── Loading states on all screens
├── Empty states on all screens
├── App icon design
├── Splash screen
├── Build signed APK
├── Install on 2-3 devices
└── Final testing checklist


📊 Phase 2 Timeline
Week 2
─────────────────────────────────────────────────
Day 1-2  │ Feature 1: Architecture Setup
Day 3-4  │ Feature 2: Sign-In (Production)
Day 5    │ Feature 3: Account Management
─────────────────────────────────────────────────

Week 3
─────────────────────────────────────────────────
Day 1-2  │ Feature 4: Transaction List
Day 3-4  │ Feature 5: Add Transaction
Day 5    │ Feature 6: Edit Transaction
─────────────────────────────────────────────────

Week 4
─────────────────────────────────────────────────
Day 1    │ Feature 7: Delete Transaction
Day 2-3  │ Feature 8: Transfer Between Accounts
Day 4    │ Feature 9: Search
Day 5    │ Feature 10: Dashboard
─────────────────────────────────────────────────

Week 5
─────────────────────────────────────────────────
Day 1-3  │ Feature 11: Polish + Error Handling
Day 4    │ APK Build + Device Installation
Day 5    │ Final Testing
─────────────────────────────────────────────────


🧭 How We'll Work Together (Session Strategy)
For each Step/Feature, our sessions will follow this pattern:
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  1. EXPLAIN   → I explain the concept before coding     │
│  2. CODE      → I provide complete, working code        │
│  3. INTEGRATE → I tell you exactly where to put it      │
│  4. TEST      → I give you test steps to verify         │
│  5. TROUBLESHOOT → We fix any issues together           │
│                                                         │
└─────────────────────────────────────────────────────────┘


📌 Ground Rules for Success



Rule
Detail



🐢 One step at a time
Never skip ahead. Each step builds on the last


✅ Verify before moving on
Always confirm success criteria before next step


🙋 Ask when stuck
No question is too basic — this is your first app


💾 Commit often
Use Git to save progress (I'll help set this up)


📝 Note your learnings
Keep a small diary of what you learned each day


🔄 Expect iteration
First attempt may not work — that's completely normal



🚀 What's Next?
You are here:
──────────────────────────────────────────
✅ Feasibility Study       DONE
✅ Technical Design        DONE
✅ UX Design               DONE
✅ Implementation Plan     DONE
──────────────────────────────────────────
⏭️  NEXT → POC Step 1: Environment Setup
──────────────────────────────────────────

---
