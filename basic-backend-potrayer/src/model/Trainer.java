package model;

/**
 * This class represents the trainer running classes at the workshop.
 */
public class Trainer extends Person {

	/**
	 * The single src.model.ClassRoom in the system.
	 */
	private ClassRoom myClassRoom;

    public Trainer(String name, String email) {
        super(name, email);
    }

    // Setter for the classroom variable
    public void setClassRoom(ClassRoom classRoom) {
        this.myClassRoom = classRoom;
    }
}