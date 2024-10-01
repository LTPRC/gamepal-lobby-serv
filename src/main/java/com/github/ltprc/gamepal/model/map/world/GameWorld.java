package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.creature.NpcBrain;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.terminal.Terminal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.websocket.Session;
import java.util.Map;
import java.util.Queue;


@EqualsAndHashCode(callSuper = true)
@Data
public class GameWorld extends GameWorldInfo {
    private Map<Integer, Region> regionMap; // regionNo, region
    private Map<String, InteractionInfo> interactionInfoMap;
    private Map<String, Map<String, Integer>> relationMap;
    private Map<String, Session> sessionMap; // userCode, session
    private Map<String, String> tokenMap; // userCode, token
    private Map<String, Long> onlineMap; // userCode, timestamp
    private Map<Integer, Game> gameMap; // gameNo, game
    private Queue<Block> eventQueue; // event
    private Map<String, Queue<Message>> messageMap; // userCode, message queue
    private Map<String, boolean[]> flagMap; // userCode, token
    private Map<String, Terminal> terminalMap; // interactionId, terminal
    private Map<String, NpcBrain> npcBrainMap; // userCode, npcBrain

    // Special block info maps
    private Map<String, Block> blockMap; // code, non-creature block
    private Map<String, Block> creatureMap; // code, creature block
//    private Map<String, PlayerInfo> playerInfoMap;
    private Map<String, BagInfo> bagInfoMap;
    private Map<String, BagInfo> preservedBagInfoMap;
    private Map<String, Map.Entry<String, Integer>> dropMap;
    private Map<String, WorldCoordinate> teleportMap;
}
