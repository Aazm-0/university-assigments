package viewcontroller;

import model.ClassRoom;
import model.Timeslot;
import model.Trainer;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * This is the interface used by trainers. It is a Boundary class. In terms of the MVC pattern, it is also a View and a Controller.
 */
public class TrainerScreen extends javax.swing.JFrame implements Observer {

    private ClassRoom classroomModel;
    private Trainer trainer;

    // Ui Display components - These will be refreshed using update and our observer
    private JLabel statusLabel;
    private JList<String> paricipantsList;
    // Defaults for no entry
    private DefaultListModel<String> participantsListModel;
    private JList<String> scheduleList;
    private DefaultListModel<String> scheduleListModel;

    // Ui form input fields
    private JTextField classTypeTextField;
    private JTextField startHourTextField;
    private JTextField maxCapacityTextField;

    public TrainerScreen(ClassRoom classroomModel, Trainer trainer) {
        this.classroomModel = classroomModel;
        this.trainer = trainer;

        // This observable will allow us to register/ subscribe this class to our observable
        classroomModel.addObserver(this);

        buildUI();
        refresh(); // Initial model state render in constructor
        setTitle("Trainer -" + trainer.getName());
        setSize(420, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        refresh(); // Here it will now rerender the states
    }

    /**
     * This method is to refresh the values in the ui after changes from model caused from our controllers nested in
     * buttons actionlisteners
     */
    private void refresh() {
        int hour = classroomModel.getCurrentTimeslotHour();
        Timeslot currentTimeslot = classroomModel.getCurrentTimeslot();

        if (currentTimeslot != null) {
            statusLabel.setText("In Session: " + currentTimeslot.getClassType() + " at "
                    + hour + ":00");
        } else {
            statusLabel.setText("No Class Running Right now");
        }

        // Updating the participants in session for every rerender we clear them and add them in again
        participantsListModel.clear();
        for (var p : classroomModel.getParticipantsInSession()) {
            participantsListModel.addElement(p.getName());
        }

        // Same logic for the schedule list clear and add back in each rerender
        scheduleListModel.clear();
        for (Timeslot timeslot : classroomModel.getSchedule()) {
            String status = (timeslot == currentTimeslot) ? "[NOW]" : "[UPCOMING]";
            scheduleListModel.addElement(
                    timeslot.getStartHour() + ":00 - " + timeslot.getClassType() + " (" +
                    timeslot.getRemainingCapacity() + " spots left)" + status
            );
        }

    }

    /**
     * Helper Component to create consistent reusable section labels
     * @param text to represent in the label
     */
    private JLabel sectionLabel(String text) {
        // Move this to seperate class if we are going to use this in other screens too

        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD,10f));
        label.setForeground(Color.gray);
        label.setBorder(BorderFactory.createEmptyBorder(8,0,2,0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * This method is to create the intial state of the UI components of this screen
     * and therir layouts the refresh then changes some of the states of these components
     */
    private void buildUI(){

//      // Layout setup of ui
        setLayout(new BorderLayout(10,10));
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Y axis bx layout so top - bottom building of ui
        // 1.1: Current Class Status UI
        addLeft(main,sectionLabel("Current Class"));
        statusLabel = new JLabel("No Class Running Right now");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
        addLeft(main,statusLabel);

        // 1.2: participants in class
        addLeft(main,sectionLabel("Participants in Session"));
        participantsListModel = new DefaultListModel<>();
        paricipantsList = new JList<>(participantsListModel);
        paricipantsList.setVisibleRowCount(3);
        addLeft(main,new JScrollPane(paricipantsList));

        main.add(Box.createVerticalStrut(10));

        // 2: Creating the createTimeslot input form Ui
        addLeft(main,sectionLabel("Add a timeslot"));
        JPanel formPanel = new JPanel(new GridLayout(3,2,6,6));

        formPanel.add(new JLabel("Class Type:"));
        classTypeTextField = new JTextField();
        formPanel.add(classTypeTextField);

        formPanel.add(new JLabel("Start Hour: (9-17)"));
        startHourTextField = new JTextField();
        formPanel.add(startHourTextField);

        formPanel.add(new JLabel("Max Capacity: (1-40)"));
        maxCapacityTextField = new JTextField();
        formPanel.add(maxCapacityTextField);

        addLeft(main,formPanel);

        //2.1 The input forms button with the controller
        JButton addButton = new JButton("Add New Timeslot");
        addButton.addActionListener(e -> {
            // Here since this is being used a controller to call the model
            // Valdation checks will be made here
            try {
                // Get input and remove whitespace
                String classType = classTypeTextField.getText().trim();
                int startHour = Integer.parseInt(startHourTextField.getText().trim());
                int maxCapacity = Integer.parseInt(maxCapacityTextField.getText().trim());

                // Validation check 1: Ranges of input hours
                if(startHour<9 || startHour > 17){
                    JOptionPane.showMessageDialog(this, "Please enter a value between 9-17 for start hour");
                    return;
                }

                // Validation check 2: Class type emtpy
                if(classType.isEmpty()){
                    JOptionPane.showMessageDialog(this, "Please enter a Class Type");
                    return;
                }

                // Validation check 3: if Capaciy is empty or not between 1-40
                if(maxCapacity<0 || maxCapacity > 40){
                    JOptionPane.showMessageDialog(this, "Please enter a value between 1-40");
                    return;
                }

                // Controller calls model after validation checks need to add more clean ones
                classroomModel.addTimeslot(classType, maxCapacity,startHour);
                // Observer handles refresh remeber
                // Clear the input forms
                classTypeTextField.setText("");
                startHourTextField.setText("");
                maxCapacityTextField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter an integer value for hours and capacity");
            }});
        main.add(Box.createVerticalStrut(6));
        addLeft(main,addButton);

        main.add(Box.createVerticalStrut(10));

        // 3: Building the schedule list
        addLeft(main,sectionLabel("Schedule"));
        scheduleListModel = new DefaultListModel<>();
        scheduleList = new JList<>(scheduleListModel);
        scheduleList.setVisibleRowCount(6);
        addLeft(main,new JScrollPane(scheduleList));

        add(new JScrollPane(main), BorderLayout.CENTER);
    }

    /**
     * Helper to fix small misalligment issue where labels get centered
     * @param main
     * @param component
     */
    private void addLeft(JPanel main, JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(component);
    }
}