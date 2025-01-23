package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.ItemConstants;
import com.github.ltprc.gamepal.manager.FarmManager;
import com.github.ltprc.gamepal.model.FarmInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

@Component
public class FarmManagerImpl implements FarmManager {

    private static final Log logger = LogFactory.getLog(FarmManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Override
    public void updateFarmStatus(GameWorld world) {
        world.getFarmMap().values().forEach(farm -> {
            if (farm.getCropStatus() == BlockConstants.CROP_STATUS_PLANTED) {
                farm.setCropFrame(farm.getCropFrame() + 1);
                if (farm.getCropFrame() >= BlockConstants.CROP_PERIOD) {
                    farm.setCropStatus(BlockConstants.CROP_STATUS_MATURE);
                    farm.setCropFrame(0);
                }
            }
        });
    }

    @Override
    public void plant(GameWorld world, String userCode, String farmId, String cropCode) {
        if (!world.getPlayerInfoMap().containsKey(userCode) || !world.getFarmMap().containsKey(farmId)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        FarmInfo farmInfo = world.getFarmMap().get(farmId);
        if (farmInfo.getCropStatus() != BlockConstants.CROP_STATUS_NONE
                && farmInfo.getCropStatus() != BlockConstants.CROP_STATUS_GATHERED) {
            logger.error(ErrorUtil.ERROR_1014);
            return;
        }
        if (!world.getBagInfoMap().containsKey(userCode)
                || !world.getBagInfoMap().get(userCode).getItems().containsKey(cropCode)
                || world.getBagInfoMap().get(userCode).getItems().get(cropCode) < 1) {
            logger.error(ErrorUtil.ERROR_1035);
            return;
        }
        if (!ItemConstants.ITEM_PLANT_MAP.containsKey(cropCode)) {
            logger.error(ErrorUtil.ERROR_1042);
            return;
        }
        playerService.generateNotificationMessage(userCode, "种植成功。");
        playerService.getItem(userCode, cropCode, -1);
        farmInfo.setCropStatus(BlockConstants.CROP_STATUS_PLANTED);
        Random random = new Random();
        farmInfo.setCropAmount(random.nextInt(3) + 3);
        farmInfo.setCropCode(ItemConstants.ITEM_PLANT_MAP.get(cropCode));
    }

    @Override
    public void gather(GameWorld world, String userCode, String farmId) {
        if (!world.getPlayerInfoMap().containsKey(userCode) || !world.getFarmMap().containsKey(farmId)) {
            logger.error(ErrorUtil.ERROR_1007);
            return;
        }
        FarmInfo farmInfo = world.getFarmMap().get(farmId);
        if (farmInfo.getCropStatus() != BlockConstants.CROP_STATUS_MATURE) {
            logger.error(ErrorUtil.ERROR_1014);
            return;
        }
        playerService.generateNotificationMessage(userCode, "采集成功。");
        playerService.getItem(userCode, farmInfo.getCropCode(), farmInfo.getCropAmount());
        farmInfo.setCropStatus(BlockConstants.CROP_STATUS_GATHERED);
        farmInfo.setCropAmount(0);
    }

    @Override
    public Optional<Block> generateCropByFarm(GameWorld world, Block farmBlock) {
        String farmId = farmBlock.getBlockInfo().getId();
        if (!world.getFarmMap().containsKey(farmId)) {
            logger.error(ErrorUtil.ERROR_1007);
            return Optional.empty();
        }
        FarmInfo farmInfo = world.getFarmMap().get(farmId);
        String cropCode = "";
        if (farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_PLANTED) {
            cropCode = farmInfo.getCropFrame() * 2 < BlockConstants.CROP_PERIOD
                    ? String.valueOf(BlockConstants.BLOCK_CODE_CROP_1)
                    : String.valueOf(BlockConstants.BLOCK_CODE_CROP_2);
        } else if (farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_MATURE) {
            cropCode = String.valueOf(BlockConstants.BLOCK_CODE_CROP_3);
        } else if (farmInfo.getCropStatus() == BlockConstants.CROP_STATUS_GATHERED) {
            cropCode = String.valueOf(BlockConstants.BLOCK_CODE_CROP_0);
        }
        return StringUtils.isNotBlank(cropCode) ? Optional.of(new Block(farmBlock.getWorldCoordinate(),
                new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, "", cropCode,
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                                BlockConstants.STRUCTURE_LAYER_MIDDLE)), new MovementInfo()))
                : Optional.empty();
    }
}
