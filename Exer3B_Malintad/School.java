public class School {
    String name;
    String location;
    int numberOfStudents;

    public School(String name, String location) {
        this.name = name;
        this.location = location;
        this.numberOfStudents = 0;
    }

    public void addStudent(Student student) {
        numberOfStudents++;
        System.out.println(student.getName() + " has been added to " + name);
    }

    public void displayInfo() {
        System.out.println("School: " + name + ", Location: " + location + ", Students: " + numberOfStudents);
    }
}