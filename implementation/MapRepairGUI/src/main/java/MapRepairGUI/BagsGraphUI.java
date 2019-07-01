package MapRepairGUI;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import util.Config;
import util.ConfigID;
import util.ProvenanceGraph;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

public class BagsGraphUI {
    private ProvenanceGraph provenanceGraph;

    private static JFrame frame;

    private JPanel jpanelGraph;
    private JPanel jpanelVisu;
    private JPanel jpanelBags;
    private JList bagsJList;
    private JList listBagsFocus;
    private Viewer viewer;
    private ViewPanel viewPanel;

    public ProvenanceGraph getProvenanceGraph() {
        return provenanceGraph;
    }

    public void setProvenanceGraph(ProvenanceGraph provenanceGraph) {
        this.provenanceGraph = provenanceGraph;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static void setFrame(JFrame frame) {
        BagsGraphUI.frame = frame;
    }

    public JPanel getJpanelGraph() {
        return jpanelGraph;
    }

    public void setJpanelGraph(JPanel jpanelGraph) {
        this.jpanelGraph = jpanelGraph;
    }

    public JPanel getJpanelVisu() {
        return jpanelVisu;
    }

    public void setJpanelVisu(JPanel jpanelVisu) {
        this.jpanelVisu = jpanelVisu;
    }

    public JPanel getJpanelBags() {
        return jpanelBags;
    }

    public void setJpanelBags(JPanel jpanelBags) {
        this.jpanelBags = jpanelBags;
    }

    public JList getBagsJList() {
        return bagsJList;
    }

    public void setBagsJList(JList bagsJList) {
        this.bagsJList = bagsJList;
    }

    public JList getListBagsFocus() {
        return listBagsFocus;
    }

    public void setListBagsFocus(JList listBagsFocus) {
        this.listBagsFocus = listBagsFocus;
    }

    public Viewer getViewer() {
        return viewer;
    }

    public void setViewer(Viewer viewer) {
        this.viewer = viewer;
    }

    public ViewPanel getViewPanel() {
        return viewPanel;
    }

    public void setViewPanel(ViewPanel viewPanel) {
        this.viewPanel = viewPanel;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Map<String, Integer> getBagsList() {
        return bagsList;
    }

    public void setBagsList(Map<String, Integer> bagsList) {
        this.bagsList = bagsList;
    }

    public Map<String, ConfigID> getBagsTuplesToConfig() {
        return bagsTuplesToConfig;
    }

    public void setBagsTuplesToConfig(Map<String, ConfigID> bagsTuplesToConfig) {
        this.bagsTuplesToConfig = bagsTuplesToConfig;
    }

    public Map<ConfigID, Integer> getConfigIDToIdBag() {
        return configIDToIdBag;
    }

    public void setConfigIDToIdBag(Map<ConfigID, Integer> configIDToIdBag) {
        this.configIDToIdBag = configIDToIdBag;
    }

    public Set<String> getListBagsInBagFocus() {
        return listBagsInBagFocus;
    }

    public void setListBagsInBagFocus(Set<String> listBagsInBagFocus) {
        this.listBagsInBagFocus = listBagsInBagFocus;
    }

    public GraphMouseManager getGraphMouseManager() {
        return graphMouseManager;
    }

    public void setGraphMouseManager(GraphMouseManager graphMouseManager) {
        this.graphMouseManager = graphMouseManager;
    }

    private Graph graph;

    private Map<String, Integer> bagsList = new HashMap<>();
    private Map<String, ConfigID> bagsTuplesToConfig = new HashMap<>();
    private Map<ConfigID, Integer> configIDToIdBag = new HashMap<>();
    private Map<Integer, ConfigID> idBagToConfigID = new HashMap<>();

    private Set<String> listBagsInBagFocus = new HashSet<>();

    private GraphMouseManager graphMouseManager = new GraphMouseManager();

    public BagsGraphUI(ProvenanceGraph provGraph) {
        this.provenanceGraph = provGraph;
        jpanelVisu.setLayout(new GridLayout());

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph = new SingleGraph("Bags", false, true);

        final Map<Integer, Map<Integer, Config>> provenance = provenanceGraph.getProvenance();

        // add nodes
        int idBag = 0;

        for (int i : provenance.keySet()) {
            Map<Integer, Config> mapTmp = provenance.get(i);
            for (int j : mapTmp.keySet()) {
                StringBuilder bagString = new StringBuilder("<html><h2>");
                bagString.append(Integer.toString(idBag))
                        .append(": ")
                        .append(mapTmp.get(j).getNewFacts().toString())
                        .append("</html>");
                bagsList.put(bagString.toString(), idBag);
                bagsTuplesToConfig.put(bagString.toString(), mapTmp.get(j).getConfigID());
                configIDToIdBag.put(mapTmp.get(j).getConfigID(), idBag);
                idBagToConfigID.put(idBag, mapTmp.get(j).getConfigID());
                idBag++;
            }
        }

        graph.addAttribute("layout.weight", 5);
        graph.addAttribute("layout.quality", 4);
        graph.addAttribute("layout.force", 1);

        jpanelGraph.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                updateGraphVisu();
            }
        });

        ArrayList<String> bagsToPrint = new ArrayList<String>(this.bagsList.keySet());
        Collections.sort(bagsToPrint, (s1, s2) -> compareBagsOrder(s1, s2));

        bagsJList.setListData(bagsToPrint.toArray());

        bagsJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                printBagGraph(bagsJList, listSelectionEvent, graph);
            }
        });
        listBagsFocus.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                printBagGraph(listBagsFocus, listSelectionEvent, graph);
            }
        });

    }

    private int compareBagsOrder(String bag1, String bag2) {
        String[] splitBag1 = bag1.split(":");
        int numBag1 = Integer.valueOf(splitBag1[0].replace("<html><h2>",""));

        String[] splitBag2 = bag2.split(":");
        int numBag2 = Integer.valueOf(splitBag2[0].replace("<html><h2>",""));

        if (numBag1 < numBag2)
            return -1;
        else if (numBag1 > numBag2)
            return 1;
        else
            return 0;

    }

    private void printBagGraph(JList selectedList, ListSelectionEvent listSelectionEvent, Graph graph) {
        Map<Integer, Map<Integer, Config>> provenance = this.provenanceGraph.getProvenance();

        // get choosen bag
        String selectedValue = selectedList.getSelectedValue().toString();
        ConfigID bagConfID = this.bagsTuplesToConfig.get(selectedValue);
        int nodeLabel = this.bagsList.get(selectedValue);
        Config bagConf = this.provenanceGraph.get(bagConfID);

        // clean graph and focus list
        this.graph.clear();
        this.listBagsInBagFocus.clear();

        // add nodes
        addNewBagNode(bagConf, nodeLabel);
        addDescendants(bagConfID.toString(), bagConf);
        addAncestors(bagConfID.toString(), bagConf);

        ArrayList<String> bagsToPrint = new ArrayList<String>(this.listBagsInBagFocus);
        Collections.sort(bagsToPrint, (s1, s2) -> compareBagsOrder(s1, s2));

        this.listBagsFocus.setListData(bagsToPrint.toArray());

    }

    public void printBagGraphWithId(int nodeLabel) {
        Map<Integer, Map<Integer, Config>> provenance = this.provenanceGraph.getProvenance();

        // get choosen bag
        ConfigID bagConfID = this.idBagToConfigID.get(nodeLabel);
        Config bagConf = this.provenanceGraph.get(bagConfID);

        // clean graph and focus list
        this.graph.clear();
        this.listBagsInBagFocus.clear();

        // add nodes
        addNewBagNode(bagConf, nodeLabel);
        addDescendants(bagConfID.toString(), bagConf);
        addAncestors(bagConfID.toString(), bagConf);
        this.listBagsFocus.setListData(this.listBagsInBagFocus.toArray());

    }

    private void addDescendants(String ancestor, Config bagConf) {
        for (ConfigID confID : bagConf.getDescendantConfigSet()) {
            Config newConfig = this.provenanceGraph.get(confID);
            addNewBagNode(newConfig, this.configIDToIdBag.get(newConfig.getConfigID()));
            this.graph.addEdge(ancestor + newConfig.getConfigID().toString(),
                    ancestor,
                    newConfig.getConfigID().toString(),
                    true);
            if (!newConfig.getDescendantConfigSet().isEmpty())
                addDescendants(newConfig.getConfigID().toString(), newConfig);
        }
    }

    private void addAncestors(String descendant, Config bagConf) {
        ConfigID confID = bagConf.getConfigID();
        if (confID.getI() > 0) {
            for (Config newConfig : this.provenanceGraph.getProvenance().get(confID.getI() - 1).values()) {
                if (newConfig.getDescendantConfigSet().contains(bagConf.getConfigID())) {
                    addNewBagNode(newConfig, this.configIDToIdBag.get(newConfig.getConfigID()));
                    this.graph.addEdge(newConfig.getConfigID().toString() + descendant,
                            newConfig.getConfigID().toString(),
                            descendant,
                            true);
                    addAncestors(newConfig.getConfigID().toString(), newConfig);
                }
            }
        }
    }

    private void addNewBagNode(Config config, int nodeLabel) {

        ConfigID confID = config.getConfigID();

        // initialization for bag to list focus
        StringBuilder bagString = new StringBuilder("<html><h2>");
        bagString.append(nodeLabel)
                .append(": ")
                .append(this.provenanceGraph.get(config.getConfigID()).getNewFacts().toString())
                .append("</html");
        this.listBagsInBagFocus.add(bagString.toString());

        // add bag to graph
        this.graph.addNode(confID.toString());
        Node currentNode = this.graph.getNode(confID.toString());

        currentNode.addAttribute("ui.label", Integer.toString(nodeLabel));
        currentNode.setAttribute("xy", confID.getI(), confID.getJ());

        if (config.getDescendantConfigSet().isEmpty())
            currentNode.addAttribute("ui.style", "fill-color: green;size: 45px;text-size: 15px;" +
                    " text-mode: normal;" +
                    // " text-background-color: blue;"+
                    " text-background-mode: rounded-box;" +
                    " text-visibility-mode: normal;" +
                    " text-color: black;" +
                    " text-style: bold;");
        else
            currentNode.addAttribute("ui.style", "fill-color: yellow;size: 45px;text-size: 15px;" +
                    " text-mode: normal;" +
                    // " text-background-color: blue;"+
                    " text-background-mode: rounded-box;" +
                    " text-visibility-mode: normal;" +
                    " text-color: black;" +
                    " text-style: bold;");
    }

    public void updateGraphVisu() {

        if (jpanelVisu.getComponentCount() > 0)
            jpanelVisu.removeAll();

        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();

        viewPanel = viewer.addDefaultView(false);
        viewPanel.setSize(jpanelVisu.getSize());
        viewPanel.doLayout();
        viewPanel.setMouseManager(this.graphMouseManager);
        this.graphMouseManager.setBagsGraphUI(this);

        jpanelVisu.add(viewPanel);

    }

    public static BagsGraphUI displayBagsGraph(ProvenanceGraph provGraph) {
        BagsGraphUI bagGraphUI = new BagsGraphUI(provGraph);
        frame = new JFrame("Bags");
        frame.setContentPane(bagGraphUI.jpanelGraph);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        return bagGraphUI;
    }
}
