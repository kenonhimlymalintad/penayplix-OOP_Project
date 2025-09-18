public class Teacher extends Person {
    String employeeID;
    String subject;

    public Teacher(String name, int age, String address, String employeeID, String subject) {
        super(name, age, address);
        this.employeeID = employeeID;
        this.subject = subject;
    }

    public void teachClass(Classroom classroom) {
        System.out.println(getName() + " is teaching in room " + classroom.roomNumber);
    }

    public void displayInfo() {
        super.displayInfo();
        System.out.println("Employee ID: " + employeeID + ", Subject: " + subject);
    }
}