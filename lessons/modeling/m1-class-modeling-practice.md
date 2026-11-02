# M1: Class modeling by practice (before any theory)

**Mode:** A (build-along, practice first)  **Block:** Modeling (M1 of M1–M8, after week 9)
**Outcome:** you can turn a one-paragraph business brief into 4–7 well-named Java classes that carry behaviour, and run 3 scenarios against them. Your first attempts will be naive; that is the point. You keep them as the "before" picture for M2.
**Before you start**
- You finished weeks 1–9: classes, interfaces, enums, records, collections, exceptions. You do **not** need any database knowledge. Do not think about storage in M1–M6: classes live in memory only. Database design is taught in M7 and M8.
- Do not read about OOD, SOLID or UML this week. Resist it. M2 gives you the theory, and it only sticks if you first feel the problem.
- Workspace: `~/shopflow-notes/modeling/` with one folder per domain (`coffee-shop/`, `vending/`, `todo/`, `library/`, `parking/`, `pizza/`), each with `src/` and `out/`. Run `git init` in `~/shopflow-notes/modeling/`.
- Per-domain commands (single package-less folder is fine):
  `javac -d out src/*.java && java -ea -cp out Scenarios`
  `-ea` enables `assert`. A scenario is a method in `Scenarios.java` that builds objects, does things and checks results with `assert`. (If you prefer JUnit, a Maven project from week 3 works too.)
- Rubric: `modeling-lab/RUBRIC.md` (only the **C** rows). Briefs: `modeling-lab/DOMAINS.md` (the warm-ups W1 Library and W2 Parking lot; the practical domains come in M5 and M6, after the concept rooms M3 and M4). The warm-ups are deliberately small and everyday so you can learn the mechanics; real systems (orders, banking, ledgers, bookings, HR) come later.

> Rule of the week: **a model is only real when a scenario runs against it.** A class diagram that cannot run is a drawing.

---

## S1 Try it (3h): the nouns-and-verbs method, on small domains
1. (10m) **Set up.** Create the folders above, `git init`, and a `notes/m1.md` file. Write today's date and "before M1, I think a good class is: ..." (one honest sentence).
2. (15m) **The method (read, 5 minutes; then try).** Take a brief and:
   - **Underline the nouns.** Each is a *candidate* class. Cross out those that are only a number, a name or a label (they become fields). Cross out actors that your program never needs to represent.
   - **Underline the verbs.** Each is a candidate *method*. Ask: which noun does this verb *happen to*? Put the method there.
   - **Underline the rules** ("at most 5", "cannot", "unless"). Each rule must live in exactly one place, next to the data it protects.
   - **Write 3 scenarios** (a normal path, a rule being broken, an edge case) *before* coding. They are your tests.
3. (40m) **Warm-up A: coffee shop (decide first, then read the reveal).** Brief:
   > A small coffee shop sells drinks (espresso, latte, tea) in three sizes. A customer orders one or more drinks; each drink can have extras (extra shot, oat milk) that cost more. The price of an order is the sum of its drinks. The barista marks an order as ready, and the customer collects it. An order can be cancelled until it is ready.
   - (10m) In `notes/m1.md` list the nouns and verbs and write your 3 scenarios. **Predict:** how many classes will you need? (Write a number.)
   - (25m) Code it in `coffee-shop/src/`. Use whatever you built in week 4 for money (`Money`) or plain `int` cents if you did not keep it. Run your scenarios.
   - (5m) Write down which part felt fuzzy.
4. (10m) **Reveal for coffee shop.** Compare with your list.
   <details><summary>One reasonable answer</summary>

   Classes: `Order` (holds lines, a status, `total()`, `markReady()`, `collect()`, `cancel()`), `OrderLine` (a drink, a size, extras, `price()`), `Drink` (name and base prices per size), `Extra` (name, price), enum `Size`, enum `OrderStatus` (`NEW`, `READY`, `COLLECTED`, `CANCELLED`). *Customer* and *barista* are actors, not classes: nothing in the brief needs to be stored about them. The rule "cancel only until ready" lives in `Order.cancel()`. Anything between 4 and 7 classes is fine.
   </details>
   Note: did you create a `Customer` class that has no behaviour? That is common. Ask: does any rule or scenario need it?
5. (35m) **Warm-up B: vending machine.** Brief:
   > A vending machine holds products in numbered slots (A1, A2, ...), each with a price and a quantity. A customer inserts coins (10, 20, 50 cents and 1 euro), chooses a slot, and gets the product if there is enough credit and stock. Change is returned. The customer can cancel and get the inserted coins back. An operator can refill slots.
   - (5m) **Predict:** which class holds the *credit* (money inserted so far)? Write your answer before reading anything.
   - (5m) Nouns, verbs, rules, 3 scenarios (enough credit; not enough credit; sold out).
   - (25m) Code it in `vending/src/` and run the scenarios. Do not read the worked solution yet (it is in S3).
6. (20m) **CRC cards (paper or index cards).** For the vending machine, write one card per class: **C**lass name on top, **R**esponsibilities on the left (what it knows and what it does, in plain words), **C**ollaborators on the right (which other classes it talks to). Rule: if a card needs more than 5 responsibilities or the word "and" in its main responsibility, split it. Walk through scenario "sold out" by moving a finger from card to card: which card receives each message?
7. (30m) **Worked example: todo list (green, read it, change it).** Create `todo/src/Task.java`, `TodoList.java`, `Scenarios.java`:
   ```java
   public final class Task {
       private final String title;
       private boolean done;
       public Task(String title) {
           if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
           this.title = title.strip();
       }
       public String title() { return title; }
       public boolean isDone() { return done; }
       public void complete() { done = true; }
   }
   public final class TodoList {
       private final List<Task> tasks = new ArrayList<>();
       public Task add(String title) { Task t = new Task(title); tasks.add(t); return t; }
       public List<Task> open() { return tasks.stream().filter(t -> !t.isDone()).toList(); }
   }
   ```
   ```java
   public class Scenarios {
       public static void main(String[] args) {
           TodoList list = new TodoList();
           Task t = list.add("  buy milk ");
           assert t.title().equals("buy milk");
           t.complete();
           assert list.open().isEmpty();
           try { list.add(" "); assert false : "blank title must be rejected"; }
           catch (IllegalArgumentException expected) { }
           System.out.println("all scenarios passed");
       }
   }
   ```
   Run it. Then change **one thing**: add `priority` (HIGH, NORMAL) and a method `TodoList.mostUrgent()`. **Predict first:** which classes must change? Count the files you touched. Notice that `Task` validates itself and `TodoList` never reaches into `Task`'s fields: that is the shape you want to copy.
8. (20m) **Notes.** In `notes/m1.md`: (a) one thing that was easy, (b) one decision you were unsure about, (c) which class in your vending machine knows the most, (d) a guess for why.

**Tips and common problems**
- `java.lang.AssertionError` never appears even when a check is wrong → you forgot `-ea` → run `java -ea -cp out Scenarios`.
- `error: class X is public, should be declared in a file named X.java` → one public class per file → move it or drop `public`.
- You have 12 classes for a 6-line brief → you turned every noun and every actor into a class → reread step 2 and cross out labels and unused actors.
- You cannot decide where a method belongs → ask "whose data does it need most?" and put it there; write the doubt in your notes, M2 will give you tools.
- Scenarios feel like extra work → they are the only way to know your model runs; write them first and keep them tiny.

**Done when**
- [ ] `coffee-shop/`, `vending/`, `todo/` each compile and `Scenarios` prints "all scenarios passed".
- [ ] You have vending-machine CRC cards and a nouns/verbs list for each domain.
- [ ] `notes/m1.md` has your number-of-classes prediction and what the result was.
- [ ] Committed in `~/shopflow-notes/modeling/` (`git commit -m "M1 S1 warm-ups"`).

---

## S2 Build (3h): first attempts on Library and Parking lot (no theory)
Today you make two deliberately naive models from `modeling-lab/DOMAINS.md`. Do not search for "how to design a library system". Your attempt, with its flaws, is the learning material.

1. (10m) **Rules.** Write in `notes/m1.md`: "I will not look at solutions. I will keep every first attempt unchanged. If a decision is unclear, I choose, write the assumption and continue." Set a visible timer for each step below; stop when it rings.
2. (15m) **Library: analyse.** Read warm-up W1. Underline nouns, verbs, rules. Answer the **Ask** questions by choosing and writing assumptions. **Decide first:** is a *book* the title or the physical copy? Write your choice and why. Write 3 scenarios: borrow and return a copy; a sixth borrow is refused; a member with an unpaid fine cannot borrow.
3. (20m) **Library: CRC cards.** Max 7 cards. Walk through the scenario "member with unpaid fine tries to borrow" with your finger: which card decides? If the answer is "nobody", you found a hole.
4. (50m) **Library: code.** In `library/src/`. Include late-return fine calculation (use `java.time.LocalDate`, week 8) and the reservation queue. Your first working version may be ugly. Commit when it compiles: `git commit -m "M1 library first attempt"` and tag it: `git tag m1-library-before`.
5. (20m) **Library: scenarios.** Write and run your 3 scenarios. Add a 4th that tries something the brief forbids (for example returning a copy that was never borrowed). Does your code reject it clearly?
6. (10m) **Break.** Stand up, no screen. Then write 3 lines in `notes/m1.md`: which class is the largest, which class has no behaviour, which rule is in more than one place.
7. (55m) **Parking lot: the whole loop, faster.** Read warm-up W2 (multi-level garage, spot sizes, ticket, fee with daily maximum).
   - (10m) Nouns, verbs, rules, assumptions, 3 scenarios (park and leave with fee; no free spot for a large vehicle; lost ticket).
   - (30m) Code it in `parking/src/`. Make the fee calculation work for at least hourly price and a daily maximum.
   - (10m) Run scenarios. Commit and tag `m1-parking-before`.
   - (5m) **Predict:** the owner now says "weekends are free". Which files do you have to edit? Write the list. Do not edit anything.

**Tips and common problems**
- You keep redesigning instead of finishing → you are polishing a draft → finish the scenarios first; M2 is for improving.
- Your fee code sits in `ParkingLot` or `Main` → natural for a first attempt → leave it, but note it; you will find it in S3.
- `LocalDate` arithmetic is awkward → use `ChronoUnit.DAYS.between(a, b)` (week 8) and test with fixed dates, never `LocalDate.now()` inside the logic.
- Time boxes overrun by 2x → use the stuck routine in `lessons/README.md`; ship an incomplete model with a note rather than skip the scenarios.

**Done when**
- [ ] `library/` and `parking/` compile; each has 3 passing scenarios plus 1 "forbidden action" scenario for Library.
- [ ] Git tags `m1-library-before` and `m1-parking-before` exist.
- [ ] Assumptions for the **Ask** questions are written in `notes/m1.md`.
- [ ] You wrote your prediction about the "weekends are free" change.

---

## S3 Practice (2h): see your own mistakes, compare with a worked solution
1. (10m) **Decide before you read.** Open your Library code. Without any list in front of you, write the 3 things you think are wrong with it. Keep this note.
2. (30m) **Naive-failure checklist.** Go through both models and count each item (tally in `notes/m1.md`, **do not fix anything yet**):
   - **Public fields** or setters for everything (`member.fines = 0`).
   - **Anemic classes**: only fields and getters; the logic lives in someone else.
   - **God class**: one class (often `Library` or `ParkingLot`) with most of the code.
   - **`Manager`, `Helper`, `Util`, `Service` classes** that operate on other classes' data.
   - **Primitives for everything**: `int status = 2`, `String type = "CAR"`, `double fee`, or `String plate` passed around raw.
   - **Rule in two places**: for example the limit 5 appears in two methods.
   - **Type switches**: `if (v instanceof Car) ... else if (v instanceof Truck) ...` or `switch (type)`.
   - **Static or global mutable state**.
3. (30m) **Compare with a worked solution (vending machine).** Read it, then list 5 differences from your S1 version in `notes/m1.md`.
   ```java
   enum Coin { TEN(10), TWENTY(20), FIFTY(50), EURO(100);
       final int cents; Coin(int cents) { this.cents = cents; } }
   record Product(String name, int priceCents) { }
   final class Slot {
       private final Product product; private int quantity;
       Slot(Product product, int quantity) { this.product = product; this.quantity = quantity; }
       boolean isEmpty() { return quantity == 0; }
       Product dispense() {
           if (isEmpty()) throw new IllegalStateException("sold out: " + product.name());
           quantity--; return product;
       }
       void refill(int amount) { if (amount <= 0) throw new IllegalArgumentException("amount"); quantity += amount; }
   }
   final class VendingMachine {
       private final Map<String, Slot> slots = new HashMap<>(); private int creditCents;
       void install(String code, Slot slot) { slots.put(code, slot); }
       void insert(Coin coin) { creditCents += coin.cents; }
       Purchase select(String code) {
           Slot slot = slots.get(code);
           if (slot == null) throw new IllegalArgumentException("unknown slot " + code);
           if (slot.isEmpty()) throw new IllegalStateException("sold out");
           int price = slot.priceCents();            // Slot exposes price from its Product
           if (creditCents < price) throw new IllegalStateException("insert " + (price - creditCents) + " more cents");
           Product p = slot.dispense(); creditCents -= price;
           return new Purchase(p, refund());          // change is just the remaining credit
       }
       int refund() { int r = creditCents; creditCents = 0; return r; }
   }
   record Purchase(Product product, int changeCents) { }
   ```
   What to look for: each class guards its own rule (`Slot` refuses to dispense when empty, `VendingMachine` guards credit); `Coin` is an enum, not an `int`; no public fields; `refund()` is the single place that clears credit; the result of a purchase is a small object instead of two loose values. (`Slot.priceCents()` is a one-line method you add. Using `int` cents is acceptable at this stage; reuse your week-4 `Money` if you built it.)
4. (25m) **Score with the rubric.** Score Library and Parking lot on rows **C1–C8** of `modeling-lab/RUBRIC.md` (0/1/2 each) and write the totals. Be strict. Write the two lowest rows for each. Row C8 needs your scenarios to run.
5. (15m) **One small fix only.** Pick the single worst smell from your tally and fix just that (for example turn `int status` into an enum). Commit as `M1 fix one smell`. Re-run scenarios. Everything else stays, ready for the M2 refactor.
6. (10m) **Prediction for M2.** Write: "After I learn OOD, my score should rise on rows ..., because ...".

**Tips and common problems**
- Everything on the checklist is ticked → that is normal for a first attempt → do not feel bad, count them; M2 is built around exactly these.
- Nothing on the checklist is ticked → be suspicious, look for classes with only getters → anemic classes are easy to miss because they look tidy.
- You want to rewrite everything now → resist; the original is your "before". Fix only one smell.

**Done when**
- [ ] Tally of naive failures for both models, rubric scores for both, and the vending comparison are in `notes/m1.md`.
- [ ] Exactly one smell fixed and committed; tags `m1-*-before` still point to the originals.

---

## S4 Consolidate (2h)
1. (20m) **Ledger.** Write 2 entries in `reality-lab/LEDGER.md` format: (a) "Anemic classes with logic in a `*Manager`", (b) "Rule duplicated in two places". Each: symptom you saw in your code, why it hurts (use your "weekends are free" prediction), what you will try in M2.
2. (15m) **Flashcards (5).**
   1) How do I find candidate classes in a brief? → Underline nouns; drop labels and unused actors.
   2) How do I decide where a method goes? → On the class whose data it needs most.
   3) What does a CRC card hold? → Class, Responsibilities, Collaborators.
   4) Why is `ParkingManager` a weak name? → It says "does things to others"; the logic belongs on the things themselves.
   5) Why write scenarios before code? → They define done and prove the model runs.
3. (25m) **Self-check (answer out loud, then open).**
   1. Name three words from a brief that should **not** become classes.
   2. A class has private fields, a getter and a setter for each. What is wrong?
   3. Where should the rule "a member cannot borrow more than 5" live, and why only there?
   4. What do the three Cs of CRC stand for and what is a collaborator?
   <details><summary>Answers</summary>

   1. A number, a name or label (they become fields); an actor the program never needs to store (for example "customer" in the coffee shop); a verb-noun like "payment processing" that is really a method.
   2. It is a data bag: any caller can bring it into an invalid state, and the logic is somewhere else (anemic). Replace setters with methods that express the business action and check rules.
   3. On the class that owns the data needed to decide (the member or the loan list it owns), in one method, so the rule exists once and cannot be bypassed.
   4. Class, Responsibility, Collaborator; a collaborator is another class this class asks for help.
   </details>
   Score: fewer than 3 of 4 right → redo S1 step 2 and retry in the next S4.
4. (40m) **Timed kata: pizza order.** Brief: *A pizzeria takes orders for pizzas (small, medium, large) with toppings. Each topping costs extra. An order has a delivery or pickup type. Delivery has a minimum order amount of 15. The kitchen marks an order as baked.* Folder `pizza/`. 10m analysis and scenarios, 25m code, 5m run. Do not polish. Tag `m1-pizza-before`.
5. (10m) **Retro.** Three lines in `retro.md`: what worked, what blocked me (and for how long), what I will change next week.
6. (10m) **Tidy.** Commit everything, `git log --oneline` shows tags and commits; copy your rubric scores to a table in `notes/m1.md`: `domain | C1..C8 | total`.

**Tips and common problems**
- You skip the kata because you are tired → it is the cheapest evidence of progress → do it at half scope rather than skip it.
- You are tempted to read about design principles → keep the question list in `notes/questions.md`; M2 starts with it.

**Done when**
- [ ] Two ledger entries, five flashcards and the self-check done.
- [ ] `pizza/` exists with scenarios passing; four "before" tags exist.
- [ ] Retro lines written; rubric table in notes.

**Output:** `coffee-shop/`, `vending/`, `todo/`, `library/`, `parking/`, `pizza/` with scenarios; CRC cards (photo or text); rubric scores; 2 ledger entries; 5 flashcards.
**If stuck:** shrink the brief to two classes and one scenario, make that green, then add one rule at a time.
