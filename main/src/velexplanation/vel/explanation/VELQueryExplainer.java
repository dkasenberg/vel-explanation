package velexplanation.vel.explanation;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.RestrictedQ;
import velexplanation.mdp.MDPContainer;
import velexplanation.rewardvector.comparator.DoubleDeckerComparator;
import velexplanation.rewardvector.comparator.MixedLexicographicWeightedComparator;
import velexplanation.single.domains.shopworld.ShopWorldDomain;
import velexplanation.single.domains.shopworld.state.ShopWorldState;
import velexplanation.single.domains.shopworld.state.ShopWorldTrinket;
import velexplanation.statehashing.HashableWrapperStateFactory;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELPlanner;
import velexplanation.vel.explanation.explanations.*;
import velexplanation.vel.explanation.query.VELQuery;
import velexplanation.vel.explanation.query.parsing.VELQueryParser;

import java.util.*;

public class VELQueryExplainer {

    public static Explanation explain(String queryText, List<VELObjective> objectives, Comparator<RealVector> comparator,
                                      Episode episode, MDPContainer origMDP) throws Exception {
        return explain(VELQueryParser.parseFromText(queryText,
                origMDP.domain,
                origMDP.initialState), objectives, comparator, episode, origMDP);
    }

    public static Explanation explain(VELQuery query, List<VELObjective> objectives, Comparator<RealVector> comparator,
                                      Episode episode, MDPContainer origMDP) throws Exception {

        if (query.isWhyQuery) {
            //        Ensure that the query is actually true of the episode in question.

            try {
                return QueryFalseExplanation.construct(query, episode, origMDP);
            } catch (Exception ignored) {
            }

//        Ensure that it is possible for the query to be false on some episode.

//        Add the negation of the query as a new objective and get the new violation costs.

            if (comparator instanceof DoubleDeckerComparator) {
                comparator = ((DoubleDeckerComparator) comparator).getBaseComparator();
            }

            if (!(comparator instanceof MixedLexicographicWeightedComparator)) {
                throw new RuntimeException("Unknown objective comparator type");
            }

            List<VELObjective> withQuery = new ArrayList<>(objectives);
            withQuery.add(query.negativeObjective);

            Comparator<RealVector> withQueryComparator = ((MixedLexicographicWeightedComparator) comparator).addAtMaxPriority();
            VELPlanner withQueryPlanner = new VELPlanner(withQuery, withQueryComparator,
                    origMDP.domain, origMDP.initialState, origMDP.hashingFactory, 0.99, true, true, true);

            OOSADomain d = (OOSADomain) withQueryPlanner.getCurrentDomain();

            Environment env = withQueryPlanner.getEnvironment();

            LearningAgent agent = new RestrictedQ(d, 0.99, origMDP.hashingFactory, 0.3, 0.1, 30);
            int maxSteps = 30;

//        Assumptions: running in a deterministic environment

            Episode ea = agent.runLearningEpisode(env, maxSteps);

            try {
                return QueryNecessarilyTrueExplanation.construct(query, ea, origMDP);
            } catch (Exception ignored) {

            }

//        Ultimately we want to explain the difference in VC rather than the raw VC of the trajectory.
            return EpisodeComparisonExplanation.construct(episode, ea, objectives,
                    (MixedLexicographicWeightedComparator) comparator, origMDP, query);
        } else {

            try {
                return QueryTrueExplanation.construct(query, episode, origMDP);
            } catch (Exception ignored) {

            }

            return QueryFalseExplanation.construct(query, episode, origMDP);
        }

    }


    public static void main(String[] args) {

        ShopWorldDomain dg = new ShopWorldDomain(1., 1., 1., 1., 0., 0.);
        OOSADomain d0 = dg.generateDomain();

        Map<String, ShopWorldTrinket> trinketMap = new HashMap<>();

        trinketMap.put("trinket1", new ShopWorldTrinket(ShopWorldTrinket.Size.MEDIUM, 15, 30, "trinket1"));
        trinketMap.put("trinket2", new ShopWorldTrinket(ShopWorldTrinket.Size.LARGE, 15, 50, "trinket2"));

        State s = new ShopWorldState(30, trinketMap);

//        String objectiveText = "E t: G(!(leftStore & !bought(t) & held(t)))";
//        String objectiveText = "E a: F(leftStore & bought(t))";
        String objectiveText = "<t>: G(!(bought(t)))";

        HashableWrapperStateFactory hf = new HashableWrapperStateFactory();

        VELPlanner planner = new VELPlanner(objectiveText, d0, s, hf, 0.99);

        OOSADomain d = (OOSADomain) planner.getCurrentDomain();

        Environment env = planner.getEnvironment();

        LearningAgent agent = new RestrictedQ(d, 0.99, hf, 0.3, 0.1, 30);
        int maxSteps = 30;

//        for(int i = 0; i < 10000; i++) {
//            Episode ea = agent.runLearningEpisode(env, maxSteps);
//            System.out.println(ea.actionSequence);
//            env.resetEnvironment();
//        }

        Episode ea = agent.runLearningEpisode(env, maxSteps);

        System.out.println("**QUERIED EPISODE:");
        System.out.println(ea.actionSequence);

        String[] queries = new String[]{
                "E t: G(!(bought(t)))?",
                "A t: G(!(bought(t)))?",
                "E t: F(bought(t))?",
                "A t: F(bought(t))?",
                "Why E t: G(!(bought(t)))?",
                "Why A t: G(!(bought(t)))?",
                "Why E t: F(bought(t))?",
                "Why A t: F(bought(t))?",
        };

        for (int i = 0; i < queries.length; i++) {
            try {
                Explanation exp = explain(queries[i], planner.objectives, planner.comp, ea, new MDPContainer(d0, s, hf));
                System.out.println(exp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
