public class Classroom {
    String roomNumber;
    int capacity;

    public Classroom(String roomNumber, int capacity) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    public void assignTeacher(Teacher teacher) {
        System.out.println(teacher.name + " has been assigned to room " + roomNumber);
    }
}