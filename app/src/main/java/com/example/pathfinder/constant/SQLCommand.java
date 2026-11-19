package com.example.pathfinder.constant;

// All SQL the app runs. Numbers match the query list in PATHFINDER.md; ? is a value the user picks.
public abstract class SQLCommand {

    // Q1 courses for each requirement of a program (? = program name)
    public static final String Q1 = """
            SELECT rg.position, rg.title AS requirement, rg.min_units, c.course_code, c.title AS course
            FROM Program p JOIN RequirementGroup rg ON rg.program_id = p.program_id
            JOIN RequirementCourse rc ON rc.group_id = rg.group_id JOIN Course c ON c.course_id = rc.course_id
            WHERE p.name = ? ORDER BY rg.position, c.course_code""";

    // Q2 programs that include a course (?1 = any code of the course)
    public static final String Q2 = """
            SELECT DISTINCT p.name, p.degree_type, p.catalog_level
            FROM Course c JOIN RequirementCourse rc ON rc.course_id = c.course_id
            JOIN RequirementGroup rg ON rg.group_id = rc.group_id JOIN Program p ON p.program_id = rg.program_id
            WHERE c.course_code = ?1 OR c.course_id IN (SELECT course_id FROM CourseCode WHERE course_code = ?1)
            ORDER BY p.name""";

    // Q3 programs per school by degree type
    public static final String Q3 = """
            SELECT s.name AS school, p.degree_type, COUNT(*) AS programs
            FROM Program p JOIN Department s ON s.department_id = p.school_id
            GROUP BY s.name, p.degree_type ORDER BY s.name, programs DESC""";

    // Q4 faculty in a department (? = department name)
    public static final String Q4 = """
            SELECT f.name, f.title, f.email, f.office, fd.is_primary
            FROM Department d JOIN FacultyDepartment fd ON fd.department_id = d.department_id
            JOIN Faculty f ON f.faculty_id = fd.faculty_id WHERE d.name = ? ORDER BY f.name""";

    // Q5 course detail, prerequisites and other codes (?1 = any code of the course)
    public static final String Q5 = """
            SELECT c.course_code, c.title, c.catalog_level, c.credits_or_units, c.prerequisite_text,
                   (SELECT group_concat(cc.course_code, ', ') FROM CourseCode cc WHERE cc.course_id = c.course_id) AS also_listed_as
            FROM Course c
            WHERE c.course_code = ?1 OR c.course_id IN (SELECT course_id FROM CourseCode WHERE course_code = ?1)""";

    // Q6 office and building for a resource category (? = category)
    public static final String Q6 = """
            SELECT r.name AS resource, o.name AS office, l.name AS building, o.phone, o.email
            FROM Resource r LEFT JOIN Office o ON o.office_id = r.office_id
            LEFT JOIN Location l ON l.location_id = o.location_id WHERE r.category = ? ORDER BY r.name""";

    // Q7 building of each department and office
    public static final String Q7 = """
            SELECT 'Department' AS kind, d.name, l.name AS building, l.address
            FROM Department d JOIN Location l ON l.location_id = d.location_id
            UNION ALL
            SELECT 'Office', o.name, l.name, l.address FROM Office o JOIN Location l ON l.location_id = o.location_id
            ORDER BY 2""";

    // Q8 programs of a department (? = department name)
    public static final String Q8 = """
            SELECT p.name, p.degree_type, p.catalog_level FROM Program p JOIN Department d ON d.department_id = p.department_id
            WHERE d.name = ? ORDER BY p.catalog_level, p.name""";

    // Q9 project centers in a term (? = term)
    public static final String Q9 = """
            SELECT pc.name, pc.location_label, pc.region, pc.project_type
            FROM ProjectCenter pc JOIN ProjectCenterTerm t ON t.project_center_id = pc.project_center_id
            WHERE t.term = ? ORDER BY pc.region, pc.name""";

    // Q10 project centers in a region (? = region)
    public static final String Q10 = """
            SELECT name, city, country, project_type FROM ProjectCenter WHERE region = ? ORDER BY country, name""";

    // Q11 directors of a project center (? = center name)
    public static final String Q11 = """
            SELECT pc.name AS center, f.name AS director, f.email, f.office
            FROM ProjectCenter pc JOIN ProjectCenterDirector pd ON pd.project_center_id = pc.project_center_id
            JOIN Faculty f ON f.faculty_id = pd.faculty_id WHERE pc.name = ? ORDER BY f.name""";

    // Q12 centers a faculty member directs (? = faculty name)
    public static final String Q12 = """
            SELECT pc.name AS center, pc.location_label, pc.project_type
            FROM Faculty f JOIN ProjectCenterDirector pd ON pd.faculty_id = f.faculty_id
            JOIN ProjectCenter pc ON pc.project_center_id = pd.project_center_id WHERE f.name = ? ORDER BY pc.name""";

    // Q13 project centers per region (chart)
    public static final String Q13 = """
            SELECT region, COUNT(*) AS centers FROM ProjectCenter GROUP BY region ORDER BY centers DESC""";

    // Q14 research centers by category or keyword (?1 = category, ?2 = '%keyword%')
    public static final String Q14 = """
            SELECT name, category, website_url FROM ResearchCenter
            WHERE category = ?1 OR name LIKE ?2 OR description LIKE ?2 ORDER BY name""";

    // Q15 keyword search across 8 tables (?1 = '%keyword%')
    public static final String Q15 = """
            SELECT 'Program' AS type, name AS result, degree_type AS detail FROM Program WHERE name LIKE ?1
            UNION ALL SELECT 'Course', course_code || ' ' || title, catalog_level FROM Course
                      WHERE title LIKE ?1 OR course_code LIKE ?1 OR course_id IN (SELECT course_id FROM CourseCode WHERE course_code LIKE ?1)
            UNION ALL SELECT 'Faculty', name, title FROM Faculty WHERE name LIKE ?1 OR title LIKE ?1
            UNION ALL SELECT 'Project Center', name, location_label FROM ProjectCenter WHERE name LIKE ?1 OR description LIKE ?1
            UNION ALL SELECT 'Research Center', name, category FROM ResearchCenter WHERE name LIKE ?1 OR description LIKE ?1
            UNION ALL SELECT 'Building', name, address FROM Location WHERE name LIKE ?1
            UNION ALL SELECT 'Dining', d.name, l.name FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id
                      WHERE d.name LIKE ?1 OR d.description LIKE ?1""";

    // Q16 halls of a housing type (? = housing type)
    public static final String Q16 = """
            SELECT l.name AS hall, rh.residents, rh.building_type, rh.meal_plan, rh.air_conditioning, l.address
            FROM ResidenceHall rh JOIN Location l ON l.location_id = rh.location_id
            WHERE rh.housing_type = ? ORDER BY l.name""";

    // Q17 dining spots and their buildings
    public static final String Q17 = """
            SELECT d.dining_id, d.name AS dining, l.name AS building, l.address, d.description, d.last_verified
            FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id ORDER BY l.name, d.name""";

    // Q18 what is inside a building (?1 = building name)
    public static final String Q18 = """
            SELECT 'Department' AS kind, d.name FROM Department d JOIN Location l ON l.location_id = d.location_id WHERE l.name = ?1
            UNION ALL SELECT 'Office', o.name FROM Office o JOIN Location l ON l.location_id = o.location_id WHERE l.name = ?1
            UNION ALL SELECT 'Housing', rh.housing_type FROM ResidenceHall rh JOIN Location l ON l.location_id = rh.location_id WHERE l.name = ?1
            UNION ALL SELECT 'Dining', dl.name FROM DiningLocation dl JOIN Location l ON l.location_id = dl.location_id WHERE l.name = ?1""";

    // Q19 dining spots not verified since a date (? = 'yyyy-MM-dd' from the DatePicker)
    public static final String Q19 = """
            SELECT d.name AS dining, l.name AS building, d.last_verified
            FROM DiningLocation d JOIN Location l ON l.location_id = d.location_id
            WHERE d.last_verified < ? ORDER BY d.last_verified, d.name""";

    // Q20 courses under a subject, including cross-listed ones (?1 = subject code, e.g. 'DS')
    public static final String Q20 = """
            SELECT c.course_code AS code, c.title, c.catalog_level, 'Primary' AS listing FROM Course c
            WHERE c.course_code LIKE ?1 || ' %'
            UNION ALL
            SELECT cc.course_code, c.title, c.catalog_level, 'Cross-listed with ' || c.course_code
            FROM CourseCode cc JOIN Course c ON c.course_id = cc.course_id WHERE cc.course_code LIKE ?1 || ' %'
            ORDER BY 1""";

    // Spinner lists
    public static final String SUBJECTS = """
            SELECT DISTINCT substr(course_code, 1, instr(course_code, ' ') - 1) AS subject FROM Course WHERE instr(course_code, ' ') > 0
            UNION SELECT substr(course_code, 1, instr(course_code, ' ') - 1) FROM CourseCode ORDER BY 1""";
    public static final String LOCATIONS = "SELECT location_id, name FROM Location ORDER BY name";

    // Dining maintenance form (last_verified comes from the DatePicker)
    public static final String INSERT_DINING =
            "INSERT INTO DiningLocation (name, location_id, description, website_url, last_verified) VALUES (?,?,?,?,?)";
    public static final String UPDATE_DINING =
            "UPDATE DiningLocation SET name=?, location_id=?, description=?, website_url=?, last_verified=? WHERE dining_id=?";
    public static final String DELETE_DINING = "DELETE FROM DiningLocation WHERE dining_id=?";
}
