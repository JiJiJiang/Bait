package controllers.limitdepthfirst;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.*;

/**
 * Created by 77 on 2017/3/13.
 */
public class Agent extends AbstractPlayer{
    StateObservation lastStateObs;//last stateObservation

    Types.ACTIONS curAction;//current action
    Types.ACTIONS bestAction;//best action to do
    double bestHeuristicValue;//best heuristic value of this best action
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
        lastStateObs=null;
        allStateObsVisited=new ArrayList<StateObservation>();
    }

    /**
     * initialize data fields.
     */
    void initialDataFields(){
        //initialize data fields.
        bestAction= Types.ACTIONS.ACTION_NIL;
        bestHeuristicValue=Double.MAX_VALUE;
        allStateObsVisited.clear();
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    private final int LIMIT=5;
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        //System.out.println(stateObs.getAvatarType());
        initialDataFields();
        limitDepthFirstSearch(stateObs,LIMIT);

        lastStateObs=stateObs.copy();
        System.out.println(bestAction);
        return bestAction;
    }

    //private boolean bestActionFound=false;
    private void limitDepthFirstSearch(StateObservation stateObs,int limit){
        //if(bestActionFound) return;
        allStateObsVisited.add(stateObs);
        if(stateObs.isGameOver()){
            if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS){
                bestAction=curAction;
                bestHeuristicValue=0;
                //bestActionFound=true;
            }
        }else if(limit==0){
            double curHeuristicValue=heuristic(stateObs);
            if(curHeuristicValue<bestHeuristicValue){
                bestAction=curAction;
                bestHeuristicValue=curHeuristicValue;
            }
        }else{//limit>=1
            for (Types.ACTIONS action : stateObs.getAvailableActions()) {
                //if(limit==LIMIT) curAction=action;
                StateObservation stCopy = stateObs.copy();
                stCopy.advance(action);
                if (isStateObsMeaningful(stCopy)&&!isVisited(stCopy)) {
                    if(limit==LIMIT) curAction=action;
                    limitDepthFirstSearch(stCopy, limit - 1);
                }
            }
        }
    }

    /**
     * judge whether a stateObservation is meaningful or not
     */
    boolean isStateObsMeaningful(StateObservation stateObs)
    {
        if(stateObs.equalPosition(lastStateObs))
            return false;
        else
            return true;
    }

    /**
     * heuristic function
     */
    private double heuristic(StateObservation stateObs)
    {
        Vector2d avatarPos=stateObs.getAvatarPosition();

        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();

        /*
        Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标
        System.out.println(goalpos.x+" g "+goalpos.y);
        Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
        System.out.println(keypos.x+" k "+keypos.y);
        */

        double heuristicValue;
        int avatarType=stateObs.getAvatarType();
        if(avatarType==1)//without key
        {
            Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标
            Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
            heuristicValue=Math.abs(avatarPos.x-keypos.x)+Math.abs(avatarPos.y-keypos.y)
            +Math.abs(goalpos.x-keypos.x)+Math.abs(goalpos.y-keypos.y);
        }else{//with key
            Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标
            heuristicValue=Math.abs(avatarPos.x-goalpos.x)+Math.abs(avatarPos.y-goalpos.y);
        }

        return heuristicValue;
    }
}
