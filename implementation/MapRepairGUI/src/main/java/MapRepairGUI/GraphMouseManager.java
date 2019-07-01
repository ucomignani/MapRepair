/**
 * based on https://stackoverflow.com/questions/16671143/retrieving-mouse-clicks-in-graphstream
 */
package MapRepairGUI;

import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.MouseManager;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class GraphMouseManager implements MouseManager {


    /**
     * The view this manager operates upon.
     */
    protected View view;


    /**
     * The graph to modify according to the view actions.
     */
    protected GraphicGraph graph;


    protected GraphicElement element;

    protected BagsGraphUI bagsGraphUI;

    @Override
    public void init(GraphicGraph gg, View view) {
        this.graph = gg;
        this.view = view;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }


    public void setBagsGraphUI(BagsGraphUI bagsGraphUI) {
        this.bagsGraphUI = bagsGraphUI;
    }

    @Override
    public void release() {
        view.removeMouseListener(this);
        view.removeMouseMotionListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        element = view.findNodeOrSpriteAt(me.getX(), me.getY());
        this.bagsGraphUI.printBagGraphWithId(Integer.valueOf(element.label));
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }
}
