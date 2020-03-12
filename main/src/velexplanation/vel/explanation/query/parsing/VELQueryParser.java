/* Generated By:JavaCC: Do not edit this line. VELQueryParser.java */
/* Insert the default contents of the file here.  May need imoprts etc.
 */
package velexplanation.vel.explanation.query.parsing;

import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import rabinizer.bdd.*;
import rabinizer.formulas.*;
import velexplanation.actions.ObjectParameterizedActionType;
import velexplanation.formula.ParamPlaceholder;
import velexplanation.vel.explanation.query.VELQuery;

import java.io.StringReader;
import java.util.*;

public class VELQueryParser implements VELQueryParserConstants {
    static private int[] jj_la1_0;

    static {
        jj_la1_init_0();
    }

    final private int[] jj_la1 = new int[0];
    final private JJCalls[] jj_2_rtns = new JJCalls[23];
    final private LookaheadSuccess jj_ls = new LookaheadSuccess();
    /**
     * Generated Token Manager.
     */
    public VELQueryParserTokenManager token_source;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    protected Map<String, List<String>> props;
    protected Map<String, List<String>> objs;
    protected Map<String, String> objectiveParams;
    protected Set<String> specificObjects;
    protected SADomain d = null;
    protected Globals globals;
    SimpleCharStream jj_input_stream;
    private int jj_ntk;
    private Token jj_scanpos, jj_lastpos;
    private int jj_la;
    private int jj_gen;
    private boolean jj_rescan = false;
    private int jj_gc = 0;
    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
    private int[] jj_expentry;
    private int jj_kind = -1;
    private int[] jj_lasttokens = new int[100];
    private int jj_endpos;

    public VELQueryParser(Globals globals, java.io.Reader reader) {
        this(reader);
        this.globals = globals;
    }

    /**
     * Constructor with InputStream.
     */
    public VELQueryParser(java.io.InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    public VELQueryParser(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new VELQueryParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Constructor.
     */
    public VELQueryParser(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new VELQueryParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Constructor with generated Token Manager.
     */
    public VELQueryParser(VELQueryParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    public static VELQuery parseFromText(String text, SADomain d, State s) throws ParseException {
        Globals globals = new Globals();
        globals.bddForFormulae = new BDDForFormulae();
        globals.bddForVariables = new BDDForVariables();
        globals.vsBDD = new ValuationSetBDD(globals);
        globals.aV = new AllValuations();

        return new VELQueryParser(globals, new StringReader(text)).parse(d, s);
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{};
    }

    public VELQuery parse(SADomain d, State s) throws ParseException {
        this.d = d;
        this.props = new HashMap<String, List<String>>();
        this.specificObjects = new HashSet<String>();

        if (d instanceof OOSADomain) {
            for (PropositionalFunction pf : ((OOSADomain) d).propFunctions()) {
                props.put(pf.getName(), Arrays.asList(pf.getParameterClasses()));
            }
        }

        for (ActionType aT : d.getActionTypes()) {
            if (!(aT instanceof ObjectParameterizedActionType)) {
                props.put(aT.typeName(), new ArrayList<String>());
            } else {
                props.put(aT.typeName(), Arrays.asList(((ObjectParameterizedActionType) aT).getParameterClasses()));
            }
        }
        Map<String, List<ObjectInstance>> map;
        if (s instanceof OOState) {
            map = OOStateUtilities.objectsByClass((OOState) s);
        } else {
            map = new HashMap<String, List<ObjectInstance>>();
        }

        this.objs = new HashMap<String, List<String>>();
        for (String key : map.keySet()) {
            List<String> objects = new ArrayList<String>();
            for (ObjectInstance o : map.get(key)) {
                objects.add(o.name());
            }
            this.objs.put(key, objects);
        }

        globals.bddForVariables.bijectionIdAtom = new BijectionIdAtom();
        this.objectiveParams = new HashMap<String, String>();
        return singleQuery();
    }

    final public VELQuery singleQuery() throws ParseException {
        List<String> quantifiedVariables = new ArrayList<String>();
        List<Boolean> quantificationTypes = new ArrayList<Boolean>();
        Formula formula;
        objectiveParams.clear();
        String var;
        boolean whyQuery = false;
        if (jj_2_1(2)) {
            jj_consume_token(WHY);
            whyQuery = true;
        } else {
            ;
        }
        if (jj_2_5(2)) {
            label_1:
            while (true) {
                if (jj_2_2(2)) {
                    ;
                } else {
                    break label_1;
                }
                if (jj_2_3(2)) {
                    jj_consume_token(UNIV);
                    var = jj_consume_token(ID).image;
                    quantifiedVariables.add(var);
                    quantificationTypes.add(true);
                } else if (jj_2_4(2)) {
                    jj_consume_token(EXIS);
                    var = jj_consume_token(ID).image;
                    quantifiedVariables.add(var);
                    quantificationTypes.add(false);
                } else {
                    jj_consume_token(-1);
                    throw new ParseException();
                }
            }
            jj_consume_token(COL);
        } else {
            ;
        }
        formula = implication();
        jj_consume_token(QM);
        {
            if (true) return new VELQuery((OOSADomain) d, formula, quantifiedVariables,
                    quantificationTypes, whyQuery);
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula implication() throws ParseException {
        Formula r = null;
        Formula result;
        result = disjunction();
        if (jj_2_6(2)) {
            jj_consume_token(IMP);
            r = disjunction();
            result = new Implication(result, r);
        } else {
            ;
        }
        {
            if (true) return result;
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula disjunction() throws ParseException {
        Formula r = null;
        Formula result;
        result = conjunction();
        label_2:
        while (true) {
            if (jj_2_7(2)) {
                ;
            } else {
                break label_2;
            }
            jj_consume_token(OR);
            r = conjunction();
            result = new Disjunction(result, r);
        }
        {
            if (true) return result;
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula conjunction() throws ParseException {
        Formula result;
        Formula r = null;
        result = until();
        label_3:
        while (true) {
            if (jj_2_8(2)) {
                ;
            } else {
                break label_3;
            }
            jj_consume_token(AND);
            r = until();
            result = new Conjunction(result, r);
        }
        {
            if (true) return result;
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula until() throws ParseException {
        Formula result;
        Formula r = null;
        result = unaryOp();
        if (jj_2_12(2)) {
            if (jj_2_9(2)) {
                jj_consume_token(UOP);
                r = unaryOp();
                result = new UOperator(result, r);
            } else if (jj_2_10(2)) {
                jj_consume_token(VOP);
                r = unaryOp();
                result = new VOperator(result, r);
            } else if (jj_2_11(2)) {
                jj_consume_token(WOP);
                r = unaryOp();
                result = new VOperator(new Disjunction(new Negation(result), r),
                        new Disjunction(result, r));
            } else {
                jj_consume_token(-1);
                throw new ParseException();
            }
        } else {
            ;
        }
        {
            if (true) return result;
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula unaryOp() throws ParseException {
        Formula f;
        if (jj_2_13(2)) {
            jj_consume_token(FOP);
            f = unaryOp();
            {
                if (true) return new FOperator(f);
            }
        } else if (jj_2_14(2)) {
            jj_consume_token(GOP);
            f = unaryOp();
            {
                if (true) return new GOperator(f);
            }
        } else if (jj_2_15(2)) {
            jj_consume_token(XOP);
            f = unaryOp();
            {
                if (true) return new XOperator(f);
            }
        } else if (jj_2_16(2)) {
            jj_consume_token(NEG);
            f = unaryOp();
            {
                if (true) return new Negation(f);
            }
        } else if (jj_2_17(2)) {
            f = atom();
            {
                if (true) return f;
            }
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
        throw new Error("Missing return statement in function");
    }

    final public Formula atom() throws ParseException {
        String atomString;
        int id;
        Formula f;
        String nextParam;
        List<String> params = new ArrayList<String>();
        if (jj_2_20(2)) {
            jj_consume_token(TRUE);
            {
                if (true) return new BooleanConstant(true, globals);
            }
        } else if (jj_2_21(2)) {
            jj_consume_token(FALSE);
            {
                if (true) return new BooleanConstant(false, globals);
            }
        } else if (jj_2_22(2)) {
            atomString = jj_consume_token(ID).image;
            if (jj_2_19(2)) {
                jj_consume_token(LPAR);
                nextParam = jj_consume_token(ID).image;
                params.add(nextParam);
                label_4:
                while (true) {
                    if (jj_2_18(2)) {
                        ;
                    } else {
                        break label_4;
                    }
                    jj_consume_token(COM);
                    nextParam = jj_consume_token(ID).image;
                    params.add(nextParam);
                }
                jj_consume_token(RPAR);
            } else {
                ;
            }
            if (!props.containsKey(atomString)) {
                if (true) throw new ParseException("Can't read atom " + atomString);
            }
            if (params.size() != props.get(atomString).size()) {
                if (true) throw new ParseException();
            }
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                String cls = props.get(atomString).get(i);
                if (objs.get(cls).contains(param)) {
                    specificObjects.add(param);
                }
                if (objectiveParams.containsKey(param) && !objectiveParams.get(param).equals(cls)) {
                    if (true) throw new ParseException();
                } else if (!objectiveParams.containsKey(param)) {
                    objectiveParams.put(param, cls);
                }
            }
            if (params.isEmpty()) {
                int i = globals.bddForVariables.bijectionIdAtom.id(atomString);
                {
                    if (true) return new Literal(atomString, i, false, globals);
                }
            }
            f = new ParamPlaceholder(atomString, params, globals);
            for (String specificObject : specificObjects) {
                f = f.applyParam(specificObject, specificObject);
            }
            {
                if (true) return f;
            }
        } else if (jj_2_23(2)) {
            jj_consume_token(LPAR);
            f = implication();
            jj_consume_token(RPAR);
            {
                if (true) return f;
            }
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
        throw new Error("Missing return statement in function");
    }

    private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_1();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_2();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private boolean jj_2_3(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_3();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(2, xla);
        }
    }

    private boolean jj_2_4(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_4();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(3, xla);
        }
    }

    private boolean jj_2_5(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_5();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(4, xla);
        }
    }

    private boolean jj_2_6(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_6();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(5, xla);
        }
    }

    private boolean jj_2_7(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_7();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(6, xla);
        }
    }

    private boolean jj_2_8(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_8();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(7, xla);
        }
    }

    private boolean jj_2_9(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_9();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(8, xla);
        }
    }

    private boolean jj_2_10(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_10();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(9, xla);
        }
    }

    private boolean jj_2_11(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_11();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(10, xla);
        }
    }

    private boolean jj_2_12(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_12();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(11, xla);
        }
    }

    private boolean jj_2_13(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_13();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(12, xla);
        }
    }

    private boolean jj_2_14(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_14();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(13, xla);
        }
    }

    private boolean jj_2_15(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_15();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(14, xla);
        }
    }

    private boolean jj_2_16(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_16();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(15, xla);
        }
    }

    private boolean jj_2_17(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_17();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(16, xla);
        }
    }

    private boolean jj_2_18(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_18();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(17, xla);
        }
    }

    private boolean jj_2_19(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_19();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(18, xla);
        }
    }

    private boolean jj_2_20(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_20();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(19, xla);
        }
    }

    private boolean jj_2_21(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_21();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(20, xla);
        }
    }

    private boolean jj_2_22(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_22();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(21, xla);
        }
    }

    private boolean jj_2_23(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return !jj_3_23();
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(22, xla);
        }
    }

    private boolean jj_3_14() {
        if (jj_scan_token(GOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3R_6() {
        if (jj_3R_7()) return true;
        return false;
    }

    private boolean jj_3R_8() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_13()) {
            jj_scanpos = xsp;
            if (jj_3_14()) {
                jj_scanpos = xsp;
                if (jj_3_15()) {
                    jj_scanpos = xsp;
                    if (jj_3_16()) {
                        jj_scanpos = xsp;
                        if (jj_3_17()) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3_13() {
        if (jj_scan_token(FOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_4() {
        if (jj_scan_token(EXIS)) return true;
        if (jj_scan_token(ID)) return true;
        return false;
    }

    private boolean jj_3_22() {
        if (jj_scan_token(ID)) return true;
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_19()) jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3_3() {
        if (jj_scan_token(UNIV)) return true;
        if (jj_scan_token(ID)) return true;
        return false;
    }

    private boolean jj_3_2() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_3()) {
            jj_scanpos = xsp;
            if (jj_3_4()) return true;
        }
        return false;
    }

    private boolean jj_3_5() {
        Token xsp;
        while (true) {
            xsp = jj_scanpos;
            if (jj_3_2()) {
                jj_scanpos = xsp;
                break;
            }
        }
        if (jj_scan_token(COL)) return true;
        return false;
    }

    private boolean jj_3_21() {
        if (jj_scan_token(FALSE)) return true;
        return false;
    }

    private boolean jj_3_1() {
        if (jj_scan_token(WHY)) return true;
        return false;
    }

    private boolean jj_3_7() {
        if (jj_scan_token(OR)) return true;
        if (jj_3R_6()) return true;
        return false;
    }

    private boolean jj_3_11() {
        if (jj_scan_token(WOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3R_9() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_20()) {
            jj_scanpos = xsp;
            if (jj_3_21()) {
                jj_scanpos = xsp;
                if (jj_3_22()) {
                    jj_scanpos = xsp;
                    if (jj_3_23()) return true;
                }
            }
        }
        return false;
    }

    private boolean jj_3_20() {
        if (jj_scan_token(TRUE)) return true;
        return false;
    }

    private boolean jj_3R_5() {
        if (jj_3R_6()) return true;
        return false;
    }

    private boolean jj_3_10() {
        if (jj_scan_token(VOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_9() {
        if (jj_scan_token(UOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_12() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_9()) {
            jj_scanpos = xsp;
            if (jj_3_10()) {
                jj_scanpos = xsp;
                if (jj_3_11()) return true;
            }
        }
        return false;
    }

    private boolean jj_3_23() {
        if (jj_scan_token(LPAR)) return true;
        if (jj_3R_10()) return true;
        return false;
    }

    private boolean jj_3_19() {
        if (jj_scan_token(LPAR)) return true;
        if (jj_scan_token(ID)) return true;
        return false;
    }

    private boolean jj_3R_7() {
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_6() {
        if (jj_scan_token(IMP)) return true;
        if (jj_3R_5()) return true;
        return false;
    }

    private boolean jj_3R_10() {
        if (jj_3R_5()) return true;
        return false;
    }

    private boolean jj_3_17() {
        if (jj_3R_9()) return true;
        return false;
    }

    private boolean jj_3_16() {
        if (jj_scan_token(NEG)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_18() {
        if (jj_scan_token(COM)) return true;
        if (jj_scan_token(ID)) return true;
        return false;
    }

    private boolean jj_3_15() {
        if (jj_scan_token(XOP)) return true;
        if (jj_3R_8()) return true;
        return false;
    }

    private boolean jj_3_8() {
        if (jj_scan_token(AND)) return true;
        if (jj_3R_7()) return true;
        return false;
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream) {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    /**
     * Reinitialise.
     */
    public void ReInit(VELQueryParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 0; i++) jj_la1[i] = -1;
        for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) c.first = null;
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            } else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        } else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) jj_add_error_token(kind, i);
        }
        if (jj_scanpos.kind != kind) return true;
        if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
        return false;
    }

    /**
     * Get the next Token.
     */
    final public Token getNextToken() {
        if (token.next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next;
            else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private int jj_ntk() {
        if ((jj_nt = token.next) == null)
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) return;
        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        } else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];
            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }
            boolean exists = false;
            for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext(); ) {
                exists = true;
                int[] oldentry = (int[]) (it.next());
                if (oldentry.length == jj_expentry.length) {
                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            exists = false;
                            break;
                        }
                    }
                    if (exists) break;
                }
            }
            if (!exists) jj_expentries.add(jj_expentry);
            if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
        }
    }

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[27];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 0; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 27; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /**
     * Enable tracing.
     */
    final public void enable_tracing() {
    }

    /**
     * Disable tracing.
     */
    final public void disable_tracing() {
    }

    private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 23; i++) {
            try {
                JJCalls p = jj_2_rtns[i];
                do {
                    if (p.gen > jj_gen) {
                        jj_la = p.arg;
                        jj_lastpos = jj_scanpos = p.first;
                        switch (i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                            case 3:
                                jj_3_4();
                                break;
                            case 4:
                                jj_3_5();
                                break;
                            case 5:
                                jj_3_6();
                                break;
                            case 6:
                                jj_3_7();
                                break;
                            case 7:
                                jj_3_8();
                                break;
                            case 8:
                                jj_3_9();
                                break;
                            case 9:
                                jj_3_10();
                                break;
                            case 10:
                                jj_3_11();
                                break;
                            case 11:
                                jj_3_12();
                                break;
                            case 12:
                                jj_3_13();
                                break;
                            case 13:
                                jj_3_14();
                                break;
                            case 14:
                                jj_3_15();
                                break;
                            case 15:
                                jj_3_16();
                                break;
                            case 16:
                                jj_3_17();
                                break;
                            case 17:
                                jj_3_18();
                                break;
                            case 18:
                                jj_3_19();
                                break;
                            case 19:
                                jj_3_20();
                                break;
                            case 20:
                                jj_3_21();
                                break;
                            case 21:
                                jj_3_22();
                                break;
                            case 22:
                                jj_3_23();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);
            } catch (LookaheadSuccess ls) {
            }
        }
        jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    static private final class LookaheadSuccess extends java.lang.Error {
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;
    }

}
