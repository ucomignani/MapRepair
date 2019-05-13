package MapRepairGUI;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import util.SafeRewriting;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Set;

public class MapRepair {

    private JPanel panelMain;
    private JTabbedPane choiceViewMap;
    private JPanel tabViewDesign;
    private JPanel tabMappDesign;
    private JButton importViewsAsButton;
    private JButton editCurrentViewButton;
    private JButton saveViewsAsButton;
    private JButton addNewViewButton;
    private JTextArea textViewsDisclosure;
    private JScrollPane textViewsScroll;
    private JTextArea viewTextArea;

    private Schema policySchema;

    public MapRepair() {

        importViewsAsButton.addActionListener(getLoadViewsListener());

        saveViewsAsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //JOptionPane.showMessageDialog(null, "test");
                File fileToSave = saveFilePath();

            }
        });
    }

    private ActionListener getLoadViewsListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                File policyViewsFile = openFilePath();

                policySchema = null;
                try {
                    policySchema = IOManager.importSchema(policyViewsFile);
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                SafeRewriting safeRew = new SafeRewriting(policySchema);

                printViewsInTextArea(viewTextArea, policySchema);
                printInstanceInTextArea(textViewsDisclosure, safeRew.getInstanceRef());
            }
        };
    }

    private void printViewsInTextArea(JTextArea textArea, Schema schema) {
        try {
            textArea.setLineWrap(true);
            textArea.setText(schema.dependenciesToViewString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTgdsInTextArea(JTextArea textArea, Schema schema) {
        try {
            textArea.setLineWrap(true);
            textArea.setText(schema.dependenciesToTgdString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printInstanceInTextArea(JTextArea textArea, Set<Atom> instance) {
        try {
            textArea.setLineWrap(true);
            textArea.setText(instanceToSimpleString(instance));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String instanceToSimpleString(Set<Atom> instance) {
        String output = "";

        for(Atom at : instance){
            output += at.toString();
            output += "\n";
        }
        return output;
    }

    public File openFilePath() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return jfc.getSelectedFile();
        }

        return null;
    }

    public File saveFilePath() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return jfc.getSelectedFile();
        }

        return null;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MapRepair");
        frame.setContentPane(new MapRepair().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
