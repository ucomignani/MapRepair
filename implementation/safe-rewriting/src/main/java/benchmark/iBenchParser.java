package benchmark;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class iBenchParser {

    public static Schema importSchema(File sourceSchemaFile, File targetSchemaFile, File mappingFile) {

        Document sourceDocument = parseXML(sourceSchemaFile);
        Document targetgDocument = parseXML(targetSchemaFile);
        Document mappingDocument = parseXML(mappingFile);

        Set<Relation> relations = parseSchema(sourceDocument);
        relations.addAll(parseSchema(targetgDocument));

        Map<String, Atom> atomMap = getAtomMap(relations);
        Set<Dependency> mapping = parseMapping(mappingDocument, atomMap);

        return new Schema(relations.toArray(new Relation[0]), mapping.toArray(new Dependency[0]));
    }

    private static Map<String, Atom> getAtomMap(Set<Relation> relations) {

        Map<String, Atom> nameToAtom = new HashMap<>();

        for (Relation relation : relations) {

            Attribute[] attributes = relation.getAttributes();
            Variable[] vars = new Variable[relation.getArity()];
            for (int i = 0; i < relation.getArity(); i++)
                vars[i] = Variable.create(attributes[i].getName());

            Atom newAtom = Atom.create(relation, vars);

            nameToAtom.put(relation.getName(), newAtom);
        }

        return nameToAtom;
    }

    private static Document parseXML(File mappingFile) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = null;
        Document document = null;


        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(mappingFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    private static Set<Relation> parseSchema(Document schemaDocument) {

        Set<Relation> output = new HashSet<>();

        // get the sequence
        NodeList schema = schemaDocument.getElementsByTagName("xs:schema").item(0).getChildNodes()
                .item(1).getChildNodes().item(1).getChildNodes().item(1).getChildNodes();

        // explore relations
        for (int i = 0; i < schema.getLength(); i++) {
            if (schema.item(i) != null && schema.item(i).getNodeName().equals("xs:element")) {

                // get attributes
                NodeList attributes = schema.item(i).getChildNodes().item(1).getChildNodes().item(1).getChildNodes();
                List<Attribute> attributeList = new ArrayList<>();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attribute = attributes.item(j);
                    if (attribute.getNodeName().equals("xs:element"))
                        attributeList.add(Attribute.create(String.class, attribute.getAttributes().getNamedItem("name").getNodeValue()));

                }
                output.add(Relation.create(
                        schema.item(i).getAttributes().getNamedItem("name").getNodeValue(), attributeList.toArray(new Attribute[0])));
            }
        }

        return output;
    }

    private static Set<Dependency> parseMapping(Document mappingDocument, Map<String, Atom> relationsToAtom) {
        Set<Dependency> parsedMapping = new HashSet<>();

        NodeList mappings = mappingDocument.getElementsByTagName("logicalMapping");
        for (int i = 0; i < mappings.getLength(); i++) {
            Set<String> bodyAtomsNames = new HashSet<>();
            Set<String> headAtomsNames = new HashSet<>();
            Set<VariableEquality> equalities = new HashSet<>();

            NodeList childs = mappings.item(i).getChildNodes();
            for (int j = 0; j < childs.getLength(); j++) {

                Node child = childs.item(j);

                if (child.getNodeName().equals("source")) {
                    bodyAtomsNames.addAll(parseSourceTarget(child));
                    equalities.addAll(parseSourceForPredicate(child));

                } else if (child.getNodeName().equals("target")) {
                    headAtomsNames.addAll(parseSourceTarget(child));

                } else if (child.getNodeName().equals("mapping")) {
                    equalities.addAll(parseMapping(child));

                }

            }

            Dependency newTgd = createTgd(bodyAtomsNames, headAtomsNames, equalities, relationsToAtom);
            parsedMapping.add(newTgd);
        }
        return parsedMapping;
    }

    private static TGD createTgd(Set<String> bodyAtomsNames, Set<String> headAtomsNames, Set<VariableEquality> equalities, Map<String, Atom> relationsToAtom) {
        Set<Atom> bodyAtomSet = new HashSet<>();
        for (String atomName : bodyAtomsNames) {
            Atom atom = relationsToAtom.get(atomName);
            bodyAtomSet.add(Atom.create(atom.getPredicate(), atom.getTerms()));
        }

        Set<Atom> headAtomSet = new HashSet<>();
        for (String atomName : headAtomsNames) {
            Atom atom = relationsToAtom.get(atomName);
            headAtomSet.add(Atom.create(atom.getPredicate(), atom.getTerms()));
        }

        for (VariableEquality equality : equalities) {
            Set<Atom> bodyAtomToReplace = new HashSet<>();
            Set<Atom> newBodyAtoms = new HashSet<>();

            Set<Atom> headAtomToReplace = new HashSet<>();
            Set<Atom> newHeadAtoms = new HashSet<>();

            for (Atom at : bodyAtomSet) {
                if (at.getPredicate().getName().equals(equality.getRelationPrimeName())) {
                    bodyAtomToReplace.add(at);
                    newBodyAtoms.add(renameVar(at, equality.getRelationPrimeVar(), equality.getRelationVar()));
                }
            }
            bodyAtomSet.removeAll(bodyAtomToReplace);
            bodyAtomSet.addAll(newBodyAtoms);

            for (Atom at : headAtomSet) {
                if (at.getPredicate().getName().equals(equality.getRelationPrimeName())) {
                    headAtomToReplace.add(at);
                    newHeadAtoms.add(renameVar(at, equality.getRelationPrimeVar(), equality.getRelationVar()));
                }
            }
            headAtomSet.removeAll(headAtomToReplace);
            headAtomSet.addAll(newHeadAtoms);

        }

        return TGD.create(bodyAtomSet.toArray(new Atom[0]), headAtomSet.toArray(new Atom[0]));
    }

    private static Atom renameVar(Atom at, Variable relationPrimeVar, Variable relationVar) {
        Term[] newTerms = at.getTerms();
        for (int i = 0; i < newTerms.length; i++) {
            if (newTerms[i].equals(relationPrimeVar))
                newTerms[i] = relationVar;
        }

        return Atom.create(at.getPredicate(), newTerms);
    }

    private static Set<VariableEquality> parseMapping(Node node) {
        Set<VariableEquality> equalities = new HashSet<>();

        String[] predicates = node.getTextContent().split(" AND ");

        splitEqualities(equalities, predicates);

        return equalities;
    }

    private static Set<VariableEquality> parseSourceForPredicate(Node node) {
        NodeList childs = node.getChildNodes();
        Set<VariableEquality> equalities = new HashSet<>();

        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeName().equals("predicate")) {
                String[] predicates = child.getTextContent().split(" AND ");

                splitEqualities(equalities, predicates);
            }
        }

        return equalities;
    }

    private static void splitEqualities(Set<VariableEquality> equalities, String[] predicates) {
        for (int j = 0; j < predicates.length; j++) {
            String[] eq = predicates[j].split(" = ");
            String leftEq = eq[0];
            String rightEq = eq[1];

            String relLeft = leftEq.split("/")[0].substring(3);
            String varLeft = leftEq.split("/")[1];

            String relRight = rightEq.split("/")[0].substring(3);
            String varRight = rightEq.split("/")[1];

            VariableEquality newEq = new VariableEquality(relLeft, Variable.create(varLeft), relRight, Variable.create(varRight));
            equalities.add(newEq);
        }
    }

    private static Set<String> parseSourceTarget(Node node) {
        NodeList childs = node.getChildNodes();
        Set<String> atomsNames = new HashSet<>();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeName().equals("entity")) {
                atomsNames.add(child.getAttributes().getNamedItem("name").getNodeValue().substring(2));
            }
        }

        return atomsNames;
    }

}