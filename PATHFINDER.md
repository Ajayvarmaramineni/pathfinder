# WPI Pathfinder — Locked Final Goal

Locked 2026-11-18. This file replaces the milestone docs, project plan and course materials as the working reference.
The folder is the Android Studio project (Java) plus this file and `WPI_Pathfinder.db` (the full source data). The app's
database is `app/src/main/assets/Pathfinder.db`.

---

## 1. What we are building

**Course:** MIS 571 Database Applications Development, Fall 2026, Prof. Jim Ryan.
**Team 3:** Ajay Ramineni, Niyati Girish Gohil, Shevlin Jaffe, Sireesha Bachu.

**Pathfinder** is an Android app (Java, Android Studio) backed by a prepopulated SQLite database of public WPI
information. A student can find, from one place:

- which courses satisfy each requirement of a program, and which programs a course counts toward
- which programs each school and department offers
- which faculty belong to a department
- which office provides a student resource, and which building it is in
- which global project centers run in a term or region, and who directs them
- which research centers and core facilities exist
- where students live (residence halls, apartments, campus houses) and where to eat
- what is inside any campus building

**Problem:** this information is spread across many wpi.edu pages and the online catalog, so a student has to know
which office or page to check first. **Solution:** one searchable database plus an app built for retrieval.

**Locked scope decisions**
- **17 tables:** Milestone 3's 14 tables plus **ResidenceHall**, **DiningLocation** and **CourseCode** (the 1NF fix
  for cross-listed course codes).
- **No events and no academic calendar.** They change constantly and the app has no live feed from wpi.edu, so a
  snapshot would go stale.
- **Dropped from the Milestone 3 mockups:** the "Ask Pathfinder" chatbot, the "Opportunity Trail" saved-items and
  progress screens, and the Clubs and Scholarships tiles. The Housing & Dining tile stays, now backed by real tables.
  The chatbot is replaced by the keyword search (Q15).
- No student login, no personal student data, no network calls. All data is public and ships inside the app.

---

## 2. Deliverables and deadlines (from the syllabus and project description)

| When | What |
|---|---|
| Thanksgiving, Nov 23–29 | No class |
| Week 13, Nov 30–Dec 6 | Individual take-home final exam due (not the project) |
| **Week 14, Dec 7–11** | **Milestone #4:** database application (code), final project report, presentation (PowerPoint + short demo). Post the presentation in the discussion forum and submit the app code on Monday; comment on other teams' presentations by Sunday. |
| Team's own target | Finished product by Dec 1 (project plan) |

**Grading rubric (250 points)**
1. Database design, final and intermediate (100): a significant design problem, done correctly and well documented.
2. Phone app (50):
   - 2.1 (20) CREATE TABLE and INSERT output in the report as executed; queries use more than one table at a time (queries and their output in the report).
   - 2.2 (15) Code for data input and output screens (documented, runs correctly).
   - 2.3 (15) Beyond the basics, e.g. calendar input for dates, graphical output.
3. Report (50): problem description, user and technical documentation, accurate space and index info, problem-specific backup, security and data quality.
4. Presentation (50): slides, problem and design, demo, answering questions, comments on other teams.

**Final report must contain** (project description):
1. Business problem, and how a database built for retrieval helps solve it.
2. Database design:
   - ERD (Visio or equivalent)
   - 3NF relations
   - data dictionary: each table's purpose; each attribute's format and why it's included; PKs and FKs; extra indexes and why
   - relationships and the assumption behind each (why 1:N, why a value is unique)
   - output of running the CREATE TABLE commands
   - output of running the INSERT commands (sample data that shows the 1:N relationships)
3. User interface and query documentation: each query in user terms (what it answers, how to run it, its output, its SQL); app navigation with screenshots.
4. Technical documentation:
   - space estimates (method in §7)
   - backup and security
   - data quality problems and how to reduce them
   - listing of the code we wrote

**AI rule (syllabus):** AI use is encouraged, but submissions must include the URL of the AI prompt and describe the
changes made to the AI output. The team is responsible for its accuracy.
**Peer evaluation:** each member splits points among the group (CL14 form).

---

## 3. Dataset files

| File | Role |
|---|---|
| `Pathfinder.db` | **Final app database.** 17 tables, 19,890 rows, 18 indexes, 3.8 MB. Lives in `app/src/main/assets/`. Built 2026-11-18 using the Milestone 3 CREATE TABLE commands exactly as submitted, plus the three new tables and the indexes below. |
| `WPI_Pathfinder.db` | Full source snapshot (28 tables), collected 2026-10-05 from public wpi.edu pages (robots.txt respected). Keeps `data_sources` (source URL and fetch time per table) and data we are not using (events, academic calendar, clubs, scholarships, etc.). |

**Verification:** every row loads under the Milestone 3 constraints with no rejections; `PRAGMA foreign_key_check` is
clean; `PRAGMA integrity_check` is ok. `sqlite3 Pathfinder.db .schema` prints the exact DDL.

**How `Pathfinder.db` maps from the source**
- `Program.department_id` comes from `catalog_areas.department_id`, an evidence-backed link recorded in the source
  DB. 299 of 315 programs have one; 16 stay NULL.
- Columns not in the Milestone 3 design were left out:
  - Course: credit_type, offering_category, prerequisite_label, same_course_as_id
  - Faculty: affiliation, education
- **Course.course_code now holds exactly one code**, the primary code from the source's parsed `course_codes` table.
  In the source catalog, 152 courses stored several cross-listed codes in one field, in forms like "BME/ME 4504",
  "MA 540/4631", "MME 524-25" and "ECE 596A and ECE 596B". Their 165 extra codes moved to **CourseCode**, one row
  each. The one exception is "ISU" (Special Topics), which has no subject-and-number code and keeps "ISU" with no
  CourseCode rows.
- **ResidenceHall** keeps only the housing columns. Each hall's name, address, description and web page are identical to
  its Location row (all 28 checked), so they are not stored twice. `student_staff` and `furniture` were left out.
- **DiningLocation** has 9 rows. The source listed a generic "Café" at George C. Gordon Library. It was updated on
  2026-11-18 to **Bagels and Bowls**, which sells smoothies, sandwiches, bagels and acai bowls. Source:
  [WPI News, 2026-11-05](https://www.wpi.edu/news/gordon-library-renovations-fill-student-areas-possibility-and-lots-light),
  which says it replaces the earlier library café.
- **DiningLocation.last_verified** holds the date each spot was last confirmed against a published source. It is
  2026-10-05 for the 8 original spots (the fetch date in `data_sources`) and 2026-11-18 for Bagels and Bowls. No date
  is estimated.

---

## 4. Final schema: 17 relations in 3NF

PK = primary key, FK = foreign key. Row counts are what `Pathfinder.db` holds.

| Relation | Rows |
|---|---|
| Location(**location_id** PK, name, address, description, website_url) | 72 |
| Department(**department_id** PK, name, unit_type, description, phone, email, location_id FK→Location, website_url) | 41 |
| Program(**program_id** PK, name, degree_type, catalog_level, school_id FK→Department, department_id FK→Department, description, catalog_url, website_url) | 315 |
| Course(**course_id** PK, course_code, title, catalog_level, description, credits_or_units, prerequisite_text, catalog_url) | 1,761 |
| RequirementGroup(**group_id** PK, program_id FK→Program, parent_group_id FK→RequirementGroup, position, title, min_units, notes) | 3,237 |
| RequirementCourse(**group_id** PK/FK→RequirementGroup, **course_id** PK/FK→Course) | 11,646 |
| Faculty(**faculty_id** PK, username, name, title, email, phone, office, profile_url) | 1,038 |
| FacultyDepartment(**faculty_id** PK/FK→Faculty, **department_id** PK/FK→Department, is_primary) | 1,082 |
| Office(**office_id** PK, name, description, phone, email, location_id FK→Location, website_url) | 78 |
| Resource(**resource_id** PK, name, category, description, office_id FK→Office, website_url) | 156 |
| ProjectCenter(**project_center_id** PK, name, city, location_label, country, region, project_type, description, website_url) | 73 |
| ProjectCenterTerm(**project_center_id** PK/FK→ProjectCenter, **term** PK) | 81 |
| ProjectCenterDirector(**project_center_id** PK/FK→ProjectCenter, **faculty_id** PK/FK→Faculty) | 96 |
| ResearchCenter(**research_center_id** PK, name, category, description, website_url) | 12 |
| **ResidenceHall**(**location_id** PK/FK→Location, housing_type, residents, building_type, amenities, meal_plan, air_conditioning) | 28 |
| **DiningLocation**(**dining_id** PK, name, location_id FK→Location, description, website_url, last_verified) | 9 |
| **CourseCode**(**course_id** PK/FK→Course, **course_code** PK) | 165 |

**New tables: data dictionary entries for the report**

ResidenceHall: campus buildings used as student housing. It is a subtype of Location (CL04 supertype/subtype).

| Attribute | Description | Domain |
|---|---|---|
| location_id | The building this hall is; also the PK | Integer; must match a Location location_id |
| housing_type | Kind of housing | Text, up to 14 characters: Residence Hall (7), Apartments (5), Campus House (16) |
| residents | Who lives there | Text, up to 40 characters, e.g. "This is a first year student building." |
| building_type | Room layout (corridor, suite, apartment) | Text, up to 350 characters; may be blank (26 of 28 filled) |
| amenities | Laundry, kitchens, lounges, etc. | Text, up to 550 characters |
| meal_plan | Meal-plan requirement for residents | Text, up to 200 characters |
| air_conditioning | Air-conditioning details | Text, up to 250 characters; may be blank (25 of 28 filled) |

DiningLocation: places to eat on campus.

| Attribute | Description | Domain |
|---|---|---|
| dining_id | ID for each dining spot | Integer, assigned automatically |
| name | Dining spot name | Text, up to 30 characters; unique within a building |
| location_id | Building it is in | Integer; must match a Location location_id |
| description | What it offers | Text, up to 200 characters |
| website_url | wpi.edu page that lists it | Web address, up to 150 characters; may be blank for spots added in the app |
| last_verified | Date the spot was last confirmed against a published source | DATE as `YYYY-MM-DD`, enforced by CHECK; set with a DatePicker in the maintenance form |

CourseCode: the extra codes a course is cross-listed under. It is a multi-valued attribute of Course, split out for
1NF.

| Attribute | Description | Domain |
|---|---|---|
| course_id | The course | Integer; must match a Course course_id |
| course_code | Another code the same course is offered under | Text, up to 10 characters, `SUBJECT NUMBER` (e.g. DS 541), enforced by CHECK |

**Revised entry: Course.course_code** — the course's primary code, e.g. MIS 571. Text, up to 9 characters, one code
only (was "cross-listed codes joined by /" in Milestone 3). Any other codes are in CourseCode.

**Relationships and assumptions** (report §2d)
- School (Department with unit_type = 'School') 1:N Program via `school_id`: a program is offered by at most one school. 37 programs have none published.
- Department 1:N Program via `department_id`: a program has at most one owning department or program unit. 16 have none.
- Program 1:N RequirementGroup: a program's requirements are its own. Requirement groups nest through `parent_group_id` (1:N, self-referencing).
- RequirementGroup M:N Course through RequirementCourse: one course can satisfy many groups and programs.
- **Course 1:N CourseCode**: a course can be cross-listed under several departments' codes (CS 541 is also DS 541).
  The PK is (course_id, course_code), not the code alone, because the catalog repeats some codes across course rows.
  For example, 53 cross-listed codes are also the primary code of another row, usually the same course listed in both
  catalogs.
- Faculty M:N Department through FacultyDepartment: joint appointments are real; `is_primary` marks the home department.
- Location 1:N Department and Location 1:N Office: each unit is housed in one building.
- Office 1:N Resource: a resource is provided by at most one office.
- **Location 1:0..1 ResidenceHall** (supertype/subtype): a hall is one building; most buildings are not halls. The shared PK `location_id` enforces at most one hall per building.
- **Location 1:N DiningLocation**: a building can hold several dining spots (Founders Hall has 5); each spot is in one building.
- ProjectCenter 1:N ProjectCenterTerm: the terms a center runs, a multi-valued attribute.
- ProjectCenter M:N Faculty through ProjectCenterDirector: centers can have co-directors, and one faculty member can direct several centers (max 5).
- ResearchCenter stands alone: no published link to departments or faculty, so none is invented.
- Unique values: names of Location, Department, Program, Office, ProjectCenter and ResearchCenter, plus Faculty.username, are UNIQUE because each is the published identifier on wpi.edu. DiningLocation is unique on (name, location_id), because "Café" can appear in more than one building.

**ERD and Bachmann diagram:** use the Milestone 3 diagrams plus three new boxes: Location 1 —o| ResidenceHall
("is a"), Location 1 —< DiningLocation ("houses"), and Course 1 —< CourseCode ("is also listed as").

**Indexes in `Pathfinder.db`** (report: "indexed attributes beyond PK/FK and why")

| Index | Why |
|---|---|
| FK columns: Department.location_id, Program.school_id, Program.department_id, RequirementGroup.program_id, RequirementGroup.parent_group_id, RequirementCourse.course_id, FacultyDepartment.department_id, Office.location_id, Resource.office_id, ProjectCenterDirector.faculty_id, DiningLocation.location_id | SQLite does not index foreign keys automatically; the joins in Q1–Q20 use them |
| Course.course_code | Look up a course by code (Q2, Q5, Q15, Q20) |
| CourseCode.course_code | Find a course by any of its cross-listed codes (Q2, Q5, Q15, Q20) |
| Faculty.name | Search and sort by name (Q4, Q15) |
| Resource.category | Filter resources by category (Q6) |
| ProjectCenterTerm.term | Centers running in a selected term (Q9) |
| ProjectCenter(region, country) | Filter and count by region (Q10, Q13) |
| ResidenceHall.housing_type | Halls by housing type (Q16) |

**Fix in the final report:** the Milestone 3 DDL declares several URL columns as VARCHAR(1000), but the data dictionary
says 60–200 characters. The real maximum is 89 characters (88–89 for most of these columns), so the DDL and dictionary
should agree.

**Known data gaps.** These are honest NULLs: unpublished values are left blank, never guessed.

| Column | Filled |
|---|---|
| Resource.office_id | 43 of 156 (none of the Career Development resources) |
| Faculty.office | 618 of 1,038 (email: 1,037) |
| Course.prerequisite_text | 1,001 of 1,761 |
| Location.address | 28 of 72 |
| Office.location_id | 66 of 78 |
| Department.location_id | 33 of 41 |
| Program.department_id | 299 of 315 |
| Program.school_id | 278 of 315 |
| ResidenceHall.building_type | 26 of 28 |
| ResidenceHall.air_conditioning | 25 of 28 |

**Repeated course listings (kept, not merged).** 31 course codes appear on two Course rows. Most are 4000-level
courses printed in both the undergraduate and graduate catalogs. The rest (24 rows in the source, linked by
`same_course_as_id`) are the same course on two catalog pages, usually with different description text. Merging
would throw away published text, so queries show `catalog_level` to tell the rows apart.

The data is a snapshot from 2026-10-05, except the Bagels and Bowls update (2026-11-18).

---

## 5. Locked query list

All 20 queries were tested against `Pathfinder.db` on 2026-11-18; the counts are from the sample input shown.
`?` marks a value the user picks (Spinner, EditText or DatePicker). Tutorials 4–5 cover the query screen (Spinner + TableView),
Tutorial 8 the list screen (ListView + Toast) and Tutorial 7 the charts.

| # | Question | Tables | Shown as | Sample → rows |
|---|---|---|---|---|
| Q1 | Which courses satisfy each requirement of a selected program? | Program, RequirementGroup, RequirementCourse, Course | list | MSIT → 30 |
| Q2 | Which programs include a selected course in their requirements (found by any of its codes)? | Course, CourseCode, RequirementCourse, RequirementGroup, Program | table | MIS 571 → 8 |
| Q3 | How many programs does each school offer, by degree type? | Program, Department | table + chart | 33 |
| Q4 | Which faculty belong to a selected department? | Department, FacultyDepartment, Faculty | list | The Business School → 54 |
| Q5 | What are the prerequisites for a course, and what other codes is it listed under? | Course, CourseCode | detail | DS 541 → 2 (CS 541 Deep Learning, also listed as DS 541, published on two graduate catalog pages); 1,001 of 1,761 courses have prerequisite text |
| Q6 | Which office provides resources in a category, and which building is it in? | Resource, Office, Location | table | The First-Year Experience → 12 (10 with an office) |
| Q7 | Which building is each department or office in? | Department, Office, Location | table | 99 |
| Q8 | Which programs does a selected department offer? | Program, Department | list | Computer Science → 14 |
| Q9 | Which project centers run in a selected term? | ProjectCenter, ProjectCenterTerm | list | D-Term → 24 |
| Q10 | Which project centers are in a selected region, and are they IQP, MQP or HUA? | ProjectCenter | list | Europe → 26 |
| Q11 | Who directs a selected project center (email, office)? | ProjectCenter, ProjectCenterDirector, Faculty | table | London, England Project Center - IQP → 1 |
| Q12 | Which centers does a selected faculty member direct? | Faculty, ProjectCenterDirector, ProjectCenter | table | Nancy Burnham → 5 |
| Q13 | How many project centers are in each region? | ProjectCenter | chart | 6 regions |
| Q14 | Which research centers match a category or keyword? | ResearchCenter | list | 7 |
| Q15 | Keyword search across programs, courses (any code), faculty, centers, buildings and dining | 8 tables (UNION ALL) | list | "smoothie" → Bagels and Bowls |
| Q16 | Which halls are of a selected housing type, and what do they offer? | ResidenceHall, Location | list | Residence Hall → 7 |
| Q17 | Where can I eat on campus, and in which building? | DiningLocation, Location | list | 9 |
| Q18 | What is inside a selected building (departments, offices, housing, dining)? | Location, Department, Office, ResidenceHall, DiningLocation | list | Founders Hall → 7 (Police office, the hall, 5 dining spots) |
| Q19 | Which dining spots have not been verified since a selected date? | DiningLocation, Location | list (DatePicker) | 2026-11-01 → 8 |
| Q20 | Which courses can be taken under a selected subject, including courses cross-listed into it? | Course, CourseCode | list (subject Spinner) | DS → 34 (17 DS courses + 17 cross-listed into DS, e.g. CS 4343 as DS 4343) |

**Data maintenance** (rubric 2.2; Tutorial 6 pattern with parameterized SQL plus a DatePicker): add, edit and delete
DiningLocation. The building is picked from a Spinner of Locations, and `last_verified` comes from a DatePicker that
defaults to today. Dining spots open and close (Bagels and Bowls is a real example), so the form keeps the data current
without a live feed, and Q19 shows which spots need re-checking.

```sql
-- Q1
SELECT rg.position, rg.title AS requirement, rg.min_units, c.course_code, c.title AS course
FROM Program p JOIN RequirementGroup rg ON rg.program_id = p.program_id
JOIN RequirementCourse rc ON rc.group_id = rg.group_id JOIN Course c ON c.course_id = rc.course_id
WHERE p.name = ? ORDER BY rg.position, c.course_code;
-- Q2   (?1 = any code of the course)
SELECT DISTINCT p.name, p.degree_type, p.catalog_level
FROM Course c JOIN RequirementCourse rc ON rc.course_id = c.course_id
JOIN RequirementGroup rg ON rg.group_id = rc.group_id JOIN Program p ON p.program_id = rg.program_id
WHERE c.course_code = ?1 OR c.course_id IN (SELECT course_id FROM CourseCode WHERE course_code = ?1)
ORDER BY p.name;
-- Q3
SELECT s.name AS school, p.degree_type, COUNT(*) AS programs
FROM Program p JOIN Department s ON s.department_id = p.school_id
GROUP BY s.name, p.degree_type ORDER BY s.name, programs DESC;
-- Q4
SELECT f.name, f.title, f.email, f.office, fd.is_primary
FROM Department d JOIN FacultyDepartment fd ON fd.department_id = d.department_id
JOIN Faculty f ON f.faculty_id = fd.faculty_id WHERE d.name = ? ORDER BY f.name;
-- Q5   (?1 = any code of the course)
SELECT c.course_code, c.title, c.catalog_level, c.credits_or_units, c.prerequisite_text,
       (SELECT group_concat(cc.course_code, ', ') FROM CourseCode cc WHERE cc.course_id = c.course_id) AS also_listed_as
FROM Course c
WHERE c.course_code = ?1 OR c.course_id IN (SELECT course_id FROM CourseCode WHERE course_code = ?1);
-- Q6
SELECT r.name AS resource, o.name AS office, l.name AS building, o.phone, o.email
FROM Resource r LEFT JOIN Office o ON o.office_id = r.office_id
LEFT JOIN Location l ON l.location_id = o.location_id WHERE r.category = ? ORDER BY r.name;
-- Q7
SELECT 'Department' AS kind, d.name, l.name AS building, l.address
FROM Department d JOIN Location l ON l.location_id = d.location_id
UNION ALL
SELECT 'Office', o.name, l.name, l.address FROM Office o JOIN Location l ON l.location_id = o.location_id
ORDER BY 2;
-- Q8
SELECT p.name, p.degree_type, p.catalog_level FROM Program p JOIN Department d ON d.department_id = p.department_id
WHERE d.name = ? ORDER BY p.catalog_level, p.name;
-- Q9
SELECT pc.name, pc.location_label, pc.region, pc.project_type
FROM ProjectCenter pc JOIN ProjectCenterTerm t ON t.project_center_id = pc.project_center_id
WHERE t.term = ? ORDER BY pc.region, pc.name;
-- Q10
SELECT name, city, country, project_type FROM ProjectCenter WHERE region = ? ORDER BY country, name;
-- Q11
SELECT pc.name AS center, f.name AS director, f.email, f.office
FROM ProjectCenter pc JOIN ProjectCenterDirector pd ON pd.project_center_id = pc.project_center_id
JOIN Faculty f ON f.faculty_id = pd.faculty_id WHERE pc.name = ? ORDER BY f.name;
-- Q12
SELECT pc.name AS center, pc.location_label, pc.project_type
FROM Faculty f JOIN ProjectCenterDirector pd ON pd.faculty_id = f.faculty_id
JOIN ProjectCenter pc ON pc.project_center_id = pd.project_center_id WHERE f.name = ? ORDER BY pc.name;
-- Q13
SELECT region, COUNT(*) AS centers FROM ProjectCenter GROUP BY region ORDER BY centers DESC;
-- Q14   (?1 = category, ?2 = '%keyword%')
SELECT name, category, website_url FROM ResearchCenter
WHERE category = ?1 OR name LIKE ?2 OR description LIKE ?2 ORDER BY name;
-- Q15   (?1 = '%keyword%')
SELECT 'Program' AS type, name AS result, degree_type AS detail FROM Program WHERE name LIKE ?1
UNION ALL SELECT 'Course', course_code || ' ' || title, catalog_level FROM Course
          WHERE title LIKE ?1 OR course_code LIKE ?1 OR course_id IN (SELECT course_id FROM CourseCode WHERE course_code LIKE ?1)
UNION ALL SELECT 'Faculty', name, title FROM Faculty WHERE name LIKE ?1 OR title LIKE ?1
UNION ALL SELECT 'Project Center', name, location_label FROM ProjectCenter WHERE name LIKE ?1 OR description LIKE ?1
UNION ALL SELECT 'Research Center', name, category FROM ResearchCenter WHERE name LIKE ?1 OR description LIKE ?1
UNION ALL SELECT 'Building', name, address FROM Location WHERE name LIKE ?1
UNION ALL SELECT 'Dining', d.name, l.name FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id
          WHERE d.name LIKE ?1 OR d.description LIKE ?1;
-- Q16
SELECT l.name AS hall, rh.residents, rh.building_type, rh.meal_plan, rh.air_conditioning, l.address
FROM ResidenceHall rh JOIN Location l ON l.location_id = rh.location_id
WHERE rh.housing_type = ? ORDER BY l.name;
-- Q17
SELECT d.dining_id, d.name AS dining, l.name AS building, l.address, d.description, d.last_verified
FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id ORDER BY l.name, d.name;
-- Q18   (?1 = building name)
SELECT 'Department' AS kind, d.name FROM Department d JOIN Location l ON l.location_id = d.location_id WHERE l.name = ?1
UNION ALL SELECT 'Office', o.name FROM Office o JOIN Location l ON l.location_id = o.location_id WHERE l.name = ?1
UNION ALL SELECT 'Housing', rh.housing_type FROM ResidenceHall rh JOIN Location l ON l.location_id = rh.location_id WHERE l.name = ?1
UNION ALL SELECT 'Dining', dl.name FROM DiningLocation dl JOIN Location l ON l.location_id = dl.location_id WHERE l.name = ?1;
-- Q19   (date from DatePicker as 'yyyy-MM-dd')
SELECT d.name AS dining, l.name AS building, d.last_verified
FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id
WHERE d.last_verified < ? ORDER BY d.last_verified, d.name;
-- Q20   (?1 = subject code, e.g. 'DS'; subject Spinner:
--        SELECT DISTINCT substr(course_code, 1, instr(course_code, ' ') - 1) AS subject FROM Course WHERE instr(course_code, ' ') > 0
--        UNION SELECT substr(course_code, 1, instr(course_code, ' ') - 1) FROM CourseCode ORDER BY 1)
SELECT c.course_code AS code, c.title, c.catalog_level, 'Primary' AS listing FROM Course c
WHERE c.course_code LIKE ?1 || ' %'
UNION ALL
SELECT cc.course_code, c.title, c.catalog_level, 'Cross-listed with ' || c.course_code
FROM CourseCode cc JOIN Course c ON c.course_id = cc.course_id WHERE cc.course_code LIKE ?1 || ' %'
ORDER BY 1;
-- Maintenance   (building Spinner: SELECT location_id, name FROM Location ORDER BY name; last_verified from DatePicker)
INSERT INTO DiningLocation (name, location_id, description, website_url, last_verified) VALUES (?,?,?,?,?);
UPDATE DiningLocation SET name=?, location_id=?, description=?, website_url=?, last_verified=? WHERE dining_id=?;
DELETE FROM DiningLocation WHERE dining_id=?;
```

---

## 6. App plan

Follow the professor's tutorial architecture (Tutorials 3–8) so the code matches what the course taught.

**Built so far (Person 1, 2026-11-18).** Tested on the Pixel_7 emulator (Android 37): a fresh install copies the
database, and all five reports return the counts above.
- **Project:** Java, package `com.example.pathfinder`, minSdk 26 (Android 8.0), targetSdk 37, no extra libraries.
  Open this folder in Android Studio.
- `constant/DBConstant`: the database file name and `DATABASE_VERSION`.
- `constant/SQLCommand`: every query above as `Q1`–`Q20`, plus the Spinner lists (`SUBJECTS`, `LOCATIONS`) and the
  dining insert/update/delete.
- `util/DBOperator`: one shared connection. `DBOperator.getInstance(this).execQuery(SQLCommand.Q8, "Computer Science")`
  returns a Cursor, and `execSQL(sql, args...)` runs inserts, updates and deletes. It copies `Pathfinder.db` from assets
  on first launch, and again whenever `DATABASE_VERSION` goes up.
- `view/TableView`: shows any Cursor as a table with a red header row; long text wraps and wide tables scroll sideways.
- `MainActivity`: the home menu with all ten buttons. Reports works; the others show "Coming soon".
- `ReportsActivity`: Spinner + Show Result + table for Q3, Q7, Q13, Q17 and Q2 (with a course-code box).
- Text lives in `res/values/strings.xml` and colors in `colors.xml`; the theme makes every Button WPI red.
- Left out until a screen needs them: `DBOpenHelper` (the copy doesn't need it), and `Pair` and `ChartGenerator` (charts).

**How to add a screen**
1. Create `XxxActivity.java` (extends `Activity`) and `res/layout/activity_xxx.xml`. Give the layout's root
   `android:fitsSystemWindows="true"`, or Android 15+ draws it under the title bar. That attribute replaces the root's
   padding, so use `android:layout_margin` on the root instead.
2. Register it in `AndroidManifest.xml` with an `android:label` (the title bar text).
3. In `MainActivity.open()`, add `else if (id == R.id.xxx_btn) startActivity(new Intent(this, XxxActivity.class));`.
4. Take SQL from `SQLCommand`. A new query goes there and into the list above.
5. If you change `Pathfinder.db`, raise `DBConstant.DATABASE_VERSION` so phones copy the new file.

**Screens** (from the Milestone 3 navigation, minus the dropped extras):

| Screen | Uses |
|---|---|
| Home menu | buttons to every screen below |
| Programs → Requirements → Course detail | Q1, Q5, Q8 (ListView, item click); course detail shows cross-listed codes |
| Courses by subject | Q20 (Spinner of subjects) → Course detail (Q5) |
| Faculty by department | Q4 |
| Offices & Resources | Q6, Q7 |
| Campus Buildings → "What's inside" | Q18 |
| Housing & Dining | Q16, Q17, Q19 (DatePicker); **Add / Edit / Delete Dining Location** form with a DatePicker for `last_verified` |
| Project Centers by term or region → Directors | Q9–Q12 |
| Research Centers | Q14 |
| Search | Q15 (keyword EditText) |
| Reports | Spinner of queries + TableView (Q2, Q3, Q7, Q13, Q17); charts for Q3 and Q13 |

**Rubric 2.3 extras:** DatePicker input (maintenance form and Q19), bar charts, ListView drill-down, keyword search
across 8 tables, building "what's inside" view.

**Chart library:** MPAndroidChart 3.1.0 is the default. It is in the professor's Tutorial 7 materials and can be added
through Gradle (JitPack), so it doesn't depend on the course zip. The fallback is AChartEngine 1.2.0, exactly as in the
Tutorial 7/8 `ChartGenerator` code, but that jar exists only in the course zip and must be kept aside to use it.

**Work split.** Everyone builds their screens plain first (TableView or a simple ListView); the UI pass comes last.

| Person | Screens | Queries | Home buttons |
|---|---|---|---|
| 1 | Foundation and Reports (done); splash, home restyle and the shared list row in the UI pass | Q2, Q3, Q7, Q13, Q17 | `reports_btn` |
| 2 | Programs → Requirements, Courses by Subject, Course detail, Faculty | Q1, Q4, Q5, Q8, Q20 | `programs_btn`, `courses_btn`, `faculty_btn` |
| 3 | Offices & Resources, Campus Buildings, Housing & Dining, dining add/edit/delete form | Q6, Q7, Q16–Q19, dining SQL | `offices_btn`, `buildings_btn`, `housing_dining_btn` |
| 4 | Project Centers → Directors, Research Centers, Search, bar charts for Q3 and Q13 | Q9–Q15, Q3, Q13 | `project_centers_btn`, `research_centers_btn`, `search_btn` |

A list query that a Spinner needs (departments, programs, regions, terms...) goes in `SQLCommand` right under the
query that uses it, so two people's additions never land on the same lines.

**UI design (locked 2026-11-19, done last).** The Milestone mockups, limited to what the data supports and to plain
Android (the chart library is the only one we add):
- Splash: WPI red with the logo, set in the theme (needs the logo image file).
- Home: red header with the app name, a search bar that opens Search, a 3-column grid of icon tiles (icons from
  File → New → Vector Asset), and Housing & Dining as a wide row.
- Lists: one shared row layout (bold title, grey subtitle, arrow) made by Person 1 and used by every list screen.
- Filters (Undergraduate / Graduate, result type, department): a row of red toggle buttons or a Spinner.
- Search results: a type label and icon on each row, from Q15's `type` column.
- Course detail: code, title, credits, level, description, prerequisites and other codes.
- Campus buildings: Directions opens the phone's maps app with the address; More Info opens `website_url`.
- Left out: photos (none in the data), an in-app map (no coordinates; needs an API key and internet), topic tags (no
  data), the chatbot, clubs, scholarships, the bottom navigation bar and Material chips.
- Report screenshots are taken after the UI pass.

**Schedule:** every screen works and is merged by Nov 28; UI pass Nov 29–Dec 1; testing, screenshots, report and slides
Dec 1–6; submit Monday Dec 7.

**Still to decide:**
- Whether the maintenance screens sit behind a simple admin PIN (an open question from the project plan).

---

## 7. Course topics the report should show (from lectures CL01–CL12)

- **Design:** ERD with crow's-foot cardinalities; 1NF→2NF→3NF (done in Milestones 2–3); Bachmann diagram;
  conceptual → logical → physical design (CL06/CL09 travel case); **supertype/subtype (CL04)** for Location →
  ResidenceHall. Present CourseCode as a Milestone 4 revision: Milestone 3's Course.course_code held "/"-joined
  cross-listed codes (a non-atomic value, a 1NF violation), so the multi-valued attribute was split into its own
  relation.
- **DDL output:** run the script the way the professor's example does, so the report has the required
  "output of running create table commands":
  `.echo ON`, `.mode list`, `.separator " | "`, `.output Create_Pathfinder_out.txt`, `.open Pathfinder.db`, `PRAGMA foreign_keys = ON;`, `DROP TABLE IF EXISTS …` in reverse FK order, then the CREATE TABLEs.
- **Views (CL09–CL11):** views support UI design and limit the columns users see. For example, a view joining
  Program/RequirementGroup/Course behind Q1, or Location with its halls and dining behind Q18.
- **Triggers (CL10):** enforce business rules. CHECK constraints cover most rules here (housing_type values,
  is_primary 0/1, project_type, the `last_verified` date format). A trigger is optional; for example, one could reject
  a `last_verified` date in the future.
- **Transactions and concurrency (Ch.10):** single-user local SQLite. Wrap DiningLocation add, edit and delete in a
  transaction (all-or-nothing); locking and deadlocks matter only if the database were shared.
- **Backup and recovery:**
  - The pristine database ships inside the APK, so reinstalling restores the baseline.
  - Edits live in the app's databases folder.
  - Recommend exporting or copying `Pathfinder.db`, and re-running the wpi.edu data refresh each term.
- **Security:**
  - All data is already public. There is no student data and no passwords.
  - Faculty emails and offices are published directory data.
  - Maintenance screens are the only write path (optional PIN).
  - Views can limit which columns users see.
- **Data quality** (intrinsic, representational, accessibility, contextual):
  - Timeliness is why events and the academic calendar were excluded.
  - Dining spots still change (Bagels and Bowls replaced the old library café). The maintenance form keeps the data
    current, `last_verified` records when each spot was confirmed, and Q19 lists the stale ones.
  - NULLs instead of guesses (see §4 gaps).
  - The source catalog writes cross-listed codes inconsistently ("BME/ME 4504", "MA 540/4631", "MME 524-25",
    "ENV1100"). They are now stored as one clean `SUBJECT NUMBER` code per row, enforced by CHECK.
  - Resource→Office links are missing for most resources.
- **Space estimates (CL11 handout):**
  - For each table, row length = sum of field sizes (text = declared size, integer = 2, date = 4).
  - Estimate rows for a realistic deployment, and again 5 years out. Start from tables with no FKs, then multiply by averages: groups per program, courses per group.
  - Report row length, row count and size per table, plus the database total for both cases.
- **Interface design (CL11 usability reading, Nielsen's heuristics):** consistent navigation, minimal screens, plain-language errors (e.g. "Please choose a query!" Toasts).
