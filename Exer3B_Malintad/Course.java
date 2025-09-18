public class Course {
    String courseID;
    String courseName;
    int credits;

    public Course(String courseID, String courseName, int credits) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.credits = credits;
    }

    public void enrollStudent(Student student) {
        System.out.println(student.getName() + " has enrolled in " + courseName);
    }

    public void assignGrade(Student student, String grade) {
        student.grade = grade;
        System.out.println(student.getName() + " has received a grade: " + grade);
    }
}