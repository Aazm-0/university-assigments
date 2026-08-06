package viewcontroller;

import model.ClassRoom;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * This is an interface to set the current time (hour of the day). It is a Boundary class. In terms of the MVC pattern, it is also a View and a Controller.
 */
public class TimerScreen extends javax.swing.JFrame implements Observer {

    private ClassRoom classRoomModel;

    // UI components
    private JLabel currentTimeLabel;
    private JComboBox<String> jumpToDropDown;

    public TimerScreen(ClassRoom classRoomModel) {
        // Again adding this as observer subscriber
        this.classRoomModel = classRoomModel;
        classRoomModel.addObserver(this);


        // Again initial call to set the values for the label
        buildUI();
        refresh();


        setTitle("Timer Screen");
        setSize(300, 200);
        // Opening screen on the center left
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int y = (screenSize.height - this.getHeight()) / 2;
        setLocation(0,y);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        refresh();
    }

    // These could be interfaces because i am following a similar pattern here

    /**
     * This method is to refresh the values in the ui after changes from model caused from our controllers nested in
     * buttons actionlisteners
     */
    private void refresh() {
        // This will update the current time label on every update from observer
        int hour = classRoomModel.getCurrentTimeslotHour();
        currentTimeLabel.setText("Current time: " + hour + ":00");
    }


    /**
     * This method is to create the intial state of the UI components of this screen
     * and therir layouts the refresh then changes some of the states of these components
     */
    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // 1.1: The current time label display
        currentTimeLabel = new JLabel("Current time:09:00");
        currentTimeLabel.setFont(currentTimeLabel.getFont().deriveFont(Font.BOLD, 16f));
        currentTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(currentTimeLabel);

        // 1.2: The next hour button for moving to the next hour
        JButton nextHourButton = new JButton("Next Hour ->>");
        nextHourButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextHourButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nextHourButton.addActionListener(e -> {
            // Again this is going to act as the controller
            int current = classRoomModel.getCurrentTimeslotHour();

            // According to the specification the time ends at 17
            if (current >= 18) {
                JOptionPane.showMessageDialog(this, "The Classes end after 18:00 no more classes");
            }

            classRoomModel.nextClass();
        });

        main.add(nextHourButton);
        add(main, BorderLayout.CENTER);

    }
}