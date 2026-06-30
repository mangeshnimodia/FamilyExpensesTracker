# Family Expense Tracker — UI Requirements

Companion to [Requirements.md](Requirements.md). Describes every screen, component, and interaction in detail. This is the authoritative UI specification; the source code is the reference implementation.

---

## Navigation Structure

```
MainActivity
└── TransactionsScreen  (NavHost root = "main_list")
    ├── → AddExpenseScreen       (route "add")
    ├── → AddExpenseScreen       (route "edit/{txnId}", pre-filled)
    └── → SubcategoryDetailScreen (route "subcategory/{category}")
```

---

## Screen: TransactionsScreen

Entry point of the app. All state is owned by `ExpenseViewModel`.

### Top App Bar

- **Title**: "Family Expenses" (`titleMedium` typography).
- **Account filter chip** (`AccountDropdown`): right of the title, before the exit button.
- **Exit button**: icon `ExitToApp`; closes the Activity.

### Period Tab Bar (`PeriodTabBar`)

A `TabRow` spanning the full width, immediately below the top bar.

| Tab label | `PeriodTab` value | Default `DateRange` on switch |
|---|---|---|
| Daily | `Daily` | `DateRange.Day(today)` |
| Monthly | `Monthly` | `DateRange.Month(thisYear, thisMonth)` |
| Yearly | `Yearly` | `DateRange.FinancialYear(fyStartYear)` |
| All | `All` | `DateRange.All` |

- Switching tab calls `viewModel.setSelectedPeriodTab()` and `viewModel.setSelectedDateRange()`.
- Financial year start: if current month ≥ April → `thisYear`; else `thisYear - 1`.

### Period Nav Bar (`PeriodNavBar`)

Shown for every period tab **except All**. Immediately below `PeriodTabBar`.

- **`<` button**: navigate to previous period; calls `PeriodNavigator.previous()`.
- **`>` button**: navigate to next period; calls `PeriodNavigator.next()`.
- Both buttons are **disabled** when `DateRange` is `All` or `Custom` (`PeriodNavigator.isNavigable()` returns false).
- **Centre column**: formatted label (via `DateRangeFormatter`) above a currency total.
- **Total line**: `₹X.XX` coloured according to filter type:
  - Expense filter → `MaterialTheme.colorScheme.error` (red).
  - Income filter → `Color(0xFF4CAF50)` (green).
  - Balance filter → red if total < 0, green otherwise.
- Total value computed from the current filter:
  - Expense → `expenseTotal` StateFlow (sum of `abs(amount)` for negative transactions).
  - Income → sum of positive-amount transactions.
  - Balance → net sum of all transactions.

### Filter Chips Row

Three equal-width `FilterChip`s in a horizontal `Row` below the nav bar.

| Label | `FilterType` | Selected when |
|---|---|---|
| Expense | `EXPENSE` | `selectedFilter.type == EXPENSE` |
| Balance | `BALANCE` | `selectedFilter.type == BALANCE` |
| Income | `INCOME` | `selectedFilter.type == INCOME` |

- Clicking a chip calls `viewModel.setSelectedFilter(selectedFilter.copy(type = type))`.
- Default: **Balance**.

### Total Banner

Shown only when `!isLoading && transactions.isNotEmpty()`.

- Full-width `Surface` with `primaryContainer` background.
- Left: "Total:" label (`labelLarge`).
- Right: `₹X.XX` (`titleLarge`, `FontWeight.Bold`, 22sp). Colour: red if net < 0, green otherwise.

### Yearly Sub-View Toggle (`YearlyViewToggle`)

Shown **only** when the selected period tab is **Yearly**.

A secondary `TabRow` (`surfaceVariant` background) with two tabs:

| Tab | `YearlyViewMode` |
|---|---|
| Date | `Date` |
| Category | `Category` |

- Default selection on entering the Yearly tab: **Category**.
- Switching the toggle changes the content list immediately (no re-fetch).

### Main Content Area

Fills the remaining space. Three possible states:

| Condition | Content |
|---|---|
| `isLoading == true` | `CircularProgressIndicator` centred. |
| `transactions.isEmpty()` | Centred column: "No transactions loaded" (`bodyLarge`) + "Tap refresh to fetch" (`bodySmall`, outline colour). |
| Yearly tab + Date mode | `MonthlyBreakdownList` |
| All other states | `CategorySummaryList` |

### Floating Action Buttons

Two FABs anchored to the bottom of the content area:

| Position | Icon | `contentDescription` | Action |
|---|---|---|---|
| Bottom-end (default Scaffold position) | `Add` | "Add Expense" | `navController.navigate("add")` |
| Bottom-start | `Refresh` | "Refresh" | `viewModel.fetchTransactions()` |

The Refresh FAB uses `secondaryContainer` / `onSecondaryContainer` colours.

### Snackbar

Errors from the ViewModel (`errorMessage` StateFlow) are shown via a `SnackbarHost`. After display, `viewModel.clearError()` is called.

---

## Component: AccountDropdown

Displayed in the top-app-bar `actions` slot.

- Renders a `FilterChip`.
  - **Label**: the selected account name, or "All Accounts" if none selected.
  - **`selected`**: `true` only when an account is chosen (not null).
- Tapping the chip opens a `DropdownMenu`.
  - First item: "All Accounts" → calls `onAccountSelected(null)`.
  - Remaining items: one per account string → calls `onAccountSelected(account)`.
- Selecting any item closes the dropdown.

---

## Component: PeriodTabBar

`TabRow` with four `Tab`s in order: Daily, Monthly, Yearly, All.

- `selectedTabIndex` matches `PeriodTab.entries.indexOf(selectedTab)`.
- Each tab text = `tab.name` ("Daily", "Monthly", "Yearly", "All").
- Clicking a tab calls `onTabSelected(tab)`.

---

## Component: PeriodNavBar

`Surface` (`primaryContainer`) containing a single `Row`:

- `TextButton("<")` — left; disabled when `!navigator.isNavigable(dateRange)`.
- Centre `Column`:
  - Line 1: formatted period label (`titleMedium`).
  - Line 2: `₹X.XX` (`bodyMedium`, `FontWeight.SemiBold`, `totalColor`).
- `TextButton(">")` — right; same disabled logic as `<`.

---

## Component: YearlyViewToggle

`TabRow` with `surfaceVariant` background, no divider, two tabs: **Date** (index 0), **Category** (index 1).

- Clicking a tab calls `onSelected(mode)`.

---

## Component: CategorySummaryList

`LazyColumn` of `CategorySummaryRow` items, each separated by `HorizontalDivider`.

### CategorySummaryRow

- Full-width, clickable → `onCategoryClick(summary)`.
- Left column:
  - Category name (`bodyLarge`, `FontWeight.Medium`).
  - "N transactions" (`bodySmall`, outline colour).
- Right: amount `%.2f` (`bodyLarge`, `FontWeight.SemiBold`). Colour: red if `totalAmount < 0`, green otherwise.
- Sorted by absolute amount descending (computed by `TransactionSummaryCalculator`).

---

## Component: MonthlyBreakdownList

`LazyColumn` of `MonthSummaryRow` items, each separated by `HorizontalDivider`.

- Always 12 rows when showing a Financial Year (April through March, zero-filled months included).
- Clicking a row calls `onMonthClick(DateRange.Month(year, month))` and navigates to the Monthly period view for that month.

### MonthSummaryRow

- Left: label formatted as `"MMMM yyyy"` (e.g., "April 2025") using `SimpleDateFormat`.
- Right: amount `%.2f` coloured red/green.

---

## Component: TransactionItem

A `Card` (2dp elevation) showing a single transaction.

### Layout

```
[ Category/Subcategory ]          [ date (bodySmall, outline) ]
[ description (bodySmall, secondary) — only if not blank      ]
[ Amount: ₹X | Account: Y (bodyMedium, bold, coloured)        ]
                                       [ Edit icon ] [ Delete icon ]
```

- Category text: `"Category/Subcategory"` if subcategory is not blank, else just `"Category"`.
- Amount colour: red (`colorScheme.error`) if `amount < 0`, green (`Color(0xFF4CAF50)`) if `amount ≥ 0`.
- Amount format: `kotlin.math.abs(txn.amount)` as a raw Double string (not zero-padded).
- Edit icon (`Edit`, primary colour) → `onEdit(txn)`.
- Delete icon (`Delete`, error colour) → `onDelete()`.

---

## Screen: SubcategoryDetailScreen

Reached via `navController.navigate("subcategory/{category}")`.

### Top App Bar

- Title: the category name (`titleMedium`).
- Navigation icon: back arrow (`ArrowBack`, `contentDescription = "Back"`) → `navController.popBackStack()`.

### Body

`LazyColumn` of `SubcategoryGroupRow` items, each separated by `HorizontalDivider`. Data source: `viewModel.categorySummaries` filtered to the matching category.

### SubcategoryGroupRow

Header row (always visible, clickable to expand/collapse):

- Left column:
  - Subcategory name (`bodyLarge`, `FontWeight.Medium`). If subcategory is blank, shows `"(none)"`.
  - "N transactions" (`bodySmall`, outline colour).
- Right: amount `%.2f` (red if < 0, green if ≥ 0).
- Trailing icon: `KeyboardArrowUp` when expanded, `KeyboardArrowDown` when collapsed.

Expanded content: one `TransactionItem` + `HorizontalDivider` per transaction, with Delete and Edit actions.

- Delete → `viewModel.deleteTransaction(txnId)`.
- Edit → `navController.navigate("edit/${txn.txnId}")`.

---

## Screen: AddExpenseScreen

Reached via `navController.navigate("add")` (new) or `navController.navigate("edit/{txnId}")` (edit). Presented as a full screen, not a dialog.

### Transaction Type Selector

Three `FilterChip`s in a row: **Expense**, **Income**, **Transfer**.

- Only one is selected at a time.
- Switching type resets category/subcategory to the appropriate defaults (only when not editing an existing transaction).
- When editing an existing transaction: type is locked to the transaction's original type (Expense if `amount < 0`, Income if `amount > 0`).

### Date Picker

- Shows a button displaying the selected date in `"EEE, dd MMM yyyy"` format.
- Tapping opens `AppDatePicker` (Material3 `DatePickerDialog`).
- Default date:
  - New transaction: the date from the current `DateRange.Day`, or today if range is not a Day.
  - Edit: the transaction's existing date.

### Form Fields (in order)

| Field | Widget | Notes |
|---|---|---|
| Amount | `OutlinedTextField` (numeric) | Positive integer. Pre-filled with `abs(amount)` for edits. |
| Category | `AppDropdown` | Options from `expenseCategories` or `incomeCategories` based on type. Disabled for Transfer type. |
| Subcategory | `AppDropdown` | Options from `categoriesMap[category]`. Disabled if category is blank. |
| Payment Method | `OutlinedTextField` | Free text. Default: "Cash". |
| Account | `AppDropdown` | For Expense/Income. Options from `viewModel.accounts`. Default: "Passbook". |
| From Account | `AppDropdown` | Transfer only. |
| To Account | `AppDropdown` | Transfer only. |
| Description | `OutlinedTextField` | Optional free text. |

### Submit Button

- Label:
  - New expense: "Add Expense"
  - New income: "Add Income"
  - New transfer: "Add Transfer"
  - Edit (any type): "Update"
- **Disabled** unless:
  - `amount.toIntOrNull()` is not null and > 0.
  - Category is not blank (unless Transfer).
  - Payment Method is not blank.
  - Account is not blank.
  - For Transfer: To Account is also not blank.
- On click: calls `viewModel.addTransaction()`, `viewModel.updateTransaction()`, or `viewModel.addAccountTransfer()` as appropriate, then `onDismiss()` (pops the back stack).

---

## Date Formatting Rules

Implemented in `DateRangeFormatter`:

| `DateRange` type | Display label |
|---|---|
| `Day` | `"EEE, dd MMM yyyy"` (e.g., "Mon, 30 Jun 2026") |
| `Month` | `"MMMM yyyy"` (e.g., "June 2026") |
| `FinancialYear` | `"FY{YY}-{YY+1}"` (e.g., "FY25-26") |
| `Custom` | `"{startDate} – {endDate}"` both in `"dd MMM yyyy"` |
| `All` | `"All Time"` |

---

## Colour Conventions

| Meaning | Colour |
|---|---|
| Expense / negative amount | `MaterialTheme.colorScheme.error` (red) |
| Income / positive amount | `Color(0xFF4CAF50)` (Material Green 500) |
| Alternate negative (summary rows) | `Color(0xFFE53935)` |
| Alternate positive (summary rows) | `Color(0xFF43A047)` |

---

## Period Navigation Logic

Implemented in `PeriodNavigator`:

| Range | Previous | Next |
|---|---|---|
| `Day` | previous calendar day | next calendar day |
| `Month` | previous calendar month (wraps year) | next calendar month |
| `FinancialYear` | `startYear - 1` | `startYear + 1` |
| `All`, `Custom` | not navigable | not navigable |
