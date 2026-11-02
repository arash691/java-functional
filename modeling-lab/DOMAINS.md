# Modeling Lab: practical business domains

Used in the modeling block (`lessons/modeling/`, weeks M1 to M8). Two kinds of domains:

- **Warm-ups (W1, W2):** tiny, everyday, no hard rules. Only for M1 and M2, to learn the mechanics of modeling.
- **Practice domains (1 to 10) and the ShopFlow capstone:** the kind of systems real backend teams build and maintain: retail, orders, banking, ledgers, bookings, billing, HR, access, inventory, contracts. You model each one first as **classes** (M5, M6), then as **data** (M8).

How to use a brief:
1. Read it once. List the nouns (candidate classes or entities) and verbs (candidate methods or relationships).
2. Questions marked **Ask** are gaps in the brief. Decide an answer, write it down as an assumption, and move on. A real client or domain expert would answer them; here you choose.
3. **Concepts** names the building blocks from M3 (parties, roles, organizations) and M4 (money, time, temporality, history, transactions) that this domain needs. Use them; do not reinvent them.
4. The **Twist** is the modeling trap of the domain. Decide how you handle it and say why.

Keep each model small: 5 to 9 classes, 5 to 9 tables. Over 12 means you are modeling the whole company, not the brief.

| # | Domain | Practised in | Main concepts |
|---|---|---|---|
| W1 | Library (warm-up) | M1, M2 | title vs copy |
| W2 | Parking lot (warm-up) | M1, M2 | pricing rule, ticket |
| 1 | Retail catalog and pricing | M5 | money, effective dating, price history |
| 2 | Orders, payments and refunds | M5 | transaction, state lifecycle, snapshot |
| 3 | Bank accounts and transfers | M5 | money, transaction, history |
| 4 | General ledger (double-entry) | M5 | ledger, immutability, reversal |
| 5 | Booking and reservations | M5 | time interval, availability, policy |
| 6 | Subscription billing and invoicing | M6 | recurring time, proration, money |
| 7 | HR and payroll | M6 | person, employee, org unit, labor, temporality |
| 8 | Identity, access and organizations | M6 | user vs person, role, tenant |
| 9 | Inventory and warehouse | M6 | quantity, movement, location |
| 10 | Insurance policy (long-lived contract) | M6 | versions, bitemporal time |
| Capstone | ShopFlow (e-commerce) | M8 | all of the above |

---

## Warm-ups (for M1 and M2 only)

### W1. Library
A city library lends books to members. A title can have many physical copies. A member may borrow up to 5 copies at once for 21 days. A late return costs a fine per day. A member with unpaid fines cannot borrow.
- **Ask:** Is a "book" the title or the physical copy?
- **Twist:** title vs copy: two different things with the same everyday name.

### W2. Parking lot
A garage has spots of different sizes (motorcycle, car, large). A vehicle gets a ticket on entry and a free spot it fits. On exit the fee is computed from the time spent, with a daily maximum.
- **Ask:** Can a car use a larger spot? What if the ticket is lost?
- **Twist:** fee rules change (hourly, weekend, event). Where does that logic live?

---

## Practice domains

### 1. Retail catalog and pricing
An online retailer sells products. A product has variants (size, color) and belongs to categories. Each variant is sold at a price per currency. Prices change over time, and marketing schedules promotions (percent off, fixed amount off, buy-two-get-one) with start and end dates. Customers see the current price; reports must show what a product cost on any past date.
- **Ask:** Is the price on the product, the variant, or in a price list? Can promotions stack? Are prices stored with or without tax?
- **Concepts:** Money, effective dating (valid from/to), price history, category tree.
- **Twist:** "What is the price?" always depends on a date (and a currency, and a promotion). A price is never one number.

### 2. Orders, payments and refunds
A customer places an order made of lines. The customer pays by card; the payment provider may approve, decline, or time out. An order can be cancelled before shipping and partly or fully refunded after. Orders move through states (new, paid, shipped, delivered, cancelled, refunded).
- **Ask:** One payment per order or many attempts? Can a refund exceed what was paid? Can lines be shipped separately?
- **Concepts:** Money, transaction (business sense), state lifecycle with history, snapshot of price and name at order time.
- **Twist:** payment attempts and refunds are *new facts*, not edits of the order. The order total must still be explainable years later.

### 3. Bank accounts and transfers
A bank has customers who own one or more accounts (current, savings), possibly jointly. Money moves by deposit, withdrawal and transfer; each movement is recorded and shown on a monthly statement. Current accounts may have an overdraft limit. Some transfers are in a different currency.
- **Ask:** Who is allowed to act on a joint account? Is a pending transfer part of the balance?
- **Concepts:** Money and currency, party/role (owner, co-owner), transaction, balance as stored vs computed, history.
- **Twist:** a transfer touches two accounts and must never half-happen. The balance is either derived from movements or stored and verified: decide and defend.

### 4. General ledger (double-entry)
A small company records every financial event as a journal entry made of lines. Each line debits or credits an account from a chart of accounts (asset, liability, income, expense, equity). In every entry, total debits equal total credits. Entries are never edited; mistakes are corrected with a reversing entry. Period closing locks a month.
- **Ask:** One currency or many? Can an account have sub-accounts? Who may post into a closed period?
- **Concepts:** Money, immutability, reversal instead of deletion, balancing invariant, accounting period (time), audit trail.
- **Twist:** append-only data. The invariant "debits = credits" belongs to the entry as a whole, not to a line.

### 5. Booking and reservations
A business rents out bookable resources (hotel rooms, meeting rooms, doctors' time). A customer books a resource for a time range. A resource cannot be double-booked. Prices vary by season. Cancellation is free until 48 hours before start; later cancellations pay a fee. A booking may be confirmed only after a deposit.
- **Ask:** Booking a resource type or a specific resource? What are the time zones of the resource and the customer? Overbooking allowed?
- **Concepts:** Time interval (start inclusive, end exclusive), time zone, availability, policy object, state lifecycle.
- **Twist:** two overlapping ranges; and the price and cancellation policy that applied *when the booking was made* must not change later.

### 6. Subscription billing and invoicing
A software company sells plans (monthly or yearly, per seat). A customer subscribes, upgrades, downgrades, pauses or cancels. At each cycle the system issues an invoice with lines (plan, extra seats, proration, tax, discount). Unpaid invoices are retried and the subscription can be suspended.
- **Ask:** When does an upgrade take effect? Are invoices editable after issue?
- **Concepts:** Money, recurring time (anchor date, month ends), proration, immutable invoice, status history.
- **Twist:** "monthly" has no fixed length (31 January + 1 month?). Proration needs exact rules and rounding.

### 7. HR and payroll
A company employs people. A person can be an employee, a contractor or both over time, and may also be a customer elsewhere. Employees belong to organization units (departments, teams) arranged in a tree, hold a position with a role and manager, and have a contract with salary and weekly hours. Employees log worked hours; payroll computes pay per month. People move between departments and get raises.
- **Ask:** Can an employee be in two units at once? Who is the manager when the manager leaves?
- **Concepts:** Party (person), employee/role as a relationship, organization unit tree, labor (work done) vs contract (agreement), effective dating, salary history.
- **Twist:** "employee" is a role a person plays during a period, not a kind of person. Almost every fact here is true *from a date until a date*.

### 8. Identity, access and organizations
A multi-company SaaS product has user accounts. A user belongs to one or more organizations (tenants) with different roles in each (admin, member, billing). Roles grant permissions. A person may have two accounts (work and private) and invites can be sent to people who do not have an account yet. Admins can suspend a user and all changes must be auditable.
- **Ask:** Are roles fixed or can organizations define their own? Does a deleted user disappear from the audit log?
- **Concepts:** User vs person vs account, organization, membership as an association with its own data, role and permission, audit/history.
- **Twist:** the user does not have one role; the *membership* (user in organization) has a role. Deleting a user must not break the history.

### 9. Inventory and warehouse
A warehouse stores products in locations (zone, shelf, bin). Goods arrive in lots with expiry dates, are picked for orders, moved between locations, and counted periodically. The system answers: how many units of product X are available now, in which locations, and which lots expire first (FEFO).
- **Ask:** Is stock stored per location or per lot or both? What happens when a count finds a difference?
- **Concepts:** Quantity and unit of measure, movement (transaction), location tree, reservation, derived vs stored stock level.
- **Twist:** stock level as a number you update (fast, loses history) vs the sum of movements (explainable, slower). Reserved is not the same as available.

### 10. Insurance policy (a long-lived contract)
An insurer sells policies for a person or company, covering things (a car, a house) for a period. A policy has versions: coverage can change mid-term (an endorsement), premiums are recalculated, and the policy can be renewed or cancelled. A claim is filed for an event on a given date and is judged against the policy as it was *on that date*, even if the policy is changed later.
- **Ask:** Can a policy have several insured parties? Can history be corrected afterwards (a mistake found)?
- **Concepts:** Party and role (policyholder, insured, beneficiary), versioning, valid time vs transaction time (bitemporal), money, status history.
- **Twist:** two clocks: "when was it true in the real world" and "when did the system learn it". The question "what did we know on date X" must be answerable.

---

## Capstone: ShopFlow (used in M8 and Week 12)
An online shop. Customers browse products grouped in categories, put products in a cart and place an order. Prices are in one currency for now. Stock is tracked per product; an order reserves stock. A payment is attempted per order and may fail. Orders move through states (new, paid, shipped, cancelled).
- **Twist:** the price and product name on an order line are copied at order time (a snapshot). Stock never goes negative. Combine domains 1, 2 and 9 in a small, coherent model.
