public class Student extends Person {
    String studentID;
    String grade;

    public Student(String name, int age, String address, String studentID, String grade) {
        super(name, age, address);
        this.studentID = studentID;
        this.grade = grade;
    }

    public void registerForClass(Course course) {
        System.out.println(getName() + " has registered for " + course.courseName);
    }

    public void displayInfo() {
        super.displayInfo();
        System.out.println("Student ID: " + studentID + ", Grade: " + grade);
    }
}