package net.silentchaos512.lib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TeleportUtils {
    public static void teleport(Player player, DimPos pos, @Nullable Direction direction) {
        teleport(player, pos.getDimensionId(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction);
    }

    public static void teleport(Player player, DimensionId dimension, double destX, double destY, double destZ, @Nullable Direction direction) {
        DimensionId oldId = DimensionId.fromWorld(player.getCommandSenderWorld());

        float rotationYaw = player.getYRot();
        float rotationPitch = player.getXRot();

        if (!oldId.equals(dimension)) {
            teleportToDimension(player, dimension, destX, destY, destZ);
        }
        if (direction != null) {
            fixOrientation(player, destX, destY, destZ, direction);
        } else {
            player.setYRot(rotationYaw);
            player.setXRot(rotationPitch);
        }
        player.teleportTo(destX, destY, destZ);
    }

    public static void teleportToDimension(Player player, DimensionId dimension, double x, double y, double z) {
        ServerLevel world = dimension.loadWorld(player.getCommandSenderWorld());
        player.changeDimension(world, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                entity.level = world;
                world.addDuringPortalTeleport((ServerPlayer) entity);
                entity.moveTo(x, y, z);
                entity.teleportTo(x, y, z);
                return entity;
            }
        });
    }

    private static void facePosition(Entity entity, double newX, double newY, double newZ, BlockPos dest) {
        double d0 = dest.getX() - newX;
        double d1 = dest.getY() - (newY + entity.getEyeHeight());
        double d2 = dest.getZ() - newZ;

        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float) (Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float) (-(Mth.atan2(d1, d3) * (180D / Math.PI)));
        entity.setXRot(updateRotation(entity.getXRot(), f1));
        entity.setYRot(updateRotation(entity.getYRot(), f));
    }

    private static float updateRotation(float angle, float targetAngle) {
        float f = Mth.wrapDegrees(targetAngle - angle);
        return angle + f;
    }

    public static Entity teleportEntity(Entity entity, DimPos pos, @Nullable Direction facing) {
        return teleportEntity(entity, pos.getDimensionId().getWorld(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, facing);
    }

    /**
     * Teleport an entity and return the new entity (as teleporting to other dimensions causes
     * entities to be killed and recreated)
     */
    public static Entity teleportEntity(Entity entity, Level destWorld, double newX, double newY, double newZ, @Nullable Direction facing) {
        Level world = entity.getCommandSenderWorld();
        if (DimensionId.fromWorld(world).equals(DimensionId.fromWorld(destWorld))) {
            if (facing != null) {
                fixOrientation(entity, newX, newY, newZ, facing);
            }
            entity.moveTo(newX, newY, newZ, entity.getYRot(), entity.getXRot());
            ((ServerLevel) destWorld).tickNonPassenger(entity);
            return entity;
        } else {
            return entity.changeDimension((ServerLevel) destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    entity = repositionEntity.apply(false);
                    if (facing != null) {
                        fixOrientation(entity, newX, newY, newZ, facing);
                    }
                    entity.teleportTo(newX, newY, newZ);
                    return entity;
                }
            });
        }
    }

    private static void fixOrientation(Entity entity, double newX, double newY, double newZ, Direction facing) {
        if (facing != Direction.DOWN && facing != Direction.UP) {
            facePosition(entity, newX, newY, newZ, new BlockPos(newX, newY, newZ).relative(facing, 4));
        }
    }
}
