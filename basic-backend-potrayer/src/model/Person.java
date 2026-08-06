package model;

/**
 * This class represents a person in general.
 */
public class Person {

	private String name;
	/**
	 * A unique identifier for the person.
	 */
	private String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}