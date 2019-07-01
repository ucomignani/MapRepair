package MapRepairGUI;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditDependency {

    private static JFrame frame;
    private JPanel editDependencyMainJPanel;

    private JButton applyButton;
    private JButton cancelButton;
    private JTextPane bodyTextPane;
    private JTextPane headTextPane;

    private Dependency outputDependency;

    public EditDependency(String typeOfDependencyToAdd, MapRepair mapRepair) {
        this.outputDependency = null;

        frame = new JFrame("New " + typeOfDependencyToAdd);
        frame.setContentPane(editDependencyMainJPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                generateNewDependency();

                switch (typeOfDependencyToAdd) {
                    case "view":
                        mapRepair.addNewView(outputDependency);
                        break;
                    case "tgd":
                        mapRepair.addNewTgd(outputDependency);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Unknow type for the dependency to add.");
                }

                frame.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                frame.dispose();
            }
        });
    }

    public EditDependency(String typeOfDependencyToAdd, Dependency dependencyToEdit, MapRepair mapRepair) {
        this.outputDependency = null;

        frame = new JFrame("Edit " + typeOfDependencyToAdd);
        frame.setContentPane(editDependencyMainJPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        bodyTextPane.setText(dependencyToEdit.bodyToString());
        headTextPane.setText(dependencyToEdit.headToString());

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                generateNewDependency();

                if (outputDependency != null) {
                    switch (typeOfDependencyToAdd) {
                        case "view":
                            mapRepair.addNewView(outputDependency);
                            break;
                        case "tgd":
                            mapRepair.addNewTgd(outputDependency);
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "Unknow type for the dependency to add.");
                    }

                    frame.dispose();
                }
            }
        });
        cancelButton.addActionListener(new

                                               ActionListener() {
                                                   @Override
                                                   public void actionPerformed(ActionEvent actionEvent) {
                                                       frame.dispose();
                                                   }
                                               });
    }

    private void generateNewDependency() {
        boolean syntaxError = false;

        Set<Atom> bodyAtoms;
        Set<Atom> headAtoms;

        // body
        bodyAtoms = new HashSet<>();

        StringBuilder bodyString = new StringBuilder(bodyTextPane.getText());
        String[] splitBodyString = bodyString.toString().split("\n");
        for (String line : splitBodyString) {
            List<Term> bodyVariables = new ArrayList<>();

            line.replace(" ", "");
            if (!line.substring(line.length() - 1).equals(")")) {
                syntaxError = true;
                break;
            }
            line.replace(")", "");

            String[] splitLine = line.split("\\(");
            if (splitLine.length != 2) {
                syntaxError = true;
                break;
            }

            for (String term : splitLine[1].replace(")", "").split(",")) {
                bodyVariables.add(Variable.create(term));
            }

            bodyAtoms.add(Atom.create(Predicate.create(splitLine[0], bodyVariables.size()), bodyVariables.toArray(new Term[0])));
        }

        // head

        headAtoms = new HashSet<>();

        StringBuilder headString = new StringBuilder(headTextPane.getText());
        String[] splitHeadString = headString.toString().split("\n");
        for (String line : splitHeadString) {
            List<Term> headVariables = new ArrayList<>();

            line.replace(" ", "");
            if (!line.substring(line.length() - 1).equals(")")) {
                syntaxError = true;
            } else {
                line.replace(")", "");

                String[] splitLine = line.split("\\(");
                if (splitLine.length != 2) {
                    syntaxError = true;
                } else {

                    for (String term : splitLine[1].replace(")", "").split(",")) {
                        headVariables.add(Variable.create(term));
                    }

                    headAtoms.add(Atom.create(Predicate.create(splitLine[0], headVariables.size()), headVariables.toArray(new Term[0])));
                }
            }
        }

        if (syntaxError == true)
            JOptionPane.showMessageDialog(null, "Syntax error.");
        else
            outputDependency = Dependency.create(bodyAtoms.toArray(new Atom[0]), headAtoms.toArray(new Atom[0]));
    }


    public Dependency newTgd() {
        return this.outputDependency;
    }
}
