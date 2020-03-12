package velexplanation.vel.explanation.visualization;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.visualizer.StatePainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.RestrictedQ;
import velexplanation.mdp.MDPContainer;
import velexplanation.mdp.RecordedActionMDP;
import velexplanation.rewardvector.comparator.DoubleDeckerComparator;
import velexplanation.rewardvector.comparator.MixedLexicographicWeightedComparator;
import velexplanation.single.domains.shopworld.ShopWorldDomain;
import velexplanation.single.domains.shopworld.state.ShopWorldState;
import velexplanation.single.domains.shopworld.state.ShopWorldTrinket;
import velexplanation.single.domains.shopworld.visualization.ShopWorldVisualizer;
import velexplanation.statehashing.HashableWrapperStateFactory;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELPlanner;
import velexplanation.vel.explanation.ExplanationHelper;
import velexplanation.vel.explanation.VELQueryExplainer;
import velexplanation.vel.explanation.explanations.CounterfactualExplanation;
import velexplanation.vel.explanation.explanations.Explanation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class ExplanationVisualizer extends JFrame {

    protected Visualizer realEpisodePainter;
    protected Visualizer counterfactualEpisodePainter;
    protected int cWidth;
    protected int cHeight;
    protected boolean alreadyInitedGUI = false;


    protected DefaultListModel realIterationListModel;
    protected JList realIterationList;
    protected JScrollPane realIterationScroller;

    protected DefaultListModel counterfactualIterationListModel;
    protected JList counterfactualIterationList;
    protected JScrollPane counterfactualIterationScroller;

    protected Episode realEpisode;
    protected Episode counterfactualEpisode;

    protected JTextField questionField;
    protected JTextArea answerField;
    protected List<VELObjective> objectives;
    protected Comparator<RealVector> objectiveComparator;
    protected MDPContainer mdp;

    protected JList[] objectiveBoxes;
    protected DefaultListModel[] objectiveListModels;

    public ExplanationVisualizer(Visualizer v,
                                 MDPContainer mdp,
                                 List<VELObjective> objectives,
                                 Comparator<RealVector> comparator,
                                 Episode episode,
                                 int w,
                                 int h) {
        this.init(v, mdp, objectives, comparator, episode, w, h);
    }

    public ExplanationVisualizer(Visualizer v,
                                 MDPContainer mdp,
                                 List<VELObjective> objectives,
                                 Comparator<RealVector> comparator,
                                 Episode episode) {
        this.init(v, mdp, objectives, comparator, episode, 800, 800);
    }

    public static void main(String[] args) {
        ShopWorldDomain dg = new ShopWorldDomain(1., 1., 1., 1., 0., 0.);
        OOSADomain d0 = dg.generateDomain();

        Map<String, ShopWorldTrinket> trinketMap = new HashMap<>();

        trinketMap.put("trinket1", new ShopWorldTrinket(ShopWorldTrinket.Size.MEDIUM, 15, 30, "trinket1"));
        trinketMap.put("trinket2", new ShopWorldTrinket(ShopWorldTrinket.Size.LARGE, 15, 50, "trinket2"));

        State s = new ShopWorldState(15, trinketMap);

        HashableWrapperStateFactory hf = new HashableWrapperStateFactory();

        MDPContainer origMDP = new RecordedActionMDP(new MDPContainer(d0, s, hf));

        d0 = (OOSADomain) origMDP.domain;
        s = origMDP.initialState;

        String objectiveText = "<t>: G(!(leftStore & !bought(t) & held(t))) >> <t>: F(leftStore & held(t))";
//        String objectiveText = "A t: F(leftStore & bought(t))";
//        String objectiveText = "<t>: G(!(bought(t)))";

        VELPlanner planner = new VELPlanner(objectiveText, d0, s, hf, 0.99);

        OOSADomain d = (OOSADomain) planner.getCurrentDomain();

        Environment env = planner.getEnvironment();

        LearningAgent agent = new RestrictedQ(d, 0.99, hf, 0.3, 0.1, 30);
        int maxSteps = 30;

        Episode episode = agent.runLearningEpisode(env, maxSteps);

        Comparator<RealVector> comparator = planner.comp;
        if (comparator instanceof DoubleDeckerComparator) {
            comparator = ((DoubleDeckerComparator) comparator).getBaseComparator();
        }

        if (!(comparator instanceof MixedLexicographicWeightedComparator)) {
            throw new RuntimeException("Unknown objective comparator type");
        }

        Visualizer v = ShopWorldVisualizer.getVisualizer();
        ExplanationVisualizer visualizer = new ExplanationVisualizer(v, new MDPContainer(d0, s, hf), planner.objectives, comparator, episode);
        visualizer.initGUI();
    }

    public void init(Visualizer v,
                     MDPContainer mdp,
                     List<VELObjective> objectives,
                     Comparator<RealVector> comparator,
                     Episode episode, int w, int h) {
        this.realEpisodePainter = v;
        this.counterfactualEpisodePainter = deepCopyVisualizer(v);

        this.realEpisode = episode;
        this.mdp = mdp;
        this.objectives = objectives;
        this.objectiveComparator = comparator;
        this.cWidth = w;
        this.cHeight = h;

        this.realIterationListModel = new DefaultListModel();

        for (Object ga : this.realEpisode.actionSequence) {

            this.realIterationListModel.addElement(ga.toString());
        }

        this.realIterationListModel.addElement("final state");


        if (!(comparator instanceof MixedLexicographicWeightedComparator)) {
            throw new RuntimeException("Comparator is the wrong class.");
        }

        RealMatrix priorities = ((MixedLexicographicWeightedComparator) comparator).getPrioritiesMatrix();

        this.objectiveListModels = new DefaultListModel[priorities.getRowDimension()];
        for (int priorityLevel = priorities.getRowDimension() - 1; priorityLevel >= 0; priorityLevel--) {
            RealVector weightsForPriority = priorities.getRowVector(priorityLevel);
            List<Integer> priorityIndices = ExplanationHelper.getNonZeroEntries(weightsForPriority);
            if (priorityIndices.isEmpty()) {
                continue;
            }
            int reverse = priorities.getRowDimension() - priorityLevel - 1;
            this.objectiveListModels[reverse] = new DefaultListModel();
            for (int i = 0; i < priorityIndices.size(); i++) {
                int objectiveIndex = priorityIndices.get(i);
                this.objectiveListModels[reverse].add(i, objectives.get(objectiveIndex));
            }
        }


        this.initGUI();

    }

    private Visualizer deepCopyVisualizer(Visualizer v) {
//        Assumes state render layer rather than state action render layer.
        Visualizer vnew = v.copy();
        StateRenderLayer srender = new StateRenderLayer();
        for (StatePainter sp : v.getStateRenderLayer().getStatePainters()) {
            srender.addStatePainter(sp);
        }
        srender.updateState(NullState.instance);
        vnew.setSetRenderLayer(srender);


        return vnew;
    }

    public void initGUI() {
        if (!this.alreadyInitedGUI) {
            this.alreadyInitedGUI = true;

//            Visualization of the observed realEpisode
            this.realEpisodePainter.setPreferredSize(new Dimension(this.cWidth, this.cHeight));

            this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.LINE_AXIS));

            Container questionContainer = new Container();
            questionContainer.setLayout(new BorderLayout());
            this.questionField = new JTextField();

            this.questionField.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ExplanationVisualizer.this.handleQueryEntered(e);
                }
            });
            questionContainer.add(this.questionField, "Center");

            questionContainer.add(new JLabel("Query:"), "West");


            JScrollPane answerPane = new JScrollPane();

            this.answerField = new JTextArea(10, 20);
            this.answerField.setEditable(false);
            this.answerField.setLineWrap(false);

            answerPane.setViewportView(this.answerField);
            JPanel questionAnswerContainer = new JPanel();
            questionAnswerContainer.setLayout(new BorderLayout());
            questionAnswerContainer.add(questionContainer, "North");
            questionAnswerContainer.add(answerPane, "Center");

            questionAnswerContainer.setPreferredSize(new Dimension(400, 600));
            questionAnswerContainer.setBorder(BorderFactory.createTitledBorder("Q & A"));

            this.getContentPane().add(questionAnswerContainer);

            JScrollPane objectivesScrollPane = new JScrollPane();
            JPanel objectiveContainer = new JPanel();
            objectiveContainer.setLayout(new BoxLayout(objectiveContainer, BoxLayout.PAGE_AXIS));
            objectiveContainer.add(new JLabel("Highest Priority"));

            this.objectiveBoxes = new JList[this.objectiveListModels.length];
            for (int i = 0; i < this.objectiveBoxes.length; i++) {
                this.objectiveBoxes[i] = new JList(this.objectiveListModels[i]);
                this.objectiveBoxes[i].setLayoutOrientation(0);
                this.objectiveBoxes[i].setVisibleRowCount(-1);
                this.objectiveBoxes[i].setAlignmentX(0.f);
                objectiveContainer.add(this.objectiveBoxes[i]);
            }

            objectiveContainer.add(new JLabel("Lowest priority"));

//            Add objective information for different priorities.
            objectivesScrollPane.add(objectiveContainer);
            objectivesScrollPane.setViewportView(objectiveContainer);
            this.getContentPane().add(objectivesScrollPane);

            Border border = BorderFactory.createTitledBorder("Agent objectives");
            objectivesScrollPane.setBorder(border);

            objectivesScrollPane.setPreferredSize(new Dimension(400, 600));

            Container episodesContainer = new Container();
            episodesContainer.setLayout(new BoxLayout(episodesContainer, BoxLayout.PAGE_AXIS));

            JPanel realEpisodeContainer = new JPanel();
            realEpisodeContainer.setLayout(new BoxLayout(realEpisodeContainer, BoxLayout.LINE_AXIS));

            realEpisodeContainer.add(this.realEpisodePainter);

            this.realIterationList = new JList(this.realIterationListModel);
            this.realIterationList.setSelectionMode(0);
            this.realIterationList.setLayoutOrientation(0);
            this.realIterationList.setVisibleRowCount(-1);
            this.realIterationList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ExplanationVisualizer.this.handleRealIterationSelection(e);
                }
            });

            this.realIterationScroller = new JScrollPane(this.realIterationList);
            this.realIterationScroller.setPreferredSize(new Dimension(150, 600));

            realEpisodeContainer.add(this.realIterationScroller);
            realEpisodeContainer.setBorder(BorderFactory.createTitledBorder("Observed Episode"));
            episodesContainer.add(realEpisodeContainer);

//            Container to show counterfactual realEpisode
            this.counterfactualEpisodePainter.setPreferredSize(new Dimension(this.cWidth, this.cHeight));

            JPanel counterfactualEpisodeContainer = new JPanel();
            counterfactualEpisodeContainer.setLayout(new BoxLayout(counterfactualEpisodeContainer, BoxLayout.LINE_AXIS));

            counterfactualEpisodeContainer.add(this.counterfactualEpisodePainter);

            this.counterfactualIterationListModel = new DefaultListModel();
            this.counterfactualIterationList = new JList(this.counterfactualIterationListModel);
            this.counterfactualIterationList.setSelectionMode(0);
            this.counterfactualIterationList.setLayoutOrientation(0);
            this.counterfactualIterationList.setVisibleRowCount(-1);
            this.counterfactualIterationList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ExplanationVisualizer.this.handleCounterfactualIterationSelection(e);
                }
            });

            this.counterfactualIterationScroller = new JScrollPane(this.counterfactualIterationList);
            this.counterfactualIterationScroller.setPreferredSize(new Dimension(150, 600));

            counterfactualEpisodeContainer.add(this.counterfactualIterationScroller);

            counterfactualEpisodeContainer.setBorder(BorderFactory.createTitledBorder("Counterfactual Episode"));

            episodesContainer.add(counterfactualEpisodeContainer);

            this.getContentPane().add(episodesContainer);

            this.pack();
            this.setVisible(true);
        }
    }

    protected void handleRealIterationSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && this.realIterationList.getSelectedIndex() != -1) {
            int index = this.realIterationList.getSelectedIndex();
            State curState = this.realEpisode.state(index);
            this.realEpisodePainter.updateState(curState);
        }

    }

    protected void handleCounterfactualIterationSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && this.counterfactualIterationList.getSelectedIndex() != -1) {
            int index = this.counterfactualIterationList.getSelectedIndex();
            State curState = this.counterfactualEpisode.state(index);
            this.counterfactualEpisodePainter.updateState(curState);
        }

    }

    public void appendToGUI(Explanation exp) {
        if (exp instanceof CounterfactualExplanation) {
            this.counterfactualEpisode = ((CounterfactualExplanation) exp).getCounterfactualEpisode();
            this.counterfactualEpisodePainter.updateState(NullState.instance);
            this.setCounterfactualIterationListData();
        }
        this.answerField.append(exp.toString());
    }

    protected void handleQueryEntered(ActionEvent e) {
        String query = e.getActionCommand();
        this.questionField.setText("");
        this.answerField.append("Responding to query: " + query + "\n");
        try {
            Explanation exp = VELQueryExplainer.explain(query, this.objectives, this.objectiveComparator, this.realEpisode, this.mdp);
            this.appendToGUI(exp);
        } catch (Exception exception) {
            this.answerField.append(exception.toString());
            exception.printStackTrace();
        }
        this.answerField.append("\n");
    }

    protected void setCounterfactualIterationListData() {
        this.counterfactualIterationListModel.clear();
        Iterator var1 = this.counterfactualEpisode.actionSequence.iterator();

        while (var1.hasNext()) {
            Action ga = (Action) var1.next();
            this.counterfactualIterationListModel.addElement(ga.toString());
        }

        this.counterfactualIterationListModel.addElement("final state");
    }

}
