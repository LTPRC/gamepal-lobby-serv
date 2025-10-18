package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.FarmInfo;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.map.InteractionInfo;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.creature.NpcBrain;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
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
    private Map<String, Long> onlineMap; // userCode, timestamp, for Player only 25/02/01
    private Map<Integer, Game> gameMap; // gameNo, game
    private Map<String, Queue<Message>> messageMap; // userCode, message queue
    private Map<String, boolean[]> flagMap; // userCode, token
    private Map<String, Terminal> terminalMap; // interactionId, terminal
    private Map<String, NpcBrain> npcBrainMap; // userCode, npcBrain
    private Map<String, Long> eventMap; // id, frame length
    private Map<String, Map<String, Block>> playerBlockMap; // userCode, blockId, transmitted block 无需保留探测顺序，只需保留id对应关系便于查询

    // Special block info maps
    private Map<String, Block> blockMap; // id, non-creature block
    private Map<String, String> sourceMap; // id, sourceId
    private Map<String, Block> creatureMap; // code, creature block
    private Map<String, PlayerInfo> playerInfoMap; // code, creature block
    private Map<String, BagInfo> bagInfoMap;
    private Map<String, BagInfo> preservedBagInfoMap;
    private Map<String, Map.Entry<String, Integer>> dropMap;
    private Map<String, WorldCoordinate> teleportMap;
    private Map<String, FarmInfo> farmMap;
}
