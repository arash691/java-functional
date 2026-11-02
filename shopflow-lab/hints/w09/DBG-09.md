# DBG-09: find the bug with only a conditional breakpoint: hint ladder

Open ONE level at a time, and only after you have tried for 10 minutes. No `System.out`, no new log lines.

<details><summary>Level 1: symptom only</summary>

The grand total differs from the reference by 6,360 cents. The difference comes from several lines; you must find out which ones and why.
</details>

<details><summary>Level 2: where to look</summary>

`BulkPricer.totalCents()` loops over 1,000 lines. `ReferencePricer.lineCents(i)` is the trusted answer for each line.
</details>

<details><summary>Level 3: the tool</summary>

Set a breakpoint on the line inside the loop. Right-click the breakpoint (IntelliJ: red dot) and add the condition `BulkPricer.lineCents(i) != ReferencePricer.lineCents(i)`. Debug `BulkPricerTest.grandTotalMatchesReference`. When it stops, use "Evaluate Expression" on `BulkPricer.quantity(i)`.
</details>

<details><summary>Level 4: the concept to search</summary>

Look at the quantity you found. Compare it with the business rule in the class Javadoc: "100 units or more". Search: "off-by-one boundary condition greater than vs greater or equal".
</details>

<details><summary>Level 5: fix outline</summary>

In `lineCents`, the discount tier test must be `quantity(index) >= 100`, not `> 100`.
</details>
