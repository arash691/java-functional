# INC-08-B: random wrong dates in the import: hint ladder

Open ONE level at a time, and only after you have tried for 10 minutes.

<details><summary>Level 1: symptom only</summary>

Parsing one date at a time always works. With many threads, some dates are wrong or the parser throws strange exceptions such as `NumberFormatException: For input string: ""`. Which part of `OrderDateParser` is shared by all threads?
</details>

<details><summary>Level 2: where to look</summary>

The `static final SimpleDateFormat FORMAT`. `final` means the reference never changes. Does it mean the object never changes?
</details>

<details><summary>Level 3: the tool</summary>

Run `parsingIsCorrectUnderConcurrency` several times and read the "first problem" text in the failure message. Then read the Javadoc of `SimpleDateFormat`, section "Synchronization". A thread dump will not help here: nothing is blocked, the shared object is being corrupted.
</details>

<details><summary>Level 4: the concept to search</summary>

Search: "SimpleDateFormat not thread safe" and "DateTimeFormatter immutable thread-safe" and Effective Java Item 17 (minimize mutability).
</details>

<details><summary>Level 5: fix outline</summary>

Replace `SimpleDateFormat` with `LocalDate.parse(text)` (ISO format `yyyy-MM-dd` is the default of `LocalDate.parse`), catching `DateTimeParseException` and rethrowing `IllegalArgumentException` with the cause. Immutable objects can be shared freely.
</details>
