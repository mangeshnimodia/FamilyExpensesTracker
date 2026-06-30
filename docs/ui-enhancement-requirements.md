# UI Enhancement Requirements

## Overview

Replace the current date-range dialog + filter dialog + two-tab toggle with a streamlined navigation model built around period tabs, arrow-based date navigation, an account dropdown in the top bar, and a category-first grouping view.

Reference screenshots: `UIScreenshots/CategoryView.jpeg`, `UIScreenshots/SubCategoryView.jpeg`, `UIScreenshots/YearlyView.jpeg`

---

## 1. Top Bar

### 1.1 Account Selector Dropdown
- The top bar title is replaced by a **single-account dropdown** showing the currently selected account name (e.g. "Passbook") with a small down-arrow indicator.
- Tapping it opens a dropdown menu listing all available accounts (fetched from the existing `accountsRepository`).
- Selecting an account re-fetches transactions filtered to that account only.
- Default selection: the first account in the list (currently defaults to "Passbook").
- The existing Filter icon (multi-select accounts + type) and DateRange icon are **removed** from the top bar actions.
- The exit (←) icon and overflow menu (⋮) remain.

### 1.2 Transaction Type Filter (Retained)
- The **Expense / Balance / Income** filter is retained.
- It moves from the old filter dialog into the top bar or as a segmented control below the account dropdown — placement TBD during implementation, but must be accessible without opening a dialog.
- Default: **Balance** (same as current behaviour).
- The category totals, date navigation expense total, and all summaries reflect the selected filter type:
  - **Expense**: show only negative-amount transactions; totals are absolute expense values.
  - **Income**: show only positive-amount transactions; totals are income values.
  - **Balance**: show all transactions; total is net (income − expense), color-coded green if positive, red if negative.

### 1.3 Add / Edit / Delete (Retained)
- The Add FAB, and per-transaction Edit and Delete actions, are retained as-is.

---

## 2. Period Tab Bar

A tab row placed immediately below the top bar with four tabs:

| Tab | DateRange produced |
|---|---|
| **Daily** | `DateRange.Day` — today by default |
| **Monthly** | `DateRange.Month` — current month by default |
| **Yearly** | `DateRange.FinancialYear` — current FY by default |
| **All** | No date filter (fetch all rows) |

- Tabs use the existing `DateRange` sealed class. A new `DateRange.All` variant is added to represent "no date restriction".
- Switching a tab resets the date context to the current period for that tab and clears loaded transactions.
- The `Weekly` tab visible in `SubCategoryView.jpeg` is **not required** per clarification.

---

## 3. Date Navigation Bar

A fixed bar shown below the period tabs (in Daily, Monthly, Yearly tabs — not shown in All tab).

### 3.1 Layout
```
[←]   <date label>   [→]
      Expense: X,XXX.XX
```

- Left `←` arrow: navigate to the **previous** period.
- Right `→` arrow: navigate to the **next** period.
- Center date label: human-readable representation of the current period.
- Below the label: **total** formatted according to the active filter type — Expense total in red, Income total in green, or Balance (net) in red/green. Uses the same color-coding convention as the existing app.

### 3.2 Date Label Format per Tab

| Tab | Label example |
|---|---|
| Daily | `30-06-2026` |
| Monthly | `01-06-2026 - 30-06-2026` |
| Yearly | `01-04-2026 - 31-03-2027` (financial year Apr–Mar) |

### 3.3 Arrow Navigation Logic

| Tab | ← goes to | → goes to |
|---|---|---|
| Daily | Previous calendar day | Next calendar day |
| Monthly | Previous calendar month | Next calendar month |
| Yearly | Previous financial year (startYear - 1) | Next financial year (startYear + 1) |

- Navigating with arrows updates `selectedDateRange` in the ViewModel but does **not** auto-fetch. The user must still hit the Refresh button to load data for the new period (see §3.5).
- The `All` tab has no date bar.

### 3.5 Refresh Button (Retained)
- The **Refresh FAB** (bottom-left of screen) is retained exactly as-is.
- Data is only fetched when the user explicitly taps Refresh — not on tab switch, not on arrow navigation, not on account change.
- Switching tab, navigating with arrows, or changing account clears the currently loaded transactions so the screen shows "No data — tap Refresh" until the user refreshes.

### 3.4 Financial Year Definition
- Financial year starts **April 1** and ends **March 31** the following year.
- FY26-27 = April 1, 2026 – March 31, 2027.
- Display in date bar as the full date range (`01-04-YYYY - 31-03-YYYY+1`), not as "FY##-##".
- The existing `DateRange.FinancialYear(startYear: Int)` model is reused as-is.

---

## 4. Content Views

### 4.1 Daily and Monthly Tabs — Category View (only view)

For Daily and Monthly tabs there is **one fixed content view: Category View**.

No view toggle radio buttons are shown for these tabs.

**Category View layout:**
- A flat scrollable list of categories for the selected period, filtered by the active filter type (Expense / Income / Balance).
- Each row: `CategoryName: total  ›` — amount color follows the same convention as the rest of the app (red for expense, green for income, red/green for balance net).
- Only categories with at least one matching transaction are shown (exclude zero-total categories).
- Tapping a row navigates to the **Subcategory Detail Screen** for that category (see §5).

### 4.2 Yearly Tab — Two Sub-Views with Radio Toggle

The Yearly tab is the only tab that shows a **view toggle** (radio buttons):

```
● Date View    ○ Category View
```

**Yearly / Date View (default):**
- A flat list of all 12 months of the financial year, each showing its expense total.
- Row format: `Month YYYY: total_expense  ›`
- All 12 months are always listed, even if total is 0.00.
- Month order: April first through March last (FY order).
- Tapping a month row navigates to the Monthly view for that specific month (switches tab to Monthly, sets date to that month).

**Yearly / Category View:**
- Same as the Monthly category view (§4.1) but scoped to the full financial year range.
- Each category row taps into the Subcategory Detail Screen for that category scoped to the full FY.

### 4.3 All Tab — Category View (only view)

The `All` tab shows the Category View (§4.1) with no date restriction. No date bar is shown.

---

## 5. Subcategory Detail Screen

Reached by tapping a category row from any Category View.

### 5.1 Layout
- Top bar: back arrow + "CategoryName" as title.
- Date Navigation Bar (same period/arrows from the parent screen).
- Expense total for this category in the date bar.
- Content: expandable subcategory groups.

### 5.2 Subcategory Groups
Each group header row (collapsed by default):
```
∧  Category:Subcategory    total_expense
```

Expanded, each transaction row beneath the header:
```
Category:Subcategory  DD-MM-YYYY    amount
Description
```

- Group header tap toggles expand/collapse.
- Amounts in red.
- Alternating row background shading (as shown in screenshot).

### 5.3 Navigation from Yearly Date View → Month → Subcategory
- Tapping a month in Yearly Date View takes the user to Monthly view for that month.
- From Monthly view the user can tap a category to see the Subcategory Detail Screen for that month.
- The back stack must allow navigating back correctly at each step.

---

## 6. Removed / Deprecated Elements

| Element | Disposition |
|---|---|
| DateRangeSelector dialog (Day/Month/FinancialYear/Custom picker) | Removed — replaced by period tabs + arrow navigation |
| TransactionFilterDialog (multi-account + type) | Partially replaced — account moves to top bar dropdown; Expense/Income/Balance filter is retained as a segmented control (not a dialog) |
| TransactionViewMode enum (`Activity`, `GroupBy`) | Removed — replaced by the new content view model |
| `GroupedTransactionsView` (Account→Category→Items) | Removed — replaced by Category View + Subcategory Detail |
| "Activity" flat transaction list | Removed from main screen; transactions are still visible inside Subcategory Detail Screen |
| DateRange icon button in top bar | Removed |
| FilterList icon button in top bar | Removed |
| Total row (primary/container colored row) | Replaced by the Expense total line in the Date Navigation Bar |

---

## 7. Data / ViewModel Changes

### 7.1 New DateRange variant
Add `DateRange.All` (object, no fields) to represent "fetch all with no date filter".

### 7.2 Selected Account (single)
ViewModel gains:
- `selectedAccount: StateFlow<String>` — single account name.
- `setSelectedAccount(account: String)` — updates selection, clears transactions.
- Accounts are loaded eagerly on ViewModel init (not deferred to dialog open).

### 7.3 Expense Total
ViewModel exposes `expenseTotal: StateFlow<Double>` — the absolute sum of all negative amounts in the current `transactions` list. UI reads this directly; no local computation in Composable.

### 7.4 Category Grouping
ViewModel (or a dedicated model class per SRP) computes:
- `categorySummary: StateFlow<List<CategorySummary>>` — list of `(categoryName, totalExpense)` sorted by totalExpense descending.
- `subcategorySummary: StateFlow<List<SubcategoryGroup>>` — list of `(categoryName, subcategoryName, totalExpense, transactions)` for a given category, used in Subcategory Detail Screen.

### 7.5 Monthly Summary (Yearly Date View)
- `monthlySummary: StateFlow<List<MonthSummary>>` — 12 entries `(year, month, totalExpense)` in FY order (Apr–Mar), computed from the loaded FY transactions locally (no extra API call).

---

## 8. New Files Required (per SRP)

### Models
| File | Contents |
|---|---|
| `DateRange.kt` | Add `object All` variant |
| `PeriodTab.kt` | Enum: `Daily, Monthly, Yearly, All` |
| `CategorySummary.kt` | `data class CategorySummary(category: String, total: Double)` |
| `SubcategoryGroup.kt` | `data class SubcategoryGroup(category: String, subcategory: String, total: Double, transactions: List<Transaction>)` |
| `MonthSummary.kt` | `data class MonthSummary(year: Int, month: Int, total: Double)` |

### UI Components
| File | Contents |
|---|---|
| `AccountDropdown.kt` | Top bar account selector composable |
| `PeriodTabBar.kt` | Tab row: Daily / Monthly / Yearly / All |
| `DateNavigationBar.kt` | `←  date label  →` + expense total row |
| `CategorySummaryList.kt` | Flat list of category rows with drill-down arrow |
| `YearlyViewToggle.kt` | Radio buttons: Date View / Category View (Yearly tab only) |
| `MonthlyBreakdownList.kt` | 12-month list rows for Yearly Date View |
| `SubcategoryDetailScreen.kt` | Full screen: expandable subcategory groups + transactions |

### Screens modified
| File | Change |
|---|---|
| `TransactionsScreen.kt` | Major refactor: new top bar, period tabs, date nav bar, route new screens |
| `TransactionViewMode.kt` | Delete — no longer used |

---

## 9. Test Requirements

Per project mandate (TDD, 100% coverage):
- Unit tests for every new model and component file.
- Tests for date arrow navigation logic (prev/next for each period type).
- Tests for FY month ordering (April first).
- Tests for category/subcategory grouping and total computation.
- Tests for `DateRange.All` handling in data source and repository layers.
- Tests for ViewModel new state flows (`selectedAccount`, `expenseTotal`, `categorySummary`, `monthlySummary`).

---

## 10. Color Coding

- Existing color convention is retained and extended throughout the new UI:
  - **Expense amounts**: red
  - **Income amounts**: green
  - **Balance (net)**: red if negative, green if positive
- No new color palette is introduced; the app's existing `MaterialTheme.colorScheme.error` (red) and `Color(0xFF4CAF50)` (green) continue to be used.

---

## 11. Out of Scope

- Chart / graph view (explicitly excluded per requirements clarification).
- Weekly tab (not required).
- Custom date range picker (removed along with the date range dialog).
