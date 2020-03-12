package velexplanation.single.domains.shopworld.visualization;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.*;
import velexplanation.single.domains.shopworld.ShopWorldDomain;
import velexplanation.single.domains.shopworld.state.ShopWorldAgent;
import velexplanation.single.domains.shopworld.state.ShopWorldState;
import velexplanation.single.domains.shopworld.state.ShopWorldTrinket;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static velexplanation.single.domains.shopworld.ShopWorldDomain.*;

public class ShopWorldVisualizer extends Visualizer {

    public static Visualizer getVisualizer() {
        StateRenderLayer r = getRenderLayer();
        Visualizer v = new Visualizer(r);
        return v;
    }

    private static StateRenderLayer getRenderLayer() {
        StateRenderLayer r = new StateRenderLayer();
        r.addStatePainter(new ShopWorldVisualizer.ShopWorldStatePainter());
        OOStatePainter oopainter = new OOStatePainter();
        oopainter.addObjectClassPainter(CLASS_AGENT, new ShopWorldVisualizer.ShopWorldAgentPainter());
        oopainter.addObjectClassPainter(CLASS_TRINKET, new ShopWorldVisualizer.ShopWorldTrinketPainter());
        r.addStatePainter(oopainter);
        return r;
    }

    public static void main(String[] args) {
        Visualizer v = ShopWorldVisualizer.getVisualizer();

        ShopWorldDomain sw = new ShopWorldDomain(0.7, 0.3, 0.5, 0.7, 10, -0.5);
        OOSADomain d0 = sw.generateDomain();
        State s0 = ShopWorldDomain.oneAgentNoTrinkets(d0, 100);

        ShopWorldDomain.addTrinket(d0, (ShopWorldState) s0, 30.0, 45.0, ShopWorldTrinket.Size.MEDIUM);
        ShopWorldDomain.addTrinket(d0, (ShopWorldState) s0, 30.0, 45.0, ShopWorldTrinket.Size.LARGE);
        ShopWorldDomain.addTrinket(d0, (ShopWorldState) s0, 30.0, 45.0, ShopWorldTrinket.Size.SMALL);

        LearningAgent agent = new QLearning(d0, 0.99, new SimpleHashableStateFactory(), 0., 0.1);

        SimulatedEnvironment env = new SimulatedEnvironment(d0, s0);

        List<Episode> episodes = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Episode episode = agent.runLearningEpisode(env);
            env.resetEnvironment();
            episodes.add(episode);
        }


        EpisodeSequenceVisualizer visualizer = new EpisodeSequenceVisualizer(v, d0, episodes);
        visualizer.initGUI();
    }

    public static class ShopWorldAgentPainter implements ObjectPainter {

        @Override
        public void paintObject(Graphics2D graphics2D, OOState ooState, ObjectInstance agent, float cWidth, float cHeight) {
            if (!(agent instanceof ShopWorldAgent)) return;

            double y = 0;

            double displayScale = 0.09375;

            int displayWidth = (int) (displayScale * cWidth);
            int displayHeight = (int) (displayScale * cHeight);

            if (((ShopWorldAgent) agent).leftStore) {
                y = 0.75 * cHeight;
            } else {
                y = 0.375 * cHeight;
            }

            graphics2D.setColor(Color.cyan);
            graphics2D.fill(new Ellipse2D.Double(cWidth / 2 - displayWidth / 2., y, displayWidth, displayHeight));

            graphics2D.setColor(Color.black);
            String moneyString = "M: " + ((ShopWorldAgent) agent).money;
            int moneyWidth = graphics2D.getFontMetrics().stringWidth(moneyString);
            int stringHeight = graphics2D.getFontMetrics().getHeight();
            graphics2D.drawString(moneyString, cWidth / 2 - moneyWidth / 2, (int) y + displayHeight / 2);
        }
    }

    public static class ShopWorldTrinketPainter implements ObjectPainter {

        @Override
        public void paintObject(Graphics2D graphics2D, OOState ooState, ObjectInstance objectInstance, float cWidth,
                                float cHeight) {
            if (!(objectInstance instanceof ShopWorldTrinket)) return;

            double displayScale = 0.;

            switch (((ShopWorldTrinket) objectInstance).size) {
                case LARGE:
                    displayScale = 0.1;
                    break;
                case MEDIUM:
                    displayScale = 0.08125;
                    break;
                case SMALL:
                    displayScale = 0.0625;
                    break;
            }

            Color c;
            if (((ShopWorldTrinket) objectInstance).bought) c = Color.green;
            else if (((ShopWorldTrinket) objectInstance).hidden) c = new Color(1.F, 0F, 0F, 0.5F);
            else c = Color.red;

            double x = 0;
            double y = 0;

            if (((ShopWorldTrinket) objectInstance).inStock) {
                List<ObjectInstance> trinkets = ooState.objectsOfClass(CLASS_TRINKET);
                int trinketIndex = trinkets.indexOf(objectInstance);

                int shelfNum = trinketIndex / 5;
                int indexOnShelf = trinketIndex % 5;
                x = 0.3125 + indexOnShelf * 0.125 - displayScale / 2.;
                y = 0.1875 + shelfNum * 0.25 - displayScale / 2.;
            } else {
                ObjectInstance agent = ooState.objectsOfClass(CLASS_AGENT).get(0);
                if (!(agent instanceof ShopWorldAgent)) return;
                if (((ShopWorldAgent) agent).leftStore) y = 0.9375 - displayScale / 2.;
                else y = 0.5625 - displayScale / 2.;
                List<ObjectInstance> inHandTrinkets = ooState.objectsOfClass(CLASS_TRINKET).stream().filter(t ->
                        StateUtilities.stringOrBoolean(t.get(VAR_HELD))
                                || StateUtilities.stringOrBoolean(t.get(VAR_BOUGHT)))
                        .collect(Collectors.toList());

                int numTrinketsInHand = inHandTrinkets.size();
                int inHandIndex = inHandTrinkets.indexOf(objectInstance);
                x = 0.5 - displayScale / 2. + 0.125 * inHandIndex - 0.0625 * (numTrinketsInHand - 1);
            }

            graphics2D.setColor(c);
            graphics2D.fillRect((int) (x * cWidth), (int) (y * cHeight), (int) (displayScale * cWidth), (int) (displayScale * cHeight));
            graphics2D.setColor(Color.black);
            graphics2D.drawRect((int) (x * cWidth), (int) (y * cHeight), (int) (displayScale * cWidth), (int) (displayScale * cHeight));

            String costString = "C: " + ((ShopWorldTrinket) objectInstance).cost;
            String valueString = "V: " + ((ShopWorldTrinket) objectInstance).value;

            int costWidth = graphics2D.getFontMetrics().stringWidth(costString);
            int valueWidth = graphics2D.getFontMetrics().stringWidth(valueString);
            int stringHeight = graphics2D.getFontMetrics().getHeight();

            graphics2D.drawString(costString, (float) ((x + displayScale / 2.) * cWidth - costWidth / 2), (float) ((y + displayScale / 2.) * cHeight));
            graphics2D.drawString(valueString, (float) ((x + displayScale / 2.) * cWidth - valueWidth / 2.), (float) ((y + displayScale / 2.) * cHeight + stringHeight));

        }
    }

    public static class ShopWorldStatePainter implements StatePainter {

        @Override
        public void paint(Graphics2D graphics2D, State state, float cWidth, float cHeight) {

//            Draw the store boundaries
            graphics2D.setColor(Color.black);
            graphics2D.fillRect(0, 0, (int) cWidth, (int) (cHeight * 0.75));
            graphics2D.setColor(Color.white);
            graphics2D.fillRect((int) (0.06 * cWidth), (int) (0.06 * cHeight), (int) (0.88 * cWidth), (int) (0.66 * cHeight));


            if (!(state instanceof OOState)) return;
//            Draw the shelf

            List<ObjectInstance> trinkets = ((OOState) state).objectsOfClass(CLASS_TRINKET);
            int numShelves = (int) Math.ceil(((double) trinkets.size()) / 5.0);

            graphics2D.setColor(Color.lightGray);
            for (int i = 0; i < numShelves; i++) {
                graphics2D.fillRect((int) (0.25 * cWidth), (int) ((0.125 + i * 0.25) * cHeight), (int) (0.625 * cWidth), (int) (0.125 * cHeight));
            }
        }
    }
}
