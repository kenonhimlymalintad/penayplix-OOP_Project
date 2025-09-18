public class ProjectTester {
    public static void main(String[] args) {

        School school = new School("Tech High School", "New York");
        Student student1 = new Student("John Doe", 16, "123 Main St", "S12345", "10th Grade");
        Teacher teacher1 = new Teacher("Mr. Smith", 35, "456 Elm St", "T001", "Math");
        Course course1 = new Course("MATH101", "Math 101", 3);
        Classroom classroom1 = new Classroom("101", 30);

        school.addStudent(student1);

        school.displayInfo();
        student1.displayInfo();
        teacher1.displayInfo();

        student1.registerForClass(course1);

        teacher1.teachClass(classroom1);

        course1.enrollStudent(student1);
        course1.assignGrade(student1, "A");
        classroom1.assignTeacher(teacher1);
    }
}