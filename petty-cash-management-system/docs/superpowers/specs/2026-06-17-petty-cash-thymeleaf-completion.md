# Petty Cash Management System — Thymeleaf Completion Plan

**Date:** 2026-06-17
**Assessment Date:** 2026-06-27 (10 days)
**Stack:** Java 21, Spring Boot 4.0, Thymeleaf, Tailwind CSS, MySQL
**Frontend Approach:** Server-side rendered Thymeleaf (NOT a separate SPA)

---

## 1. Goals for Assessment

A fully working, professionally styled petty cash system where an assessor can:

1. **Log in** as Admin, Manager, or Employee via a login page
2. **See role-specific dashboards** with real database stats
3. **Submit requests** (Employee), **Approve/Reject** (Manager), **Disburse** (Accountant)
4. **View funds, transactions, and reports** with real data
5. **See realistic sample data** — not empty tables with placeholder text
6. **Navigate a consistent, polished UI** — same design language across all pages

---

## 2. Complete File Inventory

### Files to Create
| File | Purpose |
|------|---------|
| `src/main/resources/static/css/style.css` | **Rewrite** — full Tailwind + custom styles |
| `src/main/resources/templates/login.html` | Login page with role-based redirect |
| `src/main/resources/templates/transactions.html` | Fund transaction history page |
| `src/main/resources/templates/users.html` | User management page (Admin only) |
| `src/main/resources/data.sql` | Sample data seed script |

### Files to Modify
| File | Changes |
|------|---------|
| `src/main/java/.../config/SecurityConfig.java` | Add form login, role-based access, password encoder |
| `src/main/java/.../config/DataInitializer.java` | Seed users, requests, approvals, disbursements, fund |
| `src/main/java/.../controller/ViewController.java` | Add auth context, user-aware routing, new pages |
| `src/main/resources/templates/dashboard-admin.html` | Replace hardcoded table with dynamic Thymeleaf loop |
| `src/main/resources/templates/dashboard-manager.html` | Fix employee name display (`fullName` not `firstName+lastName`) |
| `src/main/resources/templates/dashboard-employee.html` | Minor polish, new request button linking to form |
| `src/main/resources/templates/requests.html` | Add request number, date, employee name columns; add creation form |
| `src/main/resources/templates/approvals.html` | Fix column count, add manager name, decision badge styling |
| `src/main/resources/templates/disbursements.html` | Fix table structure, add all columns, add disbursement form |
| `src/main/resources/templates/funds.html` | Add replenishment form, transaction history link |
| `src/main/resources/templates/reports.html` | Replace hardcoded numbers with Thymeleaf `${...}` |
| `src/main/resources/templates/fragments/sidebar.html` | Unified sidebar matching admin dashboard theme, add login/logout |
| `src/main/resources/application.properties` | (check if any changes needed) |

---

## 3. Implementation Phases

### Phase 1: Security & Authentication (Day 1-2)

**Objective:** Login page, role-based dashboards, password encoding

#### SecurityConfig.java Changes
```java
- Disable CSRF only
- Add formLogin() with custom login page
- Add role-based URL authorization:
  - /admin/** → ADMIN only
  - /manager/** → MANAGER only
  - /employee/** → EMPLOYEE only
  - /login, /css/** → permit all
- Add BCryptPasswordEncoder bean
- Add logout functionality
```

#### ViewController.java Changes
```java
- Inject HttpSession or SecurityContext for current user
- Change root "/" redirect → login page
- Admin dashboard → check ADMIN role
- Manager dashboard → check MANAGER role
- Employee dashboard → get employee ID from authenticated user (not hardcoded 1L)
- Add /login GET mapping
- Add login error handling
```

#### login.html
- Clean form with email + password fields
- Error message display
- Link to no "register" (admin creates users)
- Matching the slate/Inter design of dashboards

#### UI Changes
- Sidebar: add active page highlighting
- Sidebar: show logged-in user name + role badge
- Sidebar: add logout button

### Phase 2: Sample Data Generator (Day 2)

**Objective:** Realistic data so assessor sees a populated system

#### DataInitializer.java (extend)
Create on startup:
- 3 Admins: Alice Admin, Bob Admin, Charlie Admin
- 3 Managers: Diana Manager, Ethan Manager, Fiona Manager
- 5 Employees: George, Hannah, Ivan, Julia, Kevin (all employees)
- 1 PettyCashFund with balance of $10,000
- 15-20 PettyCashRequests across all statuses (PENDING, APPROVED, REJECTED, DISBURSED)
- Corresponding Approvals for approved/rejected requests
- Corresponding Disbursements for disbursed requests
- FundTransactions for disbursements and replenishments

**Data shape:**
- Mix of realistic purposes: "Office Supplies", "Client Lunch", "Fuel Reimbursement", "Stationery", "Taxi Fare", "Team Breakfast", etc.
- Amounts between $10 and $500
- Dates spread across last 30 days
- Reference numbers like "REQ-001", "DISB-a1b2c3d4"

### Phase 3: UI Unification (Day 3-4)

**Objective:** Every page matches the admin dashboard's clean design

#### Design System
- **Font:** Inter (Google Fonts) — already in admin dashboard
- **Background:** `bg-slate-50`
- **Text:** `text-slate-800`
- **Cards:** `bg-white rounded-2xl shadow-sm border border-slate-100`
- **Tables:** Clean header with `bg-white text-slate-400 uppercase tracking-wider`, striped body
- **Badges:** Color-coded status pills (yellow=pending, green=disbursed, red=rejected, blue=approved)
- **Buttons:** Consistent rounded, colored (indigo primary, green success, red danger)

#### sidebar.html Rewrite
- Match admin dashboard's `bg-gray-900` dark theme
- Add active page indicator (left border or bg highlight)
- Add user info section at bottom (name + role + logout)
- Add icons to nav links

#### Individual Page Fixes

**requests.html:**
- Add request number, date, employee, amount, purpose, status columns
- Add "New Request" button/modal form
- Add status badge color coding
- Add empty state: "No requests found. Create your first request."

**approvals.html:**
- Fix column mismatch (5 columns: ID, Request#, Manager, Decision, Date)
- Add request number + employee name display
- Add decision color badges
- Add approve/reject action buttons (forms)
- Add empty state

**disbursements.html:**
- Fix table: Request#, Amount, Reference#, Disbursed By, Date
- Add disbursement action form (dropdown of approved requests + disburse button)
- Add empty state

**funds.html:**
- Current balance card (large, prominent)
- Add replenishment form (amount + description + submit)
- Transaction history table below
- Add empty state for no transactions

**reports.html:**
- Summary cards: Total Requests, Pending, Approved, Disbursed, Rejected, Current Balance
- All pulling from backend model attributes
- Add transaction history table

### Phase 4: Dynamic Data Wiring (Day 5-6)

**Objective:** All pages display real database data, forms create/modify records

#### ViewController.java Additions
```java
// New route: /employee/requests/new — show create form
// New route: /employee/requests/create — POST handler

// New route: /manager/approvals/approve/{id} — POST approve
// New route: /manager/approvals/reject/{id} — POST reject

// New route: /accountant/disbursements/new — show disbursement form  
// New route: /accountant/disbursements/create — POST disburse

// New route: /admin/funds/replenish — POST replenish

// New route: /transactions — show all fund transactions
// New route: /users — show all users (admin only)
```

#### Approval Flow (Manager)
Manager dashboard shows pending requests with inline Approve/Reject buttons.
Each button posts to `/manager/approvals/{id}?decision=APPROVE` or `REJECT`.
The backend updates request status + creates Approval record.
Redirect back to dashboard with success/error message.

#### Disbursement Flow (Accountant/Admin)
Accountant sees approved requests ready for disbursement.
Disbursement form: select request, confirm amount, submit.
Backend: reduces fund balance, creates disbursement record, creates fund transaction, updates request status to DISBURSED.

#### Fund Replenishment (Admin)
Admin enters amount + description.
Backend: increases fund balance, creates REPLENISHMENT transaction.

### Phase 5: Reports & Transactions (Day 6-7)

**Objective:** Full reporting view

#### reports.html
- Summary cards populated from `DashboardService`
- Transactions table showing all fund_transactions
- Filters: by type (DISBURSEMENT, REPLENISHMENT), by date range

#### transactions.html
- Full transaction history table
- Transaction type badge coloring
- Amount formatting (positive green for replenishment, negative red for disbursement)

### Phase 6: Polish & Testing (Day 7-10 Buffer)

- **Error handling:** Friendly error messages on form validation failures
- **Empty states:** Every table has a graceful "no data" message
- **Responsive:** Check layout at different widths
- **Consistency audit:** Every page viewed side-by-side for visual consistency
- **Demo walkthrough:** Full workflow test — create request → approve → disburse → check balance
- **Edge cases:** What happens when balance is insufficient? What happens trying to approve already-approved request?

---

## 4. Architecture Decisions

### Authentication
- Spring Security form login (NOT JWT — unnecessary complexity for server-side rendering)
- BCrypt password encoding
- Session-based auth
- Role-based URL security with `hasRole()`

### Form Handling
- Thymeleaf forms with `th:action` and `th:object`
- POST-Redirect-GET pattern
- Flash attributes for success/error messages (`RedirectAttributes`)

### Data Flow
```
Browser → Login → Spring Security → Role-based session
  → ViewController gets authenticated user
  → Calls Service layer
  → Service calls Repository
  → Returns data to Model
  → Thymeleaf renders HTML
```

### Password Management
- All sample users get a known password like `password123`
- BCrypt encoded in DataInitializer
- Documented for assessor

---

## 5. Risk Assessment

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| MySQL not running | Medium | Ensure MySQL is started before demo; have fallback |
| Sample data not loading | Low | DataInitializer with `@Order` and `if(count==0)` checks |
| Form login breaking | Low | Test after each change |
| Static resources not loading | Low | Check Thymeleaf template resolution |
| Time running out | Medium | Phase priorities: 1>2>3>4>5>6 — core workflow first |

---

## 6. Route Map (Final URL Structure)

| URL | Role | Page |
|-----|------|------|
| `/login` | All | Login page |
| `/` | All | Redirects to role-specific dashboard |
| `/admin/dashboard` | ADMIN | Admin dashboard |
| `/admin/users` | ADMIN | User management |
| `/admin/funds` | ADMIN | Fund management + replenishment |
| `/manager/dashboard` | MANAGER | Manager dashboard |
| `/manager/approvals` | MANAGER | Approval queue |
| `/employee/dashboard` | EMPLOYEE | Employee dashboard |
| `/employee/requests` | EMPLOYEE | My requests + new request form |
| `/requests` | ADMIN/MANAGER | All requests (read-only) |
| `/approvals` | ADMIN/MANAGER | Approval list |
| `/disbursements` | ADMIN/ACCOUNTANT | Disbursement list + form |
| `/funds` | ADMIN/ACCOUNTANT | Fund details |
| `/transactions` | ADMIN/ACCOUNTANT | Transaction history |
| `/reports` | ADMIN/MANAGER | Reports |

---

## 7. User Experience Flows

### Admin Flow
```
Login → Admin Dashboard (stats cards + recent disbursements table)
  → User Management (view all users)
  → Fund Management (view balance, replenish fund)
  → Reports (view all stats)
  → Transaction History
```

### Manager Flow
```
Login → Manager Dashboard (pending count + pending requests table)
  → Approve/Reject inline from dashboard
  → Approvals page (history of all decisions)
  → Reports
```

### Employee Flow  
```
Login → Employee Dashboard (my stats + my recent requests)
  → My Requests (view all, create new)
  → New Request Form (amount, purpose, submit)
  → Track status changes in real-time
```

---

## 8. Key Implementation Details

### Thymeleaf Status Badge Pattern
```html
<span th:classappend="${req.status.name() == 'PENDING'} ? 'bg-yellow-100 text-yellow-700'
    : ${req.status.name() == 'APPROVED'} ? 'bg-blue-100 text-blue-700'
    : ${req.status.name() == 'DISBURSED'} ? 'bg-green-100 text-green-700'
    : 'bg-red-100 text-red-700'"
    class="px-3 py-1 rounded-full text-xs font-semibold"
    th:text="${req.status}">
</span>
```

### Flash Message Pattern
```java
// Controller
redirectAttributes.addFlashAttribute("success", "Request approved successfully!");

// Thymeleaf layout
<div th:if="${success}" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative" role="alert">
    <span th:text="${success}"></span>
</div>
```

### Form for Request Creation
```html
<form th:action="@{/employee/requests/create}" method="post" class="space-y-4">
    <div>
        <label class="block text-sm font-medium text-slate-700">Amount ($)</label>
        <input type="number" step="0.01" name="amount" required
               class="mt-1 block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
    </div>
    <div>
        <label class="block text-sm font-medium text-slate-700">Purpose</label>
        <textarea name="purpose" rows="3" required
                  class="mt-1 block w-full rounded-lg border-slate-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"></textarea>
    </div>
    <button type="submit" class="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition">
        Submit Request
    </button>
</form>
```

---

## 9. Success Criteria for Assessment

- [ ] Assessor can login as Admin, Manager, and Employee
- [ ] Each role sees different dashboard with relevant data
- [ ] Database is populated with realistic sample data
- [ ] Tables show data from MySQL — not hardcoded placeholders
- [ ] Employee can submit a new request
- [ ] Manager sees pending request and can approve/reject it
- [ ] Approved request status updates to APPROVED
- [ ] Accountant can disburse approved request
- [ ] Fund balance decreases on disbursement
- [ ] Admin can replenish fund balance
- [ ] Reports page shows aggregated stats
- [ ] Transaction history records all fund movements
- [ ] UI is consistent across all pages
- [ ] Empty states are handled gracefully
- [ ] Navigation sidebar highlights active page
- [ ] All pages have professional styling (Inter font, slate theme)
