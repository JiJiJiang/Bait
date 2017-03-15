package controllers.Astar;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.*;

/**
 * Created by 77 on 2017/3/15.
 */
public class Agent extends AbstractPlayer{
    Types.ACTIONS bestAction;//best action to do
    double bestHeuristicValue;//best heuristic value of this best action
    ArrayList<StateObservation> allStateObsVisited;//all stateObservations visited.

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        allStateObsVisited=new ArrayList<StateObservation>();
        /*
        ArrayList<Observation> grid[][]=so.getObservationGrid();
        ArrayList<Observation> temp=grid[6][8];
        System.out.println(temp.size());
        System.out.println(temp.get(0).itype);
        */
        /*
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        System.out.println(fixedPositions.length);
        for(int i=0;i<fixedPositions.length;i++) {
            System.out.println(fixedPositions[i].size());
            System.out.println(fixedPositions[i].get(0).position);
            System.out.println(fixedPositions[i].get(0).itype);
        }
        //*/
        /*
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        System.out.println(movingPositions.length);
        for(int i=0;i<movingPositions.length;i++) {
            System.out.println(movingPositions[i].get(0).itype);
            System.out.println(movingPositions[i].size());
        }
        //*/
    }

    /**
     * judge whether a stateObs has been visited or not!
     * @param so The stateObs to judge.
     * @return true or false.
     */
    boolean isVisited(StateObservation so) {
        for(int i=allStateObsVisited.size()-1;i>=0;i--){
            if(so.equalPosition(allStateObsVisited.get(i)))
                return true;
        }
        return false;
    }

    /**
     * initialize data fields.
     */
    void initialDataFields() {
        //initialize data fields.
        bestAction = Types.ACTIONS.ACTION_NIL;
        bestHeuristicValue = Double.MAX_VALUE;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        //if(!bestAction.equals(Types.ACTIONS.ACTION_NIL))
        allStateObsVisited.add(stateObs.copy());
        initialDataFields();

        for(Types.ACTIONS action:stateObs.getAvailableActions()) {
            double curHeuristicValue=heuristic(stateObs,action);
            if(curHeuristicValue<bestHeuristicValue){
                bestAction=action;
                bestHeuristicValue=curHeuristicValue;
            }
        }

        if(bestAction.equals(Types.ACTIONS.ACTION_NIL))
            allStateObsVisited.clear();

        System.out.println(bestAction);
        return bestAction;
    }

    /**
     * heuristic function: to get the heuristic value of this stateObservation.
     * @param lastStateObs last stateObservation.
     * @param action current action
     * @return The heuristic value.
     */
    private boolean toFillHole=false;
    private Vector2d boxPos=null;//箱子坐标
    private Vector2d holePos=null;//洞坐标
    private double boxHoleDist=Double.MAX_VALUE;//箱子与洞的距离
    private double heuristic(StateObservation lastStateObs,Types.ACTIONS action) {
        //基于目标的阶段策略
        /**
         * itype对应关系:
         * 0:墙壁,1/4:玩家,2:洞,3:未知,5:蘑菇,
         * 6:钥匙,7:目标,8:箱子.
         */
        StateObservation stateObs=lastStateObs.copy();
        stateObs.advance(action);
        if(stateObs.isGameOver()){//当前状态游戏结束了
            if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS)
                return 0.0;
            else if(!toFillHole){//玩家输了,掉到洞里了(未拿到钥匙)
                toFillHole=true;
                allStateObsVisited.clear();//清空，允许往回走
                //设置洞和箱子的坐标
                calculateFillHolePos(lastStateObs,action);
                boxHoleDist=calculateFillBoxPos(lastStateObs);
                //System.out.println(boxPos);
            }
            return Double.MAX_VALUE;
        }else if(isVisited(stateObs)) {//该状态已经访问过
            return Double.MAX_VALUE;
        }else{//游戏未结束
            //上一个状态的地图上的东西
            ArrayList<Observation>[] lastMovingPositions = lastStateObs.getMovablePositions();

            //现在的地图上所有东西
            ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
            int fixedSpriteNum = fixedPositions.length;
            ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
            //int movingSpriteNum = movingPositions.length;
            Vector2d avatarPos = stateObs.getAvatarPosition();

            if(toFillHole){//现在是在填洞
                if(stateObs.getGameScore()>lastStateObs.getGameScore()) {//把洞填上
                    toFillHole=false;
                    return 0.0;
                }
                else if (isBoxsChanged(lastMovingPositions[1], movingPositions[1])) {
                    Vector2d preBoxPos=boxPos;
                    double curBoxHoleDist = calculateFillBoxPos(stateObs);
                    if (curBoxHoleDist >= boxHoleDist) {
                        boxPos=preBoxPos;
                        return Double.MAX_VALUE;
                    }else{
                        boxHoleDist=curBoxHoleDist;
                        return 0.0;
                    }
                }else{
                    //System.out.println(boxPos);
                    return boxPos.dist(avatarPos);
                }
            }else {//往蘑菇，钥匙或目标逼近
                if (stateObs.getAvatarType() == 4) {//已拿到钥匙
                    Vector2d goalPos = fixedPositions[fixedSpriteNum - 1].get(0).position;
                    return goalPos.dist(avatarPos);
                } else {//未拿到钥匙
                    if (fixedSpriteNum == 4) {//有蘑菇
                        if (isBoxsChanged(lastMovingPositions[1], movingPositions[1])) {//如果箱子位置改变了,但没有减少
                            return Double.MAX_VALUE;//不能无故改变箱子位置
                        } else {//返回与蘑菇的距离
                            Vector2d mushroomPos = fixedPositions[2].get(0).position;
                            return mushroomPos.dist(avatarPos);
                        }
                    } else {//==3,没有蘑菇（或已经拿到）,逼近钥匙
                        if (isBoxsChanged(lastMovingPositions[1], movingPositions[1])) {//如果箱子位置改变了,但没有减少
                            return Double.MAX_VALUE;//不能无故改变箱子位置
                        } else {//返回与钥匙的距离
                            Vector2d keyPos = movingPositions[0].get(0).position;
                            return keyPos.dist(avatarPos);
                        }
                    }
                }
            }
        }
    }
    /**
     * 箱子是否发生改变
     */
    boolean isBoxsChanged(ArrayList<Observation> lastBoxs,ArrayList<Observation> boxs){
        if(lastBoxs.size()!=boxs.size())
            return true;
        for(int i=0;i<lastBoxs.size();i++){
            if(lastBoxs.get(i).position.sqDist(boxs.get(i).position)>0.1)
                return true;
        }
        return false;
    }
    /**
     * 计算待填的洞的位置
     */
    private void calculateFillHolePos(StateObservation lastStateObs,Types.ACTIONS action){
        holePos=lastStateObs.getAvatarPosition();
        if(action.equals(Types.ACTIONS.ACTION_LEFT)){
            holePos.x-=50.0;
        }else if(action.equals(Types.ACTIONS.ACTION_RIGHT)){
            holePos.x+=50.0;
        }else if(action.equals(Types.ACTIONS.ACTION_DOWN)){
            holePos.y-=50.0;
        }else if(action.equals(Types.ACTIONS.ACTION_UP)) {
            holePos.y += 50.0;
        }
    }
    /**
     * 计算箱子的位置
     */
    private double calculateFillBoxPos(StateObservation stateObs){
        ArrayList<Observation> boxs=stateObs.getMovablePositions()[1];
        double minDist=Double.MAX_VALUE;
        for(Observation ob:boxs){
            if(isBoxMovable(stateObs,ob.position)) {
                double curDist = holePos.dist(ob.position);
                if (curDist < minDist) {
                    minDist = curDist;
                    boxPos = ob.position;
                }
            }
        }
        return minDist;
    }
    /**
     * 判断一个箱子是否可移动
     */
    private boolean isBoxMovable(StateObservation lastStateObs,Vector2d boxPos){
        ArrayList<Observation> grid[][]=lastStateObs.getObservationGrid();
        //System.out.println(grid[0].length);
        int count=0;
        int []x={-1,1,0,0};
        int []y={0,0,-1,1};
        for(int i=0;i<4;i++) {
            //该位置空白或是玩家
            if(grid[((int)boxPos.x)/50+x[i]][((int)boxPos.y)/50+y[i]].size()==0
                    ||(grid[((int)boxPos.x)/50+x[i]][((int)boxPos.y)/50+y[i]].size()==1
                        &&grid[((int)boxPos.x)/50+x[i]][((int)boxPos.y)/50+y[i]].get(0).itype==1)   )
                count++;
        }
        //System.out.println(count);
        if(count>=2)
            return true;
        else
            return false;
    }
}
