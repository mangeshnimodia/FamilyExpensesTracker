This document describes the list of integration tests.
These tests test the complete flow but just without the actual Google Sheet integration. Instead it uses a sheet from disk. 

Prerequisite -
1. An excel exists in tests folder with prefilled data used for tests.
2. A mock is created to Google Sheet integration layer which uses the above excel sheet.

Tests -
1. Add entry
    - GIVEN: Excel sheet is present and editable
    - WHEN: User adds an entry with all the fields with date being 2013/02/27. User hits the refresh button.
    - THEN: Entry is added to the excel and test validates the rendering on UI. Validations -
      Verify the entry is visible on UI with all these fields - Date in UI format, Amount, Account, Category, Subcategory, Payment method.
      Verify Edit/Copy/Delete options are visible on the entry.
2. Edit/Copy entry
    - GIVEN: User adds an entry
    - WHEN: User edits this entry with all the fields with date being 2013/02/28. User hits the refresh button.
    - THEN: Entry is edited in the excel and test validates the rendering on UI. Validations -
      Verify the entry is visible on UI with all these fields - Date in UI format, Amount, Account, Category, Subcategory, Payment method.
    - WHEN: User copies this entry and updates all its fields and description appended as "updated" and hits copy button and hits refresh button
    - THEN: Entry is copied in the excel and test validates the rendering on UI. Validations -
     Verify both the existing and copied entries are visible on UI with all these fields - Date in UI format, Amount, Account, Category, Subcategory, Payment method.
    - WHEN: User searches for the description
    - THEN: Validate that 2 entries are returned with all the fields.
3. Delete entry
    - GIVEN: User adds an entry
    - WHEN: User delete this entry. User hits the refresh button.
    - THEN: Entry is deleted from the excel and test validates the entry is no more shown on UI on refresh on that particular date. 

   