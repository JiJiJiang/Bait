package controllers.depthfirst;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.*;

/**
 * Created by 77 on 2017/3/13.
 */
public class Agent extends AbstractPlayer{
    ArrayList<Types.ACTIONS> path;//the path found
    int actionIndex;//current action index in path
    ArrayList<StateObservation> allStateObsVisited;//all stateObservations visited.

    //judge whether a stateObs has been visited or not!
    boolean isVisited(StateObservation so)
    {
        for(StateObservation stateObs:allStateObsVisited){
            if(so.equalPosition(stateObs))
                return true;
        }
        return false;
    }
    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
        //initialize data fields.
        path=new ArrayList<Types.ACTIONS>();
        actionIndex=0;
        allStateObsVisited=new ArrayList<StateObservation>();
    }
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if(!pathFound){
            depthFirstSearch(stateObs);
        }
        //return null;
        return path.get(actionIndex++);
    }

    boolean pathFound=false;
    private void depthFirstSearch(StateObservation stateObs){
        if(pathFound) return;
        allStateObsVisited.add(stateObs);
        if(stateObs.isGameOver()){
            if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS){
                pathFound=true;
            }
            return;
        }
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            path.add(action);
            StateObservation stCopy = stateObs.copy();
            stCopy.advance(action);
            if(!isVisited(stCopy)) {
                depthFirstSearch(stCopy);
            }
            if (!pathFound) path.remove(path.size() - 1);
        }
    }
}
