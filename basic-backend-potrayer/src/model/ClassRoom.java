package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * This class represents the trainer's online classroom. The trainer has exactly one classroom. The classroom holds the schedule of the teacher's classes for the workshop.
 * <p>
 * It may be appropriate to implement this class as an Observable. It will be observed from within the src.viewcontroller.ParticipantScreen and src.viewcontroller.TrainerScreen.
 */
public class ClassRoom extends Observable {

    /**
     * It will probably be useful for a classroom to record which trainer it belongs to. You will need to ensure that the mutual references between a src.model.Trainer and their src.model.ClassRoom are set up correctly.
     */
    private Trainer owner;

    
    /**
     * The list of time slots during the workshop, recording classes booked by participants with this trainer.
     */
    // Conversion to array list to eliminate fixed length
    private List<Timeslot> schedule = new ArrayList<>();
    /**
     * Represents whether participants booked in the current class have joined the classroom. Participants' names may be displayed in some way on the trainer's screen to indicate that the participants are in the classroom.
     */
    private List<Participant> participantsInSession = new ArrayList<>();
    /**
     * Participants who have classes in this classroom either at present or in the future and are waiting for the class to begin. A participant can only be in one lobby or classroom at a time.
     */
    private List<Participant> participantsWaitingInLobby = new ArrayList<>();
    /**
     * This is the index of the current class time slot in the schedule. It is used to determine whether a participant who tries to join the classroom is admitted now, made to wait in the lobby (because their class is in the future), or refused entry. It is updated whenever the src.viewcontroller.TimerScreen sends nextClass messages to the classroom.
     */
    // Default to -1 for no timeslot
    // private int currentClassTimeSlot = -1;
    // Changing loogic from jumpong time from one timeslot to next to an hour progress ssytem
    private int currentHour = 9;

    // Setting up mutual reference for trainer and its classroom class
    public ClassRoom(Trainer owner) {
        this.owner = owner;
        owner.setClassRoom(this);
    }

    // Helpers for joining

    /**
     * Returns the timeslot object if participant has booking for this timeslot
     *
     * @param participant
     * @param hour
     * @return
     */
    private Timeslot getBookingForParticipant(Participant participant, int hour) {
        for (Timeslot timeslot : schedule) {
            if (timeslot.getStartHour() == hour && timeslot.hasParticipants(participant)) {
                return timeslot;
            }
        }
        return null;
    }


    /**
     * Returns true if participant has future bookings for other timeslot of this classroom
     *
     * @param participant
     * @return
     */
    private boolean hasFutureBooking(Participant participant) {
        int currentHour = getCurrentTimeslotHour();
        for (Timeslot timeslot : schedule) {
            if (timeslot.getStartHour() > currentHour && timeslot.hasParticipants(participant)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A participant attempts to join the classroom. If the participant has a class booked there at the current time, then the participant is allowed in to participate in the class. If the participant's class is in the future, the participant is placed in the lobby. If the participant does not have a class in this classroom or their class was in the past, they are refused entry to the classroom.
     */
    public String join(Participant participant) {
        String result = "";
        int currentHour = getCurrentTimeslotHour();
//        System.out.println(currentHour);
        if (getBookingForParticipant(participant, currentHour) != null) {
            participantsInSession.add(participant);
            result = "ACTIVE";
        } else if (hasFutureBooking(participant)) {
            participantsWaitingInLobby.add(participant);
            result = "LOBBY";
        } else {
            result = "REFUSED";
        }

        setChanged();
        notifyObservers();

        return result;
    }


    /**
     * A participant who is currently in the classroom leaves the class.
     */
    public void leave(Participant participant) {
        // For now this will cause them to both leave waiting lobby and active class
        participantsInSession.remove(participant);
        participantsWaitingInLobby.remove(participant);

        setChanged();
        notifyObservers();
    }


    /**
     * This operation is triggered when it is time to end a class and start the next one. Any participant currently in the classroom is removed. If there are participants in the lobby who has a class booked in this classroom at the current time, they are moved from the lobby into the class with the trainer.
     */
    public void nextClass() {

        //First clear out everyone in class
        participantsInSession.clear();


        if (currentHour >= 18) {
            setChanged();
            notifyObservers();
            return;
        }
        //Increment the actual hour now
        currentHour++;

        //check if a timeslot actually starts at this hour
        Timeslot current = getCurrentTimeslot();
        if (current != null) {
            List<Participant> toMove = new ArrayList<>();
            for (Participant participant : participantsWaitingInLobby) {
                if (current.hasParticipants(participant)) {
                    toMove.add(participant);
                }
            }
            participantsWaitingInLobby.removeAll(toMove);
            participantsInSession.addAll(toMove);
        }

        setChanged();
        notifyObservers();
    }

    /**
     * This operation books a participant into a class time slot that is available. There may be many other operations to support viewing and changing class bookings.
     *
     */
    public void makeBooking(Participant participant, Timeslot timeslot) {
        timeslot.addBookedParticipant(participant);

        setChanged();
        notifyObservers();
    }


    /**
     * Add a timeslot when the trainer is offering a class. (Interaction from trainer)
     *
     * @param classType   The type of class being offered in this timeslot.
     * @param maxCapacity The maximum number of participants allowed in this timeslot.
     */
    public void addTimeslot(String classType, int maxCapacity, int startHour) {
        // Dismiss overlaping timeslots
        for (Timeslot timeslot : schedule) {
            if (timeslot.getStartHour() == startHour) {
                return;
            }
        }
        schedule.add(new Timeslot(startHour, classType, maxCapacity));
        schedule.sort((a, b) -> a.getStartHour() - b.getStartHour());

        // This is the notification call for all subscribed classses to run update
        // so for every attribute change in this model class so we dont need to call
        // manual refreshes
        setChanged();
        notifyObservers();
    }

    // Getter methods

    public List<Timeslot> getSchedule() {
        return schedule;
    }

    public Trainer getOwner() {
        return owner;
    }

    public List<Participant> getParticipantsInSession() {
        return participantsInSession;
    }

    public Timeslot getCurrentTimeslot() {
        for (Timeslot timeslot : schedule) {
            if (timeslot.getStartHour() == currentHour) {
                return timeslot;
            }
        }
        return null;
    }

    /**
     * Helper Return max int if current schedule for this classroom empty else using currentTimeSlot index
     * gives out the current start hour
     *
     * @return Current hour of timeslot of class
     */
    public int getCurrentTimeslotHour() {
//        // max current hour for now so none is less than it
//        // 9 because this is when classes begin
//        if (currentClassTimeSlot == -1 || schedule.isEmpty()) return 9;
//        // If there are no more classes then that means workshop ended
//        if (currentClassTimeSlot >= schedule.size()) return 18;
//        return schedule.get(currentClassTimeSlot).getStartHour();
        return currentHour;
    }

    public List<Participant> getParticipantsWaitingInLobby() {
        return participantsWaitingInLobby;
    }
}
