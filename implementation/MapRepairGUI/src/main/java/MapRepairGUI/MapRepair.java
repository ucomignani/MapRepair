package MapRepairGUI;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import preference.KNNPreference;
import preference.MaxAvgValuesPreference;
import preference.MaxFrontierPreference;
import preference.Preference;
import uk.ac.ox.cs.pdq.db.*;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import util.ProvenanceGraph;
import util.SafeRewriting;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class MapRepair {

    private JPanel panelMain;

    /**
     * views tab
     */
    private JTabbedPane choiceViewMap;
    private JPanel tabViewDesign;
    private JButton importViewsAsButton;
    private JButton editCurrentViewButton;
    private JButton saveViewsAsButton;
    private JButton addNewViewButton;
    private JButton highlightInformationLeakButton;


    private JTextPane textViewsDisclosure;

    private JScrollPane textViewsScroll;
    private JList viewsListArea;
    private JTextField disclosedValuesText;
    private JTextField disclosedJoinsText;
    private JButton bagGraphViewsButton;

    /**
     * mapping tab
     */
    private JComboBox prefFunChoice;
    private JTextField trainingSetPath;
    private JPanel trainingSetPanel;
    private JLabel trainingSetLabel;
    private JButton chooseTrainingSetButton;
    private JButton importReferencePolicyViewsButton;
    private JButton generateASafeMappingButton;
    private JTextField policyViewsFileTextInMap;
    private JButton importMappingButton;
    private JButton saveMappingButton;
    private JList mappingListArea;
    private JTextPane textMappingDisclosure;
    private JButton bagsGraphButton;
    private JScrollPane textViewsDisclosureScroll;
    private JButton delViewJButton;
    private JButton delTgdJButton;
    private JButton disclosureButton;
    private JTextField unwantedValues;
    private JTextField textUnwantedJoins;
    private JTextField textUnwantedValues;
    private JTextField textRepairingTime;
    private JComboBox comboBoxMApping;
    private JComboBox comboBoxViews;
    private JButton addNewTgd;
    private JButton editCurrentTgdButton;
    private BagsGraphUI bagsGraphUI;

    /**
     * data views
     */
    private File policyViewsFileViewsTab = null;
    private Schema policySchemaViewTab = null;

    private HashMap<String, Dependency> nameToDepViews = new HashMap<String, Dependency>();

    /**
     * data mappingss
     */
    private File policyViewsFileMappingTab = null;
    private File mappingFile = null;
    private File trainingSetFile = null;

    private Schema policySchemaMappingTab = null;
    private Schema mappingToRewrite = null;
    private Schema rewrittenMapping = null;

    private SafeRewriting safeRewMap = null;

    private HashMap<String, Dependency> nameToDepMapping = new HashMap<String, Dependency>();
    private Preference preferenceFun = new MaxFrontierPreference();

    private boolean isLearningPrefFunction = false;

    /**
     * examples demo
     */
    // running example
    private File policyViewsFileRE = null;
    private File mappingFileRE = null;

    private Schema policyViewsRE = null;
    private Schema mappingRE = null;

    // NHS
    private File policyViewsFileNHS = null;
    private File mappingFileNHS = null;

    private Schema policyViewsNHS = null;
    private Schema mappingNHS = null;

    // 100 tgds
    private File policyViewsFile100tgds = null;
    private File mappingFile100tgds = null;

    private Schema policyViews100tgds = null;
    private Schema mapping100tgds = null;

    // 300 tgds
    private File policyViewsFile300tgds = null;
    private File mappingFile300tgds = null;

    private Schema policyViews300tgds = null;
    private Schema mapping300tgds = null;

    public MapRepair() {

        initDemoCases();

        /**
         *  views tab
         */
        importViewsAsButton.addActionListener(getLoadViewsListener());
        saveViewsAsButton.addActionListener(getSaveViewsActionListener());
        addNewViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                callNewViewPanel();
            }
        });
        delViewJButton.addActionListener(getDelViewActionListener());
        editCurrentViewButton.addActionListener(getEditViewActionListener());

        highlightInformationLeakButton.addActionListener(getHighlightInformationLeakActionListener());

        /**
         *  mappings tab
         */
        String[] prefFunctionStrings = {"Max exported", "Max avg exported + views", "learning with k-NN"};
        for (String str : prefFunctionStrings)
            prefFunChoice.addItem(str);
        prefFunChoice.addActionListener(getPrefFunComboActionListener());

        chooseTrainingSetButton.addActionListener(getTrainingSetFile());

        importReferencePolicyViewsButton.addActionListener(getPolicyViewsMapTabActionListener());

        importMappingButton.addActionListener(getMappingActionListener());
        saveMappingButton.addActionListener(getSaveMappingActionListener());

        generateASafeMappingButton.addActionListener(getGenerateSafeMappButtonActionListener());


        bagsGraphButton.addActionListener(getBagsWindowsButtonActionListener());
        addNewTgd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                callNewTgdPanel();
            }
        });
        editCurrentTgdButton.addActionListener(getEditTgdActionListener());
        delTgdJButton.addActionListener(getDelTgdActionListener());
        disclosureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (policySchemaMappingTab == null) {
                    JOptionPane.showMessageDialog(null, "Please provide a set of reference policy views.");
                } else if (mappingToRewrite == null) {
                    JOptionPane.showMessageDialog(null, "Please provide a mapping to rewrite.");

                } else {
                    SafeRewriting safeRew = new SafeRewriting(policySchemaMappingTab);
                    rewrittenMapping = safeRew.repair(mappingToRewrite, 10);
                    printDisclosureInTextAreaWithColors(textViewsDisclosure);
                }
            }
        });
        bagGraphViewsButton.addActionListener(getViewsBagsWindowsButtonActionListener());
    }

    /**
     * For the demo
     */
    private void initDemoCases() {
        // RE
        this.policyViewsFileRE = openFile("./scenarios/RE/policyViews.xml");
        this.policyViewsRE = loadMappingFile(this.policyViewsFileRE);

        this.mappingFileRE = openFile("./scenarios/RE/MappingToRewrite.xml");
        this.mappingRE = loadMappingFile(this.mappingFileRE);

        // NHS
        this.policyViewsFileNHS = openFile("./scenarios/NHS/policyViews.xml");
        this.policyViewsNHS = loadMappingFile(this.policyViewsFileNHS);

        this.mappingFileNHS = openFile("./scenarios/NHS/MappingToRewrite.xml");
        this.mappingNHS = loadMappingFile(this.mappingFileNHS);

        // 100 tgds
        this.policyViewsFile100tgds = openFile("./scenarios/100tgds/policyViews.xml");
        this.policyViews100tgds = loadMappingFile(this.policyViewsFile100tgds);

        this.mappingFile100tgds = openFile("./scenarios/100tgds/MappingToRewrite.xml");
        this.mapping100tgds = loadMappingFile(this.mappingFile100tgds);

        // 300 tgds
        this.policyViewsFile300tgds = openFile("./scenarios/300tgds/policyViews.xml");
        this.policyViews300tgds = loadMappingFile(this.policyViewsFile300tgds);

        this.mappingFile300tgds = openFile("./scenarios/300tgds/MappingToRewrite.xml");
        this.mapping300tgds = loadMappingFile(this.mappingFile300tgds);

        // init comboBoxes
        String[] prefFunctionStrings = {"", "Running example", "NHS", "100 tgds", "300 tgds"};
        for (String str : prefFunctionStrings) {
            this.comboBoxViews.addItem(str);
            this.comboBoxMApping.addItem(str);
        }
        this.comboBoxViews.addActionListener(getScenarioViewsActionListener());
        this.comboBoxMApping.addActionListener(getScenarioMappingActionListener());

    }

    private File openFile(String path) {
        File fileTmp = new File(path);
        return fileTmp;
    }

    private Schema loadMappingFile(File file) {
        Schema schemaTmp = null;

        try {
            schemaTmp = IOManager.importSchema(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return schemaTmp;
    }

    private ActionListener getScenarioViewsActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int scenarioIndex = comboBoxViews.getSelectedIndex();
                switch (scenarioIndex) {
                    case 0:
                        break;
                    case 1:
                        policySchemaViewTab = policyViewsRE;
                        policyViewsFileViewsTab = policyViewsFileRE;
                        refreshViewTab();
                        break;
                    case 2:
                        policySchemaViewTab = policyViewsNHS;
                        policyViewsFileViewsTab = policyViewsFileNHS;
                        refreshViewTab();
                        break;
                    case 3:
                        policySchemaViewTab = policyViews100tgds;
                        policyViewsFileViewsTab = policyViewsFile100tgds;
                        refreshViewTab();
                        break;
                    case 4:
                        policySchemaViewTab = policyViews300tgds;
                        policyViewsFileViewsTab = policyViewsFile300tgds;
                        refreshViewTab();
                        break;
                    default:
                        throw new IllegalArgumentException("Not a correct value for the scenario.");
                }
            }
        };
    }

    private ActionListener getScenarioMappingActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int scenarioIndex = comboBoxMApping.getSelectedIndex();
                switch (scenarioIndex) {
                    case 0:
                        break;
                    case 1:
                        policySchemaMappingTab = policyViewsRE;
                        policyViewsFileMappingTab = policyViewsFileRE;
                        mappingToRewrite = mappingRE;
                        mappingFile = mappingFileRE;
                        safeRewMap = new SafeRewriting(policySchemaMappingTab);
                        refreshMappingTab();
                        break;
                    case 2:
                        policySchemaMappingTab = policyViewsNHS;
                        policyViewsFileMappingTab = policyViewsFileNHS;
                        mappingToRewrite = mappingNHS;
                        mappingFile = mappingFileNHS;
                        safeRewMap = new SafeRewriting(policySchemaMappingTab);
                        refreshMappingTab();
                        break;
                    case 3:
                        policySchemaMappingTab = policyViews100tgds;
                        policyViewsFileMappingTab = policyViewsFile100tgds;
                        mappingToRewrite = mapping100tgds;
                        mappingFile = mappingFile100tgds;
                        safeRewMap = new SafeRewriting(policySchemaMappingTab);
                        refreshMappingTab();
                        break;
                    case 4:
                        policySchemaMappingTab = policyViews300tgds;
                        policyViewsFileMappingTab = policyViewsFile300tgds;
                        mappingToRewrite = mapping300tgds;
                        mappingFile = mappingFile300tgds;
                        safeRewMap = new SafeRewriting(policySchemaMappingTab);
                        refreshMappingTab();
                        break;
                    default:
                        throw new IllegalArgumentException("Not a correct value for the scenario.");
                }
            }
        };
    }

    private ActionListener getDelViewActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Set<Dependency> newDependencies = new HashSet<>();
                for (Dependency dep : policySchemaViewTab.getAllDependencies()) {
                    if (dep != nameToDepViews.get(viewsListArea.getSelectedValue()))
                        newDependencies.add(dep);
                }
                policySchemaViewTab = new Schema(policySchemaViewTab.getRelations(), newDependencies.toArray(new Dependency[0]));
                refreshViewTab();
            }
        };
    }

    private ActionListener getDelTgdActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Set<Dependency> newDependencies = new HashSet<>();
                for (Dependency dep : mappingToRewrite.getAllDependencies()) {
                    if (dep != nameToDepMapping.get(mappingListArea.getSelectedValue()))
                        newDependencies.add(dep);
                }
                mappingToRewrite = new Schema(mappingToRewrite.getRelations(), newDependencies.toArray(new Dependency[0]));
                refreshMappingTab();
            }
        };
    }

    private void refreshMappingTab() {
        nameToDepMapping.clear();
        for (Dependency dep : mappingToRewrite.getAllDependencies()) {
            StringBuilder dependencyString = new StringBuilder("<html><h3>").append(dep.toTgdString()).append("</html>");
            nameToDepMapping.put(dependencyString.toString(), dep);
        }

        if (safeRewMap != null) {
            policyViewsFileTextInMap.setText(policyViewsFileMappingTab.getAbsolutePath());

            printDependenciesInListArea(mappingListArea, nameToDepMapping);
            // printInstanceInTextArea(textMappingDisclosure, policySchemaMappingTab, safeRewMap.getSimplifiedInstanceRef());
            // TODO : print repaired mapping
        } else {
            JOptionPane.showMessageDialog(null, "Please provide a set of reference policy views.");
            printDependenciesInListArea(mappingListArea, nameToDepMapping);
        }
    }


    private void refreshViewTab() {
        nameToDepViews.clear();
        for (Dependency dep : policySchemaViewTab.getAllDependencies()) {
            StringBuilder dependencyString = new StringBuilder("<html><h3>").append(dep.toViewString()).append("</html>");
            nameToDepViews.put(dependencyString.toString(), dep);
        }

        SafeRewriting safeRew = new SafeRewriting(policySchemaViewTab);

        printDependenciesInListArea(viewsListArea, nameToDepViews);
        printInstanceInTextArea(textViewsDisclosure, policySchemaViewTab, safeRew.getSimplifiedInstanceRef());
    }

    private ActionListener getHighlightInformationLeakActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SafeRewriting safeRew = new SafeRewriting(policySchemaViewTab);
                printInstanceInTextAreaWithColors(textViewsDisclosure, policySchemaViewTab, safeRew.getSimplifiedInstanceRef());
            }
        };
    }

    private ActionListener getBagsWindowsButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mappingToRewrite != null)
                    printBagsGraph(new SafeRewriting(mappingToRewrite).getProvenanceGraph());
                else
                    JOptionPane.showMessageDialog(null, "Please load a mapping before.");
            }
        };
    }

    private ActionListener getViewsBagsWindowsButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (policySchemaViewTab != null)
                    printBagsGraph(new SafeRewriting(policySchemaViewTab).getProvenanceGraph());
                else
                    JOptionPane.showMessageDialog(null, "Please load a set of views before.");
            }
        };
    }

    private ActionListener getGenerateSafeMappButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                List<String> missingData = new ArrayList<>();
                if (mappingToRewrite == null)
                    missingData.add("a mapping to rewrite");
                if (policySchemaMappingTab == null)
                    missingData.add("a set of reference policy views");
                if (isLearningPrefFunction == true && trainingSetFile == null)
                    missingData.add("a training dataset");

                if (missingData.size() > 0) {
                    StringBuilder alert = new StringBuilder("Please provide: ");
                    for (int i = 0; i < missingData.size(); i++) {
                        if (i == 0)
                            alert.append(missingData.get(i));
                        else {
                            if (i == missingData.size() - 1)
                                alert.append(" and ").append(missingData.get(i));
                            else
                                alert.append(", ").append(missingData.get(i));
                        }
                    }
                    JOptionPane.showMessageDialog(null, alert);

                } else {
                    Schema repairedSchema = safeRewMap.repair(mappingToRewrite, preferenceFun, 10);
                    printMappingInTextArea(textMappingDisclosure, repairedSchema);
                    printDisclosedValuesAndJoinsInMappingWithColors(repairedSchema, new SafeRewriting(repairedSchema).getSimplifiedInstanceRef());
                    printRepairingTime(textRepairingTime, safeRewMap.getRepairingTime());
                    // TODO : reload the mapping in list area...
                }
            }
        };
    }

    private void printRepairingTime(JTextField textRepairingTime, long repairingTime) {
        StringBuilder repairingTimeString = new StringBuilder("Repairing time: ");
        repairingTimeString.append(repairingTime).append("ms");

        this.textRepairingTime.setText(repairingTimeString.toString());
    }

    private ActionListener getPolicyViewsMapTabActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File tmpPolicyViewsFile = openFilePath();

                try {
                    policySchemaMappingTab = IOManager.importSchema(tmpPolicyViewsFile);
                    policyViewsFileMappingTab = tmpPolicyViewsFile;
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                nameToDepMapping.clear();
                for (Dependency dep : policySchemaMappingTab.getAllDependencies()) {
                    StringBuilder dependencyString = new StringBuilder("<html><h3>").append(dep.toViewString()).append("</html>");
                    nameToDepMapping.put(dependencyString.toString(), dep);
                }

                policyViewsFileTextInMap.setText(policyViewsFileMappingTab.getAbsolutePath());


                if (preferenceFun != null)
                    safeRewMap = new SafeRewriting(policySchemaMappingTab, preferenceFun);
                else
                    JOptionPane.showMessageDialog(null, "Please provide a valid training set.");

                if (mappingToRewrite != null) {
                    // TODO : print repaired mapping
                }
            }
        };
    }

    private ActionListener getMappingActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File tmpMappingFile = openFilePath();

                try {
                    mappingToRewrite = IOManager.importSchema(tmpMappingFile);
                    mappingFile = tmpMappingFile;
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                refreshMappingTab();
            }
        };
    }

    private ActionListener getTrainingSetFile() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File tmpTrainingSetFile = openFilePath();
                if (tmpTrainingSetFile.exists()) {
                    trainingSetFile = tmpTrainingSetFile;
                    trainingSetPath.setText(trainingSetFile.getAbsolutePath());
                    try {
                        preferenceFun = new KNNPreference(trainingSetFile.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "This file does not exists.");
                }
            }
        };
    }

    private ActionListener getPrefFunComboActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int prefFunIndex = prefFunChoice.getSelectedIndex();
                switch (prefFunIndex) {
                    case 0:
                        trainingSetLabel.setVisible(false);
                        trainingSetPath.setVisible(false);
                        chooseTrainingSetButton.setVisible(false);

                        preferenceFun = new MaxFrontierPreference();
                        isLearningPrefFunction = false;
                        break;
                    case 1:
                        trainingSetLabel.setVisible(false);
                        trainingSetPath.setVisible(false);
                        chooseTrainingSetButton.setVisible(false);

                        preferenceFun = new MaxAvgValuesPreference();
                        isLearningPrefFunction = false;
                        break;
                    case 2:
                        initLearnedPref();
                        isLearningPrefFunction = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Not a correct index value for the preference function.");
                }
            }

            private void initLearnedPref() {
                trainingSetLabel.setVisible(true);
                trainingSetPath.setVisible(true);
                chooseTrainingSetButton.setVisible(true);

                if (trainingSetFile == null)
                    preferenceFun = null;
            }
        };
    }

    private ActionListener getEditViewActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                callEditViewPanel(nameToDepViews.get(viewsListArea.getSelectedValue()));
            }
        };
    }

    private ActionListener getEditTgdActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                callEditTgdPanel(nameToDepMapping.get(mappingListArea.getSelectedValue()));
            }
        };
    }

    private ActionListener getSaveViewsActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                File fileToSave = saveFilePath();
                if (fileToSave != null) {
                    //JOptionPane.showMessageDialog(null,fileToSave.getAbsolutePath());
                    try {
                        IOManager.exportSchemaToXml(policySchemaViewTab, fileToSave);
                        policyViewsFileViewsTab = fileToSave;
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private ActionListener getSaveMappingActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                File fileToSave = saveFilePath();
                if (fileToSave != null) {
                    //JOptionPane.showMessageDialog(null,fileToSave.getAbsolutePath());
                    try {
                        IOManager.exportSchemaToXml(mappingToRewrite, fileToSave);
                        mappingFile = fileToSave;
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private ActionListener getLoadViewsListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                File tmpPolicyViewsFile = openFilePath();

                policySchemaViewTab = null;
                try {
                    policySchemaViewTab = IOManager.importSchema(tmpPolicyViewsFile);
                    policyViewsFileViewsTab = tmpPolicyViewsFile;
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                refreshViewTab();
            }
        };
    }

    private void printDependenciesInListArea(JList list, HashMap<String, Dependency> dependenciesMap) {
        list.setListData(dependenciesMap.keySet().toArray());
    }

    private void printTgdsInTextArea(JTextArea textArea, Schema schema) {
        try {
            textArea.setLineWrap(true);
            textArea.setText(schema.dependenciesToTgdString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printMappingInTextArea(JTextPane textArea, Schema repair) {
        Map<Term, Integer> nbOccVars = new HashMap<>();
        Map<Term, Attribute> joinRenaming = new HashMap<>();
        StringBuilder instanceHtml = new StringBuilder();
        instanceHtml.append("<html><h2>");

        StringBuilder disclosedValues = new StringBuilder();
        StringBuilder disclosedJoins = new StringBuilder();

        // html instance
        for (Dependency dep : repair.getAllDependencies()) {
            instanceHtml.append(dep.toTgdString()).append("<br><br>");
        }
        instanceHtml.append("</html>");

        // disclosed values and joins
        if (disclosedValues.length() > 1)
            disclosedValues.setLength(disclosedValues.length() - 2);

        try {
            textArea.setText(instanceHtml.toString());
            disclosedJoinsText.setText(disclosedJoins.toString());
            disclosedValuesText.setText(disclosedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printInstanceInTextArea(JTextPane textArea, Schema policySchema, Set<Atom> instance) {
        Map<Term, Integer> nbOccVars = new HashMap<>();
        Map<Term, Attribute> joinRenaming = new HashMap<>();
        Map<Attribute, Integer> indexAttRenaming = new HashMap<>();
        StringBuilder instanceHtml = new StringBuilder();
        instanceHtml.append("<html><h2>");

        StringBuilder disclosedValues = new StringBuilder();
        StringBuilder disclosedJoins = new StringBuilder();

        for (Atom at : instance) {
            for (Term term : at.getTerms()) {
                if (nbOccVars.containsKey(term))
                    nbOccVars.put(term, nbOccVars.get(term) + 1);
                else
                    nbOccVars.put(term, 1);
            }
        }

        // html instance
        for (Atom at : instance) {
            for (Relation rel : policySchema.getRelations()) {
                if (at.getPredicate().getName().toString().equals(rel.getName().toString())) {
                    Term[] terms = at.getTerms();
                    Attribute[] atts = rel.getAttributes();

                    instanceHtml.append(at.getPredicate().getName()).append("(");
                    for (int i = 0; i < atts.length; i++) {
                        if (joinRenaming.containsKey(terms[i]))
                            instanceHtml.append(joinRenaming.get(terms[i])).append(",");
                        else {
                            joinRenaming.put(terms[i], atts[i]);
                            instanceHtml.append(atts[i]).append(",");
                        }
                    }
                    instanceHtml.setLength(instanceHtml.length() - 1);
                    instanceHtml.append(")<br><br>");
                }
            }
        }
        instanceHtml.append("</html>");

        // disclosed values and joins
        if (disclosedValues.length() > 1)
            disclosedValues.setLength(disclosedValues.length() - 2);

        try {
            textArea.setText(instanceHtml.toString());
            disclosedJoinsText.setText(disclosedJoins.toString());
            disclosedValuesText.setText(disclosedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printDisclosedValuesAndJoinsInMappingWithColors(Schema policySchema, Set<Atom> instance) {
        Map<Term, Integer> nbOccVars = new HashMap<>();
        Map<Term, Attribute> joinRenaming = new HashMap<>();
        Map<Attribute, Integer> indexAttRenaming = new HashMap<>();

        StringBuilder disclosedValues = new StringBuilder();
        StringBuilder disclosedJoins = new StringBuilder();

        for (Atom at : instance) {
            for (Term term : at.getTerms()) {
                if (nbOccVars.containsKey(term))
                    nbOccVars.put(term, nbOccVars.get(term) + 1);
                else
                    nbOccVars.put(term, 1);
            }
        }

        // html instance
        for (Atom at : instance) {
            for (Relation rel : policySchema.getRelations()) {
                if (at.getPredicate().getName().toString().equals(rel.getName().toString())) {
                    Term[] terms = at.getTerms();
                    Attribute[] atts = rel.getAttributes();

                    for (int i = 0; i < atts.length; i++) {
                        if (terms[i].toString().equals("*")) {
                            disclosedValues.append(atts[i]).append(", ");
                        } else if (nbOccVars.containsKey(terms[i]) && nbOccVars.get(terms[i]) > 1) {
                            if (!joinRenaming.containsKey(terms[i])) {
                                if (indexAttRenaming.containsKey(atts[i])) {
                                    indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                    String newAttName = atts[i].getName() + indexAttRenaming.get(atts[i]).toString();
                                    Attribute newRenamedAtt =
                                            Attribute.create(String.class, newAttName);
                                    joinRenaming.put(terms[i], newRenamedAtt);
                                } else {
                                    indexAttRenaming.put(atts[i], 0);
                                    joinRenaming.put(terms[i], Attribute.create(String.class, atts[i] + indexAttRenaming.get(atts[i]).toString()));
                                }

                                disclosedJoins.append(joinRenaming.get(terms[i])).append(", ");
                            }

                        } else {
                            if (!joinRenaming.containsKey(terms[i])){
                                if (indexAttRenaming.containsKey(atts[i])) {
                                    indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                    String newAttName = atts[i].getName() + indexAttRenaming.get(atts[i]).toString();
                                    Attribute newRenamedAtt =
                                            Attribute.create(String.class, newAttName);
                                    joinRenaming.put(terms[i], newRenamedAtt);
                                } else {
                                    indexAttRenaming.put(atts[i], 0);
                                    joinRenaming.put(terms[i], Attribute.create(String.class,
                                            atts[i] + indexAttRenaming.get(atts[i]).toString()));
                                }
                            }
                        }
                    }
                }
            }
        }

        // disclosed values and joins
        if (disclosedValues.length() > 1)
            disclosedValues.setLength(disclosedValues.length() - 2);

        try {
            textUnwantedJoins.setText(disclosedJoins.toString());
            textUnwantedValues.setText(disclosedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printInstanceInTextAreaWithColors(JTextPane textArea, Schema policySchema, Set<Atom> instance) {
        Map<Term, Integer> nbOccVars = new HashMap<>();
        Map<Term, Attribute> joinRenaming = new HashMap<>();
        Map<Attribute, Integer> indexAttRenaming = new HashMap<>();
        StringBuilder instanceHtml = new StringBuilder();
        instanceHtml.append("<html><h2>");

        StringBuilder disclosedValues = new StringBuilder();
        StringBuilder disclosedJoins = new StringBuilder();

        for (Atom at : instance) {
            for (Term term : at.getTerms()) {
                if (nbOccVars.containsKey(term))
                    nbOccVars.put(term, nbOccVars.get(term) + 1);
                else
                    nbOccVars.put(term, 1);
            }
        }

        // html instance
        for (Atom at : instance) {
            for (Relation rel : policySchema.getRelations()) {
                if (at.getPredicate().getName().toString().equals(rel.getName().toString())) {
                    Term[] terms = at.getTerms();
                    Attribute[] atts = rel.getAttributes();

                    instanceHtml.append(at.getPredicate().getName()).append("(");
                    for (int i = 0; i < atts.length; i++) {
                        if (terms[i].toString().equals("*")) {
                            instanceHtml.append("<font color=red>").append(atts[i]).append("</font>,");
                            disclosedValues.append(atts[i]).append(", ");
                        } else if (nbOccVars.containsKey(terms[i]) && nbOccVars.get(terms[i]) > 1) {
                            if (joinRenaming.containsKey(terms[i]))
                                instanceHtml.append("<font color=orange>").append(joinRenaming.get(terms[i])).append("</font>,");
                            else {
                                if (indexAttRenaming.containsKey(atts[i])) {
                                    indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                    String newAttName = atts[i].getName() + indexAttRenaming.get(atts[i]).toString();
                                    Attribute newRenamedAtt =
                                            Attribute.create(String.class, newAttName);
                                    joinRenaming.put(terms[i], newRenamedAtt);
                                } else {
                                    indexAttRenaming.put(atts[i], 0);
                                    joinRenaming.put(terms[i], Attribute.create(String.class, atts[i] + indexAttRenaming.get(atts[i]).toString()));
                                }

                                instanceHtml.append("<font color=orange>").append(joinRenaming.get(terms[i])).append("</font>,");
                                disclosedJoins.append(joinRenaming.get(terms[i])).append(", ");
                            }

                        } else {
                            if (joinRenaming.containsKey(terms[i]))
                                instanceHtml.append(joinRenaming.get(terms[i])).append(",");
                            else {
                                if (indexAttRenaming.containsKey(atts[i])) {
                                    indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                    String newAttName = atts[i].getName() + indexAttRenaming.get(atts[i]).toString();
                                    Attribute newRenamedAtt =
                                            Attribute.create(String.class, newAttName);
                                    joinRenaming.put(terms[i], newRenamedAtt);
                                } else {
                                    indexAttRenaming.put(atts[i], 0);
                                    joinRenaming.put(terms[i], Attribute.create(String.class,
                                            atts[i] + indexAttRenaming.get(atts[i]).toString()));
                                }

                                //joinRenaming.put(terms[i], atts[i]);
                                instanceHtml.append(joinRenaming.get(terms[i])).append(",");
                            }
                        }
                    }
                    instanceHtml.setLength(instanceHtml.length() - 1);
                    instanceHtml.append(")<br><br>");
                }
            }
        }
        instanceHtml.append("</html>");

        // disclosed values and joins
        if (disclosedValues.length() > 1)
            disclosedValues.setLength(disclosedValues.length() - 2);

        try {
            textArea.setText(instanceHtml.toString());
            disclosedJoinsText.setText(disclosedJoins.toString());
            disclosedValuesText.setText(disclosedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private void printDisclosureInTextAreaWithColors(JTextPane textMappingDisclosure,
                                                     Set<Dependency> dependenciesSet,
                                                     Map<Dependency,Set<Predicate>> hiddenPredicates,
                                                     Map<Dependency, Map<Atom, Set<Variable>>> newFrontier,
                                                     Map<Dependency, Map<Atom, Set<Variable>>> newNonFrontier) {

        StringBuilder unwantedValues = new StringBuilder();
        StringBuilder unwantedJoins = new StringBuilder();

        StringBuilder instanceHtml = new StringBuilder();
        instanceHtml.append("<html><h2>");

        StringBuilder disclosedValues = new StringBuilder();
        StringBuilder disclosedJoins = new StringBuilder();

        for (Dependency dep : dependenciesSet) {
            for (Atom at : dep.getBody().getAtoms()) {
                if (hiddenPredicates.containsKey(dep) && hiddenPredicates.get(dep).contains(at.getPredicate())) {
                    instanceHtml.append("<font color=red>").append(at.toString()).append("<br></font>");
                } else {
                    instanceHtml.append(at.getPredicate().getName()).append("(");

                    for (Variable var : at.getFreeVariables()) {
                        if (newFrontier.containsKey(dep)
                                && newFrontier.get(dep).containsKey(at)
                                && newFrontier.get(dep).get(at).contains(var)) {
                            instanceHtml.append("<font color=red>").append(var).append("</font>,");
                            disclosedValues.append(var).append(",");
                        }else if (newNonFrontier.containsKey(dep)
                                && newNonFrontier.get(dep).containsKey(at)
                                && newNonFrontier.get(dep).get(at).contains(var)) {
                            instanceHtml.append("<font color=orange>").append(var).append("</font>,");
                            disclosedJoins.append(var).append(",");
                        } else {
                            instanceHtml.append(var).append(",");
                        }
                    }
                }
                instanceHtml.setLength(instanceHtml.length() - 1);
                instanceHtml.append(")<br>");
            }
        }

        instanceHtml.append("</html>");

        try {
            textMappingDisclosure.setText(instanceHtml.toString());
            textUnwantedJoins.setText(unwantedJoins.toString());
            textUnwantedValues.setText(unwantedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    private void printDisclosureInTextAreaWithColors(JTextPane textArea) {
        Map<Term, Integer> nbOccVars = new HashMap<>();
        Map<Term, Attribute> joinRenaming = new HashMap<>();
        Map<Attribute, Integer> indexAttRenaming = new HashMap<>();
        StringBuilder instanceHtml = new StringBuilder();
        instanceHtml.append("<html><h2>");

        StringBuilder unwantedValues = new StringBuilder();
        StringBuilder unwantedJoins = new StringBuilder();

        Multimap<String, Variable> constantToVariables = HashMultimap.create();
        Map<String, String> variableToConstant = new HashMap<>();
        List<Match> matches = new SafeRewriting(this.mappingToRewrite).getMorphism(this.rewrittenMapping);
        for (Match match : matches) {
            for (Variable var : match.getMapping().keySet()) {
                constantToVariables.put(match.getMapping().get(var).toString(), var);
                variableToConstant.put(var.getSymbol(), match.getMapping().get(var).toString());
            }
        }

        // instance to transform
        Set<Atom> instance = new SafeRewriting(this.rewrittenMapping).getSimplifiedInstanceRef();

        // html instance
        for (Atom at : instance) {
            for (Relation rel : rewrittenMapping.getRelations()) {
                if (at.getPredicate().getName().toString().equals(rel.getName().toString())) {
                    Term[] terms = at.getTerms();
                    Attribute[] atts = rel.getAttributes();
                    instanceHtml.append(at.getPredicate().getName()).append("(");
                    for (int i = 0; i < atts.length; i++) {
                        if (terms[i].toString().equals("*")) {
                            instanceHtml.append(atts[i]).append(",");
                        } else if (!terms[i].toString().equals("*")
                                && variableToConstant.containsKey(terms[i].toString())
                                && variableToConstant.get(terms[i].toString()).equals("*")) {
                            instanceHtml.append("<font color=red>").append(atts[i]).append("</font>,");
                            unwantedValues.append(atts[i]).append(", ");
                        } else if (constantToVariables.get(variableToConstant.get(terms[i].toString())).size() > 1) {
                            if (joinRenaming.containsKey(terms[i]))
                                instanceHtml.append("<font color=orange>").append(joinRenaming.get(terms[i])).append("</font>,");
                            else {
                                if (indexAttRenaming.containsKey(atts[i])) {
                                    indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                    String newAttName = atts[i].getName() + "_" + terms[i].toString();
                                    Attribute newRenamedAtt =
                                            Attribute.create(String.class, newAttName);
                                    joinRenaming.put(terms[i], newRenamedAtt);
                                } else {
                                    indexAttRenaming.put(atts[i], 0);
                                    joinRenaming.put(terms[i], Attribute.create(String.class, atts[i] + "_" + terms[i].toString()));
                                }

                                instanceHtml.append("<font color=orange>").append(joinRenaming.get(terms[i])).append("</font>,");
                                unwantedJoins.append(joinRenaming.get(terms[i])).append(", ");
                            }
                        } else {
                            if (indexAttRenaming.containsKey(atts[i])) {
                                indexAttRenaming.put(atts[i], indexAttRenaming.get(atts[i]) + 1);
                                String newAttName = atts[i].getName() + "_" + terms[i].toString();
                                Attribute newRenamedAtt =
                                        Attribute.create(String.class, newAttName);
                                joinRenaming.put(terms[i], newRenamedAtt);
                            } else {
                                indexAttRenaming.put(atts[i], 0);
                                joinRenaming.put(terms[i], Attribute.create(String.class,
                                        atts[i] + "_" + terms[i].toString()));
                            }

                            instanceHtml.append(joinRenaming.get(terms[i])).append(",");
                        }
                    }
                    instanceHtml.setLength(instanceHtml.length() - 1);
                    instanceHtml.append(")<br>");
                }
            }
        }
        instanceHtml.append("</html>");

        // disclosed values and joins
        if (unwantedValues.length() > 1)
            unwantedValues.setLength(unwantedValues.length() - 2);

        try {
            textMappingDisclosure.setText(instanceHtml.toString());
            textUnwantedJoins.setText(unwantedJoins.toString());
            textUnwantedValues.setText(unwantedValues.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File openFilePath() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showOpenDialog(null);

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

    private void printBagsGraph(ProvenanceGraph provenanceGraph) {
        BagsGraphUI bagsGraphUI = BagsGraphUI.displayBagsGraph(provenanceGraph);
        bagsGraphUI.updateGraphVisu();
    }

    private void callNewViewPanel() {
        EditDependency editDependency = new EditDependency("view", this);
    }

    private void callNewTgdPanel() {
        EditDependency editDependency = new EditDependency("tgd", this);
    }

    private void callEditViewPanel(Dependency oldDependency) {
        EditDependency editDependency = new EditDependency("view", oldDependency, this);
    }

    private void callEditTgdPanel(Dependency oldDependency) {
        EditDependency editDependency = new EditDependency("tgd", oldDependency, this);
    }

    public void addNewView(Dependency newView) {
        if (newView != null) {
            Set<Dependency> newDependencies = new HashSet<>();
            Set<Relation> newRelations = new HashSet<>();

            // old dependencies
            for (Dependency dep : policySchemaViewTab.getAllDependencies()) {
                newDependencies.add(dep);
            }
            newRelations.addAll(Arrays.asList(policySchemaViewTab.getRelations()));

            // new dependency
            newDependencies.add(newView);
            newRelations.addAll(newView.getRelations());

            policySchemaViewTab = new Schema(newRelations.toArray(new Relation[0]), newDependencies.toArray(new Dependency[0]));
            refreshViewTab();
        }
    }

    public void addNewTgd(Dependency newTgd) {
        if (newTgd != null) {
            Set<Dependency> newDependencies = new HashSet<>();
            Set<Relation> newRelations = new HashSet<>();

            // old dependencies
            for (Dependency dep : mappingToRewrite.getAllDependencies()) {
                newDependencies.add(dep);
            }
            newRelations.addAll(Arrays.asList(mappingToRewrite.getRelations()));

            // new dependency
            newDependencies.add(newTgd);
            newRelations.addAll(newTgd.getRelations());

            mappingToRewrite = new Schema(newRelations.toArray(new Relation[0]), newDependencies.toArray(new Dependency[0]));
            refreshMappingTab();
        }
    }

    public void editView(Dependency oldDependency, Dependency newView) {
        if (newView != null) {
            Set<Dependency> newDependencies = new HashSet<>();
            Set<Relation> newRelations = new HashSet<>();

            // old dependencies
            for (Dependency dep : policySchemaViewTab.getAllDependencies()) {
                if (dep != nameToDepViews.get(viewsListArea.getSelectedValue()))
                    newDependencies.add(dep);
            }
            newRelations.addAll(Arrays.asList(policySchemaViewTab.getRelations()));

            // new dependency
            newDependencies.add(newView);
            newRelations.addAll(newView.getRelations());

            policySchemaViewTab = new Schema(newRelations.toArray(new Relation[0]), newDependencies.toArray(new Dependency[0]));
            refreshViewTab();
        }
    }

    public void editTgd(Dependency oldDependency, Dependency newTgd) {
        if (newTgd != null) {
            Set<Dependency> newDependencies = new HashSet<>();
            Set<Relation> newRelations = new HashSet<>();

            // old dependencies
            for (Dependency dep : mappingToRewrite.getAllDependencies()) {
                if (dep != nameToDepMapping.get(mappingListArea.getSelectedValue()))
                newDependencies.add(dep);
            }
            newRelations.addAll(Arrays.asList(mappingToRewrite.getRelations()));

            // new dependency
            newDependencies.add(newTgd);
            newRelations.addAll(newTgd.getRelations());

            mappingToRewrite = new Schema(newRelations.toArray(new Relation[0]), newDependencies.toArray(new Dependency[0]));
            refreshMappingTab();
        }
    }
}
