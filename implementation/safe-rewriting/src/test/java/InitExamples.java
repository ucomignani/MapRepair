import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Variable;

public class InitExamples {

    public static Schema initMappingTestHidePredicate1(){
        //Create the relations of the schema
        Relation R = Relation.create("R",
                new Attribute[]{Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
        Relation S = Relation.create("S",
                new Attribute[]{Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
        Relation V = Relation.create("V",
                new Attribute[]{Attribute.create(String.class, "attribute2")});
        Relation r[] = new Relation[]{R, S, V};

        //Create the dependencies of the schema
        Dependency d[] = new Dependency[]{
                TGD.create(new Atom[]{Atom.create(R, Variable.create("x"), Variable.create("y")),
                                Atom.create(S, Variable.create("y"), Variable.create("z"))
                        },
                        new Atom[]{Atom.create(V, Variable.create("x"))}),
        };

        //Return the actual schema object
        return new Schema(r, d);
    }

    public static Schema initPolicyViewsTestHidePredicate1() {
        //Create the relations of the schema
        Relation R = Relation.create("R",
                new Attribute[]{Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
        Relation V = Relation.create("V",
                new Attribute[]{Attribute.create(String.class, "attribute2")});
        Relation r[] = new Relation[]{R, V};

        //Create the dependencies of the schema
        Dependency d[] = new Dependency[]{
                TGD.create(new Atom[]{Atom.create(R, Variable.create("x"), Variable.create("y")),
                        },
                        new Atom[]{Atom.create(V, Variable.create("x"))}),
        };

        //Return the actual schema object
        return new Schema(r, d);
    }

    public static Schema initMappingTestBreakNullJoins1() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };

        TGD tgd = TGD.create(new Atom[] { Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("x")),
                        Atom.create(S, Variable.create("z"), Variable.create("z"), Variable.create("z")),
                        Atom.create(S, Variable.create("y"), Variable.create("y"), Variable.create("x")),
                },
                new Atom[] { Atom.create(V, Variable.create("x"), Variable.create("z")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initPolicyViewsTestBreakNullJoins1() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };

        TGD tgd = TGD.create(new Atom[] { Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z")),
                        Atom.create(S, Variable.create("z"), Variable.create("y"), Variable.create("v")),
                        Atom.create(S, Variable.create("u"), Variable.create("u"), Variable.create("t")),
                },
                new Atom[] { Atom.create(V, Variable.create("x"), Variable.create("v")) });

        return new Schema(r, new Dependency[]{ tgd });

    }
    public static Schema initMappingTestBreakNullJoins2() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };


        /** letter c is used to connect components, other letters should occurs once
         *  c1 -> exported so does not need to be modified
         *  c2,c3 -> big connected component. S.c3-T.c3 need to be broken
         *  c4 -> self join that need to be broken
         */
        TGD tgd = TGD.create(new Atom[] { Atom.create(R, Variable.create("c1"), Variable.create("a")),
                        Atom.create(S, Variable.create("c1"), Variable.create("b"), Variable.create("d")),
                        Atom.create(R, Variable.create("e"), Variable.create("f")),
                        Atom.create(R, Variable.create("c2"), Variable.create("j")),
                        Atom.create(S, Variable.create("c2"), Variable.create("c3"), Variable.create("k")),
                        Atom.create(T, Variable.create("c3"), Variable.create("l")),
                        Atom.create(T, Variable.create("c4"), Variable.create("c4")),
                },
                new Atom[] { Atom.create(V, Variable.create("c1"), Variable.create("m")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initPolicyViewsTestBreakNullJoins2() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };


        /**
         * letter c is used to connect components, other letters should occurs once.
         * difference with initMappingTestBreakNullJoins1 is made on c4, which broke the possible morphism.
         * */
        TGD tgd = TGD.create(new Atom[] { Atom.create(R, Variable.create("c1"), Variable.create("a")),
                        Atom.create(S, Variable.create("c1"), Variable.create("b"), Variable.create("d")),
                        Atom.create(R, Variable.create("e"), Variable.create("f")),
                        Atom.create(R, Variable.create("c2"), Variable.create("j")),
                        Atom.create(S, Variable.create("c2"), Variable.create("c3"), Variable.create("k")),
                        Atom.create(T, Variable.create("c4"), Variable.create("l")),
                },
                new Atom[] { Atom.create(V, Variable.create("c1"), Variable.create("m")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initMappingTestBreakNullJoins3() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };

        TGD tgd = TGD.create(new Atom[] {Atom.create(R, Variable.create("x"), Variable.create("y")),
                        Atom.create(R, Variable.create("y"), Variable.create("z")),
                        Atom.create(R, Variable.create("z"), Variable.create("x")),
                },
                new Atom[] { Atom.create(V, Variable.create("x"), Variable.create("w")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initMappingTestBreakNullJoins4() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V1 = Relation.create("V1",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V1 };

        TGD tgd = TGD.create(new Atom[] {Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z")),
                        Atom.create(S, Variable.create("z"), Variable.create("u"), Variable.create("v")),
                        Atom.create(S, Variable.create("v"), Variable.create("w"), Variable.create("x")),
                        Atom.create(T, Variable.create("n"), Variable.create("n")),
                },
                new Atom[] { Atom.create(V1, Variable.create("y"), Variable.create("n")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initPolicyViewsTestBreakNullJoins4() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[] { Attribute.create(String.class, "a5"),Attribute.create(String.class, "a6")});
        Relation V = Relation.create("V",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, T, V };

        TGD tgd = TGD.create(new Atom[] {Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z")),
                        Atom.create(S, Variable.create("z"), Variable.create("u"), Variable.create("v")),
                        Atom.create(S, Variable.create("v"), Variable.create("w"), Variable.create("x")),
                        Atom.create(R, Variable.create("w"), Variable.create("w")),
                        Atom.create(T, Variable.create("n"), Variable.create("n")),
                },
                new Atom[] { Atom.create(V, Variable.create("y"), Variable.create("n")) });

        return new Schema(r, new Dependency[]{ tgd });

    }

    public static Schema initMappingTestBreakNullJoins5() {
        Relation R = Relation.create("R",
                new Attribute[] { Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[] { Attribute.create(String.class, "a2"),Attribute.create(String.class, "a3"),Attribute.create(String.class, "a4")});
        Relation V1 = Relation.create("V1",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation V2 = Relation.create("V2",
                new Attribute[] { Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[] { R, S, V1, V2 };

        TGD tgd = TGD.create(new Atom[] {Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z"))},
                           new Atom[] { Atom.create(V1, Variable.create("x"), Variable.create("z"), Variable.create("u")) });

        TGD tgd2 = TGD.create(new Atom[] {Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z"))},
            new Atom[]{Atom.create(V2, Variable.create("x"), Variable.create("y"))});


        return new Schema(r, new Dependency[]{ tgd, tgd2 });

    }

    public static Schema initPolicyViewsTestBreakNullJoins5() {
            Relation R = Relation.create("R",
                    new Attribute[]{Attribute.create(String.class, "a0"), Attribute.create(String.class, "a1")});
        Relation S = Relation.create("S",
                new Attribute[]{Attribute.create(String.class, "a2"), Attribute.create(String.class, "a3"), Attribute.create(String.class, "a4")});
        Relation T = Relation.create("T",
                new Attribute[]{Attribute.create(String.class, "a2"), Attribute.create(String.class, "a3")});
        Relation V1 = Relation.create("V1",
                new Attribute[]{Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation V2 = Relation.create("V2",
                new Attribute[]{Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[]{R, S, T, V1, V2};

            TGD tgd = TGD.create(new Atom[]{Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("z")),
                            Atom.create(T, Variable.create("z"), Variable.create("v"))},
                    new Atom[]{Atom.create(V1, Variable.create("x"), Variable.create("y"), Variable.create("z"))});

            TGD tgd2 = TGD.create(new Atom[]{Atom.create(S, Variable.create("x"), Variable.create("y"), Variable.create("y")),
                            Atom.create(R, Variable.create("y"), Variable.create("v"))},
                    new Atom[]{Atom.create(V2, Variable.create("x"), Variable.create("z"))});

            return new Schema(r, new Dependency[]{tgd, tgd2});
        }
    }

