import model.ClassRoom;
import model.Participant;
import model.Timeslot;
import model.Trainer;
import viewcontroller.ParticipantScreen;
import viewcontroller.TimerScreen;
import viewcontroller.TrainerScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * The main class which launches the Fitness Woprkshop system.
 */
public class FitnessWorkshopSystemLauncher {

    public static void main(String[] args) {
        // Initialize trainer
        Trainer t = new Trainer("Patrick", "patrick@gmail.com");
        // Initialize class
        ClassRoom powerLift = new ClassRoom(t);


        // Initialize participant
        Participant maaz = new Participant("maaz", "maaz@gmail.com");
        Participant ali = new Participant("ali", "ali@gmail.com");
        Participant sarah = new Participant("sarah","sarah@gmail.com");

        TimerScreen timerScreen = new TimerScreen(powerLift);
        TrainerScreen tS = new TrainerScreen(powerLift,t);


        new ParticipantScreen(powerLift,maaz);
        new ParticipantScreen(powerLift,ali);
        new ParticipantScreen(powerLift,sarah);
    }

}