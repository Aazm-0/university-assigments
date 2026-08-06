package viewcontroller;

import model.ClassRoom;
import model.Participant;
import model.Timeslot;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * This is the interface used by participants. It is a Boundary class. In terms of the MVC pattern, it is also a View and a Controller.
 */
public class ParticipantScreen extends javax.swing.JFrame implements Observer {

    private ClassRoom classRoomModel;
    private Participant participant;

    // Ui components that we will change in the refresh
    private JLabel statusLabel;
    private JButton joinLeaveButton;

    private DefaultListModel<String> availableListModel;
    private JList<String> availableList;

    private DefaultListModel<String> bookedListModel;
    private JList<String> bookedList;

    // State toggles for being in lobby and in class room
    private Boolean isActive = false;
    private Boolean isInLobby = false;


    private static int  moveScreenCount = 0;

    public ParticipantScreen(ClassRoom classRoomModel, Participant participant) {
        this.classRoomModel = classRoomModel;
        this.participant = participant;

        // Again this is registtered now
        classRoomModel.addObserver(this);

        // Initial build
        buildUI();
        refresh();

        setTitle("Participant -" + participant.getName());
        setSize(380, 560);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int y = (screenSize.height - this.getHeight()) / 2;

        setLocation(screenSize.width - getWidth(),y-50+moveScreenCount);
        moveScreenCount += 50;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void refresh() {
        // 1: Changing the state of join leave for the joinLeaveButton
        // Since we have the list for lobby and classroom we can check if we are in
        // in lobby or in classroom as they are getting updated automatically in next() called by trainer screen
        isActive = classRoomModel.getParticipantsInSession().contains(participant);
        isInLobby = classRoomModel.getParticipantsWaitingInLobby().contains(participant);

        if (isActive) {
            Timeslot current = classRoomModel.getCurrentTimeslot();
            String className = current.getClassType();
            statusLabel.setText("In Class: " + className + " of " + classRoomModel.getOwner().getName());
            joinLeaveButton.setText("Leave ClassRoom");
        } else if (isInLobby) {
            statusLabel.setText("Waiting in lobby for the class to start");
            joinLeaveButton.setText("Leave ClassRoom");
        } else {
            statusLabel.setText("Not Currently in ClassRoom");
            joinLeaveButton.setText("Join ClassRoom");
        }

        //2: Changing States for availabel classRooms
        // Same concept as TrainerScreen clear the list before every referesh or it will append indefinetly
        availableListModel.clear();
        for (Timeslot timeslot : classRoomModel.getSchedule()) {
            if (timeslot.getStartHour() >= classRoomModel.getCurrentTimeslotHour()) {
                // Basically since our class room model is also keeping track of our time simplified!!
                // We can check our current time from there and also get the scheduled bookings times from references
                // check if we passed those time and show only ones available in future
                String label = timeslot.getStartHour() + ":00 - " + timeslot.getClassType()
                        + " (" + timeslot.getRemainingCapacity() + " Sports Left)";
                availableListModel.addElement(label);
            }
        }

        //3: Viewing the participants own booking
        bookedListModel.clear();
        for (Timeslot timeslot : classRoomModel.getSchedule()) {
            if (timeslot.hasParticipants(participant)) {
                String label = timeslot.getStartHour() + ":00 - " + timeslot.getClassType();
                bookedListModel.addElement(label);
            }
        }

    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // 1: Status class/lobby and join button
        main.add(sectionLabel("My Status"));
        // Initial placeholder
        statusLabel = new JLabel("Not Currently in ClassRoom");
        main.add(statusLabel);

        //Placeholder
        joinLeaveButton = new JButton("Join ClassRoom");
        joinLeaveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        joinLeaveButton.addActionListener(e -> {
            // Again the controller logic
            if (isActive || isInLobby) {
                classRoomModel.leave(participant);
            } else {
                String result = classRoomModel.join(participant);
                // Should make this enum so i dont have to look back
                if (result.equals("REFUSED")) {
                    JOptionPane.showMessageDialog(this, "ClassRoom cant be joined no prior booking");
                }
            }
        });
        main.add(Box.createVerticalStrut(6));
        main.add(joinLeaveButton);

        main.add(Box.createVerticalStrut(10));

        //2: Available booking classes list
        main.add(sectionLabel("Available timeslots"));
        // placeholder
        availableListModel = new DefaultListModel<>();
        availableList = new JList<>(availableListModel);
        availableList.setVisibleRowCount(5);
        main.add(new JScrollPane(availableList));

        //2.1: Button for booking the available timeslots
        JButton bookButton = new JButton("Book ClassRoom");
        bookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookButton.addActionListener(e -> {
            int selectedIndex = availableList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Please select a ClassRoom");
                return;
            }

            // Okay now we will filter out only the future bookings
            List<Timeslot> futureSlots = classRoomModel.getSchedule().stream()
                    .filter(t -> t.getStartHour() >= classRoomModel.getCurrentTimeslotHour())
                    .toList();
            // This now gives us the booking the timeslot selected by user
            Timeslot chosen = futureSlots.get(selectedIndex);

            // Validation 1: checkk if timeslot full i believe we are doing this at model layer too
            // but that resolves quitely
            if(chosen.isFull()){
                JOptionPane.showMessageDialog(this, "This timeslot is full");
                return;
            }

            // Validation 2: Check if prebboked
            if(chosen.hasParticipants(participant)){
                JOptionPane.showMessageDialog(this, "This timeslot is already booked");
                return;
            }

            classRoomModel.makeBooking(participant, chosen);
        });
        //padding
        main.add(Box.createVerticalStrut(6));
        main.add(bookButton);
        main.add(Box.createVerticalStrut(10));

        //3: The already booked spots
        main.add(sectionLabel("Booked timeslots"));
        bookedListModel = new DefaultListModel<>();
        bookedList = new JList<>(bookedListModel);
        bookedList.setVisibleRowCount(4);
        main.add(new JScrollPane(bookedList));

        add(new JScrollPane(main), BorderLayout.CENTER);

    }

    /**
     * Helper Component to create consistent reusable section labels
     *
     * @param text to represent in the label
     */
    private JLabel sectionLabel(String text) {
        // Move this to seperate class if we are going to use this in other screens too

        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setForeground(Color.gray);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));

        return label;
    }


    @Override
    public void update(Observable o, Object arg) {
        refresh();
    }
}