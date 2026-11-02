# INC-08-A: wrong day in the sales report: hint ladder

Open ONE level at a time, and only after you have tried for 10 minutes.

<details><summary>Level 1: symptom only</summary>

The report is right on the developer laptop in New York and wrong on servers elsewhere. The sales data and the shop zone are identical in both places. What else differs between the machines?
</details>

<details><summary>Level 2: where to look</summary>

`DailySalesReport.dayOf`. The class receives `shopZone` in its constructor. Is that field used where the day is computed?
</details>

<details><summary>Level 3: the tool</summary>

Set a breakpoint in `dayOf` and run `sameReportOnAnyServer` for "Asia/Tokyo" in the debugger. Step INTO `java.sql.Date.toLocalDate()` (enable "step into library code" or use Force Step Into). Which time zone does it consult?

Also run `ZoneProbe.main` with VM option `-Duser.timezone=Asia/Tokyo`, then with `-Duser.timezone=America/New_York`.
</details>

<details><summary>Level 4: the concept to search</summary>

Search: "java default time zone TimeZone.getDefault java.sql.Date toLocalDate" and "Instant atZone toLocalDate".
</details>

<details><summary>Level 5: fix outline</summary>

Compute the day from the instant and the shop's zone, nothing implicit: `sale.placedAt().atZone(shopZone).toLocalDate()`. Delete the `java.sql.Date` conversion. Keep the guard test green.
</details>
