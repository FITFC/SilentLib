package net.silentchaos512.lib.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TeleportUtils {
    public static void teleport(PlayerEntity player, DimPos pos, @Nullable Direction direction) {
        teleport(player, pos.getDimensionId(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction);
    }

    public static void teleport(PlayerEntity player, DimensionId dimension, double destX, double destY, double destZ, @Nullable Direction direction) {
        DimensionId oldId = DimensionId.fromWorld(player.getCommandSenderWorld());

        float rotationYaw = player.yRot;
        float rotationPitch = player.xRot;

        if (!oldId.equals(dimension)) {
            teleportToDimension(player, dimension, destX, destY, destZ);
        }
        if (direction != null) {
            fixOrientation(player, destX, destY, destZ, direction);
        } else {
            player.yRot = rotationYaw;
            player.xRot = rotationPitch;
        }
        player.teleportTo(destX, destY, destZ);
    }

    public static void teleportToDimension(PlayerEntity player, DimensionId dimension, double x, double y, double z) {
        ServerWorld world = dimension.loadWorld(player.getCommandSenderWorld());
        player.changeDimension(world, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                entity.setLevel(world);
                world.addDuringPortalTeleport((ServerPlayerEntity) entity);
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

        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        entity.xRot = updateRotation(entity.xRot, f1);
        entity.yRot = updateRotation(entity.yRot, f);
    }

    private static float updateRotation(float angle, float targetAngle) {
        float f = MathHelper.wrapDegrees(targetAngle - angle);
        return angle + f;
    }

    public static Entity teleportEntity(Entity entity, DimPos pos, @Nullable Direction facing) {
        return teleportEntity(entity, pos.getDimensionId().getWorld(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, facing);
    }

    /**
     * Teleport an entity and return the new entity (as teleporting to other dimensions causes
     * entities to be killed and recreated)
     */
    public static Entity teleportEntity(Entity entity, World destWorld, double newX, double newY, double newZ, @Nullable Direction facing) {
        World world = entity.getCommandSenderWorld();
        if (DimensionId.fromWorld(world).equals(DimensionId.fromWorld(destWorld))) {
            if (facing != null) {
                fixOrientation(entity, newX, newY, newZ, facing);
            }
            entity.moveTo(newX, newY, newZ, entity.yRot, entity.xRot);
            ((ServerWorld) destWorld).tickNonPassenger(entity);
            return entity;
        } else {
            return entity.changeDimension((ServerWorld) destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
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
