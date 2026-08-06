package model;

import java.util.ArrayList;
import java.util.List;

/**
 * A one-hour timeslot during the workshop when a class may take place.
 */
public class Timeslot {

	private int startHour;
	/**
	 * These are the participants who are booked in a class with the trainer at this time.
	 */

    // Change to Array list for multiple bookings
	private List<Participant> bookedParticipants = new ArrayList<>();
	/**
	 * The type of class being offered in this timeslot.
	 */
	private String classType;
	private int maxCapacity;

    public Timeslot(int startHour, String classType, int maxCapacity) {
        this.startHour = startHour;
        this.classType = classType;
        this.maxCapacity = maxCapacity;
    }

    // Helper methods - left them public for checks from outside class

    /**
     * Boolean check for reaching maxCapacity
     * @return max capacity reached?
     */
    public boolean isFull() {
        return bookedParticipants.size() == maxCapacity;
    }

    /**
     * Boolean Check for if the timeslot already contains the participant
     * @param participant The participant booking timeslot
     * @return if participant already in timeslot
     */
    public boolean hasParticipants(Participant participant) {
        return bookedParticipants.contains(participant);
    }

    /**
     * This is the method to add paticipant to the bookedParticipant list
     * @param participant participant booking this timeslot
     */
    public void addBookedParticipant(Participant participant) {
        if (!isFull()) {
            if(!bookedParticipants.contains(participant)) {
                bookedParticipants.add(participant);
            }
        }
    }

    // Getters

	public List<Participant> getBookedParticipants() {
	    return bookedParticipants;
	}

    public int getStartHour() {
        return startHour;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getRemainingCapacity() {
        return maxCapacity - bookedParticipants.size();
    }

    public String getClassType() {
        return classType;
    }

    // To display info for the booking
    @Override
    public String toString() {
        return startHour + ":00 - " + classType + " (" + getRemainingCapacity() + " Spots left)";
    }

}