package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClassRoomTest {

    private ClassRoom classRoom;
    private Trainer trainer;
    private Participant john;
    private Participant doe;

    @BeforeEach
    public void setUp() {
        // The initial setup we want to run before each test to give them
        // same clean state

        // In my implementation for now this setup is compulsory even in
        // if we are using the screens as the logic for registering hasnt
        // been implemented
        trainer = new Trainer("Patrick","patrick@gmail.com");
        classRoom = new ClassRoom(trainer);
        john = new Participant("john", "john@gmail.com");
        doe = new Participant("doe", "doe@gmail.com");

        // Schedule is 9:00 - Yoga
        // Schedule is 10:00 - Pilates
        classRoom.addTimeslot("Yoga",4,9);
        classRoom.addTimeslot("Pilates",3,10);
    }

    // The join test from the participants end
    @Test
    public void testJoinBookedAtCurrentReturnsActive() {
        // 1: The core correct path applicant booked a timeslot
        // The tried to join the class at that time

        Timeslot nine = classRoom.getSchedule().getFirst();
        classRoom.makeBooking(john,nine);

        assertEquals("ACTIVE",classRoom.join(john));
        assertTrue(classRoom.getParticipantsInSession().contains(john));
    }

    @Test
    public void testJoinBookedInFutureReturnsLobby() {
        // 2: The second true path where if booking in future the
        // participant is sent to lobby

        // It reorders itself by time asscending
        Timeslot ten  = classRoom.getSchedule().get(1);
        classRoom.makeBooking(john,ten);

        assertEquals("LOBBY",classRoom.join(john));
        assertTrue(classRoom.getParticipantsWaitingInLobby().contains(john));
    }

    @Test
    public void testJoinNoBookingReturnsRefused() {
        // 3: Failure Path where if participant didnt book a timeslot
        // They are refused
        assertEquals("REFUSED",classRoom.join(john));
        assertFalse(classRoom.getParticipantsInSession().contains(john));
        assertFalse(classRoom.getParticipantsWaitingInLobby().contains(john));

    }

    @Test
    public void testBookingInPastRefused() {
        // 4: Failure where if booking has passed then
        // participant is refused
        Timeslot nine = classRoom.getSchedule().get(0);
        classRoom.makeBooking(john,nine);

//        System.out.println(classRoom.getCurrentTimeslotHour());
        classRoom.nextClass();
//        System.out.println(classRoom.getCurrentTimeslotHour());
        // I have an issue here my timing logic has edge case see -1 starting
        // defualt returns the time value of 9
        // calling nextClass increases index to 0 to the first scheduled classs
        // but first scheduled class can be at 9  so it returns 9 again
        classRoom.nextClass();
        assertEquals("REFUSED",classRoom.join(john));
    }

    // The leave test cases
    @Test
    public void testLeaveParticipantsRemovedFromSession(){
        // 1: Removes the participant from the active class
        Timeslot nine =  classRoom.getSchedule().get(0);
        classRoom.makeBooking(john,nine);
        classRoom.join(john);

        assertTrue(classRoom.getParticipantsInSession().contains(john));
        classRoom.leave(john);

        assertFalse(classRoom.getParticipantsInSession().contains(john));

    }

    @Test
    public void testLeaveParticipantsRemovedFromLobby(){
        // 2: Removes the participant waiting in the lobby
        Timeslot ten =  classRoom.getSchedule().get(1);
        classRoom.makeBooking(john,ten);
        classRoom.join(john);
        assertTrue(classRoom.getParticipantsWaitingInLobby().contains(john));
        classRoom.leave(john);
        assertFalse(classRoom.getParticipantsInSession().contains(john));
    }

//     Next CLass Tests
    @Test
    public void testNextClassMovesLobbyParticipantsIntoSessionn(){
        // This is the core movement of particpants logic
        // when clock moves using nextClas the participants in lobby are moved
        // now into the particpants in session

        Timeslot ten =  classRoom.getSchedule().get(1);
        classRoom.makeBooking(john,ten);
        // john in lobby here
        classRoom.join(john);

        assertTrue(classRoom.getParticipantsWaitingInLobby().contains(john));
//        classRoom.nextClass();
        classRoom.nextClass();
        assertTrue(classRoom.getParticipantsInSession().contains(john));
    }

    @Test
    public void defensiveAdvancingOfClockTest(){
        // Just checking if advancing time beyond schedule will not clal error
        // or incorrectly assignn particiapnts to a session
        for (int i = 9; i <= 17; i++) {
            classRoom.nextClass();
        }

        assertDoesNotThrow(() -> classRoom.nextClass());
        assertTrue(classRoom.getParticipantsInSession().isEmpty());
    }

    // add Timeslot test from trainer end
    @Test
    public void testAddTimeSlotsDuplicatesRejected(){
        // the test will be conducted by taking the size of the array with initia;
        // two classes at 9 and 10
        // then we will add a class at both 9 and ten
        // it should be same size rejecting both addiditons silently
        int sizeBefore =  classRoom.getSchedule().size();
        classRoom.addTimeslot("powerLift",3,9);
        classRoom.addTimeslot("running",4,10);
        assertEquals(sizeBefore,classRoom.getSchedule().size());
//        System.out.println(classRoom.getSchedule());

    }

    @Test
    public void testTimeSlotsCreatedStaySortedByHour(){
        // We will add time slot at 16 then at 12
        // then loop through checking all of the timeslots are srted or are they not

        classRoom.addTimeslot("powerLift",3,16);
        classRoom.addTimeslot("conditioning",3,12);

        List<Timeslot> schedule =  classRoom.getSchedule();

        for (int i = 0; i < schedule.size()-1; i++){
            assertTrue(schedule.get(i).getStartHour() < schedule.get(i+1).getStartHour());
        }
    }

    // Booking tests
    @Test
    public void testMaxCapacityForBookingEnforced(){
        // We will make a class with small size and add a sudent in size of students in timeslot should be sae

        classRoom.addTimeslot("powerLift",1,14);

        Timeslot smallClass = classRoom.getSchedule().get(2);

        classRoom.makeBooking(john,classRoom.getSchedule().get(2));
        classRoom.makeBooking(doe,classRoom.getSchedule().get(2));

        assertEquals(1,smallClass.getBookedParticipants().size());
        assertFalse(smallClass.hasParticipants(doe));
    }
}